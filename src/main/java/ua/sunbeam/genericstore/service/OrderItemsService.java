package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.UserOrder;

import java.util.List;

@Service
@Transactional
public class OrderItemsService {

    private final OrderService orderService;
    private final ProductService productService;
    private final OrderItemsRepository orderItemsRepository;

    public OrderItemsService(OrderService orderService, ProductService productService,
                             OrderItemsRepository orderItemsRepository) {
        this.orderService = orderService;
        this.productService = productService;
        this.orderItemsRepository = orderItemsRepository;
    }

    public boolean addItemsToOrder(List<ProductToOrderBody> products, Long orderId) throws IllegalArgumentException {
        if (products == null || products.isEmpty()) throw new IllegalArgumentException("Products is null or empty");
        UserOrder order = orderService.getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("No such order");


        for (ProductToOrderBody product : products) {
            if (product.getProductID() > 0 && product.getQuantity() > 0) {
                OrderItem item = new OrderItem();
                item.setUserOrder(order);
                item.setDispatched(false);
                item.setQuantity(product.getQuantity());
                item.setProduct(productService.findById(product.getProductID()));
                orderItemsRepository.save(item);
            } else throw new IllegalArgumentException("Bad product ID or quantity");
        }
        return true;
    }
}
