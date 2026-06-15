package com.example.movierecommendation.service;

import com.example.movierecommendation.dto.RecommendationResponse;
import com.example.movierecommendation.dto.RecommendationResponse.RecommendationMovie;
import com.example.movierecommendation.entity.Favorite;
import com.example.movierecommendation.entity.MovieEntity;
import com.example.movierecommendation.entity.Rating;
import com.example.movierecommendation.entity.User;
import com.example.movierecommendation.repository.FavoriteRepository;
import com.example.movierecommendation.repository.MovieEntityRepository;
import com.example.movierecommendation.repository.RatingRepository;
import com.example.movierecommendation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecommendationService {

    private final MovieEntityRepository movieEntityRepository;
    private final FavoriteRepository favoriteRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    public RecommendationService(
            MovieEntityRepository movieEntityRepository,
            FavoriteRepository favoriteRepository,
            RatingRepository ratingRepository,
            UserRepository userRepository
    ) {
        this.movieEntityRepository = movieEntityRepository;
        this.favoriteRepository = favoriteRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse contentBased(String imdbId, int limit) {
        MovieEntity base = movieEntityRepository.findByImdbId(imdbId).orElse(null);
        if (base == null) {
            return new RecommendationResponse("content", List.of());
        }

        String baseGenre = safe(base.getGenre());
        String baseDirector = safe(base.getDirector());
        String baseActors = safe(base.getActors());

        Set<String> baseGenreTokens = tokenizeCommaPipeColonDash(baseGenre);
        Set<String> baseDirectorTokens = tokenizeCommaPipeColonDash(baseDirector);
        Set<String> baseActorTokens = tokenizeCommaPipeColonDash(baseActors);

        List<MovieEntity> candidates = movieEntityRepository.findAll();
        List<ScoredMovie> scored = new ArrayList<>();

        for (MovieEntity c : candidates) {
            if (c == null) continue;
            if (c.getImdbId() == null) continue;
            if (c.getImdbId().equals(imdbId)) continue;

            Set<String> cGenreTokens = tokenizeCommaPipeColonDash(safe(c.getGenre()));
            Set<String> cDirectorTokens = tokenizeCommaPipeColonDash(safe(c.getDirector()));
            Set<String> cActorTokens = tokenizeCommaPipeColonDash(safe(c.getActors()));

            double genreScore = jaccard(baseGenreTokens, cGenreTokens);
            double directorScore = jaccard(baseDirectorTokens, cDirectorTokens);
            double actorScore = jaccard(baseActorTokens, cActorTokens);

            double score = (genreScore * 0.45) + (directorScore * 0.25) + (actorScore * 0.30);

            // boost slightly with rating
            Double avg = ratingRepository.avgValueByImdbId(c.getImdbId());
            Long count = ratingRepository.countByImdbId(c.getImdbId());
            double ratingBoost = (avg == null ? 0.0 : avg / 10.0) * (count == null ? 0.0 : Math.min(1.0, count / 20.0));

            score += ratingBoost;

            scored.add(new ScoredMovie(c, score, avg, count));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<RecommendationMovie> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            ScoredMovie sm = scored.get(i);
            out.add(toMovie(sm.movie, sm.avg, sm.count));
        }

        return new RecommendationResponse("content", out);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse personalized(String imdbId, int limit, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return contentBased(imdbId, limit);
        }

        User user = requireUser(authentication);
        if (user == null) {
            return contentBased(imdbId, limit);
        }

        // Build preference weights from favorites + ratings
        // Higher-rated movies contribute more weight.
        Map<String, Double> genreW = new HashMap<>();
        Map<String, Double> directorW = new HashMap<>();
        Map<String, Double> actorW = new HashMap<>();

        // Favorites
        List<Favorite> favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());
        for (Favorite f : favorites) {
            if (f == null || f.getImdbId() == null) continue;
            movieEntityRepository.findByImdbId(f.getImdbId()).ifPresent(m -> {
                addWeights(m, 1.0, genreW, directorW, actorW);
            });
        }

        // Ratings
        // (Prefer movies that user rated, and weight by (value/10))
        // Note: repo doesn't have "find all by user", so we use all ratings by scanning candidates from DB.
        // For performance on small datasets this is fine.
        List<Rating> ratings = new ArrayList<>();
        // crude approach: collect by imdbId in DB + check if user rated it
        // If your dataset is large, add a repository query.
        List<MovieEntity> allMovies = movieEntityRepository.findAll();
        for (MovieEntity m : allMovies) {
            if (m == null || m.getImdbId() == null) continue;
            Optional<Rating> r = ratingRepository.findByUserIdAndImdbId(user.getId(), m.getImdbId());
            if (r.isPresent()) ratings.add(r.get());
        }

        for (Rating r : ratings) {
            double weight = Math.max(0.1, r.getValue() / 10.0);
            movieEntityRepository.findByImdbId(r.getImdbId()).ifPresent(m -> {
                addWeights(m, weight, genreW, directorW, actorW);
            });
        }

        // If we have too little interaction data, fall back to content-based.
        // Threshold is intentionally small for the Phase 9 MVP.
        int totalInteractionSignals = favorites.size() + ratings.size();
        boolean hasAnyPreference = !(genreW.isEmpty() && directorW.isEmpty() && actorW.isEmpty());
        if (!hasAnyPreference || totalInteractionSignals < 3) {
            return contentBased(imdbId, limit);
        }


        // Score candidates
        List<MovieEntity> candidates = movieEntityRepository.findAll();
        List<ScoredMovie> scored = new ArrayList<>();

        for (MovieEntity c : candidates) {
            if (c == null || c.getImdbId() == null) continue;
            if (imdbId != null && imdbId.equals(c.getImdbId())) continue;

            double genreScore = weightedOverlap(tokenizeCommaPipeColonDash(safe(c.getGenre())), genreW);
            double directorScore = weightedOverlap(tokenizeCommaPipeColonDash(safe(c.getDirector())), directorW);
            double actorScore = weightedOverlap(tokenizeCommaPipeColonDash(safe(c.getActors())), actorW);

            double score = (genreScore * 0.50) + (directorScore * 0.20) + (actorScore * 0.30);

            Double avg = ratingRepository.avgValueByImdbId(c.getImdbId());
            Long count = ratingRepository.countByImdbId(c.getImdbId());
            double ratingBoost = (avg == null ? 0.0 : avg / 10.0) * (count == null ? 0.0 : Math.min(1.0, count / 20.0));

            score += ratingBoost;

            scored.add(new ScoredMovie(c, score, avg, count));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<RecommendationMovie> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            ScoredMovie sm = scored.get(i);
            out.add(toMovie(sm.movie, sm.avg, sm.count));
        }

        return new RecommendationResponse("personalized", out);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse collaborative(String imdbId, int limit, Authentication authentication) {
        // Simple collaborative: if we have no auth signals, fall back.
        if (authentication == null || !authentication.isAuthenticated()) {
            return contentBased(imdbId, limit);
        }
        User user = requireUser(authentication);
        if (user == null) return contentBased(imdbId, limit);

        // Find other users that rated at least N common movies.
        // No repository method exists for "find all by user", so we scan ratings by scanning all movies.
        // This is intentionally simple for the Phase 9 MVP.

        List<MovieEntity> movies = movieEntityRepository.findAll();
        Set<String> targetTokens = null;

        // If user has no ratings, fallback.
        List<Rating> myRatings = new ArrayList<>();
        for (MovieEntity m : movies) {
            if (m == null || m.getImdbId() == null) continue;
            ratingRepository.findByUserIdAndImdbId(user.getId(), m.getImdbId()).ifPresent(myRatings::add);
        }

        if (myRatings.isEmpty()) {
            return personalized(imdbId, limit, authentication);
        }

        Map<String, Double> myMovieRatings = new HashMap<>();
        for (Rating r : myRatings) myMovieRatings.put(r.getImdbId(), (double) r.getValue());

        // Discover candidate other users by scanning ratings for each imdbId in DB
        // We only have per-(user,imdbId) queries, but we can discover user ids from User table.
        // For MVP, we get all users and compute similarity via their co-rated movies.
        List<User> users = userRepository.findAll();
        Map<Long, Double> userSim = new HashMap<>();

        for (User other : users) {
            if (other == null) continue;
            if (other.getId() == null || other.getId().equals(user.getId())) continue;

            double sim = cosineSimilarityOnCoRatedMovies(user.getId(), other.getId(), movies, myMovieRatings);
            if (sim > 0.0) userSim.put(other.getId(), sim);
        }

        if (userSim.isEmpty()) {
            return personalized(imdbId, limit, authentication);
        }

        // Score each candidate movie not already rated by the user
        List<ScoredMovie> scored = new ArrayList<>();

        for (MovieEntity c : movies) {
            if (c == null || c.getImdbId() == null) continue;
            if (imdbId != null && imdbId.equals(c.getImdbId())) continue;
            if (myMovieRatings.containsKey(c.getImdbId())) continue;

            double weightedSum = 0.0;
            double simSum = 0.0;

            for (Map.Entry<Long, Double> e : userSim.entrySet()) {
                Long otherId = e.getKey();
                double sim = e.getValue();
                Optional<Rating> otherRating = ratingRepository.findByUserIdAndImdbId(otherId, c.getImdbId());
                if (otherRating.isEmpty()) continue;

                weightedSum += sim * otherRating.get().getValue();
                simSum += Math.abs(sim);
            }

            double predicted = (simSum == 0.0) ? 0.0 : (weightedSum / simSum);

            Double avg = ratingRepository.avgValueByImdbId(c.getImdbId());
            Long count = ratingRepository.countByImdbId(c.getImdbId());

            double score = predicted + (avg == null ? 0.0 : avg / 2.0);
            scored.add(new ScoredMovie(c, score, avg, count));
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));

        List<RecommendationMovie> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, scored.size()); i++) {
            ScoredMovie sm = scored.get(i);
            out.add(toMovie(sm.movie, sm.avg, sm.count));
        }

        return new RecommendationResponse("collaborative", out);
    }

    // ---------------- helpers ----------------

    private User requireUser(Authentication authentication) {
        String subject = authentication.getName();
        return userRepository.findByEmail(subject).orElse(null);
    }

    private void addWeights(MovieEntity m, double weight, Map<String, Double> genreW, Map<String, Double> directorW, Map<String, Double> actorW) {
        tokenizeCommaPipeColonDash(safe(m.getGenre())).forEach(t -> genreW.merge(t, weight, Double::sum));
        tokenizeCommaPipeColonDash(safe(m.getDirector())).forEach(t -> directorW.merge(t, weight, Double::sum));
        tokenizeCommaPipeColonDash(safe(m.getActors())).forEach(t -> actorW.merge(t, weight, Double::sum));
    }

    private double weightedOverlap(Set<String> tokens, Map<String, Double> weights) {
        if (tokens.isEmpty() || weights.isEmpty()) return 0.0;
        double sum = 0.0;
        for (String t : tokens) {
            Double w = weights.get(t);
            if (w != null) sum += w;
        }
        return sum / (tokens.size() + 1.0);
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        int inter = 0;
        for (String x : a) if (b.contains(x)) inter++;
        int union = a.size() + b.size() - inter;
        return union == 0 ? 0.0 : ((double) inter / union);
    }

    private Set<String> tokenizeCommaPipeColonDash(String s) {
        if (s == null) return Set.of();
        String cleaned = s.trim();
        if (cleaned.isEmpty() || "N/A".equalsIgnoreCase(cleaned)) return Set.of();

        // OMDb uses comma-separated for actors/genres.
        // Director is usually one token, but we still split.
        String[] parts = cleaned.split("[,|:/\\-]\\s*");
        Set<String> out = new HashSet<>();
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty() || "N/A".equalsIgnoreCase(t)) continue;
            if (t.length() < 2) continue;
            // normalize
            out.add(t.toLowerCase(Locale.ROOT));
        }
        return out;
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }

    private RecommendationMovie toMovie(MovieEntity m, Double avg, Long count) {
        RecommendationMovie rm = new RecommendationMovie();
        rm.setImdbId(m.getImdbId());
        rm.setTitle(m.getTitle());
        rm.setPoster(m.getPoster());
        rm.setYear(m.getYear());
        rm.setGenre(m.getGenre());
        rm.setDirector(m.getDirector());
        rm.setActors(m.getActors());
        rm.setAverageRating(avg == null ? 0.0 : avg);
        rm.setRatingCount(count == null ? 0L : count);
        return rm;
    }

    private double cosineSimilarityOnCoRatedMovies(Long userIdA, Long userIdB, List<MovieEntity> movies, Map<String, Double> aRatings) {
        // build vectors for co-rated movies
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        for (MovieEntity m : movies) {
            if (m == null || m.getImdbId() == null) continue;
            Double a = aRatings.get(m.getImdbId());
            if (a == null) continue;
            Optional<Rating> br = ratingRepository.findByUserIdAndImdbId(userIdB, m.getImdbId());
            if (br.isEmpty()) continue;
            xs.add(a);
            ys.add((double) br.get().getValue());
        }

        if (xs.size() < 2) return 0.0;

        // center ratings
        double meanX = xs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double meanY = ys.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double dot = 0.0;
        double normX = 0.0;
        double normY = 0.0;

        for (int i = 0; i < xs.size(); i++) {
            double x = xs.get(i) - meanX;
            double y = ys.get(i) - meanY;
            dot += x * y;
            normX += x * x;
            normY += y * y;
        }

        double denom = Math.sqrt(normX) * Math.sqrt(normY);
        if (denom == 0.0) return 0.0;
        return dot / denom;
    }

    private static class ScoredMovie {
        private final MovieEntity movie;
        private final double score;
        private final Double avg;
        private final Long count;

        private ScoredMovie(MovieEntity movie, double score, Double avg, Long count) {
            this.movie = movie;
            this.score = score;
            this.avg = avg;
            this.count = count;
        }
    }
}

