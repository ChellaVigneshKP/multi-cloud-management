'use client';

import { ReactNode } from 'react';
import { CacheProvider } from '@emotion/react';
import createEmotionCache from '@/lib/emotion-cache';

type Props = {
  children: ReactNode;
};

const cache = createEmotionCache();

export default function EmotionRegistry({ children }: Readonly<Props>) {
  return <CacheProvider value={cache}>{children}</CacheProvider>;
}