package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.error.EmailsNotVerifiedException;
import ua.sunbeam.genericstore.error.UserNotExistsException;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.service.OrderService;
import ua.sunbeam.genericstore.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/admin/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
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

    /**
     * Retrieves all orders for the authenticated user.
     * <p>
     * This endpoint is accessible at the `/my-orders` path and requires an authenticated principal.
     * It extracts the user's email from the security context to identify the user
     * and then fetches all associated orders.
     *
     * @return A {@link ResponseEntity} containing:
     * - A {@link List} of {@link UserOrder} objects and {@link HttpStatus#OK} if orders are found (or an empty list if no orders exist).
     * - {@link HttpStatus#UNAUTHORIZED} if no principal is found in the security context.
     * - {@link HttpStatus#BAD_REQUEST} if the extracted user email is empty.
     * - {@link HttpStatus#NOT_FOUND} if the user with the given email is not found.
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<UserOrder>> getMyOrders() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String email = (String) principal;
        if (email.trim().isEmpty()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Optional<LocalUser> op_localUser = userService.getUserByEmail(email);

        if (op_localUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LocalUser localUser = op_localUser.get();

        List<UserOrder> orders = orderService.getAllOrdersForUser(localUser.getId());

        if (orders.isEmpty()) return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        return ResponseEntity.ok(orders);
    }
}
