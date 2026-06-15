# TODO

## PHASE 8 — Ratings System
- [x] Backend: Add `Rating` entity (user + imdbId + value + timestamps, unique per user+imdbId)
- [x] Backend: Add `RatingRepository` (find mine, find all by imdbId, avg + count)
- [x] Backend: Add `RatingService` (submit/update rating + compute average)
- [x] Backend: Add `RatingController` endpoints for submit + average (and mine)
- [x] Frontend: Update `RatingStars.jsx` to support selectable stars + read-only display
- [x] Frontend: Add rating API calls in `frontend/src/services/movieService.jsx`
- [x] Frontend: Update `frontend/src/pages/MovieDetails.jsx` to display average rating and allow submitting your rating
- [ ] Testing: Run backend compile/tests and frontend build/lint


