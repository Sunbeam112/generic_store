package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {


    void removeById(Long id);

    List<Product> findByNameContainsIgnoreCase(String name);

    boolean existsByNameContainsIgnoreCase(String name);


}
