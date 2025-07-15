package ua.sunbeam.genericstore.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing product-related operations.
 * This class interacts with the ProductRepository to perform CRUD operations
 * and other business logic related to products.
 */
@Service
@CrossOrigin
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Constructor for ProductService.
     *
     * @param productRepository The repository for product data access.
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves all products.
     *
     * @return A list of all products.
     */
    public List<Product> findAll() {
        // Implementation to find all products
        Iterable<Product> products = productRepository.findAll();
        return (List<Product>) products;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return An Optional containing the product if found, or an empty Optional otherwise.
     */
    public Optional<Product> findById(Long id) {
        // Implementation to find a product by ID
        Optional<Product> product;
        if (productRepository.existsById(id)) {
            product = productRepository.getProductById(id);
            return product;
        }
        return Optional.empty();
    }

    /**
     * Removes a product by its ID.
     *
     * @param id The ID of the product to remove.
     */
    public void removeById(Long id) {
        // Implementation to remove a product by ID
        productRepository.removeById(id);
    }

    /**
     * Adds a new product.
     *
     * @param product The product to add.
     * @return An Optional containing the added product if successful, or an empty Optional if a product with the same ID already exists.
     * @throws IllegalArgumentException if a product with the same name already exists.
     */
    public Optional<Product> addProduct(Product product) throws IllegalArgumentException {
        if (product == null) return Optional.empty();
        if (product.getId() == null) {
            if (productRepository.existsByNameContainsIgnoreCase(product.getName())) {
                throw new IllegalArgumentException("Product with name " + product.getName() + " already exists");
            }
            return Optional.of(productRepository.save(product));
        }


        return Optional.empty();
    }

    /**
     * Adds multiple products to the repository.
     *
     * @param products A list of products to add.
     * @return An Iterable containing the saved products.
     * @throws IllegalArgumentException if the products list is null or empty.
     */
    public Iterable<Product> addAll(List<Product> products) throws IllegalArgumentException {
        if (products != null && !products.isEmpty()) {
            return productRepository.saveAll(products);
        }
        throw new IllegalArgumentException("Product list cannot be null or empty");
    }


    /**
     * Retrieves all products whose name contains the given string (case-insensitive).
     *
     * @param name The string to search for in product names.
     * @return A list of products matching the criteria, or an empty list if no matches or the name is null/empty.
     */
    public List<Product> getAllProductsByName(String name) {
        // Implementation to get all products by name
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        if (productRepository.existsByNameContainsIgnoreCase(name)) {
            return productRepository.getByNameIgnoreCase(name);
        }
        return List.of();
    }

    /**
     * Checks if a product with the given name (case-insensitive) exists.
     *
     * @param name The name to check.
     * @return True if a product with the name exists, false otherwise.
     */
    public boolean isExistsByName(String name) {
        // Implementation to check if a product exists by name
        if (name != null && !name.trim().isEmpty()) {
            return productRepository.existsByNameContainsIgnoreCase(name);
        }
        return false;
    }

    /**
     * Retrieves all products belonging to a specific category (case-insensitive).
     *
     * @param category The category to search for.
     * @return An Optional containing a list of products in the specified category, or an empty Optional if the category is null/empty or no products are found.
     */
    public Optional<List<Product>> getAllProductsByCategory(String category) {
        // Implementation to get all products by category
        if (category != null && !category.trim().isEmpty()) {
            return Optional.ofNullable(productRepository.findByCategoryIgnoreCase(category));
        }
        return Optional.empty();
    }

    /**
     * Retrieves all unique categories available for products.
     *
     * @return A list of all unique product categories.
     */
    public List<String> getAllCategories() {
        // Implementation to get all unique categories
        return productRepository.getEveryCategory();
    }

    public Object getAllProducts() {
        return productRepository.findAll();
    }
}