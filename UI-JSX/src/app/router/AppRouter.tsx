// src/app/router/AppRouter.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import AppLayout from '../layout/AppLayout';

import { HomePage } from '../pages/HomePage';
import CreditsPage from '@/features/pages/CreditsPage';
import SpecificItemPage from '@/features/pages/SpecificItemPage';
import { ItemTypeTablePage } from '@/features/itemType/components/ItemTypeTablePage';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* All pages wrapped in AppLayout */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<HomePage />} />

          {/* Item Types */}
          <Route path="/item-types" element={<ItemTypeTablePage />} />

          {/* Other pages */}
          <Route path="/credits" element={<CreditsPage />} />
          <Route path="/items/:id" element={<SpecificItemPage />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<div>Not Found</div>} />
      </Routes>
    </BrowserRouter>
  );
}
