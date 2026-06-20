import axios from 'axios';

const API_URL = 'http://localhost:5000/api';
const TOKEN_KEY = 'jwt_token';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const adminService = {
  getAnalytics: async ({ limit = 6 } = {}) => {
    const token = localStorage.getItem(TOKEN_KEY);
    return api.get('/admin/analytics', {
      params: { limit },
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  },
};

