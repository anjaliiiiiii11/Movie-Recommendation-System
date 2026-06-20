import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import MovieCard from '../components/MovieCard';
import SearchBar from '../components/SearchBar';
import Pagination from '../components/Pagination';
import Loader from '../components/Loader';
import RecommendationSection from '../components/RecommendationSection';
import { movieService } from '../services/movieService';

export default function Search() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';

  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [recommendations, setRecommendations] = useState([]);

  // Backend returns List<Movie> (array). Since backend doesn't provide totalResults,
  // we approximate pagination: if a page returns <10 items, there is no next page.
  const pageSize = 10;
  const hasNextPage = movies.length === pageSize;

  useEffect(() => {
    const searchMovies = async () => {
      if (!query) return;

      try {
        setLoading(true);
        const data = await movieService.searchMovies(query, page);

        // Expected from backend: array of movies
        if (Array.isArray(data)) {
          setMovies(data);
        } else if (data?.Response === 'True') {
          setMovies(data.Search || []);
        } else {
          setMovies([]);
        }

        // attempt to fetch content-based recommendations using the first search result as seed
        const first = Array.isArray(data) ? data[0] : data?.Search?.[0];
        const seedId = first?.imdbID || first?.imdbId;
        if (seedId) {
          try {
            const content = await movieService.getContentRecommendations(seedId, 8);
            setRecommendations(content?.movies ?? content ?? []);
          } catch (e) {
            setRecommendations([]);
          }
        } else {
          setRecommendations([]);
        }
      } catch (error) {
        console.error(error);
        setMovies([]);
        setRecommendations([]);
      } finally {
        setLoading(false);
      }
    };

    searchMovies();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, page]);

  // Pagination component needs totalPages; we set it to 1 or 2 based on hasNextPage.
  // This makes Next available only when the current page returns 10 results.
  const totalPages = hasNextPage ? 2 : 1;

  return (
    <div className="container">
      <h1>Search Results for: "{query}"</h1>
      <div style={{ marginBottom: '20px' }}>
        <SearchBar />
      </div>

      {loading ? (
        <Loader />
      ) : (
        <>
          {movies.length === 0 ? (
            <p>No movies found.</p>
          ) : (
            <div className="grid">
              {movies.map((movie, index) => (
                <MovieCard
                  key={movie.imdbID || movie.imdbId || movie.id || index}
                  movie={movie}
                />
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
          )}
          {recommendations?.length > 0 && (
            <RecommendationSection movies={recommendations} />
          )}
        </>
      )}
    </div>
  );
}
