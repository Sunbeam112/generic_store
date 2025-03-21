package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    long deleteByLocalUser(LocalUser localUser);
}
