import { useEffect, useMemo, useState } from 'react';
import { adminService } from '../services/adminService';

export default function AdminDashboard() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [analytics, setAnalytics] = useState(null);

  useEffect(() => {
    const run = async () => {
      try {
        setLoading(true);
        const res = await adminService.getAnalytics({ limit: 6 });
        setAnalytics(res.data?.data ?? null);
      } catch (e) {
        console.error(e);
        setError('Failed to load admin analytics');
      } finally {
        setLoading(false);
      }
    };
    run();
  }, []);

  const cards = useMemo(() => {
    const mostSearched = analytics?.mostSearchedMovies ?? [];
    const mostActive = analytics?.mostActiveUsers ?? [];
    const topGenres = analytics?.topGenres ?? [];
    return { mostSearched, mostActive, topGenres };
  }, [analytics]);

  if (loading) return <div className="container">Loading...</div>;
  if (error) return <div className="container">{error}</div>;

  return (
    <div className="container admin-dashboard">
      <div className="admin-header admin-header-center">
        <div>
          <h1 className="admin-title">Admin Analytics</h1>
        </div>
      </div>

      <div className="admin-summary-grid">
        <div className="admin-card">
          <div className="admin-card-title">Most searched movies</div>
          <div className="admin-list">
            {cards.mostSearched.length === 0 ? (
              <div className="admin-empty">No data yet.</div>
            ) : (
              cards.mostSearched.map((m) => (
                <div key={m.imdbId} className="admin-list-item">
                  {m.poster ? (
                    <img src={m.poster} alt={m.title} className="admin-list-avatar" />
                  ) : (
                    <div className="admin-list-avatar admin-avatar-empty">N/A</div>
                  )}
                  <div className="admin-list-item-info">
                    <div className="admin-item-title">{m.title}</div>
                    <div className="admin-item-subtitle">{m.imdbId}</div>
                  </div>
                  <div className="admin-item-count">× {m.count}</div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="admin-card">
          <div className="admin-card-title">Most active users</div>
          
          <div className="admin-list">
            {cards.mostActive.length === 0 ? (
              <div className="admin-empty">No data yet.</div>
            ) : (
              cards.mostActive.map((u) => (
                <div key={u.email} className="admin-list-item">
                  <div className="admin-list-item-info">
                    <div className="admin-item-title">{u.username || u.email}</div>
                    <div className="admin-item-subtitle">{u.email}</div>
                  </div>
                  <div className="admin-item-count">× {u.count}</div>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="admin-card">
          <div className="admin-card-title">Top genres</div>
          
          <div className="admin-list">
            {cards.topGenres.length === 0 ? (
              <div className="admin-empty">No data yet.</div>
            ) : (
              cards.topGenres.map((g) => (
                <div key={g.genre} className="admin-list-item">
                  <div className="admin-list-item-info">
                    <div className="admin-item-title">{g.genre}</div>
                  </div>
                  <div className="admin-item-count">× {g.count}</div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

