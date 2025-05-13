package com.javatechie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javatechie.dto.OrderResponseDTO;
import com.javatechie.dto.PaymentDTO;
import com.javatechie.dto.UserDTO;
import com.javatechie.entity.Order;
import com.javatechie.repo.OrderRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    public static final String PAYMENT_SERVICE_URL = "http://localhost:9091/payments";
    public static final String USERS_SERVICE_URL = "http://localhost:9089/users";
    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private RestTemplate restTemplate;

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

    public OrderResponseDTO getOrder(String orderId){
        Order order = orderRepo.findByOrderId(orderId);
        PaymentDTO payment = restTemplate.getForObject(PAYMENT_SERVICE_URL + "/" +orderId,PaymentDTO.class);
        UserDTO user = restTemplate.getForObject(USERS_SERVICE_URL + "/" +order.getUserId(), UserDTO.class);
        return OrderResponseDTO.builder()
                .order(order)
                .paymentResponse(payment)
                .userInfo(user)
                .build();
    }
}
