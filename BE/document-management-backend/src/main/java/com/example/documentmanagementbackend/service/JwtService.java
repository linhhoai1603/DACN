package com.example.documentmanagementbackend.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {
    String generateToken(UserDetails user);

    String generateToken(String username);

    String extractUsername(String token);

    <T> T extractClaims(String token, Function<Claims, T> claimResolver);

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractJTI(String token);

    boolean isTokenExpired(String token);



}



