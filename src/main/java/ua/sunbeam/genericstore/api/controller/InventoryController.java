package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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


    /**
     * Retrieves the quantity of a product from the inventory based on its ID.
     * This endpoint handles HTTP GET requests to "/getQuantity".
     *
     * @param id The ID of the product as a String. This parameter is required and must be a valid Long number.
     * @return A {@link ResponseEntity} containing:
     * - The product quantity ({@code Integer}) with {@code HttpStatus.FOUND} if the product is found and quantity is available.
     * - {@code HttpStatus.BAD_REQUEST} if the 'id' parameter is null, empty, not a valid number,
     * or if the product with the given ID is not found in the inventory.
     */
    @GetMapping("/getQuantity")
    public ResponseEntity<Integer> getQuantity(@RequestParam String id) {
        if (id == null || id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            long parsed_id = Long.parseLong(id);
            if(productService.isExistsByID(parsed_id))
            {
                int quantity = inventoryService.getProductQuantity(parsed_id);
                if (quantity == -1) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                return new ResponseEntity<>(quantity, HttpStatus.FOUND);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }
}

