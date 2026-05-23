package com.petshelter.model;

import com.petshelter.enums.UserRole;

public class Admin extends User {
    public Admin(String username, String password, String fullName, String email, String phone) {
        super(username, password, fullName, email, phone);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }

    /** Admins can approve adoptions; clients cannot. */
    public boolean canApproveAdoptions() {
        return true;
    }
}