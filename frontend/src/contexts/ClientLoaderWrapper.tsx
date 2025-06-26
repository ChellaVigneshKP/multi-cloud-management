'use client';

import { useAuth } from '@/contexts/auth-context';
import CustomLoader from '@/components/CustomLoader';

export default function ClientLoaderWrapper({ children }: Readonly<{ children: React.ReactNode }>) {
  const { loading } = useAuth();

  if (loading) {
    return <CustomLoader />;
  }
  return <>{children}</>;
}