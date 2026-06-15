# Phase 9 — Recommendation Engine (Content + Personalized + Collaborative)

## Step 1: Backend content-based recommendations
- [x] Create `RecommendationService` with content-based scoring using `MovieEntity` fields: genre/director/actors (keywords not available → 0 contribution)
- [x] Create `RecommendationController` endpoints (public) e.g. `/api/recommendations/content?imdbId=...&limit=...`
- [x] Ensure candidates are persisted `MovieEntity` records; exclude the source movie
- [x] Boost/sort using rating average from `RatingRepository`


## Step 2: Backend personalized recommendations
- [x] Extend service to compute user preference profile from `Favorite` + `Rating` (already implemented)
- [x] Add endpoint (auth required) e.g. `/api/recommendations/personalized?imdbId=...&limit=...`
- [x] If not enough interaction data, fall back to content-based


## Step 3: Backend collaborative filtering
- [ ] Implement collaborative user similarity (cosine on centered ratings) using `RatingRepository`
- [ ] Add endpoint (auth required) e.g. `/api/recommendations/collaborative?imdbId=...&limit=...`
- [ ] If too few co-ratings, fall back to personalized

## Step 4: Frontend wiring
- [ ] Add recommendation API calls to `frontend/src/services/movieService.jsx`
- [ ] Update `frontend/src/pages/MovieDetails.jsx` to fetch and render recommendations instead of `movies={[]}`

## Step 5: QA / build
- [ ] Run backend compile/tests
- [ ] Run frontend build/lint
- [ ] Smoke test endpoints + UI rendering

