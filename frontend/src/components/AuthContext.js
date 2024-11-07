import React, { createContext, useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { initializeApi } from '../api';
import Cookies from 'js-cookie';
import PropTypes from 'prop-types';

const AuthContext = createContext();

let isRefreshing = false;
let refreshTokenPromise = null;

const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return Cookies.get('isAuthenticated') === 'true';
  });
  const [user, setUser] = useState(null);
  const navigate = useNavigate();
  const api = initializeApi();

  const logout = useCallback(async () => {
    try {
      await api.post('/auth/logout', {}, { withCredentials: true });
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      Cookies.remove('isAuthenticated', { path: '/' });
      setIsAuthenticated(false);
      setUser(null);
      navigate('/login');
    }
  }, [api, navigate]);

  useEffect(() => {
    const interceptor = api.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (
          error.response &&
          error.response.status === 401 &&
          !originalRequest._retry &&
          !originalRequest.url.includes('/auth/refresh-token') &&
          !originalRequest.url.includes('/auth/login') &&
          isAuthenticated
        ) {
          originalRequest._retry = true;

          if (!isRefreshing) {
            isRefreshing = true;
            refreshTokenPromise = api.post('/auth/refresh-token', {}, { withCredentials: true })
              .then(() => {
                isRefreshing = false;
              })
              .catch(async (err) => {
                isRefreshing = false;
                await logout();
                return Promise.reject(new Error('Token refresh failed'));
              });
          }

          try {
            // Wait for the token refresh to complete
            await refreshTokenPromise;

            // Retry the original request
            return api(originalRequest);
          } catch (err) {
            console.error('Error retrying original request after token refresh:', err); // Log the error or take action
            throw err; // Reject the Promise with an Error
          }
        }

        return Promise.reject(error instanceof Error ? error : new Error(error));
      }
    );

    return () => {
      api.interceptors.response.eject(interceptor);
    };
  }, [api, logout, isAuthenticated]);

  const fetchUserData = useCallback(async () => {
    try {
      const response = await api.get('/auth/userinfo', { withCredentials: true });
      setUser(response.data);
      setIsAuthenticated(true);
    } catch (error) {
      setIsAuthenticated(false);
      setUser(null);
    }
  }, [api]);

  useEffect(() => {
    if (isAuthenticated && !user) {
      fetchUserData();
    }
  }, [isAuthenticated, user, fetchUserData]);

  const login = useCallback(async (credentials) => {
    try {
      await api.post('/auth/login', credentials, { withCredentials: true });
      Cookies.set('isAuthenticated', 'true', { expires: 7, path: '/' });
      setIsAuthenticated(true);
      await fetchUserData();
    } catch (error) {
      throw new Error('Login failed');
    }
  }, [api, fetchUserData]);

  const contextValue = useMemo(() => ({
    isAuthenticated,
    user,
    login,
    logout,
    api,
  }), [isAuthenticated, user, login, logout, api]);

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

AuthProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export { AuthContext, AuthProvider };
