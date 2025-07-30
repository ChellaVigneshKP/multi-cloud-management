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
import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { useRouter } from 'next/navigation';
import { toast } from 'react-toastify';

// Constants
const MAX_RETRY_ATTEMPTS = 2;
const CSRF_RETRY_ATTEMPTS = 2;
const REFRESH_RETRY_DELAY = 1000;
const AUTH_CHECK_INTERVAL = 5 * 60 * 1000; // 5 minutes
const CSRF_RETRY_DELAY = 1000;
const USERINFO_RETRY_DELAY = 1000;
const AUTH_FAILURE_COOLDOWN = 10000; // 10 seconds cooldown after auth failure
const MISSING_AUTH_COOKIE_ERROR = 'Missing authorization cookie';

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
    error?: string;
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
    const activeAbortControllers = useRef<Set<AbortController>>(new Set());
    const lastUserInfoFetchRef = useRef<number>(0);
    const userInfoFetchState = useRef({
        isFetching: false,
        promise: null as Promise<void> | null,
    });
    const timeZoneRef = useRef<string>(Intl.DateTimeFormat().resolvedOptions().timeZone);
    const consecutiveAuthFailuresRef = useRef(0);
    const lastAuthFailureRef = useRef<number | null>(null);

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
            userInfoFetchState.current.promise = null;

            if (authCheckIntervalRef.current) {
                clearInterval(authCheckIntervalRef.current);
            }

            // Abort all active requests
            activeAbortControllers.current.forEach(controller => controller.abort('Component unmounted'));
            activeAbortControllers.current.clear();
        };
    }, []);

    const createAbortController = () => {
        const controller = new AbortController();
        activeAbortControllers.current.add(controller);
        return controller;
    };

    const removeAbortController = (controller: AbortController) => {
        activeAbortControllers.current.delete(controller);
    };

    const setAuthCookie = useCallback((value: boolean) => {
        const cookieValue = value 
            ? `isAuthenticated=true; path=/; max-age=${60 * 60 * 24 * 30}; SameSite=Lax; secure`
            : 'isAuthenticated=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Lax; secure';
        document.cookie = cookieValue;
    }, []);

    const clearAuthState = useCallback(() => {
        setAuthCookie(false);
        if (isMountedRef.current) {
            setIsAuthenticated(false);
            setUser(null);
            setLoading(false);
        }
        csrfTokenRef.current = null;
        csrfFetchState.current = { isFetching: false, promise: null };
        lastUserInfoFetchRef.current = 0;
        consecutiveAuthFailuresRef.current = 0;
        lastAuthFailureRef.current = Date.now();
    }, [setAuthCookie]);

    const logout = useCallback(async () => {
        if (isLoggingOutRef.current) return;
        isLoggingOutRef.current = true;
        
        try {
            const controller = createAbortController();
            await api.post('/auth/logout', {}, { signal: controller.signal });
        } catch (err) {
            if (!axios.isCancel(err)) {
                console.error('Logout failed:', err);
            }
        } finally {
            setAuthCookie(false); // Clear cookie on logout
            clearAuthState();

            if (authCheckIntervalRef.current) {
                clearInterval(authCheckIntervalRef.current);
                authCheckIntervalRef.current = null;
            }

            isLoggingOutRef.current = false;
            router.push('/login');
        }
    }, [router, clearAuthState]);

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
                const controller = createAbortController();

                try {
                    const res = await api.get<{ token: string }>('/csrf', {
                        signal: controller.signal
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
                        if (isMountedRef.current) {
                            toast.error('Failed to fetch CSRF token');
                        }
                        throw new AuthError('Max CSRF retries exceeded');
                    }
                    await new Promise(resolve => setTimeout(resolve, CSRF_RETRY_DELAY));
                } finally {
                    removeAbortController(controller);
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
                extendedConfig.headers['X-Timezone'] = timeZoneRef.current;

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
        const controller = createAbortController();

        try {
            await api.post('/auth/refresh-token', {}, { signal: controller.signal });
            consecutiveAuthFailuresRef.current = 0; // Reset on success
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
            removeAbortController(controller);
        }
    }, []);

    const shouldRetryRequest = (error: AxiosError<ApiErrorResponse>): boolean => {
        return (
            error.response?.status === 401 &&
            error.response?.data?.error === MISSING_AUTH_COOKIE_ERROR
        );
    };

    const handleApiError = useCallback(async (error: unknown) => {
        if (axios.isCancel(error)) {
            return Promise.reject(error);
        }

        if (!axios.isAxiosError(error)) {
            return Promise.reject(error);
        }

        const axiosError = error as AxiosError<ApiErrorResponse>;
        const originalRequest = axiosError.config as ExtendedAxiosRequestConfig | undefined;

        // Check if we're in cooldown period
        const now = Date.now();
        if (lastAuthFailureRef.current && now - lastAuthFailureRef.current < AUTH_FAILURE_COOLDOWN) {
            return Promise.reject(new AuthError('In auth failure cooldown period', true));
        }

        // Handle token refresh for 401 errors with specific error message
        if (
            shouldRetryRequest(axiosError) &&
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
                        consecutiveAuthFailuresRef.current += 1;
                        lastAuthFailureRef.current = Date.now();
                        
                        if (refreshError instanceof AuthError && refreshError.isUnauthorized && isMountedRef.current) {
                            await logout();
                        }
                        return Promise.reject(refreshError);
                    })
                    .finally(() => {
                        refreshState.current.isRefreshing = false;
                        refreshState.current.promise = null;
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
            consecutiveAuthFailuresRef.current += 1;
            lastAuthFailureRef.current = Date.now();
            
            if (isAuthenticatedRef.current && isMountedRef.current) {
                if (axiosError.response?.data?.error === MISSING_AUTH_COOKIE_ERROR) {
                    router.push('/login');
                }
                await logout();
            }
            return Promise.reject(new AuthError('Session expired', true));
        }

        // Show toast only for non-401 errors and if component is mounted
        if (isMountedRef.current && axiosError.response?.status !== 401) {
            toast.error(errorMessage);
        }

        return Promise.reject(new Error(errorMessage));
    }, [logout, refreshTokenWithBackoff, router]);

    useEffect(() => {
        const interceptor = api.interceptors.response.use(
            (response: AxiosResponse) => response,
            handleApiError
        );

        return () => api.interceptors.response.eject(interceptor);
    }, [handleApiError]);

    const fetchUser = useCallback(async (): Promise<void> => {
        // If we're in cooldown period, skip the request
        const now = Date.now();
        if (lastAuthFailureRef.current && now - lastAuthFailureRef.current < AUTH_FAILURE_COOLDOWN) {
            return;
        }

        // If we're already fetching, return the existing promise
        if (userInfoFetchState.current.promise) {
            return userInfoFetchState.current.promise;
        }

        // Don't fetch if we just fetched recently (within 1 second)
        if (now - lastUserInfoFetchRef.current < 1000) {
            return;
        }

        userInfoFetchState.current.isFetching = true;
        userInfoFetchState.current.promise = (async () => {
            for (let attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                const controller = createAbortController();

                try {
                    if (isMountedRef.current) {
                        setLoading(true);
                    }

                    const res = await api.get<User>('/auth/userinfo', { 
                        signal: controller.signal 
                    });
                    lastUserInfoFetchRef.current = Date.now();
                    consecutiveAuthFailuresRef.current = 0;

                    if (isMountedRef.current) {
                        setUser(res.data);
                        setIsAuthenticated(true);
                    }
                    return; // Success - exit the retry loop
                } catch (error) {
                    if (axios.isCancel(error)) {
                        return;
                    }

                    if (isMountedRef.current) {
                        setUser(null);
                        setIsAuthenticated(false);
                    }

                    if (error instanceof AuthError && error.isUnauthorized) {
                        consecutiveAuthFailuresRef.current += 1;
                        lastAuthFailureRef.current = Date.now();
                        throw error;
                    }

                    if (attempt === MAX_RETRY_ATTEMPTS) {
                        throw new Error('Failed to fetch user after maximum retries');
                    }

                    // Wait before retrying
                    await new Promise(resolve => setTimeout(resolve, USERINFO_RETRY_DELAY));
                } finally {
                    removeAbortController(controller);

                    if (isMountedRef.current) {
                        setLoading(false);
                        setIsInitializing(false);
                    }
                }
            }
        })();

        try {
            return await userInfoFetchState.current.promise;
        } finally {
            userInfoFetchState.current.isFetching = false;
            userInfoFetchState.current.promise = null;
        }
    }, []);

    const refreshAuth = useCallback(async (): Promise<void> => {
        try {
            await fetchUser();
        } catch (error) {
            if (isMountedRef.current) {
                console.error('Failed to refresh auth:', error);
            }
            throw error;
        }
    }, [fetchUser]);

    useEffect(() => {
        const abortController = new AbortController();

        const initialize = async () => {
            try {
                await fetchCsrfToken();
                await fetchUser();
                
                // Only start the interval if we successfully authenticated
                if (isAuthenticatedRef.current && isMountedRef.current && !authCheckIntervalRef.current) {
                    authCheckIntervalRef.current = setInterval(() => {
                        if (isAuthenticatedRef.current && !loading && isMountedRef.current) {
                            // Skip refresh if we're in cooldown period
                            const now = Date.now();
                            if (!lastAuthFailureRef.current || now - lastAuthFailureRef.current >= AUTH_FAILURE_COOLDOWN) {
                                refreshAuth().catch(() => {});
                            }
                        }
                    }, AUTH_CHECK_INTERVAL);
                }
            } catch (error) {
                if (abortController.signal.aborted || !isMountedRef.current) return;

                console.error('Initialization error:', error);

                if (isMountedRef.current) {
                    setIsInitializing(false);
                    setLoading(false);
                }
            }
        };

        initialize();

        return () => {
            abortController.abort();
        };
    }, [fetchUser, fetchCsrfToken, loading, refreshAuth]);

    const login = useCallback(async (credentials: Credentials): Promise<void> => {
        if (pendingLoginRef.current) {
            return pendingLoginRef.current;
        }

        const controller = createAbortController();

        try {
            if (isMountedRef.current) {
                setLoading(true);
            }

            pendingLoginRef.current = (async () => {
                try {
                    await fetchCsrfToken();
                    await api.post('/auth/login', credentials, { signal: controller.signal });
                    setAuthCookie(true); // Set cookie on successful login
                    consecutiveAuthFailuresRef.current = 0;
                    lastAuthFailureRef.current = null;
                    await fetchUser();
                } finally {
                    pendingLoginRef.current = null;
                    removeAbortController(controller);
                }
            })();

            await pendingLoginRef.current;
        } catch (error) {
            setAuthCookie(false); // Ensure cookie is cleared on login failure
            if (axios.isCancel(error)) {
                return;
            }

            if (isMountedRef.current) {
                setIsAuthenticated(false);
                setLoading(false);
            }

            if (axios.isAxiosError(error) && isMountedRef.current) {
                const message = error.response?.data?.message || 'Login failed';
                toast.error(message);
                throw new AuthError(message, error.response?.status === 401);
            }

            throw error;
        }
    }, [fetchUser, fetchCsrfToken, setAuthCookie]);

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