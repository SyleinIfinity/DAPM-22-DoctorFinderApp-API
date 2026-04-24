package doctor.Security;

import doctor.Security.Exceptions.TooManyRequestsException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtpRateLimitService {
    private final ConcurrentMap<String, RateState> states = new ConcurrentHashMap<>();

    @Value("${app.security.otp.window-seconds:600}")
    private long windowSeconds;

    @Value("${app.security.otp.block-seconds:900}")
    private long blockSeconds;

    @Value("${app.security.otp.send.max-per-ip:10}")
    private int sendMaxPerIp;

    @Value("${app.security.otp.send.max-per-email:5}")
    private int sendMaxPerEmail;

    @Value("${app.security.otp.verify.max-per-ip:20}")
    private int verifyMaxPerIp;

    @Value("${app.security.otp.verify.max-per-email:10}")
    private int verifyMaxPerEmail;

    public void checkSendAllowed(String clientIp, String email) {
        checkAndConsume(
                "otp-send-ip:" + normalize(clientIp, "unknown"),
                sendMaxPerIp,
                "Gui OTP qua nhieu lan tu IP nay. Vui long thu lai sau.");
        checkAndConsume(
                "otp-send-email:" + normalize(email, "unknown"),
                sendMaxPerEmail,
                "Gui OTP qua nhieu lan cho email nay. Vui long thu lai sau.");
    }

    public void checkVerifyAllowed(String clientIp, String email) {
        checkAndConsume(
                "otp-verify-ip:" + normalize(clientIp, "unknown"),
                verifyMaxPerIp,
                "Xac thuc OTP qua nhieu lan tu IP nay. Vui long thu lai sau.");
        checkAndConsume(
                "otp-verify-email:" + normalize(email, "unknown"),
                verifyMaxPerEmail,
                "Xac thuc OTP qua nhieu lan cho email nay. Vui long thu lai sau.");
    }

    private void checkAndConsume(String key, int maxAttempts, String message) {
        long now = Instant.now().getEpochSecond();
        RateState updated =
                states.compute(
                        key,
                        (unused, existing) -> {
                            RateState state = existing;
                            if (state == null
                                    || state.windowStartedEpochSeconds <= 0
                                    || now - state.windowStartedEpochSeconds > windowSeconds) {
                                state = new RateState();
                                state.windowStartedEpochSeconds = now;
                                state.count = 0;
                                state.blockedUntilEpochSeconds = 0;
                            }

                            if (state.blockedUntilEpochSeconds > now) {
                                return state;
                            }

                            if (state.count >= maxAttempts) {
                                state.blockedUntilEpochSeconds = now + blockSeconds;
                                return state;
                            }

                            state.count += 1;
                            return state;
                        });

        if (updated != null && updated.blockedUntilEpochSeconds > now) {
            throw new TooManyRequestsException(message, updated.blockedUntilEpochSeconds - now);
        }
    }

    private String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toLowerCase();
    }

    private static final class RateState {
        private int count;
        private long windowStartedEpochSeconds;
        private long blockedUntilEpochSeconds;
    }
}
