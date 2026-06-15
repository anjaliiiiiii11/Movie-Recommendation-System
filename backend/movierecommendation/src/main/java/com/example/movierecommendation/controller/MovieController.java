// Legacy file removed in favor of MovieController.java (OMDb-only backend)
package com.example.movierecommendation.controller;


import com.example.movierecommendation.entity.Movie;
import com.example.movierecommendation.service.MovieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MovieController {

    private final MovieService service;

    public MovieController(MovieService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(service.getTrendingMovies());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        return ResponseEntity.ok(service.searchMovies(query, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable String id) {
        Movie movie = service.getMovieById(id);
        if (movie != null) {
            return ResponseEntity.ok(movie);
        }
        return ResponseEntity.notFound().build();
    }
}