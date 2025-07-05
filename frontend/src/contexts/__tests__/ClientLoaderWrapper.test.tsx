import React from 'react';
import { render, screen } from '@testing-library/react';
import ClientLoaderWrapper from '../ClientLoaderWrapper'; // adjust path if needed
import { useAuth } from '@/contexts/auth-context';

// Mock CustomLoader
jest.mock('@/components/CustomLoader', () => () => <div data-testid="custom-loader">Loading...</div>);

// Mock useAuth
jest.mock('@/contexts/auth-context', () => ({
  useAuth: jest.fn(),
}));

describe('ClientLoaderWrapper', () => {
  it('renders loader when loading is true', () => {
    (useAuth as jest.Mock).mockReturnValue({ loading: true });

    render(
      <ClientLoaderWrapper>
        <div>Child content</div>
      </ClientLoaderWrapper>
    );

    expect(screen.getByTestId('custom-loader')).toBeInTheDocument();
    expect(screen.queryByText('Child content')).not.toBeInTheDocument();
  });

  it('renders children when loading is false', () => {
    (useAuth as jest.Mock).mockReturnValue({ loading: false });

    render(
      <ClientLoaderWrapper>
        <div data-testid="child-content">Child content</div>
      </ClientLoaderWrapper>
    );

    expect(screen.getByTestId('child-content')).toBeInTheDocument();
    expect(screen.queryByTestId('custom-loader')).not.toBeInTheDocument();
  });
});