import { Link } from 'react-router-dom';

export default function Navbar() {
  return (
    <nav>
      <div className="container flex">
        <Link to="/" style={{ fontSize: '1.5rem', color: 'var(--primary)' }}>Watch Party</Link>
        <div>
          <Link to="/">Home</Link>
          <Link to="/search">Search</Link>
          <Link to="/favorites">Favorites</Link>
          <Link to="/profile">Profile</Link>
          <Link to="/login">Login</Link>
        </div>
      </div>
    </nav>
  );
}