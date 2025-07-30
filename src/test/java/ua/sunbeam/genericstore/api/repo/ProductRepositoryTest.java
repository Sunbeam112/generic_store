package ua.sunbeam.genericstore.api.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link ProductRepository}.
 * This class uses {@link DataJpaTest} to provide an in-memory H2 database
 * and focuses solely on the JPA layer, ensuring fast and isolated tests
 * for repository operations related to {@link Product} entities.
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager; // Inject TestEntityManager for controlled test data setup

    // Common products used across tests, to be set up in @BeforeEach
    private Product product1;
    private Product product2;


    /**
     * Sets up the test environment before each test method.
     * It ensures a clean slate by deleting all products and then
     * persists two predefined {@link Product} entities for testing.
     * The persistence context is flushed and cleared to detach entities for fresh retrieval
     * by the repository methods.
     */
    @BeforeEach
    void setUp() {
        entityManager.clear(); // Clear any pending changes or cached entities
        productRepository.deleteAll(); // Ensure a clean state for each test

        product1 = new Product();
        product1.setName("Laptop Pro");
        product1.setPrice(1200.00);
        product1.setDescription("High-performance laptop.");
        product1.setCategory("Electronics");
        product1.setUrlPhoto("https://example.com/laptop_pro.jpg");
        product1.setSubcategory("Computers");
        product1.setShortDescription("Powerful and sleek.");
        entityManager.persist(product1); // Use entityManager to set up initial data

        product2 = new Product();
        product2.setName("Wireless Mouse");
        product2.setPrice(25.99);
        product2.setDescription("Ergonomic wireless mouse.");
        product2.setCategory("Electronics");
        product2.setUrlPhoto("https://example.com/mouse.jpg");
        product2.setSubcategory("Peripherals");
        product2.setShortDescription("Comfortable and precise.");
        entityManager.persist(product2);

        entityManager.flush(); // Synchronize the persistence context with the underlying database
        entityManager.clear(); // Detach all managed entities to ensure fresh data retrieval
    }

    /**
     * Tests that a new {@link Product} can be successfully saved to the database,
     * verifying that it is not null and has a generated ID.
     */
    @Test
    @DisplayName("Save new product should return product with ID")
    void testSaveNewProduct_ShouldReturnSavedProductWithId() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Smartphone X");
        newProduct.setPrice(800.00);
        newProduct.setDescription("Latest model smartphone.");
        newProduct.setCategory("Electronics");
        newProduct.setUrlPhoto("https://example.com/smartphone.jpg");
        newProduct.setSubcategory("Mobile");
        newProduct.setShortDescription("Feature-rich device.");

        // Act
        Product savedProduct = productRepository.save(newProduct);

        // Assert
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isGreaterThan(0);
        assertThat(savedProduct.getName()).isEqualTo("Smartphone X");
        assertThat(savedProduct.getPrice()).isEqualTo(800.00);
        // Add more assertions for other fields to ensure all data is saved correctly
    }

    /**
     * Tests that `findAll()` retrieves all {@link Product} entities from the database.
     */
    @Test
    @DisplayName("Find all products should return all existing products")
    void testFindAll_ShouldReturnAllProducts() {
        // Arrange (products are set up in @BeforeEach)

        // Act
        List<Product> products = (List<Product>) productRepository.findAll();

        // Assert
        assertThat(products)
                .isNotNull() // The list itself should not be null
                .hasSize(2)  // It should contain exactly two elements
                .extracting(Product::getName) // Extract the 'name' property from each product
                .containsExactlyInAnyOrder("Laptop Pro", "Wireless Mouse"); // Assert the names found, ignoring order
    }

    /**
     * Tests that `deleteById()` successfully removes a {@link Product} from the database.
     */
    @Test
    @DisplayName("Delete product by ID should remove it from DB")
    void testDeleteById_ShouldRemoveProductFromDb() {
        // Arrange (product1 is available from @BeforeEach)
        Long productIdToDelete = product1.getId();

        // Act
        productRepository.deleteById(productIdToDelete);
        entityManager.flush(); // Ensure the delete operation is committed

        // Assert
        Optional<Product> deletedProduct = productRepository.findById(productIdToDelete); // Use findById as getProductById is custom
        assertThat(deletedProduct).isEmpty(); // Assert that it's empty

        // Verify product2 still exists
        assertThat(productRepository.findById(product2.getId())).isPresent();
    }

    /**
     * Tests that `existsByNameContainsIgnoreCase()` returns true for an existing product name,
     * regardless of casing or if it's a partial match.
     */
    @Test
    @DisplayName("Exists by name (case insensitive/contains) should return true for existing product")
    void testExistsByNameContainsIgnoreCase_ShouldReturnTrueForExistingName() {

        // Act & Assert
        assertThat(productRepository.existsByNameContainsIgnoreCase("Laptop Pro")).isTrue(); // Exact match
        assertThat(productRepository.existsByNameContainsIgnoreCase("laptop pro")).isTrue(); // Lowercase
        assertThat(productRepository.existsByNameContainsIgnoreCase("LaPtOp PrO")).isTrue(); // Mixed case
        assertThat(productRepository.existsByNameContainsIgnoreCase("Laptop")).isTrue();    // Partial match
        assertThat(productRepository.existsByNameContainsIgnoreCase("Pro")).isTrue();       // Another partial match
    }

    /**
     * Tests that `existsByNameContainsIgnoreCase()` returns false for a non-existing product name.
     */
    @Test
    @DisplayName("Exists by name (case insensitive/contains) should return false for non-existing product")
    void testExistsByNameContainsIgnoreCase_ShouldReturnFalseForNonExistingName() {
        // Act & Assert
        assertThat(productRepository.existsByNameContainsIgnoreCase("NonExistent Product")).isFalse();
        assertThat(productRepository.existsByNameContainsIgnoreCase("Keyboard")).isFalse();
    }


    /**
     * Tests that `findById()` returns the correct {@link Product} when it exists,
     * and an empty {@link Optional} when it does not.
     */
    @Test
    @DisplayName("Find product by ID should return correct product or empty optional")
    void testFindById_ShouldReturnCorrectProductOrEmptyOptional() {
        // Arrange (product1 and product2 are available from @BeforeEach)

        // Act
        Optional<Product> productThatExists = productRepository.findById(product1.getId());
        Optional<Product> productThatNotExists = productRepository.findById(999L); // ID assumed not to exist

        // Assert
        assertThat(productThatExists).isPresent();
        assertThat(productThatExists.get().getName()).isEqualTo(product1.getName()); // Verify content

        assertThat(productThatNotExists).isNotPresent();
    }

    // --- Additional Tests for your Custom Repository Methods ---

    /**
     * Tests `getByNameIgnoreCase()` to ensure it returns products matching the name,
     * ignoring case.
     */
    @Test
    @DisplayName("Get by name (case insensitive) should return matching products")
    void testGetByNameIgnoreCase_ShouldReturnMatchingProducts() {
        // Arrange: Add another product with the same base name but different casing
        Product similarProduct = new Product();
        similarProduct.setName("laptop pro"); // Lowercase version
        similarProduct.setPrice(1100.00);
        similarProduct.setDescription("Older model.");
        similarProduct.setCategory("Electronics");
        similarProduct.setUrlPhoto("https://example.com/laptop_old.jpg");
        similarProduct.setSubcategory("Computers");
        similarProduct.setShortDescription("Good value.");
        entityManager.persist(similarProduct);
        entityManager.flush();
        entityManager.clear(); // Clear cache to ensure fresh read

        // Act
        List<Product> products = productRepository.getByNameIgnoreCase("Laptop Pro");
        List<Product> productsLower = productRepository.getByNameIgnoreCase("laptop pro");
        List<Product> productsMixed = productRepository.getByNameIgnoreCase("LaPtOp PrO");

        // Assert
        assertThat(products).hasSize(2); // product1 ("Laptop Pro") and similarProduct ("laptop pro")
        assertThat(products).extracting(Product::getName).containsExactlyInAnyOrder("Laptop Pro", "laptop pro");
        assertThat(productsLower).hasSize(2);
        assertThat(productsMixed).hasSize(2);
    }

    /**
     * Tests `getByNameIgnoreCase()` when no products match the given name.
     */
    @Test
    @DisplayName("Get by name (case insensitive) should return empty list for no match")
    void testGetByNameIgnoreCase_ShouldReturnEmptyListForNoMatch() {
        List<Product> products = productRepository.getByNameIgnoreCase("NonExistent Product");
        assertThat(products).isEmpty();
    }

    /**
     * Tests `findByPriceLessThanEqual()` to return products with price less than or equal to the given value.
     */
    @Test
    @DisplayName("Find by price less than or equal should return correct products")
    void testFindByPriceLessThanEqual_ShouldReturnCorrectProducts() {
        // product1 price: 1200.00, product2 price: 25.99
        List<Product> cheapProducts = productRepository.findByPriceLessThanEqual(100.00);
        assertThat(cheapProducts).hasSize(1);
        assertThat(cheapProducts.get(0).getName()).isEqualTo("Wireless Mouse");

        List<Product> allProducts = productRepository.findByPriceLessThanEqual(1500.00);
        assertThat(allProducts).hasSize(2);
        assertThat(allProducts).extracting(Product::getName).containsExactlyInAnyOrder("Laptop Pro", "Wireless Mouse");
    }

    /**
     * Tests `findByCategoryIgnoreCase()` to return products matching the category, ignoring case.
     */
    @Test
    @DisplayName("Find by category (case insensitive) should return matching products")
    void testFindByCategoryIgnoreCase_ShouldReturnMatchingProducts() {
        // product1 category: "Electronics", product2 category: "Electronics"
        List<Product> electronicsProducts = productRepository.findByCategoryIgnoreCase("Electronics");
        assertThat(electronicsProducts).hasSize(2);
        assertThat(electronicsProducts).extracting(Product::getName).containsExactlyInAnyOrder("Laptop Pro", "Wireless Mouse");

        List<Product> electronicsProductsLower = productRepository.findByCategoryIgnoreCase("electronics");
        assertThat(electronicsProductsLower).hasSize(2);
    }

    /**
     * Tests `getEveryCategory()` to retrieve a distinct list of all categories.
     */
    @Test
    @DisplayName("Get every category should return distinct list of categories")
    void testGetEveryCategory_ShouldReturnDistinctCategories() {
        // Arrange: Add another product with a different category
        Product clothingProduct = new Product();
        clothingProduct.setName("T-Shirt");
        clothingProduct.setPrice(15.00);
        clothingProduct.setDescription("Cotton t-shirt.");
        clothingProduct.setCategory("Apparel"); // New category
        clothingProduct.setUrlPhoto("https://example.com/tshirt.jpg");
        clothingProduct.setShortDescription("Basic wear.");
        entityManager.persist(clothingProduct);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<String> categories = productRepository.getEveryCategory();

        // Assert
        assertThat(categories)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder("Electronics", "Apparel");
    }

    /**
     * Tests `existsById()` when a product with the given ID exists.
     */
    @Test
    @DisplayName("Exists by ID should return true for existing product ID")
    void testExistsById_ShouldReturnTrueForExistingId() {
        assertThat(productRepository.existsById(product1.getId())).isTrue();
    }

    /**
     * Tests `existsById()` when a product with the given ID does not exist.
     */
    @Test
    @DisplayName("Exists by ID should return false for non-existing product ID")
    void testExistsById_ShouldReturnFalseForNonExistingId() {
        assertThat(productRepository.existsById(999L)).isFalse();
    }

    /**
     * Tests `getProductById()` to ensure it returns the correct product when found
     * and handles the case where the product is not found.
     * Note: This method seems redundant if `findById` from CrudRepository is used.
     * Consider if you truly need both, or if `findById` is enough.
     */
    @Test
    @DisplayName("Get product by ID should return correct product or empty optional")
    void testGetProductById_ShouldReturnCorrectProductOrEmptyOptional() {
        // Act
        Optional<Product> foundProduct = productRepository.getProductById(product1.getId());
        Optional<Product> notFoundProduct = productRepository.getProductById(999L);

        // Assert
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo(product1.getName());

        assertThat(notFoundProduct).isEmpty();
    }

    // You have `removeById` in your repository. Let's add a test for it.

    /**
     * Tests the custom `removeById()` method to ensure it deletes the product.
     * Note: This is different from CrudRepository's `deleteById()`.
     */
    @Test
    @DisplayName("Remove by ID should delete the product")
    void testRemoveById_ShouldDeleteProduct() {
        // Arrange: product2 is available from @BeforeEach
        Long productIdToRemove = product2.getId();

        // Act
        productRepository.removeById(productIdToRemove);
        entityManager.flush(); // Ensure the delete operation is committed

        // Assert
        Optional<Product> removedProduct = productRepository.findById(productIdToRemove);
        assertThat(removedProduct).isEmpty();

        // Verify product1 still exists
        assertThat(productRepository.findById(product1.getId())).isPresent();
    }
}