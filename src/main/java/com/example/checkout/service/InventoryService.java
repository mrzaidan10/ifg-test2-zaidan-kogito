package com.example.checkout.service;

import com.example.checkout.model.CheckoutRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InventoryService {

    @ConfigProperty(name = "inventory", defaultValue = "")
    Map<String, Integer> initialStock;

    private final ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        initialStock.forEach(stock::putIfAbsent);
    }

    public boolean checkInventory(CheckoutRequest request) {
        System.out.println("Checking inventory for cart: " + request.getCartId());
        boolean available = stock.getOrDefault(request.getCartId(), 0) > 0;
        if (available) {
            System.out.println("Inventory is available");
        } else {
            System.out.println("Inventory is not available");
        }
        return available;
    }

    public void updateInventory(CheckoutRequest request) {
        stock.computeIfPresent(request.getCartId(), (k, v) -> v > 0 ? v - 1 : 0);
        System.out.println("Inventory updated for cart: " + request.getCartId() + " (remaining: " + stock.getOrDefault(request.getCartId(), 0) + ")");
    }

    public int getStock(String cartId) {
        return stock.getOrDefault(cartId, 0);
    }
}
