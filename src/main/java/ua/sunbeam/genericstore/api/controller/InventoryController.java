package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.service.InventoryService;
import ua.sunbeam.genericstore.service.ProductService;

@RestController
@RequestMapping("/admin/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductService productService;

    public InventoryController(InventoryService inventoryService, ProductService productService) {
        this.inventoryService = inventoryService;
        this.productService = productService;
    }

    @GetMapping("/get_quantity")
    public ResponseEntity<Object> getProductQuantity(@RequestParam Long productId) {
        if (productService.isExists(productId)) {
            int quantity = inventoryService.getProductQuantity(productId);
            return ResponseEntity.ok(quantity);
        } else {
            return ResponseEntity.badRequest().body("Product not found");
        }
    }
}

