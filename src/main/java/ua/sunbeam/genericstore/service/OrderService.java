package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.model.DAO.UserOrderRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final UserService userService;
    private final OrderItemsRepository OrderItemRepository;
    private final UserOrderRepository userOrderRepository;

    public OrderService(UserService userService, OrderItemsRepository orderItemRepository,
                        UserOrderRepository userOrderRepository) {
        this.userService = userService;
        OrderItemRepository = orderItemRepository;
        this.userOrderRepository = userOrderRepository;
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

    public UserOrder createOrder(Long userId) throws UserNotExistsException, EmailsNotVerifiedException {
        if (userOrderRepository.existsById(userId)) {
            LocalUser user = userService.GetUserByID(userId);
            if (user.isEmailVerified()) {
                UserOrder userOrder = new UserOrder();
                userOrder.setDate(new Timestamp(System.currentTimeMillis()));
                userOrder.setLocalUser(user);
                userOrder.setStatus("Pending");
                return userOrderRepository.save(userOrder);

            } else throw new EmailsNotVerifiedException();
        } else throw new UserNotExistsException();

    }
}
