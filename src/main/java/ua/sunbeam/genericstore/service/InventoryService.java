package ua.sunbeam.genericstore.service;

import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.ProductNotFoundException;
import ua.sunbeam.genericstore.model.DAO.InventoryRepository;
import ua.sunbeam.genericstore.model.Inventory;
import ua.sunbeam.genericstore.model.Product;

import java.util.Optional;

@Service
public class InventoryService {

    private final ProductService productService;
    private final InventoryRepository inventoryRepository;

    public InventoryService(ProductService productService,
                            InventoryRepository inventoryRepository) {
        this.productService = productService;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Retrieves the available quantity of a product by its ID.
     * <p>
     * This method checks for valid product ID input, attempts to find the product,
     * and then returns the quantity from its inventory.
     *
     * @param productID The ID of the product.
     * @return The quantity of the product if found and inventory exists (can be 0 or positive).
     * @throws IllegalArgumentException If the product ID is null or not a positive number.
     * @throws ProductNotFoundException If the product with the given ID is not found.
     */
    public int getProductQuantity(Long productID) throws ProductNotFoundException, IllegalArgumentException {
        if (productID == null || productID <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number.");
        }

        Optional<Product> product = productService.findById(productID);
        if (product.isEmpty()) {
            throw new ProductNotFoundException("Product with ID " + productID + " not found.");
        }

        if (product.get().getInventory() == null) {
            System.out.println("inventory is null for " + productID);
            return 0;
        }

        return product.get().getInventory().getQuantity();
    }


    public Inventory add(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public void substractItems(Inventory inventory, Integer toSubstract) {
        if (inventory.getQuantity() - toSubstract < 0)
            throw new IllegalArgumentException("Can't remove more that exists");
        inventory.setQuantity(inventory.getQuantity() - toSubstract);
        inventoryRepository.save(inventory);
    }
}

