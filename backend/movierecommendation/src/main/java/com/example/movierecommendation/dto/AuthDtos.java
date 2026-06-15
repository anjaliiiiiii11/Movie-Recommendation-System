package com.example.movierecommendation.dto;

public class AuthDtos {

    public record RegisterRequest(String username, String email, String password) {}

    public record LoginRequest(String email, String password) {}

    public record AuthResponse(String token) {}
}

