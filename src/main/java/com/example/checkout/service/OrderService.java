package com.example.checkout.service;

import com.example.checkout.model.CheckoutRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class OrderService {

    public String createOrder(CheckoutRequest request) {
        String orderId = "ORD-" + UUID.randomUUID();

        System.out.println(
                "Order created: " + orderId
                        + " for customer: " + request.getCustomerId()
        );

        return orderId;
    }
}