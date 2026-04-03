package com.example.documentmanagementbackend.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {

    private static final Dotenv dotenv = Dotenv.load();

    public static String get(String key) {
        String value = System.getenv(key);
        if (value != null) return value;

        return dotenv.get(key);
    }
}
