'use client';

import createCache from '@emotion/cache';

export default function createEmotionCache() {
  const isBrowser = typeof document !== 'undefined';

  return createCache({
    key: 'css',
    prepend: true,
    container: isBrowser ? document.head : undefined,
  });
}