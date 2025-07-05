import createEmotionCache from '../emotion-cache';
import createCache from '@emotion/cache';

jest.mock('@emotion/cache', () => jest.fn());

describe('createEmotionCache', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should call createCache with correct config in browser', () => {
    const mockCreateCache = createCache as jest.Mock;

    // Mock document for browser environment
    global.document = {
      head: {} as HTMLHeadElement,
    } as Document;

    createEmotionCache();

    expect(mockCreateCache).toHaveBeenCalledWith({
      key: 'css',
      prepend: true,
      container: document.head,
    });
  });

  it('should call createCache with undefined container in SSR', () => {
    const mockCreateCache = createCache as jest.Mock;

    // Simulate SSR (no document)
    // @ts-ignore
    delete global.document;

    createEmotionCache();

    expect(mockCreateCache).toHaveBeenCalledWith({
      key: 'css',
      prepend: true,
      container: undefined,
    });
  });
});
