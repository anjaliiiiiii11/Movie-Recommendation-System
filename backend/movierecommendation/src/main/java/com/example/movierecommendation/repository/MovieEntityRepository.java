package com.example.movierecommendation.repository;

import com.example.movierecommendation.entity.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovieEntityRepository extends JpaRepository<MovieEntity, String> {
    Optional<MovieEntity> findByImdbId(String imdbId);

    boolean existsByImdbId(String imdbId);
}

