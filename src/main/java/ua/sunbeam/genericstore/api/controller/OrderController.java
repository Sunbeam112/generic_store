package ua.sunbeam.genericstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.error.*;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.service.OrderItemsService;
import ua.sunbeam.genericstore.service.OrderService;
import ua.sunbeam.genericstore.service.UserService;
import ua.sunbeam.genericstore.service.ValidationErrorsParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/admin/order")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ValidationErrorsParser validationErrorsParser;
    private final OrderItemsService orderItemsService;

    public OrderController(OrderService orderService, UserService userService, ValidationErrorsParser validationErrorsParser, OrderItemsService orderItemsService) {
        this.orderService = orderService;
        this.userService = userService;
        this.validationErrorsParser = validationErrorsParser;

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
        Optional<LocalUser> opLocalUser = userService.tryGetCurrentUser();
        if (opLocalUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        LocalUser localUser = opLocalUser.get();

        List<UserOrder> orders = orderService.getAllOrdersForUser(localUser.getId());

        if (orders.isEmpty()) return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/create-empty-order")
    public ResponseEntity<Object> createOrderForCurrentUser() {
        Optional<LocalUser> opLocalUser = userService.tryGetCurrentUser();
        if (opLocalUser.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            LocalUser localUser = opLocalUser.get();
            createOrder(localUser.getId());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates a new order for the current user and fills it with the specified products.
     * Performs validation on the input product list and handles various business exceptions.
     *
     * @param products The list of products (with product ID and quantity) to add to the order.
     * @param result   BindingResult for capturing validation errors from the @Valid annotation.
     * @return A ResponseEntity indicating the success or failure of the order creation and item addition.
     * - HttpStatus.CREATED if the order is successfully created and items are added.
     * - HttpStatus.BAD_REQUEST with a structured error body for validation failures or business rule violations.
     * - HttpStatus.UNAUTHORIZED if the current user is not authenticated or the session is invalid.
     * - HttpStatus.INTERNAL_SERVER_ERROR for unexpected system errors.
     */
    @PostMapping("/create-order")
    public ResponseEntity<Object> createAndFillOrderForCurrentUser(
            @Valid @RequestBody List<ProductToOrderBody> products,
            BindingResult result) {

        // 1. Handle @Valid validation errors
        if (result.hasErrors()) {
            Map<String, List<String>> errors = validationErrorsParser.parseErrorsFrom(result);
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("VALIDATION_FAILED", "Input validation failed.", errors));
        }

        // 2. Authenticate user
        Optional<LocalUser> opUser = userService.tryGetCurrentUser();
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationErrorsParser.createErrorResponse("NOT_AUTHENTICATED", "User is not authenticated or session invalid."));
        }
        LocalUser user = opUser.get();

        try {
            // 3. Create order and add items within a single transaction
            // Assuming createOrder is also transactional or this method itself becomes transactional
            UserOrder createdOrder = orderService.createOrder(user.getId());
            orderItemsService.addItemsToOrder(products, createdOrder.getId());

            // 4. Success response (return the created order details)
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED); // Or a DTO of createdOrder

        } catch (EmailsNotVerifiedException e) {
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("EMAIL_NOT_VERIFIED", "User email address is not verified."));
        } catch (UserNotExistsException e) {
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("USER_NOT_EXISTS", "The specified user does not exist."));
        } catch (OrderNotFoundException e) { // This catch might be redundant if createOrder & addItems are fully atomic
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("ORDER_NOT_FOUND", e.getMessage()));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("PRODUCT_NOT_FOUND", e.getMessage()));
        } catch (InsufficientStockException e) {
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("INSUFFICIENT_STOCK", e.getMessage()));
        } catch (IllegalArgumentException e) { // Catches "Product list cannot be null or empty." from service, and other invalid args
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("INVALID_INPUT", e.getMessage()));
        } catch (Exception e) {
            // Log the exception for debugging purposes
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(validationErrorsParser.createErrorResponse("UNEXPECTED_ERROR", "An unexpected error occurred. Please try again later."));
        }
    }

}
