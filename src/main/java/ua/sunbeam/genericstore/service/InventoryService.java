package ua.sunbeam.genericstore.service;

import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.model.DAO.InventoryRepository;
import ua.sunbeam.genericstore.model.Inventory;
import ua.sunbeam.genericstore.model.Product;

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
        Product product = productService.findById(productID);
        if (product == null) return -1;
        if (product.getInventory() == null) return 0;
        return product.getInventory().getQuantity();
    }


    public boolean setItemQuantity(Long productID, int quantity) {
        if (productID == null || productID < 0) throw new IllegalArgumentException("Bad product ID");
        Product product = productService.findById(productID);
        if (product == null) throw new IllegalArgumentException("No such product");
        if (product.getInventory() == null) {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(quantity);
            inventoryRepository.save(inventory);
            return true;
        } else {
            product.getInventory().setQuantity(quantity);
            return true;
        }
    }
}

