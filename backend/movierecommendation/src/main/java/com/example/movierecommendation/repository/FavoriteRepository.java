package com.example.movierecommendation.repository;

import com.example.movierecommendation.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserIdAndImdbId(Long userId, String imdbId);

    Optional<Favorite> findByUserIdAndImdbId(Long userId, String imdbId);

    List<Favorite> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}

