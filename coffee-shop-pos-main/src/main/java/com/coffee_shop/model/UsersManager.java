package com.coffee_shop.model;

import java.util.ArrayList;
import java.util.List;

public class UsersManager {
    private final List<Users> users = new ArrayList<>();

    public UsersManager() {
    }

    public void addUser(Users user) {
        if (user != null) {
            users.add(user);
        }
    }

    public Users authenticate(String username, String password) {
        for (Users u : users) {
            if (u.matches(username, password)) {
                return u;
            }
        }
        return null;
    }

    public List<Users> getUsers() {
        return new ArrayList<>(users);
    }
}
