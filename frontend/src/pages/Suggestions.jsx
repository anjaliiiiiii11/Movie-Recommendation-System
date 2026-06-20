import { useState } from 'react';
import MovieCard from '../components/MovieCard';
import Loader from '../components/Loader';
import { movieService } from '../services/movieService';

const moodOptions = [
  { value: 'happy', label: 'Happy' },
  { value: 'sad', label: 'Sad' },
  { value: 'adventurous', label: 'Adventurous' },
  { value: 'romantic', label: 'Romantic' },
  { value: 'thriller', label: 'Thriller' },
  { value: 'any', label: 'Any' },
];

const genreOptions = [
  { value: 'any', label: 'Any' },
  { value: 'action', label: 'Action' },
  { value: 'comedy', label: 'Comedy' },
  { value: 'drama', label: 'Drama' },
  { value: 'horror', label: 'Horror' },
  { value: 'romance', label: 'Romance' },
  { value: 'sci-fi', label: 'Sci-Fi' },
  { value: 'fantasy', label: 'Fantasy' },
];

const runtimeOptions = [
  { value: 'any', label: 'Any length' },
  { value: 'under90', label: 'Under 90 minutes' },
  { value: '90to120', label: '90 - 120 minutes' },
  { value: 'over120', label: 'Over 120 minutes' },
];

function getMoodSearchQuery(mood) {
  switch (mood) {
    case 'happy':
      return 'comedy movie';
    case 'sad':
      return 'drama movie';
    case 'adventurous':
      return 'adventure movie';
    case 'romantic':
      return 'romance movie';
    case 'thriller':
      return 'thriller movie';
    default:
      return 'movie';
  }
}

function matchesMoodFilter(movie, mood) {
  if (mood === 'any') return true;
  const genre = (movie.Genre ?? movie.genre ?? '').toLowerCase();
  const plot = (movie.Plot ?? movie.plot ?? '').toLowerCase();

  if (mood === 'happy') {
    return genre.includes('comedy') || plot.includes('funny') || plot.includes('humor');
  }

  if (mood === 'sad') {
    return (
      genre.includes('drama') ||
      plot.includes('tragic') ||
      plot.includes('death') ||
      plot.includes('loss') ||
      plot.includes('heartbreak') ||
      plot.includes('sacrifice')
    );
  }

  if (mood === 'adventurous') {
    return genre.includes('fantasy') || genre.includes('adventure') || plot.includes('adventure');
  }

  if (mood === 'romantic') {
    return genre.includes('romance') || plot.includes('love') || plot.includes('relationship');
  }

  if (mood === 'thriller') {
    return genre.includes('thriller') || plot.includes('suspense') || plot.includes('mystery');
  }

  return true;
}

function parseRuntime(value) {
  if (!value) return null;
  const match = String(value).match(/(\d+)/);
  return match ? Number(match[1]) : null;
}

function matchesRuntimeFilter(movie, runtime) {
  if (runtime === 'any') return true;
  const minutes = parseRuntime(movie.Runtime ?? movie.runtime);
  if (minutes === null) return false;
  if (runtime === 'under90') return minutes < 90;
  if (runtime === '90to120') return minutes >= 90 && minutes <= 120;
  if (runtime === 'over120') return minutes > 120;
  return true;
}

function matchesGenreFilter(movie, genre) {
  if (genre === 'any') return true;
  const movieGenre = (movie.Genre ?? movie.genre ?? '').toLowerCase();
  return movieGenre.includes(genre.toLowerCase());
}

export default function Suggestions() {
  const [mood, setMood] = useState('happy');
  const [genre, setGenre] = useState('any');
  const [runtime, setRuntime] = useState('any');
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitted(true);
    setLoading(true);
    setError('');
    setMovies([]);

    const query = getMoodSearchQuery(mood) + (genre !== 'any' ? ` ${genre}` : '');

    try {
      // initial search to find a seed movie (used by recommendation endpoints)
      const searchResults = await movieService.searchMovies(query, 1);
      const rawMovies = Array.isArray(searchResults) ? searchResults : (searchResults?.Search || []);

      if (!rawMovies.length) {
        setMovies([]);
        return;
      }

      const seed = rawMovies[0];
      const seedId = seed?.imdbID || seed?.imdbId;

      // Use collaborative recommendations only (as requested). Backend may require auth.
      let recs = [];
      if (seedId) {
        try {
          recs = await movieService.getCollaborativeRecommendations(seedId, 12);
        } catch (e) {
          recs = [];
        }
      }

      // Normalize recommendations into detailed movie objects. If the API returned ID strings or minimal objects,
      // fetch full details when possible. Otherwise fall back to using the initial search results filtered client-side.
      let detailed = [];

      if (Array.isArray(recs) && recs.length > 0) {
        detailed = await Promise.all(
          recs.slice(0, 12).map(async (r) => {
            if (!r) return null;
            if (typeof r === 'string') {
              try {
                return await movieService.getMovieDetails(r);
              } catch (e) {
                return null;
              }
            }
            const id = r.imdbID || r.imdbId || r.id;
            if (id) {
              try {
                return await movieService.getMovieDetails(id);
              } catch (e) {
                // if fetching details failed, but the object looks like a movie, return it
                return r?.Title || r?.title ? r : null;
              }
            }
            return r?.Title || r?.title ? r : null;
          })
        );
      }

      // If recommendation APIs returned nothing, fall back to the previous mood-based search + filtering
      if (!detailed.length) {
        const fallback = await Promise.all(
          rawMovies.slice(0, 12).map(async (movie) => {
            const id = movie.imdbID || movie.imdbId;
            if (!id) return null;
            try {
              return await movieService.getMovieDetails(id);
            } catch (err) {
              return null;
            }
          })
        );
        detailed = fallback.filter(Boolean);
      }

      const filtered = detailed
        .filter(Boolean)
        .filter((movie) => matchesMoodFilter(movie, mood) && matchesGenreFilter(movie, genre) && matchesRuntimeFilter(movie, runtime));

      setMovies(filtered);
    } catch (err) {
      console.error(err);
      setError('Unable to load recommendations. Try again later.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="suggestions-hero">
        <div className="suggestions-badge">Mood-based picks</div>
        <h1>What should you watch tonight?</h1>
        <p>Tell us your vibe and we'll find movies that match.</p>
      </div>

      <form className="suggestions-panel" onSubmit={handleSubmit}>
        <div className="input-group">
          <label htmlFor="mood">Mood</label>
          <select id="mood" value={mood} onChange={(e) => setMood(e.target.value)}>
            {moodOptions.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>

        <div className="input-group">
          <label htmlFor="genre">Genre</label>
          <select id="genre" value={genre} onChange={(e) => setGenre(e.target.value)}>
            {genreOptions.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>

        <div className="input-group">
          <label htmlFor="runtime">Runtime</label>
          <select id="runtime" value={runtime} onChange={(e) => setRuntime(e.target.value)}>
            {runtimeOptions.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </div>

        {/* Recommendations use collaborative filtering only */}

        <button type="submit" className="btn suggestions-submit">Get recommendations</button>
      </form>

      {loading ? (
        <Loader />
      ) : error ? (
        <div className="suggestions-message">{error}</div>
      ) : submitted && movies.length === 0 ? (
        <div className="suggestions-message">
          No movies matched your vibe. Try a broader mood or a different genre.
        </div>
      ) : (
        movies.length > 0 && (
          <div className="grid" style={{ marginTop: '20px' }}>
            {movies.map((movie, index) => (
              <MovieCard
                key={movie.imdbID || movie.imdbId || movie.id || index}
                movie={movie}
              />
            ))}
          </div>
        )
      )}
    </div>
  );
}
