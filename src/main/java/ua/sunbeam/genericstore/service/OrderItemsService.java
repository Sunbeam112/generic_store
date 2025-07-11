package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.UserOrder;

import java.util.List;

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

    public boolean addItemsToOrder(@Valid @Min.List({@Min(value = 1), @Min(value = 1)}) List<ProductToOrderBody> products, Long orderId, BindingResult result) throws IllegalArgumentException {
        if (products == null || products.isEmpty()) throw new IllegalArgumentException("Products is null or empty");
        UserOrder order = orderService.getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("No such order");
        result.getFieldErrors();
        if (result.hasErrors()) {
            return false;
        }
        for (ProductToOrderBody product : products) {
            if (product.getProductID() == null || product.getQuantity() == null)
                throw new IllegalArgumentException("Product ID or quantity is null");
            if (product.getProductID() > 0 && product.getQuantity() > 0) {
                if (productService.findById(product.getProductID()).isEmpty())
                    throw new IllegalArgumentException("No such product");
                boolean isInStock = inventoryService.getProductQuantity(product.getProductID()) >= product.getQuantity();

                if (isInStock) {
                    OrderItem item = new OrderItem();
                    item.setUserOrder(order);
                    item.setDispatched(false);
                    item.setQuantity(product.getQuantity());
                    item.setProduct(productService.findById(product.getProductID()).get());
                    orderItemsRepository.save(item);
                } else {
                    throw new IllegalArgumentException("Not enough quantity of product");
                }

            } else throw new IllegalArgumentException("Bad product ID or quantity");
        }
        return true;
    }

    public List<OrderItem> getAllOrderItemsByOrderId(Long orderId) {
        UserOrder order = orderService.getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("No such order");
        List<OrderItem> orderItems;
        orderItems = orderItemsRepository.getAllOrderItemsByOrderId(order.getId());
        return orderItems;
    }
}
