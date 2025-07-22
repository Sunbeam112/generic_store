package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.error.ProductNotFoundException;
import ua.sunbeam.genericstore.service.InventoryService;

@RestController
@RequestMapping("/admin/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }


    /**
     * Retrieves the quantity of a product from the inventory based on its ID.
     * This endpoint handles HTTP GET requests to "/getQuantity".
     *
     * @param id The ID of the product as a String. This parameter is required and must be a valid Long number.
     * @return A {@link ResponseEntity} containing:
     * - The product quantity ({@code Integer}) with {@code HttpStatus.OK} if the product is found and quantity is available.
     * - {@code HttpStatus.BAD_REQUEST} if the 'id' parameter is null, empty, or not a valid number.
     * - {@code HttpStatus.NOT_FOUND} if the product with the given ID is not found in the inventory.
     */

    @GetMapping("/getQuantity")
    public ResponseEntity<Integer> getQuantity(@RequestParam String id) {
        try {
            int quantity = inventoryService.getProductQuantity(Long.parseLong(id));
            return new ResponseEntity<>(quantity, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(-2);
        }  catch (ProductNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(-2);
        }
    }

    
}

