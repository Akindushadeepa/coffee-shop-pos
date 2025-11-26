package com.coffee_shop.model;

public abstract class Users {
    private final String username;
    private final String password;

    public Users(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean matches(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public abstract String getRole();
}
