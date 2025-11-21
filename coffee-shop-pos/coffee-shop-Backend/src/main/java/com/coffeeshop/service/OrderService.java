package com.coffeeshop.service;

import com.coffeeshop.model.CartItem;
import com.coffeeshop.model.Order;
import com.coffeeshop.model.Product;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;

    public Order createOrder(List<CartItem> cartItems) {
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("COMPLETED");

        List<Order.OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Order.OrderItem item = new Order.OrderItem();
            item.setProductId(cartItem.getProductId());
            item.setProductName(cartItem.getProductName());
            item.setPrice(cartItem.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setSubtotal(cartItem.getSubtotal());
            return item;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        double total = cartItems.stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum();
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getTodayOrders() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return orderRepository.findByOrderDateBetween(startOfDay, endOfDay);
    }

    public Double getTodayTotalSales() {
        return getTodayOrders().stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
    }
}