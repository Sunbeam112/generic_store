package ua.sunbeam.genericstore.model.DAO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends CrudRepository<Product, Long> {


    void removeById(Long id);

    List<Product> findByNameContainsIgnoreCase(String name);

    boolean existsByNameContainsIgnoreCase(String name);



    List<Product> findByPriceLessThanEqual(Double price);

    List<Product> findByCategoryIgnoreCase(String category);


    @Query(value = "select distinct category from product", nativeQuery = true)
    List<String> getEveryCategory();

    List<Product> findByNameInIgnoreCase(Collection<String> names);


    @Override
    boolean existsById(Long aLong);
}
