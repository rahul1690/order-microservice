package com.javatechie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.dto.OrderResponseDTO;
import com.javatechie.dto.PaymentDTO;
import com.javatechie.dto.UserDTO;
import com.javatechie.entity.Order;
import com.javatechie.repo.OrderRepo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RefreshScope
public class OrderService {

//    public static final String PAYMENT_SERVICE_URL = "http://PAYMENT-SERVICE/payments";
//    public static final String USERS_SERVICE_URL = "http://USER-SERVICE/users";
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    @Value("${microservice.payment-service.endpoints.fetchPaymentById.uri}")
    private String paymentServiceUri;

    @Value("${microservice.user-service.endpoints.fetchUserById.uri}")
    private String userServiceUri;

    @Value("${test.value}")
    private String test;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${order.producer.topic.name}")
    private String topicName;



    public String placeOrder(Order order){
        order.setOrderId(UUID.randomUUID().toString().split("-")[0]);
        order.setPurchasedDate(new Date());
        orderRepo.save(order);
        try {
            kafkaTemplate.send(topicName, new ObjectMapper().writeValueAsString(order));
        } catch (JsonProcessingException e) {
            log.error("Error Occurred while parsing Order to Json string -> {}",e.getMessage());
        }
        return "Your order has been placed ! we will notify once it will confirm!";
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "getOrderDetails")
    public OrderResponseDTO getOrder(String orderId){
        System.out.println("************* "+test);
        Order order = orderRepo.findByOrderId(orderId);

        PaymentDTO payment = restTemplate.getForObject(paymentServiceUri + "/" +orderId,PaymentDTO.class);

        UserDTO user = restTemplate.getForObject(userServiceUri + "/" +order.getUserId(), UserDTO.class);

        return OrderResponseDTO.builder()
                .order(order)
                .paymentResponse(payment)
                .userInfo(user)
                .build();
    }

    public OrderResponseDTO getOrderDetails(Exception ex){
        log.error(ex.getMessage());
        return new OrderResponseDTO("Failed",null,null,null);
    }
}
