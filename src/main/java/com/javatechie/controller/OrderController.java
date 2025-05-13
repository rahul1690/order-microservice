package com.javatechie.controller;

import com.javatechie.dto.OrderResponseDTO;
import com.javatechie.entity.Order;
import com.javatechie.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public String placeNewOrder(@RequestBody Order order){
        return orderService.placeOrder(order);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrder(@PathVariable String orderId){
        return orderService.getOrder(orderId);
    }
}
