import { useMemo, useState } from 'react';

export default function RatingStars({
  value,
  onChange,
  disabled = false,
  outOf = 5,
}) {
  // Expect `value` in 0..outOf (can be fractional)
  const [hoverValue, setHoverValue] = useState(null);

  const displayValue = hoverValue ?? value ?? 0;

  const stars = useMemo(() => {
    const fullStars = Math.max(0, Math.min(outOf, displayValue));
    return Array.from({ length: outOf }, (_, i) => {
      const starNumber = i + 1;
      const filled = fullStars >= starNumber;
      return { starNumber, filled };
    });
  }, [displayValue, outOf]);

  return (
    <div
      style={{ display: 'inline-flex', gap: 6, alignItems: 'center', opacity: disabled ? 0.6 : 1 }}
      aria-label="rating-stars"
    >
      {stars.map(({ starNumber, filled }) => {
        const canInteract = !disabled && typeof onChange === 'function';
        return (
          <button
            key={starNumber}
            type="button"
            onMouseEnter={() => canInteract && setHoverValue(starNumber)}
            onMouseLeave={() => canInteract && setHoverValue(null)}
            onClick={() => canInteract && onChange(starNumber)}
            disabled={!canInteract}
            style={{
              cursor: canInteract ? 'pointer' : 'default',
              background: 'transparent',
              border: 'none',
              padding: 0,
              color: filled ? '#ffd700' : '#555',
              fontSize: 22,
              lineHeight: 1,
            }}
            aria-label={`${starNumber} star${starNumber === 1 ? '' : 's'}`}
          >
            ★
          </button>
        );
      })}

      <span style={{ color: '#ffd700', fontSize: 14, marginLeft: 4 }}>
        {Number((displayValue ?? 0).toFixed(1))} / {outOf}
      </span>
    </div>
  );
}

