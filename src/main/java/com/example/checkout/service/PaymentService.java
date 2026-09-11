package com.example.checkout.service;

import com.example.checkout.model.CheckoutRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentService {

    public boolean processPayment(CheckoutRequest request) {
        System.out.println("Processing payment for amount: " + request.getAmount());

        boolean successful = request.isPaymentSuccessful();

        if (successful) {
            System.out.println("Payment processed successfully");
        } else {
            System.out.println("Payment processing failed");
        }

        return successful;
    }
}