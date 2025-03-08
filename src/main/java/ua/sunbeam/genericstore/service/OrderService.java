package ua.sunbeam.genericstore.service;

import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.OrderItems;
import ua.sunbeam.genericstore.model.OrderRepository;
import ua.sunbeam.genericstore.model.UserOrder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final UserService userService;
    private final OrderItemsRepository orderItemsRepository;

    public OrderService(OrderRepository orderRepo, UserService userService,
                        OrderItemsRepository orderItemsRepository) {
        this.orderRepo = orderRepo;
        this.userService = userService;
        this.orderItemsRepository = orderItemsRepository;
    }


    private List<OrderItems> saveOrderedItems(List<ProductToOrderBody> products) throws IllegalArgumentException {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products can't be null or empty");
        }
        List<OrderItems> items = new ArrayList<>();
        for (ProductToOrderBody product : products) {
            if (product.getProductID() > 0 && product.getQuantity() > 0) {
                OrderItems item = new OrderItems();
                item.setId(product.getProductID());
                item.setQuantity(product.getQuantity());
                item.setIsDispatched(false);
                items.add(item);
                orderItemsRepository.save(item);
            } else {
                throw new IllegalArgumentException(
                        String.format("Product ID %d in quantity %d is not valid",
                                product.getProductID(), product.getQuantity()));
            }

        }

        return items;
    }

    public void createOrder(Long userID, List<ProductToOrderBody> products) throws EmailsNotVerifiedException {
        if (userID != null) {
            if (userService.IsUserExistsByID(userID)) {
                LocalUser user = userService.GetUserByID(userID);
                if (userService.IsUserEmailVerified(user.getEmail())) {
                    UserOrder order = new UserOrder();
                    order.setDateCreated(new Timestamp(System.currentTimeMillis()));
                    List<OrderItems> orderItems = saveOrderedItems(products);
                    order.setOrderItems(orderItems);
                    orderRepo.save(order);
                } else throw new EmailsNotVerifiedException();
            }
        }
        throw new UserNotExistsException();
    }

    public Iterable<UserOrder> getAllOrders() {
        Iterable<UserOrder> orders;
        orders = orderRepo.findAll();
        return orders;
    }
}
