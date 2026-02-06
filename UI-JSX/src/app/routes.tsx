// src/app/routes.tsx
import { Routes, Route } from 'react-router-dom';
import SplashPage from '@/features/pages/Splash-page';
import CreditsPage from '@/features/pages/CreditsPage';
import { ItemTypeListPage } from '@/features/itemType/components/ItemTypeListPage';

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<SplashPage />} />
      <Route path="/credits" element={<CreditsPage />} />
      <Route path="/item-types" element={<ItemTypeListPage />} />
    </Routes>
  );
}
