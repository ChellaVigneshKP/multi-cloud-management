'use client';

import React, {
    createContext,
    useContext,
    useState,
    useEffect,
    useMemo,
    useCallback,
    ReactNode,
    useRef
} from 'react';
import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig, CancelTokenSource, AxiosResponse } from 'axios';
import { useRouter } from 'next/navigation';
import { toast } from 'react-toastify';

interface Credentials {
    email: string;
    password: string;
    visitorId?: string | null;
    remember?: boolean;
}

interface User {
    id: string;
    username: string;
    email: string;
}

interface AuthContextType {
    isAuthenticated: boolean;
    user: User | null;
    login: (credentials: Credentials) => Promise<void>;
    logout: () => Promise<void>;
    api: AxiosInstance;
    loading: boolean;
    isInitializing: boolean;
    refreshAuth: () => Promise<void>;
}

interface ExtendedAxiosRequestConfig extends InternalAxiosRequestConfig {
    _retryCount?: number;
    _retry?: boolean;
}

interface ApiErrorResponse {
    message: string;
    code?: string;
    [key: string]: any;
}

class AuthError extends Error {
    constructor(message: string, public isUnauthorized: boolean = false) {
        super(message);
        this.name = 'AuthError';
    }
}

const AuthContext = createContext<AuthContextType | null>(null);

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://localhost:6061',
    withCredentials: true,
    timeout: 30000, // 30 seconds timeout
});

const MAX_RETRY_ATTEMPTS = 3;
const CSRF_RETRY_ATTEMPTS = 2;
const REFRESH_RETRY_DELAY = 1000;
const AUTH_CHECK_INTERVAL = 5 * 60 * 1000; // 5 minutes
const CSRF_RETRY_DELAY = 1000;

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);
    const [isInitializing, setIsInitializing] = useState(true);
    const router = useRouter();
    const isLoggingOutRef = useRef(false);

    const isAuthenticatedRef = useRef(isAuthenticated);
    const csrfTokenRef = useRef<string | null>(null);
    const csrfFetchState = useRef({
        isFetching: false,
        promise: null as Promise<string> | null,
    });
    const refreshState = useRef({
        isRefreshing: false,
        promise: null as Promise<void> | null,
    });
    const authCheckIntervalRef = useRef<NodeJS.Timeout | null>(null);
    const pendingLoginRef = useRef<Promise<void> | null>(null);
    const isMountedRef = useRef(true);
    const activeRequestsRef = useRef<Set<CancelTokenSource>>(new Set());

    // Update refs when state changes
    useEffect(() => {
        isAuthenticatedRef.current = isAuthenticated;
    }, [isAuthenticated]);

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            isMountedRef.current = false;
            pendingLoginRef.current = null;
            refreshState.current.promise = null;
            csrfFetchState.current.promise = null;

            if (authCheckIntervalRef.current) {
                clearInterval(authCheckIntervalRef.current);
            }

            // Cancel all active requests
            activeRequestsRef.current.forEach(source => source.cancel('Component unmounted'));
            activeRequestsRef.current.clear();
        };
    }, []);

    const createCancelToken = () => {
        const source = axios.CancelToken.source();
        activeRequestsRef.current.add(source);
        return source.token;
    };

    const removeCancelToken = (source: CancelTokenSource) => {
        activeRequestsRef.current.delete(source);
    };

    const logout = useCallback(async () => {
        if (isLoggingOutRef.current) return; // Already logging out
        isLoggingOutRef.current = true;
        try {
            const cancelToken = createCancelToken();
            await api.post('/auth/logout', {}, { cancelToken });
        } catch (err) {
            if (!axios.isCancel(err)) {
                console.error('Logout failed:', err);
            }
        } finally {
            if (isMountedRef.current) {
                setIsAuthenticated(false);
                setUser(null);
                setLoading(false);
            }

            csrfTokenRef.current = null;
            

            if (authCheckIntervalRef.current) {
                clearInterval(authCheckIntervalRef.current);
                authCheckIntervalRef.current = null;
            }

            router.push('/login');
        }
    }, [router]);

    const fetchCsrfToken = useCallback(async (): Promise<string> => {
        if (csrfTokenRef.current) {
            return csrfTokenRef.current;
        }

        if (csrfFetchState.current.promise) {
            return await csrfFetchState.current.promise;
        }

        csrfFetchState.current.isFetching = true;
        csrfFetchState.current.promise = (async () => {
            for (let attempt = 0; attempt < CSRF_RETRY_ATTEMPTS; attempt++) {
                const source = axios.CancelToken.source();
                activeRequestsRef.current.add(source);

                try {
                    const res = await api.get<{ token: string }>('/csrf', {
                        cancelToken: source.token
                    });
                    const token = res.data?.token;
                    if (!token) throw new AuthError('No CSRF token received');
                    csrfTokenRef.current = token;
                    return token;
                } catch (err) {
                    if (axios.isCancel(err)) {
                        throw err;
                    }

                    if (attempt === CSRF_RETRY_ATTEMPTS - 1) {
                        toast.error('Failed to fetch CSRF token');
                        throw new AuthError('Max CSRF retries exceeded');
                    }
                    await new Promise(resolve => setTimeout(resolve, CSRF_RETRY_DELAY));
                } finally {
                    activeRequestsRef.current.delete(source);
                }
            }
            throw new AuthError('Max CSRF retries exceeded');
        })();

        try {
            return await csrfFetchState.current.promise;
        } finally {
            csrfFetchState.current.isFetching = false;
            csrfFetchState.current.promise = null;
        }
    }, []);

    useEffect(() => {
        const requestInterceptor = api.interceptors.request.use(
            async (config: InternalAxiosRequestConfig) => {
                const extendedConfig = config as ExtendedAxiosRequestConfig;
                extendedConfig.headers['X-Timezone'] = Intl.DateTimeFormat().resolvedOptions().timeZone;

                const safeMethods = ['get', 'head', 'options'];
                if (
                    safeMethods.includes(extendedConfig.method?.toLowerCase() || '') ||
                    !extendedConfig.url?.startsWith('/')
                ) {
                    return extendedConfig;
                }

                try {
                    const token = await fetchCsrfToken();
                    extendedConfig.headers['X-XSRF-TOKEN'] = token;
                } catch (error) {
                    if (error instanceof AuthError) {
                        await logout();
                    }
                    return Promise.reject(error);
                }
                return extendedConfig;
            },
            error => Promise.reject(error)
        );

        return () => api.interceptors.request.eject(requestInterceptor);
    }, [fetchCsrfToken, logout]);

    const refreshTokenWithBackoff = useCallback(async (attempt = 1): Promise<void> => {
        const source = axios.CancelToken.source();
        activeRequestsRef.current.add(source);

        try {
            await api.post('/auth/refresh-token', {}, { cancelToken: source.token });
        } catch (error) {
            if (axios.isCancel(error)) {
                throw error;
            }

            if (attempt < MAX_RETRY_ATTEMPTS) {
                const delay = REFRESH_RETRY_DELAY * Math.pow(2, attempt - 1);
                await new Promise(resolve => setTimeout(resolve, delay));
                return refreshTokenWithBackoff(attempt + 1);
            }
            throw new AuthError('Token refresh failed', true);
        } finally {
            activeRequestsRef.current.delete(source);
        }
    }, []);

    const handleApiError = useCallback(async (error: unknown) => {
        if (axios.isCancel(error)) {
            return Promise.reject(error);
        }

        if (!axios.isAxiosError(error)) {
            return Promise.reject(error);
        }

        const axiosError = error as AxiosError<ApiErrorResponse>;
        const originalRequest = axiosError.config as ExtendedAxiosRequestConfig | undefined;

        // Handle token refresh for 401 errors
        if (
            axiosError.response?.status === 401 &&
            originalRequest &&
            (originalRequest._retryCount ?? 0) < MAX_RETRY_ATTEMPTS &&
            !originalRequest.url?.includes('/auth/login') &&
            isAuthenticatedRef.current
        ) {
            originalRequest._retryCount = (originalRequest._retryCount ?? 0) + 1;

            if (!refreshState.current.isRefreshing) {
                refreshState.current.isRefreshing = true;
                refreshState.current.promise = refreshTokenWithBackoff()
                    .catch(async (refreshError) => {
                        if (refreshError instanceof AuthError && refreshError.isUnauthorized) {
                            await logout();
                        }
                        return Promise.reject(refreshError);
                    })
                    .finally(() => {
                        refreshState.current.isRefreshing = false;
                    });
            }

            try {
                await refreshState.current.promise;
                return api(originalRequest);
            } catch (refreshError) {
                return Promise.reject(refreshError);
            }
        }

        // Extract error message
        const errorMessage = axiosError.response?.data?.message ||
            axiosError.message ||
            'Request failed';

        if (axiosError.response?.status === 401) {
            if (isAuthenticatedRef.current) {
                await logout();
                return Promise.reject(new AuthError('Session expired', true));
            }

            return Promise.reject(new AuthError(errorMessage, true));
        }


        // Show toast for non-401 errors
        if (axiosError.response?.status !== 401) {
            toast.error(errorMessage);
        }

        return Promise.reject(new Error(errorMessage));
    }, [logout, refreshTokenWithBackoff]);

    useEffect(() => {
        const interceptor = api.interceptors.response.use(
            (response: AxiosResponse) => response,
            handleApiError
        );

        return () => api.interceptors.response.eject(interceptor);
    }, [handleApiError]);

    const fetchUser = useCallback(async (): Promise<void> => {
        const source = axios.CancelToken.source();
        activeRequestsRef.current.add(source);

        try {
            if (isMountedRef.current) {
                setLoading(true);
            }

            const res = await api.get<User>('/auth/userinfo', { cancelToken: source.token });

            if (isMountedRef.current) {
                setUser(res.data);
                setIsAuthenticated(true);
            }
        } catch (error) {
            if (axios.isCancel(error)) {
                return;
            }

            if (isMountedRef.current) {
                setUser(null);
                setIsAuthenticated(false);
            }

            if (error instanceof AuthError && error.isUnauthorized) {
                throw error;
            }

            throw new Error('Failed to fetch user');
        } finally {
            activeRequestsRef.current.delete(source);

            if (isMountedRef.current) {
                setLoading(false);
                setIsInitializing(false);
            }
        }
    }, []);

    const refreshAuth = useCallback(async (): Promise<void> => {
        try {
            await fetchUser();
        } catch (error) {
            console.error('Failed to refresh auth:', error);
            throw error;
        }
    }, [fetchUser]);

    useEffect(() => {
        const abortController = new AbortController();

        const initialize = async () => {
            try {
                await fetchCsrfToken();
                await fetchUser();
            } catch (error) {
                if (abortController.signal.aborted) return;

                console.error('Initialization error:', error);

                if (isMountedRef.current) {
                    setIsInitializing(false);
                    setLoading(false);
                }
            }
        };

        initialize();

        const interval = setInterval(() => {
            if (isAuthenticatedRef.current && !loading && isMountedRef.current) {
                refreshAuth().catch(() => { });
            }
        }, AUTH_CHECK_INTERVAL);

        if (authCheckIntervalRef.current) {
            clearInterval(authCheckIntervalRef.current);
        }
        authCheckIntervalRef.current = interval;


        return () => {
            abortController.abort();

            if (authCheckIntervalRef.current) {
                clearInterval(authCheckIntervalRef.current);
            }
        };
    }, [fetchUser, fetchCsrfToken, loading, refreshAuth]);

    const login = useCallback(async (credentials: Credentials): Promise<void> => {
        if (pendingLoginRef.current) {
            return pendingLoginRef.current;
        }

        const source = axios.CancelToken.source();
        activeRequestsRef.current.add(source);

        try {
            if (isMountedRef.current) {
                setLoading(true);
            }

            pendingLoginRef.current = (async () => {
                try {
                    await fetchCsrfToken();
                    await api.post('/auth/login', credentials, { cancelToken: source.token });
                    await fetchUser();
                } finally {
                    pendingLoginRef.current = null;
                    activeRequestsRef.current.delete(source);
                }
            })();

            await pendingLoginRef.current;
        } catch (error) {
            if (axios.isCancel(error)) {
                return;
            }

            if (isMountedRef.current) {
                setIsAuthenticated(false);
                setLoading(false);
            }

            if (axios.isAxiosError(error)) {
                const message = error.response?.data?.message || 'Login failed';
                toast.error(message);
                throw new AuthError(message, error.response?.status === 401);
            }

            throw error;
        }
    }, [fetchUser, fetchCsrfToken]);

    const value = useMemo<AuthContextType>(() => ({
        isAuthenticated,
        user,
        login,
        logout,
        api,
        loading,
        isInitializing,
        refreshAuth,
    }), [isAuthenticated, user, login, logout, loading, isInitializing, refreshAuth]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};

export { AuthContext, api };
export type { AuthContextType };