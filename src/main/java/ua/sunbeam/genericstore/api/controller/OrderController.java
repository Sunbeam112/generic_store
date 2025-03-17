package ua.sunbeam.genericstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.service.OrderItemsService;
import ua.sunbeam.genericstore.service.OrderService;

import java.util.List;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/admin/order")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemsService orderItemsService;

    public OrderController(OrderService orderService, OrderItemsService orderItemsService) {
        this.orderService = orderService;
        this.orderItemsService = orderItemsService;
    }

    @CrossOrigin
    @PostMapping("/create")
    public ResponseEntity<Object> createOrder(@RequestParam Long userID) {
        try {
            UserOrder order = orderService.createOrder(userID);
            if (order != null) {
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (EmailsNotVerifiedException e) {
            return ResponseEntity.badRequest().body("EMAIL_NOT_VERIFIED");
        } catch (UserNotExistsException e) {
            return ResponseEntity.badRequest().body("USER_NOT_EXISTS");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @CrossOrigin
    @PostMapping("/set")
    public ResponseEntity<Object> fillOrder(Long orderID, @Valid @RequestBody List<ProductToOrderBody> products) {
        UserOrder order = orderService.getOrderById(orderID);
        if (order == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        try {
            boolean isAdded = orderItemsService.addItemsToOrder(products, order.getId());
            if (isAdded) return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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


}
