package ua.sunbeam.genericstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.sunbeam.genericstore.model.DAO.InventoryRepository;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Inventory;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.model.ProductImage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link ProductService} class using Mockito.
 * These tests focus on the business logic of the service layer,
 * mocking interactions with the {@link ProductRepository} and {@link InventoryRepository}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock // Mock the InventoryRepository as it's a dependency in ProductService
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;

    /**
     * Sets up common test data before each test method execution.
     * Initializes two {@link Product} objects with distinct IDs and properties.
     */
    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setPrice(1200.00);
        product1.setDescription("Powerful laptop");
        product1.setCategory("Electronics");
        product1.setUrlPhoto("url1");
        product1.setSubcategory("Computers");
        product1.setShortDescription("Fast and efficient");

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setPrice(25.00);
        product2.setDescription("Wireless mouse");
        product2.setCategory("Electronics");
        product2.setUrlPhoto("url2");
        product2.setSubcategory("Peripherals");
        product2.setShortDescription("Ergonomic design");
    }

    /**
     * Tests the {@link ProductService#addProduct(Product)} method when adding a
     * brand-new product (ID is null) and no product with the same name exists.
     * Verifies that the product is saved and an inventory record is created.
     */
    @Test
    @DisplayName("addProduct (new product, no ID, no existing name) should return saved product")
    void addProduct_NewProduct_NoId_Success() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setName("Smartphone X");
        newProduct.setPrice(800.00);
        newProduct.setDescription("Latest model smartphone.");
        newProduct.setCategory("Electronics");
        newProduct.setUrlPhoto("https://example.com/smartphone.jpg");
        newProduct.setSubcategory("Mobile");
        newProduct.setShortDescription("Feature-rich device.");
        newProduct.setId(null); // Ensure ID is null for this scenario

        // Mock repository calls based on service logic for a new product
        when(productRepository.existsByNameContainsIgnoreCase(newProduct.getName())).thenReturn(false);

        // Mock the first save call to simulate ID generation
        when(productRepository.save(newProduct)).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(3L); // Simulate generated ID
            }
            return saved;
        });

        // Mock the inventory save
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inv = invocation.getArgument(0);
            inv.setId(10L); // Simulate ID for inventory
            return inv;
        });

        // Act
        Optional<Product> result = productService.addProduct(newProduct);

        // Assert
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getId()).isNotNull();
                    assertThat(p.getName()).isEqualTo(newProduct.getName());
                    assertThat(p.getInventory()).isNotNull(); // Verify inventory was set
                });

        // Verify repository interactions
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase(newProduct.getName());
        // Service saves the product initially, then again after setting inventory.
        // Mockito will count this as two calls if `any(Product.class)` is used for the second `when`.
        // A more precise test might chain mocks or verify specific product instances if they change between saves.
        // For simplicity, we verify it was called twice.
        verify(productRepository, times(2)).save(any(Product.class));
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    /**
     * Tests the {@link ProductService#addProduct(Product)} method when a new product
     * (ID is null) is attempted to be added but a product with the same name already exists.
     * Verifies that an `IllegalArgumentException` is thrown.
     */
    @Test
    @DisplayName("addProduct (new product, no ID, existing name) should throw IllegalArgumentException")
    void addProduct_NewProduct_NoId_ExistingName_ThrowsIllegalArgumentException() {
        // Arrange
        Product existingNamedProduct = new Product();
        existingNamedProduct.setName(product1.getName()); // Use an existing name
        existingNamedProduct.setId(null);

        // Mock that a product with this name already exists
        when(productRepository.existsByNameContainsIgnoreCase(existingNamedProduct.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.addProduct(existingNamedProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product with name " + existingNamedProduct.getName() + " already exists");

        // Verify repository interactions: only the existence check should happen
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase(existingNamedProduct.getName());
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    /**
     * Tests the {@link ProductService#addProduct(Product)} method when trying to add
     * a product that already has an ID. The service logic dictates this should return
     * an empty {@link Optional}.
     */
    @Test
    @DisplayName("addProduct (product with existing ID) should return empty Optional")
    void addProduct_ProductWithExistingId_ReturnsEmptyOptional() {
        // Arrange
        // product1 already has an ID (1L) from @BeforeEach

        // Act
        Optional<Product> result = productService.addProduct(product1);

        // Assert
        assertThat(result).isEmpty();

        // Verify no repository interactions, as service directly returns empty Optional
        verify(productRepository, never()).existsById(anyLong());
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    /**
     * Tests the {@link ProductService#addProduct(Product)} method when a `null`
     * product is provided as input.
     */
    @Test
    @DisplayName("addProduct (null product) should return empty Optional")
    void addProduct_NullProduct_ReturnsEmptyOptional() {
        // Act
        Optional<Product> result = productService.addProduct(null);

        // Assert
        assertThat(result).isEmpty();

        // Verify no repository interactions
        verify(productRepository, never()).save(any(Product.class));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    /**
     * Tests the {@link ProductService#findAll()} method when products exist in the repository.
     * Verifies that all products are returned.
     */
    @Test
    @DisplayName("findAll should return all existing products")
    void findAll_ReturnsAllProducts() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // Act
        List<Product> products = productService.findAll();

        // Assert
        assertThat(products)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(product1, product2); // Check content, order doesn't matter
        verify(productRepository, times(1)).findAll();
    }

    /**
     * Tests the {@link ProductService#findAll()} method when no products exist.
     * Verifies that an empty list is returned.
     */
    @Test
    @DisplayName("findAll should return empty list when no products exist")
    void findAll_NoProducts_ReturnsEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> products = productService.findAll();

        // Assert
        assertThat(products)
                .isNotNull()
                .isEmpty();
        verify(productRepository, times(1)).findAll();
    }

    /**
     * Tests the {@link ProductService#findById(Long)} method when a product with the
     * given ID exists.
     */
    @Test
    @DisplayName("findById (existing ID) should return the product")
    void findById_ExistingId_ReturnsProduct() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product1));

        // Act
        Optional<Product> foundProduct = productService.findById(1L);

        // Assert
        assertThat(foundProduct)
                .isPresent()
                .hasValue(product1); // Direct value comparison for Optional
        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).getProductById(1L);
    }

    /**
     * Tests the {@link ProductService#findById(Long)} method when no product with the
     * given ID exists.
     */
    @Test
    @DisplayName("findById (non-existing ID) should return empty Optional")
    void findById_NonExistingId_ReturnsEmptyOptional() {
        // Arrange
        when(productRepository.existsById(99L)).thenReturn(false);

        // Act
        Optional<Product> foundProduct = productService.findById(99L);

        // Assert
        assertThat(foundProduct).isEmpty();
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).getProductById(anyLong()); // Should not call getProductById
    }

    /**
     * Tests the {@link ProductService#removeById(Long)} method to ensure it calls
     * the repository's remove method.
     */
    @Test
    @DisplayName("removeById should successfully remove the product")
    void removeById_RemovesProductSuccessfully() {
        // Arrange: Void method mock
        doNothing().when(productRepository).removeById(1L);

        // Act
        productService.removeById(1L);

        // Assert
        verify(productRepository, times(1)).removeById(1L);
    }

    /**
     * Tests the {@link ProductService#getAllProductsByName(String)} method when
     * products matching the name exist (case-insensitively).
     */
    @Test
    @DisplayName("getAllProductsByName (matching name) should return products")
    void getAllProductsByName_MatchingName_ReturnsProducts() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase("Laptop")).thenReturn(true);
        when(productRepository.getByNameIgnoreCase("Laptop")).thenReturn(Collections.singletonList(product1));

        // Act
        List<Product> products = productService.getAllProductsByName("Laptop");

        // Assert
        assertThat(products)
                .isNotNull()
                .hasSize(1)
                .containsExactly(product1);
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("Laptop");
        verify(productRepository, times(1)).getByNameIgnoreCase("Laptop");
    }

    /**
     * Tests the {@link ProductService#getAllProductsByName(String)} method when
     * no products match the given name.
     */
    @Test
    @DisplayName("getAllProductsByName (no matching name) should return empty list")
    void getAllProductsByName_NoMatchingName_ReturnsEmptyList() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase("NonExistent")).thenReturn(false);

        // Act
        List<Product> products = productService.getAllProductsByName("NonExistent");

        // Assert
        assertThat(products)
                .isNotNull()
                .isEmpty();
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("NonExistent");
        verify(productRepository, never()).getByNameIgnoreCase(anyString()); // Should not call getByNameIgnoreCase
    }

    /**
     * Tests the {@link ProductService#getAllProductsByName(String)} method when a
     * `null` name is provided.
     */
    @Test
    @DisplayName("getAllProductsByName (null name) should return empty list")
    void getAllProductsByName_NullName_ReturnsEmptyList() {
        // Act
        List<Product> products = productService.getAllProductsByName(null);

        // Assert
        assertThat(products)
                .isNotNull()
                .isEmpty();
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
        verify(productRepository, never()).getByNameIgnoreCase(anyString());
    }

    /**
     * Tests the {@link ProductService#getAllProductsByName(String)} method when an
     * empty string name is provided.
     */
    @Test
    @DisplayName("getAllProductsByName (empty name) should return empty list")
    void getAllProductsByName_EmptyName_ReturnsEmptyList() {
        // Act
        List<Product> products = productService.getAllProductsByName("");

        // Assert
        assertThat(products)
                .isNotNull()
                .isEmpty();
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
        verify(productRepository, never()).getByNameIgnoreCase(anyString());
    }

    /**
     * Tests the {@link ProductService#isExistsByName(String)} method when a product
     * with the given name exists.
     */
    @Test
    @DisplayName("isExistsByName (existing name) should return true")
    void isExistsByName_ExistingName_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase("Laptop")).thenReturn(true);

        // Act
        boolean exists = productService.isExistsByName("Laptop");

        // Assert
        assertThat(exists).isTrue();
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("Laptop");
    }

    /**
     * Tests the {@link ProductService#isExistsByName(String)} method when no product
     * with the given name exists.
     */
    @Test
    @DisplayName("isExistsByName (non-existing name) should return false")
    void isExistsByName_NonExistingName_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase("NonExistent")).thenReturn(false);

        // Act
        boolean exists = productService.isExistsByName("NonExistent");

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("NonExistent");
    }

    /**
     * Tests the {@link ProductService#isExistsByName(String)} method when a `null`
     * name is provided.
     */
    @Test
    @DisplayName("isExistsByName (null name) should return false")
    void isExistsByName_NullName_ReturnsFalse() {
        // Act
        boolean exists = productService.isExistsByName(null);

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
    }

    /**
     * Tests the {@link ProductService#isExistsByName(String)} method when an
     * empty string name is provided.
     */
    @Test
    @DisplayName("isExistsByName (empty name) should return false")
    void isExistsByName_EmptyName_ReturnsFalse() {
        // Act
        boolean exists = productService.isExistsByName("");

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
    }

    /**
     * Tests the {@link ProductService#getAllProductsByCategory(String)} method when
     * products matching the category exist (case-insensitively).
     */
    @Test
    @DisplayName("getAllProductsByCategory (matching category) should return products")
    void getAllProductsByCategory_MatchingCategory_ReturnsProducts() {
        // Arrange
        when(productRepository.findByCategoryIgnoreCase("Electronics")).thenReturn(Arrays.asList(product1, product2));

        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory("Electronics");

        // Assert
        assertThat(products)
                .isPresent()
                .hasValueSatisfying(list -> {
                    assertThat(list).isNotNull();
                    assertThat(list).hasSize(2);
                    assertThat(list).containsExactlyInAnyOrder(product1, product2);
                });
        verify(productRepository, times(1)).findByCategoryIgnoreCase("Electronics");
    }

    /**
     * Tests the {@link ProductService#getAllProductsByCategory(String)} method when
     * no products match the given category.
     */
    @Test
    @DisplayName("getAllProductsByCategory (no matching category) should return empty Optional with empty list")
    void getAllProductsByCategory_NoMatchingCategory_ReturnsEmptyOptionalWithEmptyList() {
        // Arrange
        when(productRepository.findByCategoryIgnoreCase("NonExistent")).thenReturn(Collections.emptyList());

        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory("NonExistent");

        // Assert
        assertThat(products)
                .isPresent() // The Optional itself is present
                .hasValueSatisfying(List::isEmpty); // But the list inside is empty
        verify(productRepository, times(1)).findByCategoryIgnoreCase("NonExistent");
    }

    /**
     * Tests the {@link ProductService#getAllProductsByCategory(String)} method when a
     * `null` category is provided.
     */
    @Test
    @DisplayName("getAllProductsByCategory (null category) should return empty Optional")
    void getAllProductsByCategory_NullCategory_ReturnsEmptyOptional() {
        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory(null);

        // Assert
        assertThat(products).isEmpty();
        verify(productRepository, never()).findByCategoryIgnoreCase(anyString());
    }

    /**
     * Tests the {@link ProductService#getAllProductsByCategory(String)} method when an
     * empty string category is provided.
     */
    @Test
    @DisplayName("getAllProductsByCategory (empty category) should return empty Optional")
    void getAllProductsByCategory_EmptyCategory_ReturnsEmptyOptional() {
        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory("");

        // Assert
        assertThat(products).isEmpty();
        verify(productRepository, never()).findByCategoryIgnoreCase(anyString());
    }

    /**
     * Tests the {@link ProductService#getAllCategories()} method to retrieve all
     * unique categories from the repository.
     */
    @Test
    @DisplayName("getAllCategories should return all unique categories")
    void getAllCategories_ReturnsAllUniqueCategories() {
        // Arrange
        List<String> expectedCategories = Arrays.asList("Electronics", "Books", "Clothing");
        when(productRepository.getEveryCategory()).thenReturn(expectedCategories);

        // Act
        List<String> categories = productService.getAllCategories();

        // Assert
        assertThat(categories)
                .isNotNull()
                .hasSize(3)
                .containsExactlyInAnyOrder("Electronics", "Books", "Clothing");
        verify(productRepository, times(1)).getEveryCategory();
    }

    /**
     * Tests the {@link ProductService#getAllCategories()} method when no categories
     * exist in the repository.
     */
    @Test
    @DisplayName("getAllCategories should return empty list when no categories exist")
    void getAllCategories_NoCategories_ReturnsEmptyList() {
        // Arrange
        when(productRepository.getEveryCategory()).thenReturn(Collections.emptyList());

        // Act
        List<String> categories = productService.getAllCategories();

        // Assert
        assertThat(categories)
                .isNotNull()
                .isEmpty();
        verify(productRepository, times(1)).getEveryCategory();
    }

    /**
     * Tests the {@link ProductService#addAll(List)} method with a valid list of products.
     * Verifies that `saveAll` is called and the saved products are returned.
     */
    @Test
    @DisplayName("addAll (valid products) should return saved products")
    void addAll_ValidProducts_ReturnsSavedProducts() {
        // Arrange
        List<Product> productsToAdd = Arrays.asList(product1, product2);
        when(productRepository.saveAll(productsToAdd)).thenReturn(productsToAdd);

        // Act
        Iterable<Product> savedProducts = productService.addAll(productsToAdd);

        // Assert
        assertThat(savedProducts)
                .isNotNull()
                .containsExactlyInAnyOrder(product1, product2);
        verify(productRepository, times(1)).saveAll(productsToAdd);
    }

    /**
     * Tests the {@link ProductService#addAll(List)} method when a `null` list is provided.
     * Verifies that an `IllegalArgumentException` is thrown.
     */
    @Test
    @DisplayName("addAll (null list) should throw IllegalArgumentException")
    void addAll_NullList_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> productService.addAll(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product list cannot be null or empty");

        verify(productRepository, never()).saveAll(any());
    }

    /**
     * Tests the {@link ProductService#addAll(List)} method when an empty list is provided.
     * Verifies that an `IllegalArgumentException` is thrown.
     */
    @Test
    @DisplayName("addAll (empty list) should throw IllegalArgumentException")
    void addAll_EmptyList_ThrowsIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> productService.addAll(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product list cannot be null or empty");

        verify(productRepository, never()).saveAll(any());
    }

    /**
     * Tests the {@link ProductService#isExistsByID(long)} method when a product with
     * the given ID exists.
     */
    @Test
    @DisplayName("isExistsByID (existing ID) should return true")
    void isExistsByID_ExistingId_ReturnsTrue() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean exists = productService.isExistsByID(1L);

        // Assert
        assertThat(exists).isTrue();
        verify(productRepository, times(1)).existsById(1L);
    }

    /**
     * Tests the {@link ProductService#isExistsByID(long)} method when no product with
     * the given ID exists.
     */
    @Test
    @DisplayName("isExistsByID (non-existing ID) should return false")
    void isExistsByID_NonExistingId_ReturnsFalse() {
        // Arrange
        when(productRepository.existsById(99L)).thenReturn(false);

        // Act
        boolean exists = productService.isExistsByID(99L);

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, times(1)).existsById(99L);
    }

    /**
     * Tests {@link ProductService#generateDisplayOrderForEachProductImage(Product)}
     * when a single product image with no display order is present.
     * Verifies that its display order is set to 1 and the product is linked.
     */
    @Test
    @DisplayName("generateDisplayOrderForEachProductImage (single image, no order) should set order to 1")
    void generateDisplayOrderForEachProductImage_SingleImageNoDisplayOrder_SetsTo1() {
        // Arrange
        ProductImage image1 = new ProductImage();
        image1.setImageUrl("url1");

        product1.setProductImages(Collections.singletonList(image1));

        // Act
        productService.generateDisplayOrderForEachProductImage(product1);

        // Assert
        assertThat(image1.getDisplayOrder()).isEqualTo(1);
        assertThat(image1.getProduct()).isEqualTo(product1);
    }

    /**
     * Tests {@link ProductService#generateDisplayOrderForEachProductImage(Product)}
     * when multiple product images with no display orders are present.
     * Verifies that sequential display orders are assigned and products are linked.
     */
    @Test
    @DisplayName("generateDisplayOrderForEachProductImage (multiple images, no order) should set sequential order")
    void generateDisplayOrderForEachProductImage_MultipleImagesNoDisplayOrder_SetsSequential() {
        // Arrange
        ProductImage image1 = new ProductImage();
        image1.setImageUrl("url1");
        ProductImage image2 = new ProductImage();
        image2.setImageUrl("url2");
        ProductImage image3 = new ProductImage();
        image3.setImageUrl("url3");

        product1.setProductImages(Arrays.asList(image1, image2, image3));

        // Act
        productService.generateDisplayOrderForEachProductImage(product1);

        // Assert
        assertThat(image1.getDisplayOrder()).isEqualTo(1);
        assertThat(image2.getDisplayOrder()).isEqualTo(2);
        assertThat(image3.getDisplayOrder()).isEqualTo(3);
        assertThat(image1.getProduct()).isEqualTo(product1);
        assertThat(image2.getProduct()).isEqualTo(product1);
        assertThat(image3.getProduct()).isEqualTo(product1);
    }

    /**
     * Tests {@link ProductService#generateDisplayOrderForEachProductImage(Product)}
     * when some images have existing display orders and others do not.
     * Verifies that existing orders are preserved and new orders are assigned sequentially
     * starting from 1 for images that don't have one.
     */
    @Test
    @DisplayName("generateDisplayOrderForEachProductImage (mixed orders) should preserve existing and set new sequentially")
    void generateDisplayOrderForEachProductImage_ImagesWithSomeDisplayOrder_KeepsExistingAndSetsNew() {
        // Arrange
        ProductImage image1 = new ProductImage();
        image1.setImageUrl("url1");
        image1.setDisplayOrder(10); // Existing display order

        ProductImage image2 = new ProductImage();
        image2.setImageUrl("url2"); // No display order

        ProductImage image3 = new ProductImage();
        image3.setImageUrl("url3");
        image3.setDisplayOrder(20); // Existing display order

        product1.setProductImages(Arrays.asList(image1, image2, image3));

        // Act
        productService.generateDisplayOrderForEachProductImage(product1);

        // Assert
        assertThat(image1.getDisplayOrder()).isEqualTo(10); // Should remain 10
        assertThat(image2.getDisplayOrder()).isEqualTo(1); // Should be set as the first "new" one
        assertThat(image3.getDisplayOrder()).isEqualTo(20); // Should remain 20
        assertThat(image1.getProduct()).isEqualTo(product1);
        assertThat(image2.getProduct()).isEqualTo(product1);
        assertThat(image3.getProduct()).isEqualTo(product1);
    }

    /**
     * Tests {@link ProductService#generateDisplayOrderForEachProductImage(Product)}
     * when the product has an empty list of images.
     * Verifies that no changes occur.
     */
    @Test
    @DisplayName("generateDisplayOrderForEachProductImage (empty images list) should do nothing")
    void generateDisplayOrderForEachProductImage_NoImages_DoesNothing() {
        // Arrange
        product1.setProductImages(Collections.emptyList());

        // Act
        productService.generateDisplayOrderForEachProductImage(product1);

        // Assert
        assertThat(product1.getProductImages()).isEmpty(); // No change
    }

    /**
     * Tests {@link ProductService#generateDisplayOrderForEachProductImage(Product)}
     * when the product has a null list of images.
     * Verifies that no changes occur.
     */
    @Test
    @DisplayName("generateDisplayOrderForEachProductImage (null images list) should do nothing")
    void generateDisplayOrderForEachProductImage_NullImagesList_DoesNothing() {
        // Arrange
        product1.setProductImages(null);

        // Act
        productService.generateDisplayOrderForEachProductImage(product1);

        // Assert
        assertThat(product1.getProductImages()).isNull(); // No change
    }
}