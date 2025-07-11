package ua.sunbeam.genericstore.service;

import org.springframework.stereotype.Service;
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


    public int getProductQuantity(Long productID) {
        if (productID == null || productID <= 0) return -1;
        Optional<Product> product = productService.findById(productID);
        if (product.isEmpty()) return -1;
        if (product.get().getInventory() == null) return 0;
        return product.get().getInventory().getQuantity();
    }


    public boolean setItemQuantity(Long productID, int quantity) {
        if (productID == null || productID < 0) throw new IllegalArgumentException("Bad product ID");
        Optional<Product> product = productService.findById(productID);
        if (product.isPresent()) {
            if (product.get().getInventory() == null) {
                Inventory inventory = new Inventory();
                inventory.setProduct(product.get());
                inventory.setQuantity(quantity);
                inventoryRepository.save(inventory);
                return true;
            }
            product.get().getInventory().setQuantity(quantity);
            return true;
        }
        throw new IllegalArgumentException("No such product with ID: " + productID);
    }
}

