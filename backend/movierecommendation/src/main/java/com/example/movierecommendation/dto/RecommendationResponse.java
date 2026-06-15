package com.example.movierecommendation.dto;

import java.util.List;

/**
 * Simple API payload for recommendations.
 */
public class RecommendationResponse {

    private String strategy;
    private List<RecommendationMovie> movies;

    public RecommendationResponse() {
    }

    public RecommendationResponse(String strategy, List<RecommendationMovie> movies) {
        this.strategy = strategy;
        this.movies = movies;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public List<RecommendationMovie> getMovies() {
        return movies;
    }

    public void setMovies(List<RecommendationMovie> movies) {
        this.movies = movies;
    }

    public static class RecommendationMovie {
        private String imdbId;
        private String title;
        private String poster;
        private String year;
        private String genre;
        private String director;
        private String actors;

        private Double averageRating;
        private Long ratingCount;

        public RecommendationMovie() {
        }

        public RecommendationMovie(String imdbId, String title, String poster, String year, String genre, String director, String actors) {
            this.imdbId = imdbId;
            this.title = title;
            this.poster = poster;
            this.year = year;
            this.genre = genre;
            this.director = director;
            this.actors = actors;
        }

        public String getImdbId() {
            return imdbId;
        }

        public void setImdbId(String imdbId) {
            this.imdbId = imdbId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPoster() {
            return poster;
        }

        public void setPoster(String poster) {
            this.poster = poster;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getGenre() {
            return genre;
        }

        public void setGenre(String genre) {
            this.genre = genre;
        }




        public void setDirector(String director) {
            this.director = director;
        }

        public String getActors() {
            return actors;
        }


        public void setActors(String actors) {
            this.actors = actors;
        }

        public String getDirector() {
            return director;
        }


        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }

        public Long getRatingCount() {
            return ratingCount;
        }

        public void setRatingCount(Long ratingCount) {
            this.ratingCount = ratingCount;
        }
    }
}

