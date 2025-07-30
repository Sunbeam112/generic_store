package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import ua.sunbeam.genericstore.model.DAO.UserRepository;
import ua.sunbeam.genericstore.model.LocalUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for the {@link UserRepository}.
 * This class uses {@link DataJpaTest} to provide an in-memory database
 * and focus only on the JPA layer, ensuring fast and isolated tests
 * for repository operations.
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private LocalUser testUser;
    private LocalUser anotherUser;

    /**
     * Sets up the test environment before each test method.
     * It clears the entity manager, deletes all existing users to ensure test isolation,
     * and then persists two predefined {@link LocalUser} entities for testing.
     * The persistence context is flushed and cleared to detach entities for fresh retrieval.
     */
    @BeforeEach
    void setUp() {
        // Clearing before delete is a good defensive practice
        entityManager.clear();
        userRepository.deleteAll(); // Ensures a clean state in the database

        testUser = new LocalUser();
        testUser.setEmail("test.user@example.com");
        testUser.setPassword("hashedPassword1"); // Ensure password is set for completeness
        testUser.setEmailVerified(true);
        entityManager.persist(testUser); // Use entityManager.persist for initial setup

        anotherUser = new LocalUser();
        anotherUser.setEmail("another.user@domain.com");
        anotherUser.setPassword("hashedPassword2"); // Ensure password is set
        anotherUser.setEmailVerified(false);
        entityManager.persist(anotherUser);

        // Crucial for detaching entities and ensuring subsequent reads hit the DB
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Tests that a {@link LocalUser} can be successfully found by its ID
     * when the user exists in the database.
     */
    @Test
    void testFindById_Found() {
        Optional<LocalUser> foundUser = userRepository.findById(testUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(testUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("test.user@example.com");
        assertThat(foundUser.get().isEmailVerified()).isTrue();
        // Optional: Assert password if you deem it necessary to verify retrieval of all fields
        // assertThat(foundUser.get().getPassword()).isEqualTo("hashedPassword1");
    }

    /**
     * Tests that `findById` returns an empty {@link Optional} when
     * no {@link LocalUser} with the given ID exists in the database.
     */
    @Test
    void testFindById_NotFound() {
        Long nonExistentId = 999L;

        Optional<LocalUser> foundUser = userRepository.findById(nonExistentId);

        assertThat(foundUser).isNotPresent();
    }

    /**
     * Tests that a {@link LocalUser} can be successfully found by their email,
     * irrespective of the casing, when the user exists in the database.
     */
    @Test
    void testFindByEmailIgnoreCase_Found() {
        // Test with exact casing
        Optional<LocalUser> foundUser = userRepository.findByEmailIgnoreCase("test.user@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test.user@example.com");
        assertThat(foundUser.get().getId()).isEqualTo(testUser.getId());

        // Test with all uppercase casing
        Optional<LocalUser> foundUserUpper = userRepository.findByEmailIgnoreCase("TEST.USER@EXAMPLE.COM");
        assertThat(foundUserUpper).isPresent();
        assertThat(foundUserUpper.get().getEmail()).isEqualTo("test.user@example.com"); // Email field retains original casing
        assertThat(foundUserUpper.get().getId()).isEqualTo(testUser.getId());

        // Test with mixed casing
        Optional<LocalUser> foundUserMixed = userRepository.findByEmailIgnoreCase("TeSt.UsEr@eXaMpLe.CoM");
        assertThat(foundUserMixed).isPresent();
        assertThat(foundUserMixed.get().getEmail()).isEqualTo("test.user@example.com"); // Email field retains original casing
        assertThat(foundUserMixed.get().getId()).isEqualTo(testUser.getId());
    }

    /**
     * Tests that `findByEmailIgnoreCase` returns an empty {@link Optional} for
     * various invalid or non-existent email inputs (null, empty string, non-existent email).
     *
     * @param email The email string to test for non-existence.
     */
    @ParameterizedTest(name = "testFindByEmailIgnoreCase_NotFoundOrInvalid[email=''{0}'']")
    @NullSource // Provides a null argument
    @EmptySource // Provides an empty string argument
    @ValueSource(strings = {"nonexistent@domain.com", "another.nonexistent@test.com"})
    // Provides string arguments
    void testFindByEmailIgnoreCase_NotFoundOrInvalid(String email) {
        Optional<LocalUser> foundUser = userRepository.findByEmailIgnoreCase(email);
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void testSaveNewUser() {
        LocalUser newUser = new LocalUser();
        newUser.setEmail("new.user@example.com");
        newUser.setPassword("newHashedPassword");
        newUser.setEmailVerified(true);

        LocalUser savedUser = userRepository.save(newUser);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new.user@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("newHashedPassword"); // Assert password is saved
        assertThat(savedUser.isEmailVerified()).isTrue();

        // Use entityManager.find to bypass repository cache if necessary for strict verification
        LocalUser foundInDb = entityManager.find(LocalUser.class, savedUser.getId());
        assertThat(foundInDb).isNotNull();
        assertThat(foundInDb.getEmail()).isEqualTo("new.user@example.com");
        assertThat(foundInDb.getPassword()).isEqualTo("newHashedPassword");
    }

    @Test
    void testSaveUserWithDuplicateEmail_ShouldThrowException() {
        LocalUser duplicateUser = new LocalUser();
        duplicateUser.setEmail(testUser.getEmail()); // Use an existing email
        duplicateUser.setPassword("somePassword");
        duplicateUser.setEmailVerified(false);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush(); // Force the write to database to trigger the constraint
        }, "Saving a user with a duplicate email should throw DataIntegrityViolationException.");
    }

    @Test
    void testUpdateUser() {
        Long userId = testUser.getId();
        // Fetch from repository to ensure it's a managed entity from this test's context
        LocalUser userToUpdate = userRepository.findById(userId).orElseThrow(
                () -> new AssertionError("Test user should be found for update"));

        userToUpdate.setEmail("updated.email@example.com");
        userToUpdate.setEmailVerified(false);
        userToUpdate.setPassword("new_secure_password"); // Update password as well for complete test

        LocalUser updatedUser = userRepository.save(userToUpdate);
        entityManager.flush(); // Flush changes to DB
        entityManager.clear(); // Clear cache to ensure fresh read from DB

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(userId);
        assertThat(updatedUser.getEmail()).isEqualTo("updated.email@example.com");
        assertThat(updatedUser.isEmailVerified()).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo("new_secure_password"); // Assert updated password

        Optional<LocalUser> fetchedUpdatedUser = userRepository.findById(userId);
        assertThat(fetchedUpdatedUser).isPresent();
        assertThat(fetchedUpdatedUser.get().getEmail()).isEqualTo("updated.email@example.com");
        assertThat(fetchedUpdatedUser.get().getPassword()).isEqualTo("new_secure_password");
        assertThat(fetchedUpdatedUser.get().isEmailVerified()).isFalse();
    }

    @Test
    void testDeleteUser() {
        Long userIdToDelete = testUser.getId();
        userRepository.deleteById(userIdToDelete);
        entityManager.flush(); // Ensure delete is committed to DB

        Optional<LocalUser> deletedUser = userRepository.findById(userIdToDelete);
        assertThat(deletedUser).isNotPresent();

        // Verify other users are unaffected
        assertThat(userRepository.findById(anotherUser.getId())).isPresent();
    }

    @Test
    void testCountUsers() {
        long count = userRepository.count();
        assertThat(count).isEqualTo(2); // Two users are set up initially

        LocalUser thirdUser = new LocalUser();
        thirdUser.setEmail("third.user@example.com");
        thirdUser.setPassword("pass");
        thirdUser.setEmailVerified(true);
        userRepository.save(thirdUser);
        entityManager.flush(); // Ensure new user is persisted

        assertThat(userRepository.count()).isEqualTo(3);
    }
}