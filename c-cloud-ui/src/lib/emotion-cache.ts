'use client';

import createCache from '@emotion/cache';

const isBrowser = typeof document !== 'undefined';

export default function createEmotionCache() {
  return createCache({
    key: 'css',
    prepend: true,
    container: isBrowser ? document.head : undefined,
  });
}