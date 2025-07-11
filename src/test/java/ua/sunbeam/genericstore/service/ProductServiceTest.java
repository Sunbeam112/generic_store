package ua.sunbeam.genericstore.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Initialize common product objects for reuse in tests
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

    @Test
    void addProduct_NewProduct_ReturnsAddedProduct() {
        // Arrange
        // Simulate that a product with this ID does not exist
        when(productRepository.existsById(anyLong())).thenReturn(false);
        // Simulate successful save operation, returning the saved product
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        // Act
        Optional<Product> result = productService.addProduct(product1);

        // Assert
        assertThat(result).isPresent(); // Verify that an Optional with a value is returned
        assertThat(result.get()).isEqualTo(product1); // Verify that the correct product is returned
        verify(productRepository, times(1)).existsById(anyLong()); // Ensure existsById was called once
        verify(productRepository, times(1)).save(product1); // Ensure save was called once with the correct product
    }

    @Test
    void addProduct_ProductWithExistingId_ReturnsEmptyOptional() {
        // Arrange
        // Simulate that a product with this ID already exists
        when(productRepository.existsById(product1.getId())).thenReturn(true);

        // Act
        Optional<Product> result = productService.addProduct(product1);

        // Assert
        assertThat(result).isEmpty(); // Verify that an empty Optional is returned
        verify(productRepository, times(1)).existsById(product1.getId()); // Ensure existsById was called once
        verify(productRepository, never()).save(any(Product.class)); // Ensure save was never called
    }

    @Test
    void findAll_ReturnsAllProducts() {
        // Arrange
        // Simulate the repository returning a list of products
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // Act
        List<Product> products = productService.findAll();

        // Assert
        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(2);
        assertThat(products).containsExactlyInAnyOrder(product1, product2); // Check content and order (if relevant, otherwise just content)
        verify(productRepository, times(1)).findAll(); // Ensure findAll was called once
    }

    @Test
    void findAll_NoProducts_ReturnsEmptyList() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Product> products = productService.findAll();

        // Assert
        assertThat(products).isNotNull();
        assertThat(products).isEmpty();
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findById_ExistingId_ReturnsProduct() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true); // Simulate product exists
        when(productRepository.getProductById(1L)).thenReturn(Optional.of(product1)); // Simulate finding the product

        // Act
        Optional<Product> foundProduct = productService.findById(1L);

        // Assert
        assertThat(foundProduct).isPresent(); // Verify that the Optional contains a value
        assertThat(foundProduct.get()).isEqualTo(product1); // Verify the correct product is returned
        verify(productRepository, times(1)).existsById(1L); // Ensure existsById was called
        verify(productRepository, times(1)).getProductById(1L); // Ensure getProductById was called
    }

    @Test
    void findById_NonExistingId_ReturnsEmptyOptional() {
        // Arrange
        when(productRepository.existsById(99L)).thenReturn(false); // Simulate product does not exist

        // Act
        Optional<Product> foundProduct = productService.findById(99L);

        // Assert
        assertThat(foundProduct).isEmpty(); // Verify that the Optional is empty
        verify(productRepository, times(1)).existsById(99L); // Ensure existsById was called
        verify(productRepository, never()).getProductById(anyLong()); // Ensure getProductById was NOT called
    }

    @Test
    void removeById_RemovesProductSuccessfully() {
        // Arrange
        // We don't need to mock return value for void methods, just ensure the call happens
        doNothing().when(productRepository).removeById(1L);

        // Act
        productService.removeById(1L);

        // Assert
        verify(productRepository, times(1)).removeById(1L); // Verify removeById was called once
    }

    @Test
    void getAllProductsByName_MatchingName_ReturnsProducts() {
        // Arrange
        // Mock with anyString() because the service converts to lowercase internally
        when(productRepository.existsByNameContainsIgnoreCase(anyString())).thenReturn(true);
        when(productRepository.getByNameIgnoreCase(anyString())).thenReturn(Collections.singletonList(product1));

        // Act
        List<Product> products = productService.getAllProductsByName("Laptop"); // Pass the original casing

        // Assert
        assertThat(products).isNotNull();
        assertThat(products.size()).isEqualTo(1);
        assertThat(products.get(0)).isEqualTo(product1);
        // Verify with anyString() or specifically with the expected lowercase if you want to be precise about the argument passed after conversion
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("Laptop");
        verify(productRepository, times(1)).getByNameIgnoreCase("Laptop");
    }

    @Test
    void getAllProductsByName_NoMatchingName_ReturnsEmptyList() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase(anyString())).thenReturn(false); // Mock for any string, as service converts

        // Act
        List<Product> products = productService.getAllProductsByName("NonExistent"); // Pass the original casing

        // Assert
        assertThat(products).isNotNull();
        assertThat(products).isEmpty();
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("NonExistent");
        verify(productRepository, never()).getByNameIgnoreCase(anyString());
    }

    @Test
    void getAllProductsByName_NullName_ReturnsEmptyList() {
        // Act
        List<Product> products = productService.getAllProductsByName(null);

        // Assert
        assertThat(products).isNotNull();
        assertThat(products).isEmpty();
        // Verify no repository methods were called for null input
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
        verify(productRepository, never()).getByNameIgnoreCase(anyString());
    }

    @Test
    void getAllProductsByName_EmptyName_ReturnsEmptyList() {
        // Act
        List<Product> products = productService.getAllProductsByName("");

        // Assert
        assertThat(products).isNotNull();
        assertThat(products).isEmpty();
        // Verify no repository methods were called for empty input
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
        verify(productRepository, never()).getByNameIgnoreCase(anyString());
    }

    @Test
    void isExistsByName_ExistingName_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase(anyString())).thenReturn(true); // Mock for any string

        // Act
        boolean exists = productService.isExistsByName("Laptop"); // Pass the original casing

        // Assert
        assertThat(exists).isTrue();
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("Laptop");
    }

    @Test
    void isExistsByName_NonExistingName_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByNameContainsIgnoreCase(anyString())).thenReturn(false); // Mock for any string

        // Act
        boolean exists = productService.isExistsByName("NonExistent"); // Pass the original casing

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, times(1)).existsByNameContainsIgnoreCase("NonExistent");
    }

    @Test
    void isExistsByName_NullName_ReturnsFalse() {
        // Act
        boolean exists = productService.isExistsByName(null);

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
    }

    @Test
    void isExistsByName_EmptyName_ReturnsFalse() {
        // Act
        boolean exists = productService.isExistsByName("");

        // Assert
        assertThat(exists).isFalse();
        verify(productRepository, never()).existsByNameContainsIgnoreCase(anyString());
    }

    @Test
    void getAllProductsByCategory_MatchingCategory_ReturnsProducts() {
        // Arrange
        when(productRepository.findByCategoryIgnoreCase(anyString())).thenReturn(Arrays.asList(product1, product2)); // Mock for any string

        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory("Electronics"); // Pass original casing

        // Assert
        assertThat(products).isPresent();
        assertThat(products.get()).isNotNull();
        assertThat(products.get().size()).isEqualTo(2);
        assertThat(products.get()).containsExactlyInAnyOrder(product1, product2);
        verify(productRepository, times(1)).findByCategoryIgnoreCase("Electronics");
    }

    @Test
    void getAllProductsByCategory_NoMatchingCategory_ReturnsEmptyOptional() {
        // Arrange
        when(productRepository.findByCategoryIgnoreCase(anyString())).thenReturn(Collections.emptyList()); // Mock for any string

        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory("NonExistent"); // Pass original casing

        // Assert
        assertThat(products).isPresent(); // The Optional itself is present, but the list inside is empty
        assertThat(products.get()).isEmpty();
        verify(productRepository, times(1)).findByCategoryIgnoreCase("NonExistent"); // Verify with expected lowercase argument
    }

    @Test
    void getAllProductsByCategory_NullCategory_ReturnsEmptyOptional() {
        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory(null);

        // Assert
        assertThat(products).isEmpty();
        verify(productRepository, never()).findByCategoryIgnoreCase(anyString());
    }

    @Test
    void getAllProductsByCategory_EmptyCategory_ReturnsEmptyOptional() {
        // Act
        Optional<List<Product>> products = productService.getAllProductsByCategory("");

        // Assert
        assertThat(products).isEmpty();
        verify(productRepository, never()).findByCategoryIgnoreCase(anyString());
    }

    @Test
    void getAllCategories_ReturnsAllUniqueCategories() {
        // Arrange
        List<String> expectedCategories = Arrays.asList("Electronics", "Books", "Clothing");
        when(productRepository.getEveryCategory()).thenReturn(expectedCategories);

        // Act
        List<String> categories = productService.getAllCategories();

        // Assert
        assertThat(categories).isNotNull();
        assertThat(categories.size()).isEqualTo(3);
        assertThat(categories).containsExactlyInAnyOrder("Electronics", "Books", "Clothing");
        verify(productRepository, times(1)).getEveryCategory();
    }

    @Test
    void getAllCategories_NoCategories_ReturnsEmptyList() {
        // Arrange
        when(productRepository.getEveryCategory()).thenReturn(Collections.emptyList());

        // Act
        List<String> categories = productService.getAllCategories();

        // Assert
        assertThat(categories).isNotNull();
        assertThat(categories).isEmpty();
        verify(productRepository, times(1)).getEveryCategory();
    }
}