package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.model.ProductImage;
import ua.sunbeam.genericstore.service.ProductService;
import ua.sunbeam.genericstore.service.csv.ProductCSVReader;
import ua.sunbeam.genericstore.service.csv.ProductCSVWriter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        if (product.getProductImages() != null) {
            for (ProductImage productImage : product.getProductImages()) {
                productImage.setProduct(product);
            }
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
            @RequestParam(name = "directory", required = false) String directoryPath) { // Added parameter

        List<Product> products = (List<Product>) productService.getAllProducts();

        String fileName = "products_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        Path saveDirectory;
        if (directoryPath != null && !directoryPath.trim().isEmpty()) {
            saveDirectory = Paths.get(directoryPath);
            // Ensure the directory exists, create if not
            try {
                Files.createDirectories(saveDirectory);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(("Error creating directory: " + e.getMessage()).getBytes());
            }
        } else {

            saveDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
        }

        Path filePathToSave = saveDirectory.resolve(fileName); // Resolve the full path

        try {

            productCSVWriter.writeToCSV(filePathToSave.toString(), products);


            byte[] csvBytes = Files.readAllBytes(filePathToSave);

            Files.delete(filePathToSave);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(csvBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(("Error exporting products: " + e.getMessage()).getBytes());
        }
    }
}
