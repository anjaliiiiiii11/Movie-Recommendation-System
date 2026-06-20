package com.example.movierecommendation.controller;

import com.example.movierecommendation.dto.AdminAnalyticsResponse;
import com.example.movierecommendation.service.AdminAnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService service;

    public AdminAnalyticsController(AdminAnalyticsService service) {
        this.service = service;
    }

    @GetMapping
    public AdminAnalyticsResponse analytics(
            Authentication authentication,
            @RequestParam(defaultValue = "6") int limit
    ) {
        return service.analytics(authentication, limit);
    }
}

