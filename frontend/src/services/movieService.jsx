import axios from 'axios';
import { authService } from './authService';

const API_URL = 'http://localhost:5000/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = authService.getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const movieService = {

  // Get all movies
  getAllMovies: async () => {
    const response = await api.get('/movies');
    return response.data;
  },

  // Search movies
  searchMovies: async (query, page = 1) => {
    const response = await api.get(
      `/movies/search?q=${encodeURIComponent(query)}&page=${page}`
    );
    return response.data;
  },

  // Get movie details
  getMovieDetails: async (id) => {
    const response = await api.get(`/movies/${id}`);
    return response.data;
  },

  // Favorites
  addFavorite: async (imdbId) => {
    const res = await api.post(`/favorites/${encodeURIComponent(imdbId)}`);
    return res.data;
  },

  removeFavorite: async (imdbId) => {
    const res = await api.delete(`/favorites/${encodeURIComponent(imdbId)}`);
    return res.data;
  },

  getFavorites: async () => {
    const res = await api.get('/favorites');
    return res.data;
  },

  isFavorite: async (imdbId) => {
    const res = await api.get(`/favorites/${encodeURIComponent(imdbId)}/exists`);
    return res.data;
  },

  // Ratings
  submitRating: async (imdbId, value) => {
    const res = await api.post(`/ratings/${encodeURIComponent(imdbId)}`,
      { value }
    );
    return res.data;
  },

  getAverageRating: async (imdbId) => {
    const res = await api.get(`/ratings/${encodeURIComponent(imdbId)}/average`);
    return res.data;
  },

  getMyRating: async (imdbId) => {
    const res = await api.get(`/ratings/${encodeURIComponent(imdbId)}/mine`);
    return res.data;
  },

  // Recommendations
  getContentRecommendations: async (imdbId, limit = 8) => {
    const res = await api.get(
      `/recommendations/content?imdbId=${encodeURIComponent(imdbId)}&limit=${limit}`
    );
    return res.data;
  },

  // Personalized/collaborative require auth; backend will fall back if unauthenticated.
  getPersonalizedRecommendations: async (imdbId, limit = 8) => {
    const res = await api.get(
      `/recommendations/personalized?imdbId=${encodeURIComponent(imdbId)}&limit=${limit}`
    );
    return res.data;
  },

  getCollaborativeRecommendations: async (imdbId, limit = 8) => {
    const res = await api.get(
      `/recommendations/collaborative?imdbId=${encodeURIComponent(imdbId)}&limit=${limit}`
    );
    return res.data;
  },
};

export default movieService;


