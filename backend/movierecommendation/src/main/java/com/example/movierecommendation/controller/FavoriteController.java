package com.example.movierecommendation.controller;

import com.example.movierecommendation.entity.Favorite;
import com.example.movierecommendation.entity.MovieEntity;
import com.example.movierecommendation.service.FavoriteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{imdbId}")
    public Map<String, String> add(@PathVariable String imdbId, Authentication authentication) {
        favoriteService.addFavorite(imdbId, authentication);
        return Map.of("status", "ok");
    }

    @DeleteMapping("/{imdbId}")
    public Map<String, String> remove(@PathVariable String imdbId, Authentication authentication) {
        favoriteService.removeFavorite(imdbId, authentication);
        return Map.of("status", "ok");
    }

    @GetMapping
    public List<MovieEntity> list(Authentication authentication) {
        return favoriteService.listFavorites(authentication);
    }

    @GetMapping("/{imdbId}/exists")
    public Map<String, Boolean> exists(@PathVariable String imdbId, Authentication authentication) {
        return Map.of("exists", favoriteService.isFavorite(imdbId, authentication));
    }
}

