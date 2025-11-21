package com.coffeeshop.controller;

import com.coffeeshop.model.CartItem;
import com.coffeeshop.model.Order;
import com.coffeeshop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody List<CartItem> cartItems) {
        Order order = orderService.createOrder(cartItems);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/today")
    public List<Order> getTodayOrders() {
        return orderService.getTodayOrders();
    }

    @GetMapping("/today/total")
    public ResponseEntity<Map<String, Double>> getTodayTotal() {
        Double total = orderService.getTodayTotalSales();
        return ResponseEntity.ok(Map.of("totalSales", total));
    }
}