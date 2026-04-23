package doctor.Utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashHelper {
    private static final String HASH_ALGORITHM = "SHA-256";

    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("plainPassword is required");
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = messageDigest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Cannot initialize SHA-256", ex);
        }
    }

    public boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return hashPassword(plainPassword).equals(storedHash);
    }
}
