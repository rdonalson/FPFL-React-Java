import { describe, expect, it } from 'vitest';
import { act, render, screen } from '@testing-library/react';
import { ApiErrorBoundary } from './ApiErrorBoundary';
import { InLineMessage } from '@/components/InLineMessage';
import { BreakpointOverlay } from '@/components/BreakpointOverlay';

describe('ApiErrorBoundary', () => {
  it('renders its children without an error', () => {
    render(<ApiErrorBoundary error={null}>content</ApiErrorBoundary>);
    expect(screen.getByText('content')).toBeInTheDocument();
  });

  it('shows 4xx errors inline instead of the content', () => {
    render(
      <ApiErrorBoundary error={{ status: 404, message: 'Item 9 not found' }}>
        content
      </ApiErrorBoundary>,
    );
    expect(screen.getByText('404: Item 9 not found')).toBeInTheDocument();
    expect(screen.queryByText('content')).not.toBeInTheDocument();
  });

  it('leaves 5xx and network errors to the global toast', () => {
    const { container } = render(
      <ApiErrorBoundary error={{ status: 500, message: 'x' }}>content</ApiErrorBoundary>,
    );
    expect(container).toBeEmptyDOMElement();
  });
});

describe('InLineMessage', () => {
  it('prefixes the status when there is one', () => {
    render(<InLineMessage severity="error" status={400} message="Bad request" />);
    render(<InLineMessage severity="info" message="Heads up" />);
    expect(screen.getByText('400: Bad request')).toBeInTheDocument();
    expect(screen.getByText('Heads up')).toBeInTheDocument();
  });
});

describe('BreakpointOverlay', () => {
  it('shows the width and follows resizes', () => {
    window.innerWidth = 1024;
    render(<BreakpointOverlay />);
    expect(screen.getByText('1024px')).toBeInTheDocument();
    expect(screen.getByText('LG')).toHaveStyle({ opacity: '1' });

    act(() => {
      window.innerWidth = 500;
      window.dispatchEvent(new Event('resize'));
    });
    expect(screen.getByText('500px')).toBeInTheDocument();
    expect(screen.getByText('SM')).toHaveStyle({ opacity: '1' });
  });
});
