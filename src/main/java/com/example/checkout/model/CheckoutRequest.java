package com.example.checkout.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class CheckoutRequest {
    private String customerId;
    private String cartId;
    private double amount;
    private boolean cartValid;
    private boolean stockAvailable;
    private boolean paymentSuccessful;

    public CheckoutRequest() {}

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isCartValid() { return cartValid; }
    public void setCartValid(boolean cartValid) { this.cartValid = cartValid; }

    public boolean isStockAvailable() { return stockAvailable; }
    public void setStockAvailable(boolean stockAvailable) { this.stockAvailable = stockAvailable; }

    public boolean isPaymentSuccessful() { return paymentSuccessful; }
    public void setPaymentSuccessful(boolean paymentSuccessful) { this.paymentSuccessful = paymentSuccessful; }
}