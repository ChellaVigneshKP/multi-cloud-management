import { NextRequest, NextResponse } from 'next/server';

export function middleware(req: NextRequest) {
  const isAuthenticated = req.cookies.get('isAuthenticated')?.value === 'true';
  const { pathname } = req.nextUrl;

  const isProtected =
    pathname.startsWith('/dashboard') ||
    pathname.startsWith('/profile') ||
    pathname.startsWith('/settings') ||
    pathname.startsWith('/clouds') ||
    pathname.startsWith('/vms');

  // ✅ Redirect unauthenticated users trying to access protected routes
  if (isProtected && !isAuthenticated) {
    return NextResponse.redirect(new URL('/login', req.url));
  }

  // ✅ Redirect authenticated users trying to access login page
  if (pathname === '/login' && isAuthenticated) {
    return NextResponse.redirect(new URL('/dashboard', req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/dashboard',
    '/profile',
    '/settings',
    '/clouds',
    '/vms',
    '/login',
  ],
};
