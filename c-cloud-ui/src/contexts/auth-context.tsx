'use client';

import {
    createContext,
    useContext,
    useState,
    useEffect,
    useMemo,
    useCallback,
    ReactNode,
} from 'react';
import axios, { AxiosInstance } from 'axios';
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
    // Extend this interface based on your backend response
}

interface AuthContextType {
    isAuthenticated: boolean | null;
    user: User | null;
    login: (credentials: Credentials) => Promise<void>;
    logout: () => Promise<void>;
    api: AxiosInstance;
    loading: boolean;
}

// Initial context value
const AuthContext = createContext<AuthContextType | null>(null);

// Axios instance
const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL ?? 'https://localhost:6061',
    withCredentials: true, // Include credentials for cookie-based auth
});

// Token refresh state
let isRefreshing = false;
let refreshTokenPromise: Promise<unknown> | null = null;

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    const logout = useCallback(async () => {
        try {
            await api.post('/auth/logout');
        } catch (err) {
            console.error('Logout failed:', err);
        } finally {
            setIsAuthenticated(false);
            setUser(null);
            router.push('/login');
        }
    }, [router]);

    // Axios interceptor for handling token refresh on 401
    useEffect(() => {
        const interceptor = api.interceptors.response.use(
            res => res,
            async (err) => {
                const originalRequest = err.config;

                if (
                    err.response?.status === 401 &&
                    !originalRequest._retry &&
                    !originalRequest.url.includes('/auth/login') &&
                    isAuthenticated
                ) {
                    originalRequest._retry = true;

                    if (!isRefreshing) {
                        isRefreshing = true;
                        refreshTokenPromise = api.post('/auth/refresh-token').finally(() => {
                            isRefreshing = false;
                        });
                    }

                    await refreshTokenPromise;
                    return api(originalRequest);
                }

                return Promise.reject(err instanceof Error ? err : new Error(JSON.stringify(err)));
            }
        );

        return () => api.interceptors.response.eject(interceptor);
    }, [isAuthenticated]);

    // Fetch user info
    const fetchUser = useCallback(async () => {
        try {
            setLoading(true);
            const res = await api.get<User>('/auth/userinfo');
            setUser(res.data);
            setIsAuthenticated(true);
        } catch {
            setUser(null);
            setIsAuthenticated(false);
            toast.error('Failed to fetch user info');
        } finally {
            setLoading(false);
        }
    }, []);

    // Initialize auth on app load
    useEffect(() => {
        const initializeAuth = async () => {
            await fetchUser(); // Let backend verify token via cookies
        };
        initializeAuth();
    }, [fetchUser]);

    const login = useCallback(async (credentials: Credentials) => {
        try {
            await api.post('/auth/login', credentials); // Cookies set by backend
            await fetchUser();
        } catch (error) {
            setIsAuthenticated(false);
            throw error;
        }
    }, [fetchUser]);

    const value: AuthContextType = useMemo(() => ({
        isAuthenticated,
        user,
        login,
        logout,
        api,
        loading,
    }), [isAuthenticated, user, login, logout, loading]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Hook to consume the auth context
export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};

export { AuthContext, api };