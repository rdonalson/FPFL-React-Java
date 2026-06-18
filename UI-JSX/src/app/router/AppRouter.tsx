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

// Weekly
import AddWeeklyPage from '@/features/catalog-command/transactions/components/items/occurrence/weekly/AddWeeklyPage';
import EditWeeklyPage from '@/features/catalog-command/transactions/components/items/occurrence/weekly/EditWeeklyPage';

// Every Two Weeks
import AddEveryTwoWeeksPage from '@/features/catalog-command/transactions/components/items/occurrence/every-two-weeks/AddEveryTwoWeeksPage';
import EditEveryTwoWeeksPage from '@/features/catalog-command/transactions/components/items/occurrence/every-two-weeks/EditEveryTwoWeeksPage';

// Bi-Monthly
import EditBiMonthlyPage from '@/features/catalog-command/transactions/components/items/occurrence/bi-monthly/EditBiMonthlyPage';
import AddBiMonthlyPage from '@/features/catalog-command/transactions/components/items/occurrence/bi-monthly/AddBiMonthlyPage';

// Monthly
import EditMonthlyPage from '@/features/catalog-command/transactions/components/items/occurrence/monthly/EditMonthlyPage';
import AddMonthlyPage from '@/features/catalog-command/transactions/components/items/occurrence/monthly/AddMonthlyPage';

// Quarterly
import EditQuarterlyPage from '@/features/catalog-command/transactions/components/items/occurrence/quarterly/EditQuarterlyPage';
import AddQuarterlyPage from '@/features/catalog-command/transactions/components/items/occurrence/quarterly/AddQuarterlyPage';

// Semi-Annual
import AddSemiAnnualPage from '@/features/catalog-command/transactions/components/items/occurrence/semi-annual/AddSemiAnnualPage';
import EditSemiAnnualPage from '@/features/catalog-command/transactions/components/items/occurrence/semi-annual/EditSemiAnnualPage';
import EditAnnualPage from '@/features/catalog-command/transactions/components/items/occurrence/annual/EditAnnualPage';
import AddAnnualPage from '@/features/catalog-command/transactions/components/items/occurrence/annual/AddAnnualPage';

// Annual
// import AddAnnualPage from '@/features/catalog-command/transactions/components/items/occurrence/annual/AddAnnualPage';
// import EditAnnualPage from '@/features/catalog-command/transactions/components/items/occurrence/annual/EditAnnualPage';

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        {/* Public */}
        <Route path="/" element={<HomePage />} />

        {/* Authenticated */}
        <Route element={<AuthGate />}>
          {/* Admin-only */}
          <Route element={<AdminRouteGuard />}>
            <Route path="/command/admin/item-types" element={<ItemTypeTablePage />} />
            <Route path="/command/admin/time-periods" element={<TimePeriodTablePage />} />
            <Route path="/status" element={<div>Status Page Coming Soon</div>} />
          </Route>

          {/* Shared authenticated */}
          <Route path="/command/transactions/initial-amount" element={<InitialAmountPage />} />

          {/* ====================================================== */}
          {/*                     CREDIT FLOWS (1)                   */}
          {/* ====================================================== */}
          <Route path="/command/transactions/credits">
            {/* List */}
            <Route index element={<ItemsListPage itemType={1} />} />

            {/* Select Period */}
            <Route path="new" element={<SelectPeriodPage itemType={1} />} />

            {/* One-Time (Period 1) */}
            <Route path="new/1" element={<AddOneTimePage itemType={1} />} />
            <Route path="1/:id/edit" element={<EditOneTimePage itemType={1} />} />

            {/* Daily (Period 2) */}
            <Route path="new/2" element={<AddDailyPage itemType={1} />} />
            <Route path="2/:id/edit" element={<EditDailyPage itemType={1} />} />

            {/* Weekly (Period 3) */}
            <Route path="new/3" element={<AddWeeklyPage itemType={1} />} />
            <Route path="3/:id/edit" element={<EditWeeklyPage itemType={1} />} />

            {/* Every Two Weeks (Period 4) */}
            <Route path="new/4" element={<AddEveryTwoWeeksPage itemType={1} />} />
            <Route path="4/:id/edit" element={<EditEveryTwoWeeksPage itemType={1} />} />

            {/* Bi-Monthly (Period 5) */}
            <Route path="new/5" element={<AddBiMonthlyPage itemType={1} />} />
            <Route path="5/:id/edit" element={<EditBiMonthlyPage itemType={1} />} />

            {/* Monthly (Period 6) */}
            <Route path="new/6" element={<AddMonthlyPage itemType={1} />} />
            <Route path="6/:id/edit" element={<EditMonthlyPage itemType={1} />} />

            {/* Quarterly (Period 7) */}
            <Route path="new/7" element={<AddQuarterlyPage itemType={1} />} />
            <Route path="7/:id/edit" element={<EditQuarterlyPage itemType={1} />} />

            {/* Semi-Annual (Period 8) */}
            <Route path="new/8" element={<AddSemiAnnualPage itemType={1} />} />
            <Route path="8/:id/edit" element={<EditSemiAnnualPage itemType={1} />} />

            {/* Annual (Period 9) */}
            <Route path="new/9" element={<AddAnnualPage itemType={1} />} />
            <Route path="9/:id/edit" element={<EditAnnualPage itemType={1} />} />
          </Route>

          {/* ====================================================== */}
          {/*                     DEBIT FLOWS (2)                    */}
          {/* ====================================================== */}
          <Route path="/command/transactions/debits">
            {/* List */}
            <Route index element={<ItemsListPage itemType={2} />} />

            {/* Select Period */}
            <Route path="new" element={<SelectPeriodPage itemType={2} />} />

            {/* One-Time (Period 1) */}
            <Route path="new/1" element={<AddOneTimePage itemType={2} />} />
            <Route path="1/:id/edit" element={<EditOneTimePage itemType={2} />} />

            {/* Daily (Period 2) */}
            <Route path="new/2" element={<AddDailyPage itemType={2} />} />
            <Route path="2/:id/edit" element={<EditDailyPage itemType={2} />} />

            {/* Weekly (Period 3) */}
            <Route path="new/3" element={<AddWeeklyPage itemType={2} />} />
            <Route path="3/:id/edit" element={<EditWeeklyPage itemType={2} />} />

            {/* Every Two Weeks (Period 4) */}
            <Route path="new/4" element={<AddEveryTwoWeeksPage itemType={2} />} />
            <Route path="4/:id/edit" element={<EditEveryTwoWeeksPage itemType={2} />} />

            {/* Bi-Monthly (Period 5) */}
            <Route path="new/5" element={<AddBiMonthlyPage itemType={2} />} />
            <Route path="5/:id/edit" element={<EditBiMonthlyPage itemType={2} />} />

            {/* Monthly (Period 6) */}
            <Route path="new/6" element={<AddMonthlyPage itemType={2} />} />
            <Route path="6/:id/edit" element={<EditMonthlyPage itemType={2} />} />

            {/* Quarterly (Period 7) */}
            <Route path="new/7" element={<AddQuarterlyPage itemType={2} />} />
            <Route path="7/:id/edit" element={<EditQuarterlyPage itemType={2} />} />

            {/* Semi-Annual (Period 8) */}
            <Route path="new/8" element={<AddSemiAnnualPage itemType={2} />} />
            <Route path="8/:id/edit" element={<EditSemiAnnualPage itemType={2} />} />

            {/* Annual (Period 9) */}
            <Route path="new/9" element={<AddAnnualPage itemType={2} />} />
            <Route path="9/:id/edit" element={<EditAnnualPage itemType={2} />} />
          </Route>
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<div>Not Found</div>} />
    </Routes>
  );
}
