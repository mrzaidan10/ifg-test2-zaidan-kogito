package com.example.checkout.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationService {

    public void sendConfirmation(String orderId) {
        System.out.println(
                "Checkout confirmation sent for order: " + orderId
        );
    }
}