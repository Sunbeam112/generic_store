package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.model.ProductImage;
import ua.sunbeam.genericstore.service.CsvFileUtil;
import ua.sunbeam.genericstore.service.ProductService;
import ua.sunbeam.genericstore.service.csv.ProductCSVReader;
import ua.sunbeam.genericstore.service.csv.ProductCSVWriter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    private final ProductService productService;
    private final ProductCSVReader productCSVReader;
    private final ProductCSVWriter productCSVWriter;

    public ProductController(ProductService productService, ProductCSVReader productCSVReader, ProductCSVWriter productCSVWriter) {
        this.productService = productService;
        this.productCSVReader = productCSVReader;
        this.productCSVWriter = productCSVWriter;
    }


    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/category={input}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String input) {
        Optional<List<Product>> products;
        products = productService.getAllProductsByCategory(input);
        return products.map(productList -> new ResponseEntity<>(productList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/name={input}")
    public ResponseEntity<List<Product>> findAllProductsByName(@PathVariable String input) {
        List<Product> products;
        products = productService.getAllProductsByName(input);
        if (products.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/id={id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));


    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") Long id) {
        try {
            productService.removeById(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().build();

    }


    @PostMapping(value = "/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity addProduct(@RequestBody Product product) {
        if (product == null || product.getName() == null || product.getPrice() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (product.getName().isEmpty() || product.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Price must be greater than 0 and name cannot be empty");
        }
        try {
            if (product.getProductImages() != null || !product.getProductImages().isEmpty()) {
                if (product.getProductImages().size() == 1) {
                    ProductImage productImage = product.getProductImages().get(0);
                    if (productImage.getDisplayOrder() == null) productImage.setDisplayOrder(1);
                    productImage.setProduct(product);
                    product.setProductImages(List.of(productImage));
                } else {
                    int i = 0;
                    for (ProductImage productImage : product.getProductImages()) {
                        if (productImage.getDisplayOrder() == null) {
                            i++;
                            productImage.setDisplayOrder(i);
                        }
                        productImage.setProduct(product);

                    }
                }
            }
        } catch (NullPointerException e) {

        }

        Optional<Product> savedProduct;
        try {
            savedProduct = productService.addProduct(product);
            return savedProduct.map(value -> {
                URI location = URI.create(String.format("/product/id=%d", value.getId()));
                return ResponseEntity.created(location).body(value);
            }).orElseGet(() -> ResponseEntity.internalServerError().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Product with name " + product.getName() + " already exists");
        }

    }


    @GetMapping("/category/all")
    public ResponseEntity<List<String>> getAllProductsByCategory() {
        List<String> categories;
        categories = productService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/importFromCSV")
    public ResponseEntity<List<Product>> parseFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            var products = productCSVReader.importProductsFromCSV(file);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/importFromCSVAndSave")
    public ResponseEntity<List<Product>> parseProductsFromCSVAndSave(@RequestParam("file") MultipartFile file) {
        try {
            var products = productCSVReader.importProductsFromCSV(file);
            List<Product> savedProducts = (List<Product>) productService.addAll(products);
            return new ResponseEntity<>(savedProducts, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportProductsToCsv(
            @RequestParam(name = "directory", required = false) String directoryPath) {
        Path filePath = null;
        try {
            List<Product> products = productService.getAllProducts();
            String fileName = CsvFileUtil.generateCsvFileName();
            filePath = CsvFileUtil.determineAndCreateSaveDirectory(directoryPath, fileName);

            productCSVWriter.writeToCSV(filePath.toString(), products);

            byte[] csvBytes = CsvFileUtil.readAllBytes(filePath);

            return CsvFileUtil.buildCsvResponse(csvBytes, fileName);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error during product CSV export: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("An unexpected error occurred: " + e.getMessage()).getBytes());
        } finally {
            if (filePath != null) {
                try {
                    CsvFileUtil.deleteFile(filePath);
                } catch (IOException e) {
                    System.err.println("Failed to delete temporary CSV file: " + filePath + " - " + e.getMessage());
                }
            }
        }
    }
}
