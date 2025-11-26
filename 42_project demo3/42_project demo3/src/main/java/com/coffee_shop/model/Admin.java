package com.coffee_shop.model;

public class Admin extends Users {
    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}
