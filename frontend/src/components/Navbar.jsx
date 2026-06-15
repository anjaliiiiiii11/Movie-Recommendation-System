import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

export default function Navbar() {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(authService.isAuthenticated());

  useEffect(() => {
    const sync = () => setIsLoggedIn(authService.isAuthenticated());

    // localStorage changes in other tabs trigger this, but we also call sync manually on logout.
    window.addEventListener('storage', sync);

    return () => window.removeEventListener('storage', sync);
  }, []);

  const onLogout = () => {
    authService.clearToken();
    setIsLoggedIn(false);
    navigate('/login');
  };

  return (
    <nav>
      <div className="container flex">
        <Link to="/" style={{ fontSize: '1.5rem', color: 'var(--primary)' }}>Movie Sphere</Link>
        <div>
          <Link to="/">Home</Link>
          <Link to="/search">Search</Link>
          <Link to="/favorites">Favorites</Link>
          <Link to="/suggestions">Suggestions</Link>
          <Link to="/profile">Profile</Link>
          {isLoggedIn ? (
            <a className="link" href="#" onClick={(e) => { e.preventDefault(); onLogout(); }}>
              Logout
            </a>
          ) : (
            <Link to="/login">Login</Link>
          )}
        </div>
      </div>
    </nav>
  );
}


