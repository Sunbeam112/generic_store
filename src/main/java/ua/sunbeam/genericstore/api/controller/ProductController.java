package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.service.CsvFileUtil;
import ua.sunbeam.genericstore.service.InventoryService;
import ua.sunbeam.genericstore.service.ProductService;
import ua.sunbeam.genericstore.service.csv.ProductCSVReader;
import ua.sunbeam.genericstore.service.csv.ProductCSVWriter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {
    private final ProductService productService;
    private final ProductCSVReader productCSVReader;
    private final ProductCSVWriter productCSVWriter;
    private final InventoryService inventoryService;

    public ProductController(ProductService productService, ProductCSVReader productCSVReader, ProductCSVWriter productCSVWriter, InventoryService inventoryService) {
        this.productService = productService;
        this.productCSVReader = productCSVReader;
        this.productCSVWriter = productCSVWriter;
        this.inventoryService = inventoryService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/category={input}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String input) {
        List<Product> products = productService.getAllProductsByCategory(input)
                .orElse(Collections.emptyList());
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/name={input}")
    public ResponseEntity<List<Product>> findAllProductsByName(@PathVariable String input) {
        List<Product> products = productService.getAllProductsByName(input);
        // It's already returning an empty list if no products, so just return OK
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/id={id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));


    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.removeById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // 500 Internal Server Error
        }
    }


    @PostMapping(value = "/add")
    public ResponseEntity addProduct(@RequestBody Product product) {
        if (product == null || product.getName() == null || product.getPrice() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (product.getName().isEmpty() || product.getPrice() <= 0) {
            return ResponseEntity.badRequest().body("Price must be greater than 0 and name cannot be empty");
        }
        try {
            productService.generateDisplayOrderForEachProductImage(product);
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().build();
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
