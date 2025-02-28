package ua.sunbeam.genericstore.api.repo;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void ProductRepository_SaveAll_ReturnsSavedProduct() {
        //Arrange
        Product product = new Product();
        product.setName("Test");
        product.setPrice(200.00);
        product.setDescription("Test");
        product.setCategory("Test");
        product.setUrlPhoto("https://www.google.com");
        product.setSubcategory("Test");
        product.setShortDescription("Test");

        //Act
        Product savedProduct = productRepository.save(product);

        //Assert
        Assertions.assertThat(savedProduct).isNotNull();
        Assertions.assertThat(savedProduct.getId()).isGreaterThan(0);
    }


    @Test
    public void ProductRepository_GetAll_ReturnsAllProducts() {
        Product firstProduct = new Product();
        firstProduct.setName("Test");
        firstProduct.setPrice(200.00);
        firstProduct.setDescription("Test");
        firstProduct.setCategory("Test");
        firstProduct.setUrlPhoto("https://www.google.com");
        firstProduct.setSubcategory("Test");
        firstProduct.setShortDescription("Test");

        Product secondProduct = new Product();
        secondProduct.setName("Test");
        secondProduct.setPrice(200.00);
        secondProduct.setDescription("Test");
        secondProduct.setCategory("Test");
        secondProduct.setUrlPhoto("https://www.google.com");
        secondProduct.setSubcategory("Test");
        secondProduct.setShortDescription("Test");

        productRepository.save(firstProduct);
        productRepository.save(secondProduct);
        List products = (List) productRepository.findAll();

        Assertions.assertThat(products.size()).isEqualTo(2);
    }


    @Test
    public void ProductRepository_DeleteByID_ReturnsNothing() {


        Product product = new Product();
        product.setName("Test");
        product.setPrice(200.00);
        product.setDescription("Test");
        product.setCategory("Test");
        product.setUrlPhoto("https://www.google.com");
        product.setSubcategory("Test");
        product.setShortDescription("Test");
        Product savedProduct = productRepository.save(product);


        productRepository.deleteById(savedProduct.getId());

        Optional<Product> deletedProduct = productRepository.getProductById(savedProduct.getId());

        Assertions.assertThat(deletedProduct).isEqualTo(Optional.empty());

    }

    @Test
    public void ProductRepository_ExistsByName_ReturnsTrue() {
        Product product = new Product();
        product.setName("Test");
        product.setPrice(200.00);
        product.setDescription("Test");
        product.setCategory("Test");
        product.setUrlPhoto("https://www.google.com");
        product.setSubcategory("Test");
        product.setShortDescription("Test");

        Product savedProduct = productRepository.save(product);

        boolean productExists = productRepository.existsByNameContainsIgnoreCase(product.getName());
        Assertions.assertThat(productExists).isTrue();
    }

    @Test
    public void ProductRepository_FindByID_ReturnsSearchedProduct() {
        Product firstProduct = new Product();
        firstProduct.setName("Test");
        firstProduct.setPrice(200.00);
        firstProduct.setDescription("Test");
        firstProduct.setCategory("Test");
        firstProduct.setUrlPhoto("https://www.google.com");
        firstProduct.setSubcategory("Test");
        firstProduct.setShortDescription("Test");

        Product secondProduct = new Product();
        secondProduct.setName("Test");
        secondProduct.setPrice(200.00);
        secondProduct.setDescription("Test");
        secondProduct.setCategory("Test");
        secondProduct.setUrlPhoto("https://www.google.com");
        secondProduct.setSubcategory("Test");
        secondProduct.setShortDescription("Test");

        productRepository.save(firstProduct);
        productRepository.save(secondProduct);

        Optional<Product> productThatExists = productRepository.findById(firstProduct.getId());
        Optional<Product> productThatNotExists = productRepository.findById(100L);

        Assertions.assertThat(productThatExists.isPresent()).isTrue();
        Assertions.assertThat(productThatExists.get().getName()).isEqualTo(firstProduct.getName());
        Assertions.assertThat(productThatNotExists.isPresent()).isFalse();

    }
}
