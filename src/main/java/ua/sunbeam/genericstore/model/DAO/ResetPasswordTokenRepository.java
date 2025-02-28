package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.ResetPasswordToken;

public interface ResetPasswordTokenRepository extends CrudRepository<ResetPasswordToken, Long> {
}
