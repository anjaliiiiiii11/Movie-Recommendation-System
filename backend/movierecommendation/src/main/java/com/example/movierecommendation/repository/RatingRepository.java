package com.example.movierecommendation.repository;

import com.example.movierecommendation.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByUserIdAndImdbId(Long userId, String imdbId);

    List<Rating> findAllByImdbId(String imdbId);

    @Query("select avg(r.value) from Rating r where r.imdbId = :imdbId")
    Double avgValueByImdbId(@Param("imdbId") String imdbId);

    @Query("select count(r) from Rating r where r.imdbId = :imdbId")
    Long countByImdbId(@Param("imdbId") String imdbId);

    boolean existsByUserIdAndImdbId(Long userId, String imdbId);
}

