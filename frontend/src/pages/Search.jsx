import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import MovieCard from '../components/MovieCard';
import SearchBar from '../components/SearchBar';
import Pagination from '../components/Pagination';
import Loader from '../components/Loader';
import { movieService } from '../services/movieService';

export default function Search() {
  const [searchParams] = useSearchParams();
  const query = searchParams.get('q') || '';

  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);

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
          return;
        }

        // Backward compatibility if API ever returns OMDB-like object
        if (data?.Response === 'True') {
          setMovies(data.Search || []);
          return;
        }

        setMovies([]);
      } catch (error) {
        console.error(error);
        setMovies([]);
      } finally {
        setLoading(false);
      }
    };

    searchMovies();
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
        </>
      )}
    </div>
  );
}
