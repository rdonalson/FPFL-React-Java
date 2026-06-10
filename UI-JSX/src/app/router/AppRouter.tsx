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
import { AdminRouteGuard } from './AdminRouteGuard';

export function AppRouter() {
  return (
    <Routes>
      {/* AppLayout wraps everything so topbar/sidebar are always present */}
      <Route element={<AppLayout />}>
        {/* Public Home */}
        <Route path="/" element={<HomePage />} />

        {/* Authenticated area */}
        <Route element={<AuthGate />}>
          {/* Admin-only routes */}
          <Route element={<AdminRouteGuard />}>
            <Route path="/command/admin/item-types" element={<ItemTypeTablePage />} />
            <Route path="/command/admin/time-periods" element={<TimePeriodTablePage />} />
            <Route path="/status" element={<div>Status Page Coming Soon</div>} />
          </Route>

          {/* Authenticated but NOT admin-only */}
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
