package ua.sunbeam.genericstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = new EncryptionService();
        ReflectionTestUtils.setField(encryptionService, "saltRounds", 10);
        encryptionService.postConstruct();
    }

    @Test
    void testEncryptPasswordGeneratesValidHash() {
        String password = "mySecurePassword123";
        String encryptedHash = encryptionService.encryptPassword(password);

        assertThat(encryptedHash).isNotNull().isNotEmpty();

        assertThat(encryptedHash).matches("^\\$2[ayb]\\$\\d{2}\\$.{53}$");
    }

    @Test
    void testDecryptPasswordMatchesCorrectPassword() {
        String password = "mySecurePassword123";
        String encryptedHash = encryptionService.encryptPassword(password); // Encrypt first

        boolean matches = encryptionService.decryptPassword(password, encryptedHash);

        assertThat(matches).isTrue();
    }

    @Test
    void testDecryptPasswordDoesNotMatchIncorrectPassword() {
        String originalPassword = "mySecurePassword123";
        String incorrectPassword = "wrongPassword";
        String encryptedHash = encryptionService.encryptPassword(originalPassword); // Encrypt original

        boolean matches = encryptionService.decryptPassword(incorrectPassword, encryptedHash);

        assertThat(matches).isFalse();
    }

    @Test
    void testDecryptPasswordDoesNotMatchDifferentHash() {
        String password = "mySecurePassword123";
        String someOtherHash = "$2a$10$abcdefghijklmnopqrstuvwxy1234567890abcdefghijklmnopqrstuvwx"; // A valid-looking but arbitrary hash

        boolean matches = encryptionService.decryptPassword(password, someOtherHash);

        assertThat(matches).isFalse();
    }

    @Test
    void testSaltIsGeneratedOnPostConstruct() {

        String generatedSalt = (String) ReflectionTestUtils.getField(encryptionService, "salt");

        assertThat(generatedSalt).isNotNull().isNotEmpty();

        assertThat(generatedSalt).matches("^\\$2[ayb]\\$\\d{2}\\$.{22}$"); // Regex for BCrypt salt structure
    }
}
