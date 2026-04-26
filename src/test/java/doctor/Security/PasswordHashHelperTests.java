package doctor.Security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashHelperTests {
    private final PasswordHashHelper passwordHashHelper =
            new PasswordHashHelper(new BCryptPasswordEncoder(12));

    @Test
    void hashPassword_shouldGenerateDifferentHashButStillMatch() {
        String firstHash = passwordHashHelper.hashPassword("abc12345");
        String secondHash = passwordHashHelper.hashPassword("abc12345");
        Assertions.assertNotEquals(firstHash, secondHash);
        Assertions.assertTrue(passwordHashHelper.matches("abc12345", firstHash));
        Assertions.assertTrue(passwordHashHelper.matches("abc12345", secondHash));
    }

    @Test
    void matches_shouldReturnTrueForCorrectPassword() {
        String hash = passwordHashHelper.hashPassword("abc12345");
        Assertions.assertTrue(passwordHashHelper.matches("abc12345", hash));
    }

    @Test
    void matches_shouldReturnFalseForIncorrectPassword() {
        String hash = passwordHashHelper.hashPassword("abc12345");
        Assertions.assertFalse(passwordHashHelper.matches("wrong-password", hash));
    }
}
