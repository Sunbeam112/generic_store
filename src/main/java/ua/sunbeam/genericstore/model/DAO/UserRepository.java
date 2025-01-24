package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.LocalUser;

import java.util.Optional;

public interface UserRepository extends CrudRepository<LocalUser, Long> {


    @Override
    public Optional<LocalUser> findById(Long id);

    Optional<LocalUser> findByEmailIgnoreCase(String email);


}
