package ua.sunbeam.genericstore.api.controller;

import jakarta.transaction.Transactional;
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
    @CrossOrigin
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping(value= "/category={input}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String input) {
        Optional<List<Product>> products;
        products = productService.getAllProductsByCategory(input);
        return products.map(productList -> new ResponseEntity<>(productList, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/name={name}")
    public ResponseEntity<List<Product>> findProductByNameAll(@PathVariable String name) {
        List<Product> products;
        products = productService.getAllProductsByName(name);
        if (products.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(products, HttpStatus.OK);
    }


    @GetMapping("/id={id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Optional<Product> product;
        try{
        Long parsedID = Long.parseLong(id,10);
        product = productService.findById(parsedID);
        }
        catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }


    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable(value = "id") Long id) {
        try {
            productService.removeById(id);
        } catch (Exception e) {
            return new ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok().build();

    }




    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        if (productService.addProduct(product)) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }


}
