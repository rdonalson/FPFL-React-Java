// src/app/router/AppRouter.tsx
import { Routes, Route } from 'react-router-dom';

import AppLayout from '../layout/AppLayout';

import { HomePage } from '../pages/HomePage';
import CreditsPage from '@/features/pages/CreditsPage';
import SpecificItemPage from '@/features/pages/SpecificItemPage';
import { ItemTypeTablePage } from '@/features/catalog-command/admin/itemType/components/ItemTypeTablePage';
import { TimePeriodTablePage } from '@/features/catalog-command/admin/timePeriod/components/TimePeriodTablePage';
import InitialAmountPage from '@/features/catalog-command/transactions/components/initial-amount/InitialAmountPage';

import AuthGate from './AuthGate';

export function AppRouter() {
  return (
    <Routes>
      {/* AppLayout wraps all pages so topbar is always present */}
      <Route element={<AppLayout />}>
        {/* Public Home page */}
        <Route path="/" element={<HomePage />} />

        {/* Protected routes */}
        <Route element={<AuthGate />}>
          <Route path="/command/admin/item-types" element={<ItemTypeTablePage />} />
          <Route path="/command/admin/time-periods" element={<TimePeriodTablePage />} />
          <Route path="/command/transactions/initial-amount" element={<InitialAmountPage />} />

          <Route path="/credits" element={<CreditsPage />} />
          <Route path="/items/:id" element={<SpecificItemPage />} />
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<div>Not Found</div>} />
    </Routes>
  );
}
