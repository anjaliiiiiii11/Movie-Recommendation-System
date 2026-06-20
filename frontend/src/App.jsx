import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';

// Pages
import Home from './pages/Home';
import Search from './pages/Search';
import AdminDashboard from './pages/AdminDashboard';

import MovieDetails from './pages/MovieDetails';

import Login from './pages/Login';
import Register from './pages/Register';
import Favorites from './pages/Favorites';
import Profile from './pages/Profile';
import RequireAuth from './routes/RequireAuth';
import Suggestions from './pages/Suggestions';
import './App.css';

function App() {

  return (
    <Router>
      <div className="app-wrapper">
        <Navbar />
        <main style={{ minHeight: '80vh' }}>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/search" element={<Search />} />
            <Route path="/movie/:id" element={<MovieDetails />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/favorites" element={<Favorites />} />
            <Route path="/suggestions" element={<RequireAuth><Suggestions /></RequireAuth>} />
            <Route path="/admin" element={<RequireAuth><AdminDashboard /></RequireAuth>} />

            <Route path="/profile" element={<RequireAuth><Profile /></RequireAuth>} />
          </Routes>

        </main>
        <Footer />
      </div>
    </Router>
  );
}

export default App;

