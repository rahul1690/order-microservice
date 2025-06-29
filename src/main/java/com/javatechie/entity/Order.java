package com.javatechie.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "ORDER_TBL")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String category;
    private double price;
    private Date purchasedDate;
    private String orderId;
    private String userId;
    private String paymentMode;
}
