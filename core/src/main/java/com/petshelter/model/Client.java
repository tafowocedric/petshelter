package com.petshelter.model;

import com.petshelter.enums.UserRole;

public class Client extends User {

    public Client(String username, String password, String fullName, String email, String phone) {
        super(username, password, fullName, email, phone);
    }

    @Override
    public UserRole getRole() {
        return UserRole.CLIENT;
    }
}