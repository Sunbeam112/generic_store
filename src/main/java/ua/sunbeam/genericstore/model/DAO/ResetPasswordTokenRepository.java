package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.sunbeam.genericstore.model.ResetPasswordToken;

@Repository
public interface ResetPasswordTokenRepository extends CrudRepository<ResetPasswordToken, Long> {
    ResetPasswordToken getByTokenIgnoreCase(String token);

    @Override
    boolean existsById(Long aLong);

    void flush();
}
