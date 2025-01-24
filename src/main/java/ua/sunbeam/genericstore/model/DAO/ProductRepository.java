package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {

    @Override
    Optional<Product> findById(Long id);

    @Override
    boolean existsById(Long aLong);

    @Override
    void deleteById(Long id);


}
