package com.example.movierecommendation.dto;

import java.util.List;

public class AdminAnalyticsResponse {

    public record MovieCount(String imdbId, String title, String poster, long count) {}

    public record GenreCount(String genre, long count) {}

    public record UserCount(String email, String username, long count) {}

    public record Analytics(
            List<MovieCount> mostSearchedMovies,
            List<UserCount> mostActiveUsers,
            List<GenreCount> topGenres
    ) {}

    private final String type;
    private final Analytics data;

    public AdminAnalyticsResponse(String type, Analytics data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Analytics getData() {
        return data;
    }
}

