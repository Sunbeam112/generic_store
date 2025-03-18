package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<List<OrderItem>> getAllOrderItems(@RequestParam Long orderId) {
        UserOrder order = orderService.getOrderById(orderId);
        if (order == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<OrderItem> orderItems;
        orderItems = orderItemsService.getAllOrderItemsByOrderId(order.getId());
        if (orderItems.isEmpty()) return new ResponseEntity<>(HttpStatus.FOUND);

        return new ResponseEntity<>(orderItems, HttpStatus.OK);
    }

}
