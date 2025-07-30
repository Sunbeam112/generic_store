package ua.sunbeam.genericstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.error.InsufficientStockException;
import ua.sunbeam.genericstore.error.OrderNotFoundException;
import ua.sunbeam.genericstore.error.ProductNotFoundException;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.service.OrderItemsService;
import ua.sunbeam.genericstore.service.OrderService;
import ua.sunbeam.genericstore.service.ValidationErrorsParser;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/order/items")
public class OrderItemsController {
    private final OrderItemsService orderItemsService;
    private final OrderService orderService;
    private final ValidationErrorsParser validationErrorsParser;

    public OrderItemsController(OrderItemsService orderItemsService, OrderService orderService, ValidationErrorsParser validationErrorsParser) {
        this.orderItemsService = orderItemsService;
        this.orderService = orderService;
        this.validationErrorsParser = validationErrorsParser;
    }

    @GetMapping("/get")
    public ResponseEntity<List<OrderItem>> getAllOrderItems(@RequestParam Long orderID) {
        Optional<UserOrder> order = orderService.getOrderById(orderID);
        if (order.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<OrderItem> orderItems;
        try {
            orderItems = orderItemsService.getAllOrderItemsByOrderId(order.get().getId());
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (orderItems.isEmpty()) return new ResponseEntity<>(HttpStatus.FOUND);

        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }


    @CrossOrigin
    @PostMapping("/set")
    public ResponseEntity<Object> fillOrder(@RequestParam Long orderID, @Valid @RequestBody List<ProductToOrderBody> products, BindingResult result) {
        Optional<UserOrder> order = orderService.getOrderById(orderID);
        if (order.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        try {
            boolean isAdded = orderItemsService.addItemsToOrder(products, order.get().getId());
            result.getFieldErrors();
            if (result.hasErrors()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (isAdded) return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (OrderNotFoundException e) { // NEW CATCH BLOCK
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("ORDER_NOT_FOUND", e.getMessage()));
        } catch (ProductNotFoundException e) { // NEW CATCH BLOCK
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("PRODUCT_NOT_FOUND", e.getMessage()));
        } catch (InsufficientStockException e) { // NEW CATCH BLOCK
            return ResponseEntity.badRequest().body(validationErrorsParser.createErrorResponse("INSUFFICIENT_STOCK", e.getMessage()));
            
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
