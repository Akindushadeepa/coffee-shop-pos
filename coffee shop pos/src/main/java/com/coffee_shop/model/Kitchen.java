package com.coffee_shop.model;

public class Kitchen extends Users {
    public Kitchen(String username, String password) {
        super(username, password);
    }

    @Override
    public String getRole() {
        return "KITCHEN";
    }
}
