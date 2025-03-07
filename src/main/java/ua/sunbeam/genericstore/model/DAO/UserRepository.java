package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.sunbeam.genericstore.model.LocalUser;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<LocalUser, Long> {


    @Override
    Optional<LocalUser> findById(Long id);

    Optional<LocalUser> findByEmailIgnoreCase(String email);


}
