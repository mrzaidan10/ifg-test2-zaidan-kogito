package com.example.checkout.service;

import com.example.checkout.model.CheckoutRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CartService {

    public boolean validateCart(CheckoutRequest request) {
        System.out.println("Validating cart: " + request.getCartId());

        boolean valid = request.isCartValid();

        if (valid) {
            System.out.println("Cart validation successful");
        } else {
            System.out.println("Cart validation failed");
        }

        return valid;
    }
}