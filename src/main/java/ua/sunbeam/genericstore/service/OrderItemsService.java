package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.error.InsufficientStockException;
import ua.sunbeam.genericstore.error.OrderNotFoundException;
import ua.sunbeam.genericstore.error.ProductNotFoundException;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.Product;
import ua.sunbeam.genericstore.model.UserOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@Transactional
public class OrderItemsService {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderItemsRepository orderItemsRepository;
    private final InventoryService inventoryService;

    public OrderItemsService(OrderService orderService, ProductService productService, OrderItemsRepository orderItemsRepository, InventoryService inventoryService) {
        this.orderService = orderService;
        this.productService = productService;
        this.orderItemsRepository = orderItemsRepository;
        this.inventoryService = inventoryService;
    }


    @Transactional
    public boolean addItemsToOrder(List<ProductToOrderBody> products, Long orderId)
            throws OrderNotFoundException, ProductNotFoundException, InsufficientStockException, IllegalArgumentException {

        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Product list cannot be null or empty.");
        }

        UserOrder order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found."));


        Map<Long, Product> productMap = new HashMap<>();

        for (ProductToOrderBody productToOrder : products) {
            Product existingProduct = productService.findById(productToOrder.getProductID())
                    .orElseThrow(() -> new ProductNotFoundException("Product with ID " + productToOrder.getProductID() + " not found."));

            int availableQuantity = (existingProduct.getInventory().getQuantity() == null)
                    ? 0 : existingProduct.getInventory().getQuantity();

            if (productToOrder.getQuantity() > availableQuantity) {
                throw new InsufficientStockException("Not enough quantity for product " + productToOrder.getProductID() + ". Available: " + availableQuantity + ", Requested: " + productToOrder.getQuantity());
            }
            productMap.put(productToOrder.getProductID(), existingProduct); // Store for later use
        }


        for (ProductToOrderBody productToOrder : products) {
            Product existingProduct = productMap.get(productToOrder.getProductID());

            OrderItem item = new OrderItem();
            item.setUserOrder(order);
            item.setDispatched(false);
            item.setQuantity(productToOrder.getQuantity());
            item.setProduct(existingProduct);
            orderItemsRepository.save(item);
            inventoryService.substractItems(existingProduct.getInventory(), productToOrder.getQuantity());
        }
        return true;
    }

    public List<OrderItem> getAllOrderItemsByOrderId(Long id) throws OrderNotFoundException {
        Optional<UserOrder> opOrder = orderService.getOrderById(id);
        if (opOrder.isPresent()) {
            return opOrder.get().getOrderItems();
        }
        throw new OrderNotFoundException("Order " + id.toString() + "was not found!");
    }
}
