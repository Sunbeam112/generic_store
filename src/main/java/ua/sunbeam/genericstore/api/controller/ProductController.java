package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.service.ProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {


    ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Product> findProductById(@PathVariable long id) {
        Optional<Product> product = Optional.ofNullable(productService.findById(id));
        return product.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        if (productService.addProduct(product)) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }


}
