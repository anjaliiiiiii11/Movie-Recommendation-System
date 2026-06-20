import { useEffect, useState } from 'react';
import MovieCard from '../components/MovieCard';
import Loader from '../components/Loader';
import RecommendationSection from '../components/RecommendationSection';
import { movieService } from '../services/movieService';
import RequireAuth from '../routes/RequireAuth';

function FavoritesPage() {
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [personalized, setPersonalized] = useState([]);

  useEffect(() => {
    const fetchFavorites = async () => {
      try {
        setLoading(true);
        const data = await movieService.getFavorites();
        setFavorites(Array.isArray(data) ? data : []);
        // fetch personalized recommendations based on first favorite as seed
        const list = Array.isArray(data) ? data : [];
        if (list.length) {
          const seedId = list[0]?.imdbId || list[0]?.imdbID || list[0]?.id;
          if (seedId) {
            try {
              const rec = await movieService.getPersonalizedRecommendations(seedId, 12);
              setPersonalized(rec?.movies ?? rec ?? []);
            } catch {
              setPersonalized([]);
            }
          }
        }
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
        {personalized?.length > 0 && (
          <RecommendationSection movies={personalized} />
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

