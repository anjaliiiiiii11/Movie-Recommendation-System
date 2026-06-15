import axios from 'axios';

const API_URL = 'http://localhost:5000/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const TOKEN_KEY = 'jwt_token';

export const authService = {
  getToken: () => localStorage.getItem(TOKEN_KEY),

  setToken: (token) => {
    if (token) localStorage.setItem(TOKEN_KEY, token);
  },

  clearToken: () => localStorage.removeItem(TOKEN_KEY),

  isAuthenticated: () => {
    const token = localStorage.getItem(TOKEN_KEY);
    return !!token;
  },

  login: async ({ email, password }) => {
    const res = await api.post('/auth/login', { email, password });
    authService.setToken(res.data.token);
    return res.data;
  },

  register: async ({ username, email, password }) => {
    const res = await api.post('/auth/register', { username, email, password });
    authService.setToken(res.data.token);
    return res.data;
  },

  getMe: async () => {
    const token = authService.getToken();
    if (!token) return null;

    const res = await api.get('/auth/me', {
      headers: { Authorization: `Bearer ${token}` },
    });

    return res.data;
  },

  // convenience: attaches token to all axios requests
  applyAuthHeader: (config = {}) => {
    const token = authService.getToken();
    if (!token) return config;
    return {
      ...config,
      headers: {
        ...(config.headers || {}),
        Authorization: `Bearer ${token}`,
      },
    };
  },
};



