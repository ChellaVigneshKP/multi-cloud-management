// src/components/AuthContext.js
import React, { createContext, useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { initializeApi } from '../api';
import Cookies from 'js-cookie';

const AuthContext = createContext();

// Variables to manage token refreshing status
let isRefreshing = false;
let subscribers = [];

// Notify all subscribers when token is refreshed
function onAccessTokenFetched() {
  subscribers.forEach((callback) => callback());
  subscribers = [];
}

// Add a request to the subscribers queue
function addSubscriber(callback) {
  subscribers.push(callback);
}

const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    const authFlag = Cookies.get('isAuthenticated');
    return authFlag === 'true';
  });
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  // Initialize the API instance
  const api = initializeApi();

  // Define `logout` after `api` is initialized
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

  // Set up the interceptor after `logout` is defined
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
          isAuthenticated // Check if user is authenticated
        ) {
          originalRequest._retry = true;

          if (!isRefreshing) {
            isRefreshing = true;
            try {
              await api.post('/auth/refresh-token', {}, { withCredentials: true });
              onAccessTokenFetched();
            } catch (err) {
              await logout();
              return Promise.reject(err);
            } finally {
              isRefreshing = false;
            }
          }

          // Wait until the token refresh completes before retrying the request
          return new Promise((resolve) => {
            addSubscriber(() => resolve(api(originalRequest)));
          });
        }

        return Promise.reject(error);
      }
    );

    // Eject the interceptor when the component unmounts
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

  // Fetch user data if authenticated and user data is not already loaded
  useEffect(() => {
    if (isAuthenticated && !user) {
      fetchUserData();
    }
  }, [isAuthenticated, user, fetchUserData]);

  const login = async (credentials) => {
    try {
      await api.post('/auth/login', credentials, { withCredentials: true });
      Cookies.set('isAuthenticated', 'true', { expires: 7, path: '/' });
      setIsAuthenticated(true);
      await fetchUserData(); // Fetch user data after successful login
    } catch (error) {
      throw error; // Re-throw the error to be handled in the login page
    }
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout, api }}>
      {children}
    </AuthContext.Provider>
  );
};

export { AuthContext, AuthProvider };
