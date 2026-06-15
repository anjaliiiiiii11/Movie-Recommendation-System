import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function SearchBar() {
  const [term, setTerm] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e) => {
    e.preventDefault();
    if(term) navigate(`/search?q=${term}`);
  };

  return (
    <form onSubmit={handleSearch} style={{display:'flex', gap:'10px'}}>
      <input 
        type="text" 
        placeholder="Search movies..." 
        value={term}
        onChange={(e) => setTerm(e.target.value)}
      />
      <button type="submit" className="btn">Search</button>
    </form>
  );
}