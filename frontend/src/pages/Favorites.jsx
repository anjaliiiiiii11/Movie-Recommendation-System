import { useEffect, useState } from 'react';
import MovieCard from '../components/MovieCard';
import Loader from '../components/Loader';
import { movieService } from '../services/movieService';
import RequireAuth from '../routes/RequireAuth';

function FavoritesPage() {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        setLoading(true);
        const data = await movieService.getFavorites();
        setFavorites(Array.isArray(data) ? data : []);
      } catch {
        setFavorites([]);
      } finally {

        setLoading(false);
      }
    };

    fetchFavorites();
  }, []);

  return (
    <div className="container">
      <h1>My Favorites</h1>

      {loading ? (
        <Loader />
      ) : favorites.length === 0 ? (
        <p>No favorites yet.</p>
      ) : (
        <div className="grid">
          {favorites.map((movie, idx) => (
            <MovieCard
              key={movie.imdbId || movie.imdb_id || movie.id || idx}
              movie={movie}
            />
          ))}
        </div>
      )}
    </div>
  );
}

export default function Favorites() {
  return (
    <RequireAuth>
      <FavoritesPage />
    </RequireAuth>
  );
}

