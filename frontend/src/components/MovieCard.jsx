import { Link } from 'react-router-dom';

export default function MovieCard({ movie }) {
  // Handle different data formats from OMDb vs backend Movie entity
  const poster = movie.Poster || movie.poster || movie.poster_path;
  const title = movie.Title || movie.title;
  const year = movie.Year || movie.year;
  const rating = movie.imdbRating || movie.vote_average;
  // Backend entity serializes id as `imdbId`.
  // OMDb uses `imdbID`. Prefer explicit IMDb ids and never fall back to `movie.id`.
  const id = movie.imdbID ?? movie.imdbId;


  return (
    <div className="card">
      {poster && poster !== 'N/A' ? (
        <img 
          src={poster} 
          alt={title} 
          style={{ width: '100%', borderRadius: '4px', height: '300px', objectFit: 'cover' }} 
        />
      ) : (
        <div style={{ width: '100%', height: '300px', background: '#555', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          No Image
        </div>
      )}
      
      <h3 className="movie-title" title={title}>{title}</h3>
      <div className="flex">
        <span>{year}</span>
        {rating && rating !== 'N/A' && (
          <span style={{ color: '#ffd700' }}>⭐ {rating}</span>
        )}
      </div>
      
      <Link to={`/movie/${id}`}>
        <button className="btn" style={{ width: '100%', marginTop: '10px' }}>
          View Details
        </button>
      </Link>
    </div>
  );
}