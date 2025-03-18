package ua.sunbeam.genericstore.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.service.ProductService;

@RestController
@RequestMapping("/admin/inventory")
public class InventoryController {

    private final ProductService productService;

    public InventoryController(ProductService productService) {
        this.productService = productService;
    }

    public int getProductQuantity(Long productID) {
        if (productID == null || productID <= 0) return -1;
        Product product = productService.findById(productID);
        if (product == null) return -1;
        if (product.getInventory() == null) return 0;
        return product.getInventory().getQuantity();
    }
}
