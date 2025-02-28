package ua.sunbeam.genericstore.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    public void ProductService_CreateProduct_ReturnsProductRepository() {
        Product initialProduct = new Product();
        initialProduct.setName("Test");
        initialProduct.setPrice(200.00);
        initialProduct.setDescription("Test");
        initialProduct.setCategory("Test");
        initialProduct.setUrlPhoto("https://www.google.com");
        initialProduct.setSubcategory("Test");
        initialProduct.setShortDescription("Test");

        when(productRepository.save(Mockito.any(Product.class)))
                .thenReturn(initialProduct);

        Boolean productIsAdded = productService.addProduct(initialProduct);
        Assertions.assertThat(productIsAdded).isTrue();

    }

    @Test
    public void ProductService_FindAllProducts_ReturnsAllProducts() {
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

        given(productRepository.findAll())
                .willReturn(List.of(firstProduct, secondProduct));

        var products = productService.findAll();
        Assertions.assertThat(products).isNotNull();
        Assertions.assertThat(products.size()).isEqualTo(2);
    }

    @Test
    public void ProductService_FindProductById_ReturnsProduct() {
        Product firstProduct = new Product();
        firstProduct.setId(1L);
        firstProduct.setName("Test");
        firstProduct.setPrice(200.00);
        firstProduct.setDescription("Test");
        firstProduct.setCategory("Test");
        firstProduct.setUrlPhoto("https://www.google.com");
        firstProduct.setSubcategory("Test");
        firstProduct.setShortDescription("Test");

        when(productRepository.existsById(1L))
                .thenReturn(true);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(firstProduct));

    }


}
