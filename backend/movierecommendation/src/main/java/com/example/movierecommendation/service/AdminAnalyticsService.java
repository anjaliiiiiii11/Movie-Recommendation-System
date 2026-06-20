package com.example.movierecommendation.service;

import com.example.movierecommendation.dto.AdminAnalyticsResponse;

import com.example.movierecommendation.dto.AdminAnalyticsResponse.GenreCount;
import com.example.movierecommendation.dto.AdminAnalyticsResponse.MovieCount;
import com.example.movierecommendation.dto.AdminAnalyticsResponse.UserCount;

import com.example.movierecommendation.entity.Favorite;
import com.example.movierecommendation.entity.MovieEntity;
import com.example.movierecommendation.entity.Rating;
import com.example.movierecommendation.entity.User;
import com.example.movierecommendation.repository.FavoriteRepository;
import com.example.movierecommendation.repository.MovieEntityRepository;
import com.example.movierecommendation.repository.RatingRepository;
import com.example.movierecommendation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    private static final String DEFAULT_ADMIN_EMAIL = "anjali.kumari01107@gmail.com";

    private final MovieEntityRepository movieEntityRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public AdminAnalyticsService(
            MovieEntityRepository movieEntityRepository,
            FavoriteRepository favoriteRepository,
            RatingRepository ratingRepository,
            UserRepository userRepository
    ) {
        this.movieEntityRepository = movieEntityRepository;
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Authentication required");
        }
        String email = authentication.getName();
        if (email == null || !email.equalsIgnoreCase(DEFAULT_ADMIN_EMAIL)) {
            throw new IllegalArgumentException("Admin only");
        }
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse analytics(Authentication authentication, int limit) {
        requireAdmin(authentication);

        List<MovieCount> mostSearched = mostSearchedMovies(limit);
        List<UserCount> mostActive = mostActiveUsers(limit);
        List<GenreCount> topGenres = topGenres(limit);

        return new AdminAnalyticsResponse(
                "analytics",
                new AdminAnalyticsResponse.Analytics(mostSearched, mostActive, topGenres)
        );
    }

    @Transactional(readOnly = true)
    public List<MovieCount> mostSearchedMovies(int limit) {
        // MVP proxy: search popularity = number of favorites + number of ratings per movie
        List<Favorite> favorites = favoriteRepository.findAll();
        List<Rating> ratings = ratingRepository.findAll();
        Map<String, Long> favCountByImdb = new HashMap<>();
        for (Favorite f : favorites) {
            if (f == null || f.getImdbId() == null) continue;
            favCountByImdb.merge(f.getImdbId(), 1L, Long::sum);
        }

        Map<String, Long> ratingCountByImdb = new HashMap<>();
        for (Rating r : ratings) {
            if (r == null || r.getImdbId() == null) continue;
            ratingCountByImdb.merge(r.getImdbId(), 1L, Long::sum);
        }

        // Build combined score
        Map<String, Long> scoreByImdb = new HashMap<>();
        for (Map.Entry<String, Long> e : favCountByImdb.entrySet()) {
            scoreByImdb.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Long> e : ratingCountByImdb.entrySet()) {
            scoreByImdb.merge(e.getKey(), e.getValue(), Long::sum);
        }

        // Map to titles/posters
        List<MovieEntity> movies = movieEntityRepository.findAll();
        Map<String, MovieEntity> movieByImdb = movies.stream()
                .filter(Objects::nonNull)
                .filter(m -> m.getImdbId() != null)
                .collect(Collectors.toMap(MovieEntity::getImdbId, m -> m, (a, b) -> a));

        return scoreByImdb.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(Math.max(0, limit))
                .map(e -> {
                    MovieEntity m = movieByImdb.get(e.getKey());
                    String title = m != null ? m.getTitle() : e.getKey();
                    String poster = m != null ? m.getPoster() : null;
                    return new MovieCount(e.getKey(), title, poster, e.getValue());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserCount> mostActiveUsers(int limit) {
        // MVP proxy: activity = favorites added + ratings submitted
        List<Favorite> favorites = favoriteRepository.findAll();
        List<Rating> ratings = ratingRepository.findAll();

        Map<Long, Long> favByUser = new HashMap<>();
        for (Favorite f : favorites) {
            if (f == null || f.getUser() == null) continue;
            favByUser.merge(f.getUser().getId(), 1L, Long::sum);
        }

        Map<Long, Long> ratingsByUser = new HashMap<>();
        for (Rating r : ratings) {
            if (r == null || r.getUser() == null) continue;
            ratingsByUser.merge(r.getUser().getId(), 1L, Long::sum);
        }

        Map<Long, Long> scoreByUserId = new HashMap<>();
        for (Map.Entry<Long, Long> e : favByUser.entrySet()) scoreByUserId.put(e.getKey(), e.getValue());
        for (Map.Entry<Long, Long> e : ratingsByUser.entrySet()) scoreByUserId.merge(e.getKey(), e.getValue(), Long::sum);

        List<User> users = userRepository.findAll();
        Map<Long, User> userById = users.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        return scoreByUserId.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(Math.max(0, limit))
                .map(e -> {
                    User u = userById.get(e.getKey());
                    String email = u != null ? u.getEmail() : String.valueOf(e.getKey());
                    String username = u != null ? u.getUsername() : null;
                    return new UserCount(email, username, e.getValue());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenreCount> topGenres(int limit) {
        // Count genre tokens across persisted movies
        List<MovieEntity> movies = movieEntityRepository.findAll();
        Map<String, Long> counts = new HashMap<>();

        for (MovieEntity m : movies) {
            if (m == null) continue;
            String genre = m.getGenre();
            if (genre == null || genre.isBlank() || "N/A".equalsIgnoreCase(genre)) continue;

            // OMDb genres are comma-separated
            String[] parts = genre.split(",");
            for (String p : parts) {
                String g = p == null ? null : p.trim();
                if (g == null || g.isBlank() || "N/A".equalsIgnoreCase(g)) continue;
                g = g.toLowerCase(Locale.ROOT);
                counts.merge(g, 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(Math.max(0, limit))
                .map(e -> new GenreCount(capitalize(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return s;
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}

