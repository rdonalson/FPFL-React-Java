import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Home } from '@/app/pages/Home';
import CreditsPage from '@/features/pages/CreditsPage';
import SpecificItemPage from '@/features/pages/SpecificItemPage';
import { ItemTypeListPage } from '@/features/itemType/components/ItemTypeListPage';
import { ItemTypeDetailPage } from '@/features/itemType/components/itemTypeDetailPage';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />

        <Route path="/credits" element={<CreditsPage />} />
        <Route path="/items/:id" element={<SpecificItemPage />} />
        <Route path="/item-types" element={<ItemTypeListPage />} />
        <Route path="/item-types/:id" element={<ItemTypeDetailPage />} />

        {/* 404 */}
        <Route path="*" element={<div>Page Not Found</div>} />
      </Routes>
    </BrowserRouter>
  );
}
