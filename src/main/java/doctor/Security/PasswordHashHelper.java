package doctor.Security;

import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class PasswordHashHelper {
    private final PasswordEncoder passwordEncoder;

    public PasswordHashHelper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("plainPassword is required");
        }
        return passwordEncoder.encode(plainPassword);
    }

    public boolean matches(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, storedHash);
    }
}
