export default function Pagination({ currentPage, totalPages, onPageChange }) {
  return (
    <div className="flex" style={{ marginTop: '20px', justifyContent: 'center' }}>
      <button 
        className="btn" 
        disabled={currentPage === 1}
        onClick={() => onPageChange(currentPage - 1)}
      >
        Prev
      </button>
      <span style={{margin: '0 15px'}}>Page {currentPage} of {totalPages}</span>
      <button 
        className="btn" 
        disabled={currentPage === totalPages}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Next
      </button>
    </div>
  );
}