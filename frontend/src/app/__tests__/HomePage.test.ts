import HomePage from '../page';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';

// 1. Mock next/headers and next/navigation
jest.mock('next/headers', () => ({
  cookies: jest.fn(),
}));

jest.mock('next/navigation', () => ({
  redirect: jest.fn(() => { throw new Error('redirect'); }), // behaves like real redirect
}));

// 2. Cast with 'unknown' first to satisfy TypeScript
const mockCookies = cookies as unknown as jest.Mock;
const mockRedirect = redirect as unknown as jest.Mock;

describe('HomePage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('redirects to /dashboard if auth_token is present', async () => {
    mockCookies.mockReturnValue({
      get: (key: string) =>
        key === 'auth_token' ? { name: 'auth_token', value: 'abc123' } : undefined,
    });

    try {
      await HomePage();
    } catch (err) {
        expect((err as Error).message).toBe('redirect');
    } // Ignore the throw

    expect(mockRedirect).toHaveBeenCalledWith('/dashboard');
  });

  it('redirects to /login if auth_token is missing', async () => {
    mockCookies.mockReturnValue({
      get: () => undefined,
    });

    try {
      await HomePage();
    } catch (err) {
        expect((err as Error).message).toBe('redirect');
    } // Ignore the throw

    expect(mockRedirect).toHaveBeenCalledWith('/login');
  });
});