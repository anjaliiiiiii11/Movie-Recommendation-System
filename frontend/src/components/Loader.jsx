export default function Loader() {
  return (
    <div style={{ textAlign: 'center', padding: '50px' }}>
      <div style={{ 
        border: '5px solid #f3f3f3', 
        borderTop: '5px solid var(--primary)', 
        borderRadius: '50%', 
        width: '50px', 
        height: '50px', 
        animation: 'spin 1s linear infinite',
        margin: '0 auto'
      }}></div>
    </div>
  );
}