package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
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
    private final UserOrderRepository orderRepository;
    private final UserOrderRepository userOrderRepository;

    public OrderService(UserService userService, UserOrderRepository orderRepository, UserOrderRepository userOrderRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
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

    public UserOrder createOrder(Long userID) throws UserNotExistsException, EmailsNotVerifiedException {
        if (userOrderRepository.existsById(userID)) {
            LocalUser user = userService.getUserByID(userID);
            if (user.isEmailVerified()) {
                UserOrder userOrder = new UserOrder();
                userOrder.setDate(new Timestamp(System.currentTimeMillis()));
                userOrder.setLocalUser(user);
                userOrder.setStatus("Pending");
                return userOrderRepository.save(userOrder);

            } else throw new EmailsNotVerifiedException();
        } else throw new UserNotExistsException();

    }

    public List<UserOrder> getAllOrdersForUser(Long userID) {
        if (userService.isUserExistsByID(userID)) {
            return orderRepository.getAllOrdersByUserIdOrderByDateAsc(userID);
        }
        return null;
    }
}
