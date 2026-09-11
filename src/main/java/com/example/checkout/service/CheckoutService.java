package com.example.checkout.service;

import jakarta.enterprise.context.ApplicationScoped;
import com.example.checkout.model.CheckoutRequest;

@ApplicationScoped
public class CheckoutService {

    public boolean validateCart(CheckoutRequest req) {
        return req != null && req.isCartValid();
    }

    public boolean checkInventory(CheckoutRequest req) {
        return req != null && req.isStockAvailable();
    }

    public boolean processPayment(CheckoutRequest req) {
        return req != null && req.isPaymentSuccessful();
    }

    public String createOrder(CheckoutRequest req) {
        String orderId = "ORD-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        System.out.println("Created Order ID: " + orderId);
        return orderId;
    }

    public void updateInventory(CheckoutRequest req) {
        System.out.println("Inventory Updated for Cart: " + (req != null ? req.getCartId() : "Unknown"));
    }

    public void sendConfirmation(String orderId) {
        System.out.println("Confirmation Sent for Order: " + orderId);
    }

    public void completeCheckout(CheckoutRequest req) {
        System.out.println("Checkout Workflow Successfully Completed!");
    }

    public void rejectCheckout(CheckoutRequest req) {
        System.out.println("Checkout Rejected for Customer: " + (req != null ? req.getCustomerId() : "Unknown"));
    }

    public void handleOutOfStock(CheckoutRequest req) {
        System.out.println("Out of Stock for Cart: " + (req != null ? req.getCartId() : "Unknown"));
    }

    public void handlePaymentFailure(CheckoutRequest req) {
        System.out.println("Payment Failed for Amount: " + (req != null ? req.getAmount() : 0));
    }
}