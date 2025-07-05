package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.OrderRequestBody;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.error.UserNotFoundException;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.service.OrderService;

import java.util.List;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/admin/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @CrossOrigin
    @PostMapping("/create-new")
    public ResponseEntity<Object> createOrder(@RequestParam Long userID) throws UserNotExistsException {
        try {
            UserOrder order = orderService.createNewEmptyOrderForUser(userID);

            if (order != null) {
                return new ResponseEntity<>(HttpStatus.CREATED);

            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (EmailsNotVerifiedException e) {
            return ResponseEntity.badRequest().body("EMAIL_NOT_VERIFIED");
        } catch (UserNotExistsException | UserNotFoundException e) {
            return ResponseEntity.badRequest().body("USER_NOT_EXISTS");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @CrossOrigin
    @PostMapping("/set")
    public ResponseEntity<Object> setOrder(@RequestBody OrderRequestBody orderBody, BindingResult result) throws UserNotExistsException, EmailsNotVerifiedException {
        try {
            if (orderService.existsById(orderBody.getOrderID())) {
                UserOrder order = orderService.placeOrder(orderBody, result);
                if (order != null) {
                    return new ResponseEntity<>(HttpStatus.CREATED);
                }
            }
            return ResponseEntity.badRequest().body("ORDER_NOT_FOUND");
        } catch (EmailsNotVerifiedException e) {
            return ResponseEntity.badRequest().body("EMAIL_NOT_VERIFIED");
        } catch (UserNotExistsException | UserNotFoundException e) {
            return ResponseEntity.badRequest().body("USER_NOT_EXISTS");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @CrossOrigin
    @GetMapping("/all-orders")
    public ResponseEntity<Object> getAllOrders() {
        List<UserOrder> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            return ResponseEntity.ok("NO_ORDERS");
        }

        return ResponseEntity.ok(orders);
    }


    @CrossOrigin
    @GetMapping("/all-orders/user")
    public ResponseEntity<List<UserOrder>> getAllOrdersForUser(@RequestParam Long userID) {
        List<UserOrder> orders = orderService.getAllOrdersForUser(userID);
        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(orders);
    }


}
