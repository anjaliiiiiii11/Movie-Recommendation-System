package com.example.movierecommendation.service;

import com.example.movierecommendation.entity.Favorite;
import com.example.movierecommendation.entity.MovieEntity;
import com.example.movierecommendation.entity.User;
import com.example.movierecommendation.repository.FavoriteRepository;
import com.example.movierecommendation.repository.MovieEntityRepository;
import com.example.movierecommendation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieEntityRepository movieEntityRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            UserRepository userRepository,
            MovieEntityRepository movieEntityRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.movieEntityRepository = movieEntityRepository;
    }

    private User requireUser(Authentication authentication) {
        String subject = authentication.getName();

        // JWT subject is expected to be email, but be defensive:
        return userRepository.findByEmail(subject)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }


    @Transactional
    public void addFavorite(String imdbId, Authentication authentication) {
        if (imdbId == null || imdbId.isBlank()) return;
        User user = requireUser(authentication);

        if (favoriteRepository.existsByUserIdAndImdbId(user.getId(), imdbId)) {
            return;
        }

        // Ensure movie exists in persistence layer (movies are persisted from OMDb searches/details)
        Optional<MovieEntity> movie = movieEntityRepository.findByImdbId(imdbId);
        if (movie.isEmpty()) {
            // Keep behavior lenient: allow favorite creation even if movie not persisted yet.
            // Frontend can still show title/poster once the movie is fetched.
        }

        favoriteRepository.save(new Favorite(user, imdbId));
    }

    @Transactional
    public void removeFavorite(String imdbId, Authentication authentication) {
        if (imdbId == null || imdbId.isBlank()) return;
        User user = requireUser(authentication);

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndImdbId(user.getId(), imdbId);
        existing.ifPresent(favoriteRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<MovieEntity> listFavorites(Authentication authentication) {
        User user = requireUser(authentication);
        // Return persisted movies for display
        return favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(f -> movieEntityRepository.findByImdbId(f.getImdbId()).orElse(null))
                .filter(m -> m != null)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String imdbId, Authentication authentication) {

        if (imdbId == null || imdbId.isBlank()) return false;
        User user = requireUser(authentication);
        return favoriteRepository.existsByUserIdAndImdbId(user.getId(), imdbId);
    }
}

