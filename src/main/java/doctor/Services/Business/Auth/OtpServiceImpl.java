package doctor.Services.Business.Auth;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import doctor.Models.DTOs.Auth.Responses.OtpConsumeResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpSendResponseDto;
import doctor.Models.DTOs.Auth.Responses.OtpVerifyResponseDto;
import doctor.Services.Interfaces.Auth.OtpService;
import doctor.Utils.OtpMailHelper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_VERIFIED = "VERIFIED";
    private static final String STATUS_USED = "USED";
    private static final String STATUS_EXPIRED = "EXPIRED";
    private static final String STATUS_LOCKED = "LOCKED";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PURPOSE_PATTERN = Pattern.compile("^[A-Z_]+$");

    private final Firestore firestore;
    private final OtpMailHelper otpMailHelper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.collection:otp_stage}")
    private String otpCollection;

    @Value("${app.otp.length:6}")
    private int otpLength;

    @Value("${app.otp.expire-seconds:300}")
    private long otpExpireSeconds;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.lock-seconds:900}")
    private long lockSeconds;

    @Override
    public OtpSendResponseDto sendOtp(String email, String purpose, boolean forceResend) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPurpose = normalizePurpose(purpose);
        Instant now = Instant.now();

        DocumentReference documentReference =
                firestore.collection(otpCollection).document(buildDocumentId(normalizedEmail, normalizedPurpose));
        DocumentSnapshot snapshot = await(documentReference.get());
        OtpStageSnapshot currentStage = parseStageSnapshot(snapshot.exists() ? snapshot.getData() : null);

        if (currentStage.isLocked(now)) {
            return buildSendResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_LOCKED,
                    false,
                    0L,
                    secondsUntil(now, currentStage.resendAfter()),
                    "OTP tam khoa do nhap sai qua nhieu lan. Vui long thu lai sau.");
        }

        if (!STATUS_LOCKED.equals(currentStage.status())
                && currentStage.attemptCount() >= currentStage.maxAttempts()) {
            Instant lockUntil = now.plusSeconds(lockSeconds);
            markLocked(
                    documentReference,
                    normalizedEmail,
                    normalizedPurpose,
                    now,
                    lockUntil,
                    currentStage.attemptCount(),
                    currentStage.maxAttempts(),
                    currentStage.createdAt());
            return buildSendResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_LOCKED,
                    false,
                    0L,
                    secondsUntil(now, lockUntil),
                    "OTP tam khoa do nhap sai qua nhieu lan. Vui long thu lai sau.");
        }

        if (currentStage.hasPendingAndNotExpired(now)) {
            if (!forceResend) {
                return buildSendResult(
                        normalizedEmail,
                        normalizedPurpose,
                        STATUS_PENDING,
                        false,
                        secondsUntil(now, currentStage.expiresAt()),
                        secondsUntil(now, currentStage.resendAfter()),
                        "OTP hien tai van con hieu luc, khong can gui lai.");
            }

            if (currentStage.resendAfter() != null && now.isBefore(currentStage.resendAfter())) {
                return buildSendResult(
                        normalizedEmail,
                        normalizedPurpose,
                        STATUS_PENDING,
                        false,
                        secondsUntil(now, currentStage.expiresAt()),
                        secondsUntil(now, currentStage.resendAfter()),
                        "Chua den thoi gian resend OTP.");
            }
        }

        if (STATUS_VERIFIED.equals(currentStage.status()) && !forceResend) {
            return buildSendResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_VERIFIED,
                    false,
                    0L,
                    0L,
                    "OTP da duoc xac thuc, khong can gui lai.");
        }

        if (STATUS_USED.equals(currentStage.status()) && !forceResend) {
            return buildSendResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_USED,
                    false,
                    0L,
                    0L,
                    "OTP da duoc su dung. Gui lai voi forceResend=true neu can.");
        }

        if (STATUS_PENDING.equals(currentStage.status()) && currentStage.isExpired(now)) {
            markExpired(documentReference, now);
        }

        String otpCode = generateOtpCode();
        Instant expiresAt = now.plusSeconds(otpExpireSeconds);
        Instant resendAfter = now.plusSeconds(resendCooldownSeconds);

        savePendingStage(
                documentReference,
                normalizedEmail,
                normalizedPurpose,
                hashOtp(otpCode),
                now,
                expiresAt,
                resendAfter,
                currentStage.createdAt(),
                currentStage.maxAttempts());

        try {
            otpMailHelper.sendOtpToGmail(
                    normalizedEmail, otpCode, normalizedPurpose, Duration.ofSeconds(otpExpireSeconds));
        } catch (RuntimeException ex) {
            markExpired(documentReference, now);
            throw ex;
        }

        return buildSendResult(
                normalizedEmail,
                normalizedPurpose,
                STATUS_PENDING,
                true,
                secondsUntil(now, expiresAt),
                secondsUntil(now, resendAfter),
                "Gui OTP thanh cong.");
    }

    @Override
    public OtpVerifyResponseDto verifyOtp(String email, String purpose, String otpCode) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPurpose = normalizePurpose(purpose);
        String normalizedOtp = normalizeOtpCode(otpCode);
        Instant now = Instant.now();

        DocumentReference documentReference =
                firestore.collection(otpCollection).document(buildDocumentId(normalizedEmail, normalizedPurpose));
        DocumentSnapshot snapshot = await(documentReference.get());
        if (!snapshot.exists()) {
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_EXPIRED,
                    false,
                    Math.max(1, maxAttempts),
                    0L,
                    0L,
                    "Khong ton tai OTP. Vui long gui OTP moi.");
        }

        OtpStageSnapshot currentStage = parseStageSnapshot(snapshot.getData());

        if (currentStage.isLocked(now)) {
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_LOCKED,
                    false,
                    0,
                    0L,
                    secondsUntil(now, currentStage.resendAfter()),
                    "OTP dang bi khoa tam thoi do nhap sai qua nhieu lan.");
        }

        if (STATUS_USED.equals(currentStage.status())) {
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_USED,
                    false,
                    0,
                    0L,
                    0L,
                    "OTP da duoc su dung. Vui long gui OTP moi.");
        }

        if (STATUS_VERIFIED.equals(currentStage.status())) {
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_VERIFIED,
                    true,
                    Math.max(0, currentStage.maxAttempts() - currentStage.attemptCount()),
                    0L,
                    0L,
                    "OTP da xac thuc tu truoc.");
        }

        if (!STATUS_PENDING.equals(currentStage.status())) {
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_EXPIRED,
                    false,
                    Math.max(0, currentStage.maxAttempts() - currentStage.attemptCount()),
                    0L,
                    0L,
                    "OTP khong o trang thai xac thuc. Vui long gui OTP moi.");
        }

        if (currentStage.isExpired(now)) {
            markExpired(documentReference, now);
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_EXPIRED,
                    false,
                    Math.max(0, currentStage.maxAttempts() - currentStage.attemptCount()),
                    0L,
                    0L,
                    "OTP da het han. Vui long gui OTP moi.");
        }

        if (currentStage.otpHash() == null || !hashOtp(normalizedOtp).equals(currentStage.otpHash())) {
            int nextAttempt = currentStage.attemptCount() + 1;
            if (nextAttempt >= currentStage.maxAttempts()) {
                Instant lockUntil = now.plusSeconds(lockSeconds);
                markLocked(
                        documentReference,
                        normalizedEmail,
                        normalizedPurpose,
                        now,
                        lockUntil,
                        nextAttempt,
                        currentStage.maxAttempts(),
                        currentStage.createdAt());
                return buildVerifyResult(
                        normalizedEmail,
                        normalizedPurpose,
                        STATUS_LOCKED,
                        false,
                        0,
                        secondsUntil(now, currentStage.expiresAt()),
                        secondsUntil(now, lockUntil),
                        "Nhap sai OTP qua so lan cho phep. He thong da tam khoa.");
            }

            updateAttemptCount(documentReference, now, nextAttempt);
            return buildVerifyResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_PENDING,
                    false,
                    Math.max(0, currentStage.maxAttempts() - nextAttempt),
                    secondsUntil(now, currentStage.expiresAt()),
                    secondsUntil(now, currentStage.resendAfter()),
                    "OTP khong dung.");
        }

        markVerified(documentReference, now);
        return buildVerifyResult(
                normalizedEmail,
                normalizedPurpose,
                STATUS_VERIFIED,
                true,
                Math.max(0, currentStage.maxAttempts() - currentStage.attemptCount()),
                secondsUntil(now, currentStage.expiresAt()),
                0L,
                "Xac thuc OTP thanh cong.");
    }

    @Override
    public OtpConsumeResponseDto consumeVerifiedOtp(String email, String purpose) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedPurpose = normalizePurpose(purpose);
        Instant now = Instant.now();

        DocumentReference documentReference =
                firestore.collection(otpCollection).document(buildDocumentId(normalizedEmail, normalizedPurpose));
        DocumentSnapshot snapshot = await(documentReference.get());
        if (!snapshot.exists()) {
            return buildConsumeResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_EXPIRED,
                    false,
                    "Khong ton tai OTP de su dung.");
        }

        OtpStageSnapshot currentStage = parseStageSnapshot(snapshot.getData());
        if (STATUS_USED.equals(currentStage.status())) {
            return buildConsumeResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_USED,
                    true,
                    "OTP da duoc su dung truoc do.");
        }

        if (currentStage.isLocked(now)) {
            return buildConsumeResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_LOCKED,
                    false,
                    "OTP dang bi khoa tam thoi.");
        }

        if (STATUS_VERIFIED.equals(currentStage.status())) {
            markUsed(documentReference, now);
            return buildConsumeResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_USED,
                    true,
                    "Da danh dau OTP la da su dung.");
        }

        if (STATUS_PENDING.equals(currentStage.status()) && currentStage.isExpired(now)) {
            markExpired(documentReference, now);
            return buildConsumeResult(
                    normalizedEmail,
                    normalizedPurpose,
                    STATUS_EXPIRED,
                    false,
                    "OTP da het han.");
        }

        return buildConsumeResult(
                normalizedEmail,
                normalizedPurpose,
                currentStage.status(),
                false,
                "OTP chua duoc xac thuc, khong the su dung.");
    }

    private void savePendingStage(
            DocumentReference documentReference,
            String email,
            String purpose,
            String otpHash,
            Instant now,
            Instant expiresAt,
            Instant resendAfter,
            Instant createdAt,
            int maxAttemptsValue) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("purpose", purpose);
        payload.put("otpHash", otpHash);
        payload.put("expiresAt", Date.from(expiresAt));
        payload.put("resendAfter", Date.from(resendAfter));
        payload.put("attemptCount", 0);
        payload.put("maxAttempts", Math.max(1, maxAttemptsValue));
        payload.put("status", STATUS_PENDING);
        payload.put("createdAt", Date.from(createdAt != null ? createdAt : now));
        payload.put("updatedAt", Date.from(now));
        payload.put("verifiedAt", null);
        payload.put("usedAt", null);
        await(documentReference.set(payload));
    }

    private void markLocked(
            DocumentReference documentReference,
            String email,
            String purpose,
            Instant now,
            Instant lockUntil,
            int attemptCountValue,
            int maxAttemptsValue,
            Instant createdAt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("purpose", purpose);
        payload.put("status", STATUS_LOCKED);
        payload.put("attemptCount", Math.max(0, attemptCountValue));
        payload.put("maxAttempts", Math.max(1, maxAttemptsValue));
        payload.put("resendAfter", Date.from(lockUntil));
        payload.put("updatedAt", Date.from(now));
        if (createdAt != null) {
            payload.put("createdAt", Date.from(createdAt));
        }
        await(documentReference.set(payload, SetOptions.merge()));
    }

    private void markExpired(DocumentReference documentReference, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", STATUS_EXPIRED);
        payload.put("expiresAt", Date.from(now));
        payload.put("updatedAt", Date.from(now));
        await(documentReference.set(payload, SetOptions.merge()));
    }

    private void updateAttemptCount(DocumentReference documentReference, Instant now, int nextAttempt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("attemptCount", Math.max(0, nextAttempt));
        payload.put("updatedAt", Date.from(now));
        payload.put("status", STATUS_PENDING);
        await(documentReference.set(payload, SetOptions.merge()));
    }

    private void markVerified(DocumentReference documentReference, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", STATUS_VERIFIED);
        payload.put("verifiedAt", Date.from(now));
        payload.put("updatedAt", Date.from(now));
        await(documentReference.set(payload, SetOptions.merge()));
    }

    private void markUsed(DocumentReference documentReference, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", STATUS_USED);
        payload.put("usedAt", Date.from(now));
        payload.put("updatedAt", Date.from(now));
        await(documentReference.set(payload, SetOptions.merge()));
    }

    private OtpSendResponseDto buildSendResult(
            String email,
            String purpose,
            String status,
            boolean otpSent,
            long expiresInSeconds,
            long resendInSeconds,
            String message) {
        return new OtpSendResponseDto(
                email,
                purpose,
                status,
                otpSent,
                Math.max(0L, expiresInSeconds),
                Math.max(0L, resendInSeconds),
                message);
    }

    private OtpVerifyResponseDto buildVerifyResult(
            String email,
            String purpose,
            String status,
            boolean verified,
            int attemptsRemaining,
            long expiresInSeconds,
            long resendInSeconds,
            String message) {
        return new OtpVerifyResponseDto(
                email,
                purpose,
                status,
                verified,
                Math.max(0, attemptsRemaining),
                Math.max(0L, expiresInSeconds),
                Math.max(0L, resendInSeconds),
                message);
    }

    private OtpConsumeResponseDto buildConsumeResult(
            String email, String purpose, String status, boolean consumed, String message) {
        return new OtpConsumeResponseDto(email, purpose, status, consumed, message);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        String normalized = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("email format is invalid");
        }
        return normalized;
    }

    private String normalizePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("purpose is required");
        }
        String normalized = purpose.trim().toUpperCase();
        if (!PURPOSE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("purpose must be uppercase with underscore");
        }
        return normalized;
    }

    private String normalizeOtpCode(String otpCode) {
        if (otpCode == null || otpCode.isBlank()) {
            throw new IllegalArgumentException("otpCode is required");
        }
        String normalized = otpCode.trim();
        if (!normalized.matches("\\d+")) {
            throw new IllegalArgumentException("otpCode must be numeric");
        }
        return normalized;
    }

    private String buildDocumentId(String email, String purpose) {
        return purpose + "__" + email.replace("@", "_at_").replace(".", "_dot_");
    }

    private String generateOtpCode() {
        int digitLength = Math.max(4, otpLength);
        StringBuilder builder = new StringBuilder(digitLength);
        for (int i = 0; i < digitLength; i++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }

    private String hashOtp(String otpCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(otpCode.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Cannot initialize SHA-256", ex);
        }
    }

    private long secondsUntil(Instant now, Instant target) {
        if (target == null) {
            return 0L;
        }
        return Math.max(0L, target.getEpochSecond() - now.getEpochSecond());
    }

    private OtpStageSnapshot parseStageSnapshot(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return new OtpStageSnapshot(
                    STATUS_EXPIRED, null, null, null, 0, Math.max(1, maxAttempts), null);
        }

        String status = toStringValue(data.get("status"), STATUS_EXPIRED).toUpperCase();
        String otpHash = toStringValue(data.get("otpHash"), null);
        Instant expiresAt = toInstant(data.get("expiresAt"));
        Instant resendAfter = toInstant(data.get("resendAfter"));
        int attemptCount = Math.max(0, toInteger(data.get("attemptCount"), 0));
        int maxAttemptsValue = Math.max(1, toInteger(data.get("maxAttempts"), maxAttempts));
        Instant createdAt = toInstant(data.get("createdAt"));

        return new OtpStageSnapshot(
                status, otpHash, expiresAt, resendAfter, attemptCount, maxAttemptsValue, createdAt);
    }

    private Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Timestamp timestamp) {
            return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        }
        if (value instanceof Date date) {
            return date.toInstant();
        }
        if (value instanceof Number number) {
            return Instant.ofEpochMilli(number.longValue());
        }
        return null;
    }

    private int toInteger(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private String toStringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String converted = String.valueOf(value).trim();
        if (converted.isBlank()) {
            return defaultValue;
        }
        return converted;
    }

    private <T> T await(ApiFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Firestore operation interrupted", ex);
        } catch (ExecutionException ex) {
            Throwable rootCause = ex.getCause() == null ? ex : ex.getCause();
            throw new IllegalStateException("Firestore operation failed", rootCause);
        }
    }

    private record OtpStageSnapshot(
            String status,
            String otpHash,
            Instant expiresAt,
            Instant resendAfter,
            int attemptCount,
            int maxAttempts,
            Instant createdAt) {
        private boolean isLocked(Instant now) {
            return STATUS_LOCKED.equals(status)
                    && resendAfter != null
                    && now.getEpochSecond() < resendAfter.getEpochSecond();
        }

        private boolean hasPendingAndNotExpired(Instant now) {
            return STATUS_PENDING.equals(status)
                    && expiresAt != null
                    && now.getEpochSecond() < expiresAt.getEpochSecond();
        }

        private boolean isExpired(Instant now) {
            return expiresAt == null || now.getEpochSecond() >= expiresAt.getEpochSecond();
        }
    }
}
