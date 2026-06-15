// Legacy file removed in favor of MovieService.java (OMDb-only backend)
package com.example.movierecommendation.service;


import com.example.movierecommendation.entity.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final MoviePersistenceService moviePersistenceService;

    public MovieService(
            RestTemplate restTemplate,
            @Value("${omdb.api.key}") String apiKey,
            @Value("${omdb.api.url}") String baseUrl,
            MoviePersistenceService moviePersistenceService) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.moviePersistenceService = moviePersistenceService;
    }


    public List<Movie> searchMovies(String query, int page) {
        String url = baseUrl + "?s=" + query + "&page=" + page + "&apikey=" + apiKey;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"True".equals(response.get("Response"))) {
            return Collections.emptyList();
        }

        List<Movie> movies = new ArrayList<>();
        List<Map> search = (List<Map>) response.get("Search");
        if (search == null) return movies;

        for (Map m : search) {
            Movie movie = mapToMovieBasic(m);
            movies.add(movie);
            // Save searched movies into DB (avoid duplicates)
            moviePersistenceService.saveIfNotExists(movie);
        }

        return movies;
    }

    public Movie getMovieById(String id) {
        String url = baseUrl + "?i=" + id + "&apikey=" + apiKey;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"True".equals(response.get("Response"))) {
            return null;
        }
        Movie movie = mapToFullMovie(response);
        // Save movie details too (safe due to dedup by imdbId)
        moviePersistenceService.saveIfNotExists(movie);
        return movie;

    }

    public List<Movie> getTrendingMovies() {
        // OMDb-only fallback "trending" list by title
        List<String> titles = List.of("Batman", "Titanic", "Avengers", "Inception", "Joker");
        List<Movie> movies = new ArrayList<>();

        for (String title : titles) {
            Movie movie = getMovieByTitle(title);
            if (movie != null) movies.add(movie);
        }

        return movies;
    }

    private Movie getMovieByTitle(String title) {
        String url = baseUrl + "?t=" + title + "&apikey=" + apiKey;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !"True".equals(response.get("Response"))) {
            return null;
        }
        return mapToFullMovie(response);
    }

    private Movie mapToMovieBasic(Map m) {
        Movie movie = new Movie();
        movie.setImdbId((String) m.get("imdbID"));
        movie.setTitle((String) m.get("Title"));
        movie.setYear((String) m.get("Year"));
        movie.setType((String) m.get("Type"));
        movie.setPoster((String) m.get("Poster"));
        return movie;
    }

    @SuppressWarnings("unchecked")
    private Movie mapToFullMovie(Map m) {
        Movie movie = mapToMovieBasic(m);
        movie.setPlot((String) m.get("Plot"));
        movie.setGenre((String) m.get("Genre"));
        movie.setDirector((String) m.get("Director"));
        movie.setActors((String) m.get("Actors"));
        movie.setRuntime((String) m.get("Runtime"));
        movie.setRated((String) m.get("Rated"));
        movie.setLanguage((String) m.get("Language"));
        movie.setBoxOffice((String) m.get("BoxOffice"));
        movie.setAwards((String) m.get("Awards"));
        movie.setImdbRating((String) m.get("imdbRating"));

        // Ratings
        List<Map> ratingsList = (List<Map>) m.get("Ratings");
        if (ratingsList != null) {
            for (Map r : ratingsList) {
                String source = (String) r.get("Source");
                String value = (String) r.get("Value");
                if ("Rotten Tomatoes".equals(source)) {
                    movie.setRottenTomatoes(value);
                } else if ("Metacritic".equals(source)) {
                    movie.setMetacritic(value);
                }
            }
        }
        return movie;
    }
}

