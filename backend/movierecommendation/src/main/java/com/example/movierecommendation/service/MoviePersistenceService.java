package com.example.movierecommendation.service;

import com.example.movierecommendation.entity.Movie;
import com.example.movierecommendation.entity.MovieEntity;
import com.example.movierecommendation.repository.MovieEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoviePersistenceService {

    private final MovieEntityRepository movieEntityRepository;

    public MoviePersistenceService(MovieEntityRepository movieEntityRepository) {
        this.movieEntityRepository = movieEntityRepository;
    }

    @Transactional
    public void saveIfNotExists(Movie movie) {
        if (movie == null) return;
        if (movie.getImdbId() == null || movie.getImdbId().isBlank()) return;

        // Avoid duplicates by imdbId unique identity
        if (movieEntityRepository.existsByImdbId(movie.getImdbId())) {
            return;
        }

        MovieEntity entity = map(movie);
        movieEntityRepository.save(entity);
    }

    @Transactional
    public void saveOrUpdate(Movie movie) {
        if (movie == null) return;
        if (movie.getImdbId() == null || movie.getImdbId().isBlank()) return;

        MovieEntity existing = movieEntityRepository.findByImdbId(movie.getImdbId()).orElse(null);
        if (existing == null) {
            movieEntityRepository.save(map(movie));
            return;
        }

        apply(existing, movie);
        movieEntityRepository.save(existing);
    }

    private MovieEntity map(Movie movie) {
        MovieEntity e = new MovieEntity(movie.getImdbId());
        apply(e, movie);
        return e;

    }

    private void apply(MovieEntity target, Movie source) {

        target.setTitle(source.getTitle());
        target.setYear(source.getYear());
        target.setType(source.getType());
        target.setPoster(source.getPoster());
        target.setPlot(source.getPlot());
        target.setGenre(source.getGenre());
        target.setDirector(source.getDirector());
        target.setActors(source.getActors());
        target.setRuntime(source.getRuntime());
        target.setImdbRating(source.getImdbRating());
        target.setRottenTomatoes(source.getRottenTomatoes());
        target.setMetacritic(source.getMetacritic());
        target.setBoxOffice(source.getBoxOffice());
        target.setRated(source.getRated());
        target.setLanguage(source.getLanguage());
        target.setAwards(source.getAwards());
    }
}

