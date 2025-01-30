package ua.sunbeam.genericstore.service;

import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        Iterable<Product> products = productRepository.findAll();
        return (List<Product>) products;
    }


    public void removeById(Long id) {
        productRepository.removeById(id);
    }


    /**
     * @return true if product is added, otherwise false
     */
    public boolean addProduct(Product product) {
        if (product.getId() == null) {
            productRepository.save(product);
            return true;
        } else return false;
    }


    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        if (productRepository.existsByNameContainsIgnoreCase(name)) {
            return productRepository.findByNameContainsIgnoreCase(name);
        }
        return List.of();
    }


    public boolean isExistsByName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return productRepository.existsByNameContainsIgnoreCase(name);
        }
        return false;
    }
}
