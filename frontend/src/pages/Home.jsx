import { useState, useEffect } from 'react';
import MovieCard from '../components/MovieCard';
import Loader from '../components/Loader';
import { movieService } from '../services/movieService';

export default function Home() {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchTrendingMovies = async () => {
      try {
        setLoading(true);

        // Backend “popular titles”/trending logic is inside GET /api/movies
        const data = await movieService.getAllMovies();
        if (Array.isArray(data) && data.length) {
          setMovies(data);
        } else {
          setMovies([]);
        }
      } catch (err) {
        // Retry once using a direct fetch to avoid axios baseURL/trailing-slash quirks.
        try {
          const res = await fetch('http://localhost:5000/api/movies');
          if (!res.ok) {
            console.error(`Movies endpoint returned HTTP ${res.status}`);
            setMovies([]);
            return;
          }
          const data = await res.json();
          if (Array.isArray(data) && data.length) {
            setMovies(data);
          } else {
            setMovies([]);
          }
        } catch (retryErr) {
          setError('Failed to load movies');
          console.error(err);
          console.error(retryErr);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchTrendingMovies();
  }, []);

  if (loading) return <Loader />;
  if (error) return <div className="container">{error}</div>;

  return (
    <div className="container">
      <h1>🔥 Trending Movies</h1>
      <div className="grid">
        {movies.map((movie, index) => (
          <MovieCard key={movie.imdbID || movie.imdbId || movie.id || index} movie={movie} />
        ))}
      </div>
    </div>
  );
}

