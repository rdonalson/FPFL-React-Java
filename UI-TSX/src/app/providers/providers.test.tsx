import { afterEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { useContext } from 'react';
import { useQuery } from '@tanstack/react-query';
import { AppErrorBoundary } from './error/AppErrorBoundary';
import { AppProviders } from './AppProviders';
import { ToastContext } from './toast/toast-context';
import { MenuProvider } from './MenuProvider';
import { useMenuContext } from '@/app/layout/context/useMenuContext';

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

function Boom(): never {
  throw new Error('render failed');
}

describe('AppErrorBoundary', () => {
  it('shows a friendly page and reports the error', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    const fetchSpy = vi.fn(async () => ({}) as Response);
    vi.stubGlobal('fetch', fetchSpy);

    render(
      <AppErrorBoundary>
        <Boom />
      </AppErrorBoundary>,
    );

    expect(await screen.findByText('Something went wrong')).toBeInTheDocument();
    expect(fetchSpy).toHaveBeenCalledWith(
      expect.stringMatching(/\/client-logs$/),
      expect.objectContaining({ method: 'POST' }),
    );
    const body = JSON.parse(
      (fetchSpy.mock.calls[0] as unknown as [string, RequestInit])[1].body as string,
    );
    expect(body).toMatchObject({ level: 'error', url: 'UI_RENDER', message: 'render failed' });
  });

  it('renders children when nothing goes wrong', () => {
    render(
      <AppErrorBoundary>
        <p>All good</p>
      </AppErrorBoundary>,
    );
    expect(screen.getByText('All good')).toBeInTheDocument();
  });
});

describe('AppProviders', () => {
  it('provides the toast ref', () => {
    function Probe() {
      const toast = useContext(ToastContext);
      return <span>{toast && 'current' in toast ? 'has toast' : 'no toast'}</span>;
    }
    render(
      <AppProviders>
        <Probe />
      </AppProviders>,
    );
    expect(screen.getByText('has toast')).toBeInTheDocument();
  });

  it('shows a toast for server errors from queries', async () => {
    function Failing() {
      useQuery({
        queryKey: ['x'],
        queryFn: async () => {
          throw Object.assign(new Error('Database is down'), { status: 503 });
        },
      });
      return null;
    }
    render(
      <AppProviders>
        <Failing />
      </AppProviders>,
    );
    expect(await screen.findByText('Database is down')).toBeInTheDocument();
  });

  it('leaves 4xx errors to the page that asked', async () => {
    function Failing() {
      const q = useQuery({
        queryKey: ['y'],
        queryFn: async () => {
          throw Object.assign(new Error('Item 4 not found'), { status: 404 });
        },
      });
      return <span>{q.isError ? 'query failed' : 'loading'}</span>;
    }
    render(
      <AppProviders>
        <Failing />
      </AppProviders>,
    );
    expect(await screen.findByText('query failed')).toBeInTheDocument();
    expect(screen.queryByText('Item 4 not found')).not.toBeInTheDocument();
  });
});

describe('MenuProvider', () => {
  it('toggles the open submenu and closes the sidebar', () => {
    const onClose = vi.fn();
    function Probe() {
      const { activeIndex, toggleIndex, closeMenu } = useMenuContext();
      return (
        <>
          <span>active:{activeIndex ?? 'none'}</span>
          <button onClick={() => toggleIndex('a')}>toggle</button>
          <button onClick={closeMenu}>close</button>
        </>
      );
    }
    render(
      <MenuProvider onClose={onClose}>
        <Probe />
      </MenuProvider>,
    );

    fireEvent.click(screen.getByText('toggle'));
    expect(screen.getByText('active:a')).toBeInTheDocument();
    fireEvent.click(screen.getByText('toggle'));
    expect(screen.getByText('active:none')).toBeInTheDocument();
    fireEvent.click(screen.getByText('close'));
    expect(onClose).toHaveBeenCalled();
  });

  it('the menu hook refuses to work outside a provider', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    function Probe() {
      useMenuContext();
      return null;
    }
    expect(() => render(<Probe />)).toThrow('useMenuContext must be used within a MenuProvider');
  });
});
