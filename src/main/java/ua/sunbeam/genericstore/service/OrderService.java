package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import ua.sunbeam.genericstore.api.model.OrderRequestBody;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.api.security.UUIDUtils;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.error.UserNotFoundException;
import ua.sunbeam.genericstore.model.Address;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.model.DAO.UserOrderRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final UserService userService;
    private final UserOrderRepository orderRepository;
    private final UserOrderRepository userOrderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderItemsRepository orderItemsRepository;


    public OrderService(UserService userService, UserOrderRepository orderRepository, UserOrderRepository userOrderRepository, ProductService productService, InventoryService inventoryService, OrderItemsRepository orderItemsRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.userOrderRepository = userOrderRepository;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderItemsRepository = orderItemsRepository;
    }

    public boolean deleteOrder(Long id) {
        if (userOrderRepository.existsById(id)) {
            userOrderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UserOrder> getAllOrders() {
        return userOrderRepository.findAll();
    }

    public UserOrder getOrderById(Long id) {
        Optional<UserOrder> userOrderOptional = userOrderRepository.findById(id);
        return userOrderOptional.orElse(null);
    }

    public UserOrder saveOrder(UserOrder userOrder) {
        return userOrderRepository.save(userOrder);
    }


    public List<UserOrder> getAllOrdersForUser(Long userID) {
        if (userService.isUserExistsByID(userID)) {
            return orderRepository.getAllOrdersByUserIdOrderByDateAsc(userID);
        }
        return null;
    }

    @Transactional
    public UserOrder placeOrder(OrderRequestBody orderBody, BindingResult result) throws EmailsNotVerifiedException, UserNotFoundException {
        if (userService.isUserExistsByID(orderBody.getUserID())) {
            if (userService.isUserHasAddress(orderBody.getUserID(), orderBody.getAddressID())) {
                LocalUser user = null;
                user = userService.getUserByID(orderBody.getUserID());
                if (user.isEmailVerified()) {
                    Address selectedAddress = user.getAddresses().stream().filter(a -> a.getId().equals(orderBody.getAddressID())).findFirst().orElse(null);
                    UserOrder userOrder = new UserOrder();
                    List<OrderItem> itemsAdded = addItemsToOrder(orderBody.getProducts(), orderBody.getOrderID(), result);
                    userOrder.setPaymentId(UUIDUtils.generateUUIDWithoutDashes());
                    userOrder.setShipmentId(UUIDUtils.generateUUIDWithoutDashes());
                    userOrder.setDate(new Timestamp(System.currentTimeMillis()));
                    userOrder.setLocalUser(user);
                    userOrder.setOrderItems(itemsAdded);
                    userOrder.setStatus("Pending");
                    userOrder.setAddress(selectedAddress);
                    return userOrderRepository.save(userOrder);

                } else throw new EmailsNotVerifiedException();
            } else {
                throw new IllegalArgumentException("User has no such address");
            }
        } else {
            throw new UserNotExistsException();
        }

    }


    public UserOrder createNewEmptyOrderForUser(Long userID) throws EmailsNotVerifiedException, UserNotExistsException, UserNotFoundException {
        if (userService.isUserExistsByID(userID)) {
            LocalUser user = userService.getUserByID(userID);
            if (user.isEmailVerified()) {
                UserOrder order = new UserOrder();
                order.setLocalUser(user);
                return orderRepository.save(order);
            }
            throw new EmailsNotVerifiedException();
        }
        throw new UserNotExistsException();
    }


    public boolean existsById(Long id) {
        return userOrderRepository.existsById(id);
    }


    public List<OrderItem> addItemsToOrder(@Valid @Min.List({@Min(value = 1), @Min(value = 1)}) List<ProductToOrderBody> products, Long orderId, BindingResult result) throws IllegalArgumentException {
        if (products == null || products.isEmpty()) throw new IllegalArgumentException("Products is null or empty");
        UserOrder order = getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("No such order");
        result.getFieldErrors();
        if (result.hasErrors()) {
            return null;
        }
        List<OrderItem> items = new ArrayList<>();
        for (ProductToOrderBody product : products) {
            if (product.getProductID() == null || product.getQuantity() == null)
                throw new IllegalArgumentException("Product ID or quantity is null");
            if (product.getProductID() > 0 && product.getQuantity() > 0) {
                if (productService.findById(product.getProductID()) == null)
                    throw new IllegalArgumentException("No such product");
                boolean isInStock = inventoryService.getProductQuantity(product.getProductID()) >= product.getQuantity();

                if (isInStock) {
                    OrderItem item = new OrderItem();
                    item.setUserOrder(order);
                    item.setDispatched(false);
                    item.setQuantity(product.getQuantity());
                    item.setProduct(productService.findById(product.getProductID()));
                    items.add(orderItemsRepository.save(item));
                } else {
                    throw new IllegalArgumentException("Not enough quantity of product");
                }

            } else throw new IllegalArgumentException("Bad product ID or quantity");
        }
        return items;
    }

    public List<OrderItem> getAllOrderItemsByOrderId(Long orderId) {
        UserOrder order = getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("No such order");
        List<OrderItem> orderItems;
        orderItems = orderItemsRepository.getAllOrderItemsByOrderId(order.getId());
        return orderItems;
    }


}
