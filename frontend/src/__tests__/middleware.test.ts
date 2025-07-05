import { middleware } from '../middleware';
import { NextRequest, NextResponse } from 'next/server';

jest.mock('next/server', () => ({
  NextResponse: {
    redirect: jest.fn(),
    next: jest.fn(),
  },
}));

const mockRedirect = NextResponse.redirect as jest.Mock;
const mockNext = NextResponse.next as jest.Mock;

const createMockRequest = (
  pathname: string,
  isAuthenticated = 'false'
): NextRequest =>
  ({
    nextUrl: {
      pathname,
      origin: 'http://localhost',
      href: `http://localhost${pathname}`,
    },
    url: `http://localhost${pathname}`,
    cookies: {
      get: (key: string) =>
        key === 'isAuthenticated'
          ? { name: 'isAuthenticated', value: isAuthenticated }
          : undefined,
    },
  } as unknown as NextRequest);

describe('middleware', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('redirects unauthenticated user from protected route to /login', () => {
    const req = createMockRequest('/dashboard', 'false');
    middleware(req);
    expect(mockRedirect).toHaveBeenCalledWith(new URL('/login', req.url));
  });

  it('allows unauthenticated access to non-protected route', () => {
    const req = createMockRequest('/about', 'false');
    middleware(req);
    expect(mockNext).toHaveBeenCalled();
  });

  it('redirects authenticated user from /login to /dashboard', () => {
    const req = createMockRequest('/login', 'true');
    middleware(req);
    expect(mockRedirect).toHaveBeenCalledWith(new URL('/dashboard', req.url));
  });

  it('allows authenticated user to access protected route', () => {
    const req = createMockRequest('/vms', 'true');
    middleware(req);
    expect(mockNext).toHaveBeenCalled();
  });

  it('allows unauthenticated user to access /login', () => {
    const req = createMockRequest('/login', 'false');
    middleware(req);
    expect(mockNext).toHaveBeenCalled();
  });
});
