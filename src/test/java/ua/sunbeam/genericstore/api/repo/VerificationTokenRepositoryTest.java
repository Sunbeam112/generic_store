package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.DAO.VerificationTokenRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.VerificationToken;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
public class VerificationTokenRepositoryTest {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository localUserRepository; // Assuming you have this repository for LocalUser

    private LocalUser testUser;

    @BeforeEach
    void setUp() {
        // Create and save a LocalUser for testing
        testUser = new LocalUser();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123"); // Password should be encoded in a real app
        testUser.setEmailVerified(false);
        testUser = localUserRepository.save(testUser);
    }

    @Test
    void testFindByToken() {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setLocalUser(testUser);
        verificationToken.setCreatedTimestamp(Timestamp.from(Instant.now()));
        verificationTokenRepository.save(verificationToken);

        Optional<VerificationToken> foundToken = verificationTokenRepository.findByToken(token);

        Assertions.assertTrue(foundToken.isPresent(), "Verification token should be found");
        Assertions.assertEquals(token, foundToken.get().getToken(), "Found token should match the original");
        Assertions.assertEquals(testUser.getId(), foundToken.get().getLocalUser().getId(), "Token should belong to the correct user");
    }

    @Test
    void testFindByToken_NotFound() {
        Optional<VerificationToken> foundToken = verificationTokenRepository.findByToken("nonexistent_token");
        Assertions.assertFalse(foundToken.isPresent(), "Verification token should not be found");
    }

    @Test
    void testDeleteByLocalUser() {
        // Create multiple tokens for the same user
        VerificationToken token1 = new VerificationToken();
        token1.setToken(UUID.randomUUID().toString());
        token1.setLocalUser(testUser);
        token1.setCreatedTimestamp(Timestamp.from(Instant.now().minusSeconds(3600))); // 1 hour ago
        verificationTokenRepository.save(token1);

        VerificationToken token2 = new VerificationToken();
        token2.setToken(UUID.randomUUID().toString());
        token2.setLocalUser(testUser);
        token2.setCreatedTimestamp(Timestamp.from(Instant.now()));
        verificationTokenRepository.save(token2);

        // Verify tokens exist
        Assertions.assertEquals(2, verificationTokenRepository.count(), "There should be two tokens for the user");

        // Perform deletion
        long deletedCount = verificationTokenRepository.deleteByLocalUser(testUser);

        Assertions.assertEquals(2, deletedCount, "Two tokens should have been deleted");
        Assertions.assertEquals(0, verificationTokenRepository.count(), "No tokens should remain for the user");

        // Verify tokens are actually gone by trying to find them
        Assertions.assertFalse(verificationTokenRepository.findByToken(token1.getToken()).isPresent());
        Assertions.assertFalse(verificationTokenRepository.findByToken(token2.getToken()).isPresent());
    }

    @Test
    void testDeleteByLocalUser_NoTokensForUser() {
        // Create another user
        LocalUser anotherUser = new LocalUser();
        anotherUser.setEmail("anotheruser@example.com");
        anotherUser.setPassword("anotherpassword");
        anotherUser.setEmailVerified(true);
        anotherUser = localUserRepository.save(anotherUser);

        // No tokens for testUser yet, ensure no deletion occurs
        long deletedCount = verificationTokenRepository.deleteByLocalUser(testUser);
        Assertions.assertEquals(0, deletedCount, "No tokens should be deleted if user has none");
    }


    @Test
    void testSaveVerificationToken_UniqueTokenConstraint() {
        String duplicateToken = UUID.randomUUID().toString();

        VerificationToken token1 = new VerificationToken();
        token1.setToken(duplicateToken);
        token1.setLocalUser(testUser);
        token1.setCreatedTimestamp(Timestamp.from(Instant.now()));
        verificationTokenRepository.save(token1);

        VerificationToken token2 = new VerificationToken();
        token2.setToken(duplicateToken); // Same token
        token2.setLocalUser(testUser);
        token2.setCreatedTimestamp(Timestamp.from(Instant.now()));

        // Expect a DataIntegrityViolationException due to unique constraint
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            verificationTokenRepository.save(token2);
        }, "Saving a duplicate token should throw DataIntegrityViolationException");
    }

    @Test
    void testSaveVerificationToken_NotNullConstraints() {
        // Test case 1: token is null
        VerificationToken invalidToken1 = new VerificationToken();
        invalidToken1.setLocalUser(testUser);
        invalidToken1.setCreatedTimestamp(Timestamp.from(Instant.now()));
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            verificationTokenRepository.save(invalidToken1);
        }, "Saving with null token should throw DataIntegrityViolationException");

        // Test case 2: localUser is null
        VerificationToken invalidToken2 = new VerificationToken();
        invalidToken2.setToken("some_token_2");
        invalidToken2.setCreatedTimestamp(Timestamp.from(Instant.now()));
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            verificationTokenRepository.save(invalidToken2);
        }, "Saving with null localUser should throw DataIntegrityViolationException");

        // Test case 3: createdTimestamp is null
        VerificationToken invalidToken3 = new VerificationToken();
        invalidToken3.setToken("some_token_3");
        invalidToken3.setLocalUser(testUser);
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> {
            verificationTokenRepository.save(invalidToken3);
        }, "Saving with null createdTimestamp should throw DataIntegrityViolationException");
    }
}