import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import Loader from '../components/Loader';
import RecommendationSection from '../components/RecommendationSection';
import { movieService } from '../services/movieService';
import RatingStars from '../components/RatingStars';

export default function MovieDetails() {

  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Ratings (backend)
  const [avgRating, setAvgRating] = useState(null); // 0..10 (float)
  const [avgCount, setAvgCount] = useState(0);
  const [myValue, setMyValue] = useState(null); // 1..10
  const [ratingBusy, setRatingBusy] = useState(false);


  useEffect(() => {
    const fetchMovieDetails = async () => {
      try {
        setLoading(true);
        const data = await movieService.getMovieDetails(id);

        // Backend returns a Movie object (does not include OMDb's { Response: 'True' }).
        // So we treat any successful payload as a found movie.
        if (data && (data.imdbId || data.imdbID || data.title || data.Title)) {
          setMovie(data);
        } else {
          setError('Movie not found');
        }
      } catch (err) {
        setError('Error loading movie');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchMovieDetails();
  }, [id]);

  const [recommendations, setRecommendations] = useState([]);


  // Load ratings + recommendations after movie loads
  useEffect(() => {

    const imdbId = movie?.imdbID || movie?.imdbId || movie?.id;
    if (!imdbId) return;

    const run = async () => {
      const [avgRes, mineRes] = await Promise.all([
        movieService.getAverageRating(imdbId),
        movieService.getMyRating(imdbId),
      ]);

      setAvgRating(avgRes?.average ?? 0);
      setAvgCount(avgRes?.count ?? 0);

      if (mineRes?.rated) setMyValue(mineRes?.value);
      else setMyValue(null);


      // Recs (content-first, then personalized)
      const content = await movieService.getContentRecommendations(imdbId, 8);
      setRecommendations(content?.movies ?? []);

      // Collaborative (auth required on backend). If it fails or returns empty,
      // fall back to personalized (which itself falls back to content).
      try {
        const collab = await movieService.getCollaborativeRecommendations(imdbId, 8);
        if (collab?.movies?.length) {
          setRecommendations(collab?.movies ?? []);
          return;
        }
      } catch {
        // ignore and try personalized
      }

      // If logged in, also try personalized (backend will fall back if unauthenticated)
      try {
        const personalized = await movieService.getPersonalizedRecommendations(imdbId, 8);
        if (personalized?.movies?.length) {
          setRecommendations(personalized?.movies ?? []);
        }
      } catch {
        // ignore
      }


    };

    run();
  }, [movie]);

  if (loading) return <Loader />;

  if (error) return <div className="container">{error}</div>;
  if (!movie) return null;

  // Support both OMDb style keys and persisted/entity style keys
  const poster = movie.Poster ?? movie.poster ?? movie.poster_path;
  const title = movie.Title ?? movie.title;
  const year = movie.Year ?? movie.year;
  const runtime = movie.Runtime ?? movie.runtime;
  const imdbRating = movie.imdbRating ?? movie.imdb_rating;
  const genre = movie.Genre ?? movie.genre;
  const plot = movie.Plot ?? movie.plot;
  const director = movie.Director ?? movie.director;
  const actors = movie.Actors ?? movie.actors;

  const rawRatings = movie.Ratings ?? movie.ratings;


  return (
    <div className="container">
      <div className="flex" style={{ alignItems: 'flex-start', gap: '30px', flexWrap: 'wrap' }}>
        <div style={{ minWidth: '300px' }}>
          {poster && poster !== 'N/A' ? (
            <img 
              src={poster} 
              alt={title} 
              style={{ width: '100%', borderRadius: '8px' }} 
            />
          ) : (
            <div style={{ width: '300px', height: '450px', background: '#333', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              No Image
            </div>
          )}
        </div>
        
        <div style={{ flex: 1 }}>
          <h1>{title}</h1>
          <div className="flex">
            <span>📅 {year}</span>
            <span>⏱️ {runtime}</span>
            <span>⭐ {imdbRating}</span>
          </div>
          
          <div style={{ margin: '20px 0' }}>
            <strong>Genre:</strong> {genre}
          </div>
          
          <h3>Plot</h3>
          <p>{plot}</p>
          
          <div style={{ marginTop: '20px' }}>
            <strong>Director:</strong> {director}
          </div>
          <div>
            <strong>Cast:</strong> {actors}
          </div>
          
          <h3> Ratings </h3>
          <ul>
            {rawRatings?.map((rating, index) => (
              <li key={index}>
                {(rating.Source ?? rating.source)}: <strong>{rating.Value ?? rating.value}</strong>
              </li>
            ))}
          </ul>

          <div style={{ marginTop: 18, padding: '12px 0' }}>

            <h4 style={{ margin: 0 }}>Community rating</h4>
            <div style={{ marginTop: 8 }}>
              <RatingStars
                value={avgRating == null ? 0 : avgRating / 2}
                outOf={5}
                disabled
              />
            </div>
            <div style={{ fontSize: 14, color: '#aaa', marginTop: 6 }}>
              {avgRating == null ? 'Loading...' : `${(avgRating / 2).toFixed(1)} / 5 (${avgCount} ratings)`}
            </div>

            <div style={{ marginTop: 16 }}>
              <h4 style={{ margin: 0 }}>Your rating</h4>
              <div style={{ marginTop: 8 }}>
                <RatingStars
                  value={myValue == null ? 0 : myValue / 2}
                  outOf={5}
                  disabled={!movie?.imdbID && !movie?.imdbId && !movie?.id}
                  onChange={(starValue5) => {
                    const next = Math.round(starValue5 * 2);
                    setMyValue(next);
                  }}
                />
              </div>
              <button
                className="btn"
                style={{ marginTop: 10 }}
                type="button"
                disabled={ratingBusy || myValue == null}
                onClick={async () => {
                  try {
                    setRatingBusy(true);
                    const imdbId = movie?.imdbID || movie?.imdbId || movie?.id;
                    await movieService.submitRating(imdbId, myValue);
                    // refresh average + my rating
                    const [avgRes, mineRes] = await Promise.all([
                      movieService.getAverageRating(imdbId),
                      movieService.getMyRating(imdbId),
                    ]);
                    setAvgRating(avgRes?.average ?? 0);
                    setAvgCount(avgRes?.count ?? 0);
                    if (mineRes?.rated) setMyValue(mineRes?.value);
                    else setMyValue(null);
                  } catch {
                    // ignore
                  } finally {

                    setRatingBusy(false);
                  }
                }}
              >
                {ratingBusy ? 'Submitting...' : 'Submit rating'}
              </button>
            </div>
          </div>


          <FavoriteButton movie={movie} />
        </div>
      </div>

      <RecommendationSection movies={recommendations} />
    </div>
  );
}



function FavoriteButton({ movie }) {
  const [isFav, setIsFav] = useState(false);
  const [busy, setBusy] = useState(false);

  const imdbId = movie?.imdbID || movie?.imdbId || movie?.id;

  useEffect(() => {
    const run = async () => {
      if (!imdbId) return;
      try {
        const res = await movieService.isFavorite(imdbId);
        setIsFav(!!res?.exists);
      } catch {
        setIsFav(false);
      }
    };
    run();
  }, [imdbId]);

  const onClick = async () => {
    if (!imdbId || busy) return;
    try {
      setBusy(true);
      if (isFav) {
        await movieService.removeFavorite(imdbId);
        setIsFav(false);
      } else {
        await movieService.addFavorite(imdbId);
        setIsFav(true);
      }
    } catch {
      // ignore
    } finally {

      setBusy(false);
    }
  };

  return (
    <button
      className="btn"
      style={{ marginTop: '20px' }}
      type="button"
      onClick={onClick}
      disabled={busy}
    >
      {isFav ? '✅ Remove Favorite' : '❤️ Add to Favorites'}
    </button>
  );
}
