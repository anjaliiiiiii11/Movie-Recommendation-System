package com.example.movierecommendation.service;

import com.example.movierecommendation.entity.Rating;
import com.example.movierecommendation.entity.User;
import com.example.movierecommendation.repository.RatingRepository;
import com.example.movierecommendation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    private User requireUser(Authentication authentication) {
        String subject = authentication.getName();
        return userRepository.findByEmail(subject)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public Map<String, Object> submitRating(String imdbId, int value, Authentication authentication) {
        if (imdbId == null || imdbId.isBlank()) {
            throw new IllegalArgumentException("imdbId is required");
        }
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException("value must be between 1 and 10");
        }

        User user = requireUser(authentication);

        Optional<Rating> existing = ratingRepository.findByUserIdAndImdbId(user.getId(), imdbId);
        Rating rating = existing.orElseGet(() -> new Rating(user, imdbId, value));

        rating.setValue(value);


        Rating saved = ratingRepository.save(rating);

        return Map.of(
                "imdbId", saved.getImdbId(),
                "value", saved.getValue(),
                "status", "ok"
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> average(String imdbId) {
        if (imdbId == null || imdbId.isBlank()) {
            throw new IllegalArgumentException("imdbId is required");
        }

        Double avg = ratingRepository.avgValueByImdbId(imdbId);
        Long count = ratingRepository.countByImdbId(imdbId);

        double safeAvg = (avg == null) ? 0.0d : avg;

        return Map.of(
                "imdbId", imdbId,
                "average", safeAvg,
                "count", count
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> mine(String imdbId, Authentication authentication) {
        if (imdbId == null || imdbId.isBlank()) {
            throw new IllegalArgumentException("imdbId is required");
        }

        User user = requireUser(authentication);
        return ratingRepository.findByUserIdAndImdbId(user.getId(), imdbId)
                .<Map<String, Object>>map(r -> Map.of(
                        "imdbId", imdbId,
                        "value", r.getValue(),
                        "rated", true
                ))
                .orElseGet(() -> Map.of(
                        "imdbId", imdbId,
                        "rated", false
                ));
    }
}

