package ua.sunbeam.genericstore.api.csv; // Corrected package declaration

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ua.sunbeam.genericstore.error.CsvProcessingException;
import ua.sunbeam.genericstore.model.DAO.ProductRepository;
import ua.sunbeam.genericstore.model.Product;

import ua.sunbeam.genericstore.service.csv.ProductCSVReader; // Correct import for the class under test

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductCSVReaderTest {

    @Mock
    private ProductRepository productRepository;

    private ProductCSVReader productCSVReader;

    @BeforeEach
    void setUp() {
        productCSVReader = new ProductCSVReader();
    }

    private MockMultipartFile createMockCsvFile(String content, String filename, String contentType) {
        return new MockMultipartFile(
                "file",
                filename,
                contentType,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Should successfully import products from a valid CSV file")
    void importProductsFromCSV_Success() {
        String csvContent = "name,price,description,category,url_photo,subcategory,short_description\n" +
                "ProductA,10.00,DescA,CatA,urlA,SubA,ShortA\n" +
                "ProductB,20.50,DescB,CatB,urlB,SubB,ShortB";

        MultipartFile file = createMockCsvFile(csvContent, "products.csv", "text/csv");

        List<Product> products = productCSVReader.importProductsFromCSV(file);

        assertNotNull(products);
        assertEquals(2, products.size());

        Product product1 = products.getFirst();
        assertEquals("ProductA", product1.getName());
        assertEquals(10.00, product1.getPrice());
        assertEquals("DescA", product1.getDescription());
        assertEquals("CatA", product1.getCategory());
        assertEquals("urlA", product1.getUrlPhoto());
        assertEquals("SubA", product1.getSubcategory());
        assertEquals("ShortA", product1.getShortDescription());

        Product product2 = products.get(1);
        assertEquals("ProductB", product2.getName());
        assertEquals(20.50, product2.getPrice());
        assertEquals("DescB", product2.getDescription());
        assertEquals("CatB", product2.getCategory());
        assertEquals("urlB", product2.getUrlPhoto());
        assertEquals("SubB", product2.getSubcategory());
        assertEquals("ShortB", product2.getShortDescription());

        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("Should successfully import products with different header casing (due to ignoreHeaderCase=true)")
    void importProductsFromCSV_DifferentHeaderCasing() {
        String csvContent = "NAME,PRICE,DESCRIPTION,CATEGORY,URL_PHOTO,SUBCATEGORY,SHORT_DESCRIPTION\n" +
                "ProductC,30.00,DescC,CatC,urlC,SubC,ShortC";

        MultipartFile file = createMockCsvFile(csvContent, "products_caps.csv", "text/csv");

        List<Product> products = productCSVReader.importProductsFromCSV(file);

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("ProductC", products.get(0).getName());
        assertEquals(30.00, products.get(0).getPrice());
    }

    @Test
    @DisplayName("Should successfully import products with reordered headers")
    void importProductsFromCSV_ReorderedHeaders() {
        String csvContent = "price,name,short_description,url_photo,description,category,subcategory\n" +
                "40.00,ProductD,ShortD,urlD,DescD,CatD,SubD";

        MultipartFile file = createMockCsvFile(csvContent, "products_reordered.csv", "text/csv");

        List<Product> products = productCSVReader.importProductsFromCSV(file);

        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals("ProductD", products.get(0).getName());
        assertEquals(40.00, products.get(0).getPrice());
        assertEquals("ShortD", products.get(0).getShortDescription());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if file is empty")
    void importProductsFromCSV_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(emptyFile);
        });
        assertEquals("File is empty", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if file type is not CSV")
    void importProductsFromCSV_InvalidFileType() {
        MultipartFile file = createMockCsvFile("content", "doc.txt", "text/plain");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertEquals("Invalid file type. Please upload a CSV file.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if file extension is not .csv but content type is text/csv")
    void importProductsFromCSV_InvalidFileExtension() {
        MultipartFile file = createMockCsvFile("content", "data.xls", "text/csv");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertEquals("Invalid file type. Please upload a CSV file.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if file extension is .csv but content type is not text/csv")
    void importProductsFromCSV_InvalidContentType() {
        MultipartFile file = createMockCsvFile("content", "data.csv", "application/json");
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertEquals("Invalid file type. Please upload a CSV file.", thrown.getMessage());
    }


    @Test
    @DisplayName("Should throw CsvProcessingException if CSV file has no header row")
    void importProductsFromCSV_NoHeaderRow() {
        String csvContent = "ProductA,10.00,DescA,CatA,urlA,SubA,ShortA";

        MultipartFile file = createMockCsvFile(csvContent, "no_header.csv", "text/csv");

        CsvProcessingException thrown = assertThrows(CsvProcessingException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertTrue(thrown.getMessage().contains("CSV file must contain a header row"));
        assertTrue(thrown.getCause() instanceof IOException);
    }


    @Test
    @DisplayName("Should throw IllegalArgumentException if essential headers are missing")
    void importProductsFromCSV_MissingHeaders() {
        String csvContent = "name,description,category,url_photo,subcategory,short_description\n" +
                "ProductA,DescA,CatA,urlA,SubA,ShortA";

        MultipartFile file = createMockCsvFile(csvContent, "missing_header.csv", "text/csv");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertTrue(thrown.getMessage().contains("Missing expected headers: price"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if unexpected headers are present (strict check)")
    void importProductsFromCSV_UnexpectedHeaders() {
        String csvContent = "name,price,description,category,url_photo,subcategory,short_description,extra_column\n" +
                "ProductA,10.00,DescA,CatA,urlA,SubA,ShortA,Extra";

        MultipartFile file = createMockCsvFile(csvContent, "unexpected_header.csv", "text/csv");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertTrue(thrown.getMessage().contains("Unexpected headers found: extra_column"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if price is not a valid number")
    void importProductsFromCSV_InvalidPriceFormat() {
        String csvContent = "name,price,description,category,url_photo,subcategory,short_description\n" +
                "ProductA,NOT_A_NUMBER,DescA,CatA,urlA,SubA,ShortA";

        MultipartFile file = createMockCsvFile(csvContent, "invalid_price.csv", "text/csv");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertTrue(thrown.getMessage().contains("Error in CSV record #1: For input string"));
        assertTrue(thrown.getCause() instanceof NumberFormatException);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if price is empty")
    void importProductsFromCSV_EmptyPrice() {
        String csvContent = "name,price,description,category,url_photo,subcategory,short_description\n" +
                "ProductA,,DescA,CatA,urlA,SubA,ShortA";

        MultipartFile file = createMockCsvFile(csvContent, "empty_price.csv", "text/csv");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertTrue(thrown.getMessage().contains("Error in CSV record #1: Price cannot be empty"));
    }

    @Test
    @DisplayName("Should handle multiple rows with mixed valid/invalid data (first invalid)")
    void importProductsFromCSV_MultipleRows_FirstInvalid() {
        String csvContent = "name,price,description,category,url_photo,subcategory,short_description\n" +
                "ProductA,INVALID,DescA,CatA,urlA,SubA,ShortA\n" +
                "ProductB,20.00,DescB,CatB,urlB,SubB,ShortB";

        MultipartFile file = createMockCsvFile(csvContent, "multi_row_invalid.csv", "text/csv");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productCSVReader.importProductsFromCSV(file);
        });
        assertTrue(thrown.getMessage().contains("Error in CSV record #1"));
        assertTrue(thrown.getCause() instanceof NumberFormatException);
    }
}