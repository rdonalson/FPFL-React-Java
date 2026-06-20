// src/components/BreakpointOverlay.tsx
import React, { useEffect, useState } from 'react';

export function BreakpointOverlay() {
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    const handler = () => setWidth(window.innerWidth);
    window.addEventListener('resize', handler);
    return () => window.removeEventListener('resize', handler);
  }, []);

  const bp =
    width >= 1536
      ? '2XL'
      : width >= 1280
        ? 'XL'
        : width >= 1024
          ? 'LG'
          : width >= 768
            ? 'MD'
            : 'SM';

  return (
    <div
      style={{
        position: 'fixed',
        bottom: 10,
        right: 10,
        padding: '6px 10px',
        background: 'rgba(0,0,0,0.7)',
        color: 'white',
        borderRadius: '6px',
        fontSize: '12px',
        zIndex: 99999,
        pointerEvents: 'none',
        fontFamily: 'monospace',
      }}
    >
      <span style={{ opacity: bp === 'SM' ? 1 : 0.4 }}>SM</span>
      {' | '}
      <span style={{ opacity: bp === 'MD' ? 1 : 0.4 }}>MD</span>
      {' | '}
      <span style={{ opacity: bp === 'LG' ? 1 : 0.4 }}>LG</span>
      {' | '}
      <span style={{ opacity: bp === 'XL' ? 1 : 0.4 }}>XL</span>
      {' | '}
      <span style={{ opacity: bp === '2XL' ? 1 : 0.4 }}>2XL</span>
      <div style={{ marginTop: 4, opacity: 0.6 }}>{width}px</div>
    </div>
  );
}
