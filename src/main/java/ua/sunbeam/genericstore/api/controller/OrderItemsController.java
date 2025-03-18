package ua.sunbeam.genericstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.ProductToOrderBody;
import ua.sunbeam.genericstore.model.DAO.OrderItemsRepository;
import ua.sunbeam.genericstore.model.OrderItem;
import ua.sunbeam.genericstore.model.UserOrder;
import ua.sunbeam.genericstore.service.OrderItemsService;
import ua.sunbeam.genericstore.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/admin/order/items")
public class OrderItemsController {
    private final OrderItemsService orderItemsService;
    private final OrderItemsRepository orderItemsRepository;
    private final OrderService orderService;

    public OrderItemsController(OrderItemsService orderItemsService, OrderItemsRepository orderItemsRepository, OrderService orderService) {
        this.orderItemsService = orderItemsService;
        this.orderItemsRepository = orderItemsRepository;
        this.orderService = orderService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<OrderItem>> getAllOrderItems(@RequestParam Long orderID) {
        UserOrder order = orderService.getOrderById(orderID);
        if (order == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<OrderItem> orderItems;
        orderItems = orderItemsService.getAllOrderItemsByOrderId(order.getId());
        if (orderItems.isEmpty()) return new ResponseEntity<>(HttpStatus.FOUND);

        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }


    @CrossOrigin
    @PostMapping("/set")
    public ResponseEntity<Object> fillOrder(@RequestParam Long orderID, @Valid @RequestBody List<ProductToOrderBody> products, BindingResult result) {
        UserOrder order = orderService.getOrderById(orderID);
        if (order == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        try {
            boolean isAdded = orderItemsService.addItemsToOrder(products, order.getId(), result);
            result.getFieldErrors();
            if (result.hasErrors()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            if (isAdded) return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


}
