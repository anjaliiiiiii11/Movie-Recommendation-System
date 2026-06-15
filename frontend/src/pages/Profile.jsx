import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

export default function Profile() {
  const navigate = useNavigate();
  const [me, setMe] = useState(null);

  useEffect(() => {
    authService
      .getMe()
      .then((data) => setMe(data))
      .catch(() => setMe(null));
  }, []);

  const onLogout = () => {
    authService.clearToken();
    navigate('/login');
  };

  return (
    <div className="container">
      <h1>User Profile</h1>
      <div className="card">
        <h2>Email: {me?.email || '...'}</h2>
        <button className="btn" type="button" onClick={onLogout}>Logout</button>
      </div>
    </div>
  );
}

