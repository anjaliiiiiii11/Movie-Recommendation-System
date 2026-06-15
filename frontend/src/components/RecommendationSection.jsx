import MovieCard from './MovieCard';

export default function RecommendationSection({ movies }) {
  return (
    <div style={{ marginTop: '40px' }}>
      <h2>Recommendations</h2>
      <div className="grid">
        {movies.slice(0, 4).map(movie => (
            <MovieCard key={movie.imdbId ?? movie.id} movie={movie} />
        ))}
      </div>
    </div>
  );
}