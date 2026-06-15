package com.example.movierecommendation.entity;

/**
 * Entity removed for OMDb-only backend.
 * Kept as plain POJO for JSON serialization.
 */
public class Movie {

    private String imdbId;


    private String title;

    private String year;

    private String type;

    private String poster;

    private String plot;

    private String genre;

    private String director;

    private String actors;

    private String runtime;

    private String imdbRating;

    private String rottenTomatoes;

    private String metacritic;

    private String boxOffice;

    private String rated;

    private String language;

    private String awards;

    // user/favorites removed for OMDb-only backend


    // Constructors
    public Movie() {}

    // Getters and Setters
    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    public String getRottenTomatoes() { return rottenTomatoes; }
    public void setRottenTomatoes(String rottenTomatoes) { this.rottenTomatoes = rottenTomatoes; }

    public String getMetacritic() { return metacritic; }
    public void setMetacritic(String metacritic) { this.metacritic = metacritic; }

    public String getBoxOffice() { return boxOffice; }
    public void setBoxOffice(String boxOffice) { this.boxOffice = boxOffice; }

    public String getRated() { return rated; }
    public void setRated(String rated) { this.rated = rated; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getAwards() { return awards; }
    public void setAwards(String awards) { this.awards = awards; }

    public User getUser() { return null; }
    public void setUser(User user) { }
}


