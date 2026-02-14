// src/app/router/AppRouter.tsx
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { MainLayout } from "../layout/MainLayout";

import { HomePage } from "../pages/HomePage";
import { ItemTypeDetailPage } from "@/features/itemType/components/itemTypeDetailPage";
import { ItemTypeListPage } from "@/features/itemType/components/ItemTypeListPage";
import CreditsPage from "@/features/pages/CreditsPage";
import SpecificItemPage from "@/features/pages/SpecificItemPage";

export function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>

        {/* THIS IS REQUIRED */}
        <Route element={<MainLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/item-types" element={<ItemTypeListPage />} />
          <Route path="/item-types/:id" element={<ItemTypeDetailPage />} />
          <Route path="/credits" element={<CreditsPage />} />
          <Route path="/items/:id" element={<SpecificItemPage />} />


        </Route>

        {/* fallback */}
        <Route path="*" element={<div>Not Found</div>} />
      </Routes>
    </BrowserRouter>
  );
}