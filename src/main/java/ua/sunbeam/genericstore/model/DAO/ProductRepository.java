package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CrudRepository<Product, Long> {


    void removeById(Long id);

    List<Product> findByNameContainsIgnoreCase(String name);

    boolean existsByNameContainsIgnoreCase(String name);

    List<Product> findByDescriptionIgnoreCase(String description);



    List<Product> findByPriceLessThanEqual(Double price);

    List<Product> findByCategoryIgnoreCase(String category);


}
