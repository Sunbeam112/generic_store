package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {


    void removeById(Long id);

    List<Product> getByNameIgnoreCase(String name);

    boolean existsByNameContainsIgnoreCase(String name);


    List<Product> findByPriceLessThanEqual(Double price);

    List<Product> findByCategoryIgnoreCase(String category);


    @Query(value = "select distinct category from product", nativeQuery = true)
    List<String> getEveryCategory();


    @Override
    boolean existsById(Long id);

    Optional<Product> getProductById(Long id);
}
