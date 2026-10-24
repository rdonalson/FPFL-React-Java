// src/test/render.tsx — render with the providers pages expect (router + react-query)
import type { ReactElement } from 'react';
import { fireEvent, render } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

export function renderWithProviders(
  ui: ReactElement,
  { route = '/', path = '*' }: { route?: string; path?: string } = {},
) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  const result = render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[route]}>
        <Routes>
          <Route path={path} element={ui} />
          <Route path="*" element={<div data-testid="navigated-away" />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
  return { ...result, client };
}

/** PrimeReact InputNumber only commits a typed value on blur. */
export function typeNumber(input: HTMLElement, value: string | number) {
  fireEvent.focus(input);
  fireEvent.change(input, { target: { value: String(value) } });
  fireEvent.blur(input);
}
