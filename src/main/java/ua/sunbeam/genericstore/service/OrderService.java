package ua.sunbeam.genericstore.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.OrderNotFoundException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.model.DAO.UserOrderRepository;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.UserOrder;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service

public class OrderService {

    private final UserService userService;
    private final UserOrderRepository userOrderRepository;

    public OrderService(UserService userService, UserOrderRepository userOrderRepository) {
        this.userService = userService;
        this.userOrderRepository = userOrderRepository;
    }

    @Transactional
    public void deleteOrder(Long id) throws OrderNotFoundException {
        if (!userOrderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order with ID " + id + " not found for deletion.");
        }
        userOrderRepository.deleteById(id);
    }

    public List<UserOrder> getAllOrders() {
        return userOrderRepository.findAll();
    }

    public Optional<UserOrder> getOrderById(Long id) {
        return userOrderRepository.findById(id);

    }

    @Transactional
    public UserOrder saveOrder(UserOrder userOrder) {
        return userOrderRepository.save(userOrder);
    }

    @Transactional
    public UserOrder createOrder(Long userID) throws UserNotExistsException, EmailsNotVerifiedException {
        Optional<LocalUser> opUser = userService.getUserByID(userID);
        if (opUser.isPresent()) {
            if (opUser.get().isEmailVerified()) {
                return createNewEntityOrderForUser(opUser.get());
            }
            throw new EmailsNotVerifiedException();
        }
        throw new UserNotExistsException();
    }

    public List<UserOrder> getAllOrdersForUser(Long userID) throws UserNotExistsException {
        if (userService.isUserExistsByID(userID)) {
            return userOrderRepository.getAllOrdersByUserIdOrderByDateAsc(userID);
        }
        throw new UserNotExistsException();
    }

    private UserOrder createNewEntityOrderForUser(LocalUser user) {
        UserOrder userOrder = new UserOrder();
        userOrder.setDate(new Timestamp(System.currentTimeMillis()));
        userOrder.setLocalUser(user);
        userOrder.setStatus("Pending");


        return userOrderRepository.save(userOrder);
    }
}
