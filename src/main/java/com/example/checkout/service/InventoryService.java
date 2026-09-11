package com.example.checkout.service;

import com.example.checkout.model.CheckoutRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InventoryService {

    public boolean checkInventory(CheckoutRequest request) {
        System.out.println("Checking inventory for cart: " + request.getCartId());

        boolean available = request.isStockAvailable();

        if (available) {
            System.out.println("Inventory is available");
        } else {
            System.out.println("Inventory is not available");
        }

        return available;
    }

    public void updateInventory(CheckoutRequest request) {
        System.out.println("Inventory updated for cart: " + request.getCartId());
    }
}