import { NextRequest, NextResponse } from 'next/server';

export function middleware(req: NextRequest) {
  const isAuthenticated = req.cookies.get('isAuthenticated')?.value === 'true';
  const { pathname } = req.nextUrl;

  const protectedRoutes = [
    '/dashboard',
    '/profile',
    '/settings',
    '/clouds',
    '/vms'
  ];

  const isProtected = protectedRoutes.some(route => pathname.startsWith(route));
  const isAuthRoute = pathname === '/login' || pathname === '/register';

  // Redirect unauthenticated users trying to access protected routes
  if (isProtected && !isAuthenticated) {
    const loginUrl = new URL('/login', req.url);
    loginUrl.searchParams.set('from', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // Redirect authenticated users away from auth routes
  if (isAuthRoute && isAuthenticated) {
    const redirectTo = req.nextUrl.searchParams.get('from') || '/dashboard';
    return NextResponse.redirect(new URL(redirectTo, req.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/dashboard/:path*',
    '/profile/:path*',
    '/settings/:path*',
    '/clouds/:path*',
    '/vms/:path*',
    '/login',
    '/register'
  ],
};