package com.example.movierecommendation.controller;

import com.example.movierecommendation.service.RatingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/{imdbId}")
    public Map<String, Object> submit(
            @PathVariable String imdbId,
            @RequestBody Map<String, Object> body,
            Authentication authentication
    ) {
        Object v = body.get("value");
        if (v == null) {
            throw new IllegalArgumentException("value is required");
        }

        int value;
        if (v instanceof Number n) {
            value = n.intValue();
        } else {
            value = Integer.parseInt(String.valueOf(v));
        }

        return ratingService.submitRating(imdbId, value, authentication);
    }

    @GetMapping("/{imdbId}/average")
    public Map<String, Object> average(@PathVariable String imdbId) {
        return ratingService.average(imdbId);
    }

    @GetMapping("/{imdbId}/mine")
    public Map<String, Object> mine(@PathVariable String imdbId, Authentication authentication) {
        return ratingService.mine(imdbId, authentication);
    }
}

