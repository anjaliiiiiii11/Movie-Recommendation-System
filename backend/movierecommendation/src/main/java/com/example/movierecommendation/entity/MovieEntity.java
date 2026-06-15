package com.example.movierecommendation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class MovieEntity {

    @Id
    @Column(name = "imdb_id", nullable = false, length = 32)
    private String imdbId;

    @Column(name = "title")
    private String title;

    @Column(name = "year")
    private String year;

    @Column(name = "type")
    private String type;

    @Column(name = "poster")
    private String poster;

    @Column(name = "plot", columnDefinition = "TEXT")
    private String plot;

    @Column(name = "genre")
    private String genre;

    @Column(name = "director")
    private String director;

    @Column(name = "actors", columnDefinition = "TEXT")
    private String actors;

    @Column(name = "runtime")
    private String runtime;

    @Column(name = "imdb_rating")
    private String imdbRating;

    @Column(name = "rotten_tomatoes")
    private String rottenTomatoes;

    @Column(name = "metacritic")
    private String metacritic;

    @Column(name = "box_office")
    private String boxOffice;

    @Column(name = "rated")
    private String rated;

    @Column(name = "language")
    private String language;

    @Column(name = "awards", columnDefinition = "TEXT")
    private String awards;

    protected MovieEntity() {
    }

    public MovieEntity(String imdbId) {
        this.imdbId = imdbId;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDirector() {
        return director;
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

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
    }

    public String getRottenTomatoes() {
        return rottenTomatoes;
    }

    public void setRottenTomatoes(String rottenTomatoes) {
        this.rottenTomatoes = rottenTomatoes;
    }

    public String getMetacritic() {
        return metacritic;
    }

    public void setMetacritic(String metacritic) {
        this.metacritic = metacritic;
    }

    public String getBoxOffice() {
        return boxOffice;
    }

    public void setBoxOffice(String boxOffice) {
        this.boxOffice = boxOffice;
    }

    public String getRated() {
        return rated;
    }

    public void setRated(String rated) {
        this.rated = rated;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAwards() {
        return awards;
    }

    public void setAwards(String awards) {
        this.awards = awards;
    }
}

