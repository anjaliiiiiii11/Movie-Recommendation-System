package com.example.movierecommendation.controller;

import com.example.movierecommendation.dto.RecommendationResponse;
import com.example.movierecommendation.service.RecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/content")
    public RecommendationResponse content(
            @RequestParam String imdbId,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return recommendationService.contentBased(imdbId, limit);
    }

    @GetMapping("/personalized")
    public RecommendationResponse personalized(
            @RequestParam(required = false) String imdbId,
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication
    ) {
        String safeImdb = (imdbId == null || imdbId.isBlank()) ? "" : imdbId;
        if (safeImdb.isBlank()) {
            // no anchor movie provided → requires an anchor for this MVP; fall back to empty list
            return new RecommendationResponse("personalized", java.util.List.of());
        }
        return recommendationService.personalized(safeImdb, limit, authentication);
    }

    @GetMapping("/collaborative")
    public RecommendationResponse collaborative(
            @RequestParam(required = false) String imdbId,
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication
    ) {
        String safeImdb = (imdbId == null || imdbId.isBlank()) ? "" : imdbId;
        if (safeImdb.isBlank()) {
            return new RecommendationResponse("collaborative", java.util.List.of());
        }
        return recommendationService.collaborative(safeImdb, limit, authentication);
    }
}

