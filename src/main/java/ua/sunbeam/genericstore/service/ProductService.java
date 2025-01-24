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

    public Product findById(long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
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
}
