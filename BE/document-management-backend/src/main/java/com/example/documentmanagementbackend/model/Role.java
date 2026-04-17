package com.example.documentmanagementbackend.model;

public enum Role {
    USERS,
    ADMIN,
    DIRECTOR;

    public static Role fromSignupValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return USERS;
        }

        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "ADMINS" -> ADMIN;
            case "DIRECTOR" -> DIRECTOR;
            case "EMPLOYEE", "USER", "USERS" -> USERS;
            default -> throw new IllegalArgumentException("Invalid role: " + value);
        };
    }
}
