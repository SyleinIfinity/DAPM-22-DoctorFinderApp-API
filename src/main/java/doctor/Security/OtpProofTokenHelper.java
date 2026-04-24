package doctor.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OtpProofTokenHelper {
    private static final String CLAIM_PURPOSE = "purpose";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_VALUE = "OTP_PROOF";

    private final Key signingKey;
    private final long proofExpireSeconds;

    public OtpProofTokenHelper(
            @Value("${app.otp.proof-secret}") String proofSecret,
            @Value("${app.otp.proof-expire-seconds:900}") long proofExpireSeconds) {
        if (proofSecret == null || proofSecret.isBlank()) {
            throw new IllegalStateException("app.otp.proof-secret is required");
        }
        byte[] keyBytes = proofSecret.trim().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.otp.proof-secret must be at least 32 characters");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.proofExpireSeconds = Math.max(60L, proofExpireSeconds);
    }

    public String generateProofToken(String email, String purpose) {
        String normalizedEmail = normalizeRequired(email, "email").toLowerCase();
        String normalizedPurpose = normalizeRequired(purpose, "purpose").toUpperCase();
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(normalizedEmail)
                .claim(CLAIM_PURPOSE, normalizedPurpose)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_VALUE)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(proofExpireSeconds)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateProofToken(String token, String expectedEmail, String expectedPurpose) {
        String normalizedToken = normalizeRequired(token, "otpProofToken");
        String normalizedEmail = normalizeRequired(expectedEmail, "expectedEmail").toLowerCase();
        String normalizedPurpose = normalizeRequired(expectedPurpose, "expectedPurpose").toUpperCase();

        Claims claims;
        try {
            claims =
                    Jwts.parserBuilder()
                            .setSigningKey(signingKey)
                            .build()
                            .parseClaimsJws(normalizedToken)
                            .getBody();
        } catch (JwtException ex) {
            throw new IllegalArgumentException("otpProofToken khong hop le hoac da het han");
        }

        String tokenType = valueAsString(claims.get(CLAIM_TOKEN_TYPE)).toUpperCase();
        String purpose = valueAsString(claims.get(CLAIM_PURPOSE)).toUpperCase();
        String email = valueAsString(claims.getSubject()).toLowerCase();

        if (!TOKEN_TYPE_VALUE.equals(tokenType)) {
            throw new IllegalArgumentException("otpProofToken khong hop le");
        }
        if (!normalizedPurpose.equals(purpose)) {
            throw new IllegalArgumentException("otpProofToken khong dung muc dich");
        }
        if (!normalizedEmail.equals(email)) {
            throw new IllegalArgumentException("otpProofToken khong khop voi email");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private String valueAsString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
