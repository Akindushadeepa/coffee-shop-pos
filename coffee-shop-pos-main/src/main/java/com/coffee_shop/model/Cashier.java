package com.coffee_shop.model;

public class Cashier extends Users {
    public Cashier(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "CASHIER";
    }
}
