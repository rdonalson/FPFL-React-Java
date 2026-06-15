// src/app/router/AppRouter.tsx
import { Routes, Route } from 'react-router-dom';

import AppLayout from '../layout/AppLayout';

import { HomePage } from '../pages/HomePage';
import { ItemTypeTablePage } from '@/features/catalog-command/admin/itemType/components/ItemTypeTablePage';
import { TimePeriodTablePage } from '@/features/catalog-command/admin/timePeriod/components/TimePeriodTablePage';
import InitialAmountPage from '@/features/catalog-command/transactions/components/initial-amount/InitialAmountPage';

import AuthGate from './AuthGate';
import { AdminRouteGuard } from './AdminRouteGuard';

import ItemsListPage from '@/features/catalog-command/transactions/components/items/list/ItemsListPage';
import SelectPeriodPage from '@/features/catalog-command/transactions/components/items/occurrence/SelectPeriodPage';

// One-Time
import AddOneTimePage from '@/features/catalog-command/transactions/components/items/occurrence/one-time/AddOneTimePage';
import EditOneTimePage from '@/features/catalog-command/transactions/components/items/occurrence/one-time/EditOneTimePage';

// Daily
import AddDailyPage from '@/features/catalog-command/transactions/components/items/occurrence/daily/AddDailyPage';
import EditDailyPage from '@/features/catalog-command/transactions/components/items/occurrence/daily/EditDailyPage';

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

          {/* ----------------------------- */}
          {/* CREDIT FLOWS (itemType = 1)   */}
          {/* ----------------------------- */}
          <Route path="/command/transactions/credits" element={<ItemsListPage itemType={1} />} />

          {/* Select Period */}
          <Route
            path="/command/transactions/credits/new"
            element={<SelectPeriodPage itemType={1} />}
          />

          {/* One-Time (Period 1) */}
          <Route
            path="/command/transactions/credits/new/1"
            element={<AddOneTimePage itemType={1} />}
          />
          <Route
            path="/command/transactions/credits/1/:id/edit"
            element={<EditOneTimePage itemType={1} />}
          />

          {/* Daily (Period 2) */}
          <Route
            path="/command/transactions/credits/new/2"
            element={<AddDailyPage itemType={1} />}
          />
          <Route
            path="/command/transactions/credits/2/:id/edit"
            element={<EditDailyPage itemType={1} />}
          />

          {/* ----------------------------- */}
          {/* DEBIT FLOWS (itemType = 2)    */}
          {/* ----------------------------- */}
          <Route path="/command/transactions/debits" element={<ItemsListPage itemType={2} />} />

          {/* Select Period */}
          <Route
            path="/command/transactions/debits/new"
            element={<SelectPeriodPage itemType={2} />}
          />

          {/* One-Time (Period 1) */}
          <Route
            path="/command/transactions/debits/new/1"
            element={<AddOneTimePage itemType={2} />}
          />
          <Route
            path="/command/transactions/debits/1/:id/edit"
            element={<EditOneTimePage itemType={2} />}
          />

          {/* Daily (Period 2) */}
          <Route
            path="/command/transactions/debits/new/2"
            element={<AddDailyPage itemType={2} />}
          />
          <Route
            path="/command/transactions/debits/2/:id/edit"
            element={<EditDailyPage itemType={2} />}
          />
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<div>Not Found</div>} />
    </Routes>
  );
}
