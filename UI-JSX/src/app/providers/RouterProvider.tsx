// src/app/providers/RouterProvider.tsx
import { BrowserRouter } from 'react-router-dom';
import { AppRoutes } from '../routes';

export function RouterProvider() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}
