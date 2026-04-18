// src/app/router/AppRouter.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';

import AppLayout from '../layout/AppLayout';

import { HomePage } from '../pages/HomePage';
import CreditsPage from '@/features/pages/CreditsPage';
import SpecificItemPage from '@/features/pages/SpecificItemPage';
import { ItemTypeTablePage } from '@/features/catalog-command/admin/itemType/components/ItemTypeTablePage';
import { TimePeriodTablePage } from '@/features/catalog-command/admin/timePeriod/components/TimePeriodTablePage';
import InitialAmountPage from '@/features/catalog-command/transactions/components/initial-amount/InitialAmountPage';

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* All pages wrapped in AppLayout */}
        <Route element={<AppLayout />}>
          <Route path="/" element={<HomePage />} />

          {/* Item Types */}
          <Route path="/command/admin/item-types" element={<ItemTypeTablePage />} />
          {/* Time Periods */}
          <Route path="/command/admin/time-periods" element={<TimePeriodTablePage />} />

          {/* Initial Amount */}
          <Route path="/command/transactions/initial-amount" element={<InitialAmountPage />} />

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
