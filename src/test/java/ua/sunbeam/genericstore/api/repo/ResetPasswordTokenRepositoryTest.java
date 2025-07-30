package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ua.sunbeam.genericstore.model.DAO.ResetPasswordTokenRepository;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.ResetPasswordToken;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@DataJpaTest
public class ResetPasswordTokenRepositoryTest {

    @Autowired
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Autowired
    private UserRepository localUserRepository;

    private LocalUser testUser;

    @BeforeEach
    void setUp() {
        testUser = new LocalUser();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setEmailVerified(true);
        testUser = localUserRepository.save(testUser);
    }

    @Test
    void testGetByTokenIgnoreCase() {
        String token = UUID.randomUUID().toString();
        String lowerCaseToken = token.toLowerCase();
        String upperCaseToken = token.toUpperCase();

        ResetPasswordToken resetToken = new ResetPasswordToken();
        resetToken.setToken(token);
        resetToken.setLocalUser(testUser);
        resetToken.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        resetToken.setIsTokenUsed(false);
        resetPasswordTokenRepository.save(resetToken);

        ResetPasswordToken foundToken = resetPasswordTokenRepository.getByTokenIgnoreCase(token);
        Assertions.assertNotNull(foundToken, "Token should be found with original case");
        Assertions.assertEquals(token, foundToken.getToken(), "Found token's case should match original stored token");

        foundToken = resetPasswordTokenRepository.getByTokenIgnoreCase(lowerCaseToken);
        Assertions.assertNotNull(foundToken, "Token should be found with lower case");
        Assertions.assertEquals(token, foundToken.getToken(), "Found token's case should match original stored token");

        foundToken = resetPasswordTokenRepository.getByTokenIgnoreCase(upperCaseToken);
        Assertions.assertNotNull(foundToken, "Token should be found with upper case");
        Assertions.assertEquals(token, foundToken.getToken(), "Found token's case should match original stored token");
    }

    @Test
    void testGetByTokenIgnoreCase_NotFound() {
        ResetPasswordToken foundToken = resetPasswordTokenRepository.getByTokenIgnoreCase("nonexistent_token");
        Assertions.assertNull(foundToken, "No token should be found for a nonexistent token");
    }

    @Test
    void testExistsById() {
        ResetPasswordToken resetToken = new ResetPasswordToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setLocalUser(testUser);
        resetToken.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        resetToken.setIsTokenUsed(false);
        resetToken = resetPasswordTokenRepository.save(resetToken);

        Assertions.assertTrue(resetPasswordTokenRepository.existsById(resetToken.getId()), "Token should exist by its ID");
        Assertions.assertFalse(resetPasswordTokenRepository.existsById(-99L), "Token should not exist for a non-existent ID");
    }

    @Test
    void testSaveResetPasswordToken_UniqueTokenConstraint() {
        String duplicateToken = UUID.randomUUID().toString();

        ResetPasswordToken token1 = new ResetPasswordToken();
        token1.setToken(duplicateToken);
        token1.setLocalUser(testUser);
        token1.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        token1.setIsTokenUsed(false);
        resetPasswordTokenRepository.save(token1);
        resetPasswordTokenRepository.flush(); // Force flush for unique constraint check

        ResetPasswordToken token2 = new ResetPasswordToken();
        token2.setToken(duplicateToken); // Same token
        token2.setLocalUser(testUser);
        token2.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(2, ChronoUnit.HOURS)));
        token2.setIsTokenUsed(false);

        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            resetPasswordTokenRepository.save(token2);
            resetPasswordTokenRepository.flush(); // Force flush here too
        }, "Saving a duplicate token should throw DataIntegrityViolationException");
    }

    @Test
    void testSaveResetPasswordToken_NotNullConstraints() {
        // Test case 1: token is null
        ResetPasswordToken invalidToken1 = new ResetPasswordToken();
        invalidToken1.setLocalUser(testUser);
        invalidToken1.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        invalidToken1.setIsTokenUsed(false);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            resetPasswordTokenRepository.save(invalidToken1);
            resetPasswordTokenRepository.flush(); // Force flush
        }, "Saving with null token should throw DataIntegrityViolationException");

        // Test case 2: expiryDateInMilliseconds is null
        ResetPasswordToken invalidToken2 = new ResetPasswordToken();
        invalidToken2.setToken(UUID.randomUUID().toString());
        invalidToken2.setLocalUser(testUser);
        invalidToken2.setIsTokenUsed(false);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            resetPasswordTokenRepository.save(invalidToken2);
            resetPasswordTokenRepository.flush(); // Force flush
        }, "Saving with null expiryDateInMilliseconds should throw DataIntegrityViolationException");

        // Test case 3: isTokenUsed is null (if the column in DB is truly NOT NULL and default isn't applied on null input)
        // Note: For primitive `boolean`, this test isn't applicable. For `Boolean` wrapper, if the DB's
        // `NOT NULL` constraint applies *before* `@ColumnDefault`, this will fail.
        // Given your current DDL, it's possible H2 applies the default before NOT NULL on explicit null for Boolean.
        // It's safer to rely on testing truly non-nullable fields like 'token' and 'expiryDateInMilliseconds'.
        // However, if you want to explicitly test, ensure the DB schema for H2 respects NOT NULL over DEFAULT when NULL is explicitly provided.
        // If it still doesn't fail, it means H2 is applying the default or the column isn't truly NOT NULL.
        ResetPasswordToken invalidToken3 = new ResetPasswordToken();
        invalidToken3.setToken(UUID.randomUUID().toString());
        invalidToken3.setLocalUser(testUser);
        invalidToken3.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        invalidToken3.setIsTokenUsed(null); // Explicitly set to null
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            resetPasswordTokenRepository.save(invalidToken3);
            resetPasswordTokenRepository.flush(); // Force flush
        }, "Saving with null isTokenUsed should throw DataIntegrityViolationException (if DB enforces it)");
    }

    @Test
    void testSaveResetPasswordToken_LocalUserNotNullConstraint() {
        // Test case: localUser is null
        ResetPasswordToken invalidToken = new ResetPasswordToken();
        invalidToken.setToken(UUID.randomUUID().toString());
        invalidToken.setExpiryDateInMilliseconds(Timestamp.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        invalidToken.setIsTokenUsed(false);
        // localUser is not set, so it's null
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            resetPasswordTokenRepository.save(invalidToken);
            resetPasswordTokenRepository.flush(); // Force flush
        }, "Saving with null localUser should throw DataIntegrityViolationException");
    }
}