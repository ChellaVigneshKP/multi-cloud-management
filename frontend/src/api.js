import axios from 'axios';

// Define base API URL once
const BASE_API_URL = process.env.REACT_APP_API_BASE_URL || 'http://192.168.1.8:6061';

// Create Axios instance
const api = axios.create({
  baseURL: BASE_API_URL,
  withCredentials: true,  // Include cookies in requests
});

// In-memory variable for storing the access token
let accessToken = null;

// Function to set the access token
export const setAccessToken = (token) => {
  accessToken = token;
};

// Function to clear the access token
export const clearAccessToken = () => {
  accessToken = null;
};

// Request Interceptor
api.interceptors.request.use(
  (config) => {
    // Attach the access token from memory
    if (accessToken) {
      config.headers['Authorization'] = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Variables for refresh token management
let isRefreshing = false;
let subscribers = [];

// Function to notify all subscribers with the new token
function onAccessTokenFetched(newAccessToken) {
  subscribers.forEach((callback) => callback(newAccessToken));
  subscribers = [];
}

// Function to add subscribers to the queue
function addSubscriber(callback) {
  subscribers.push(callback);
}

// Response Interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 errors with a single refresh attempt
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (!isRefreshing) {
        isRefreshing = true;

        try {
          // Attempt to refresh the token
          const response = await axios.post(
            `${BASE_API_URL}/auth/refresh-token`,
            {},
            { withCredentials: true } // Ensure cookies are sent
          );

          // Update the access token in memory
          accessToken = response.data.accessToken;

          // Notify all queued requests with the new token
          onAccessTokenFetched(accessToken);
        } catch (err) {
          // If refresh token fails, clear access token and redirect to login
          clearAccessToken();
          window.location.href = '/login';
          return Promise.reject(err);
        } finally {
          isRefreshing = false;
        }
      }

      // Queue subsequent requests until the token refresh completes
      return new Promise((resolve) => {
        addSubscriber((newToken) => {
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          resolve(api(originalRequest));
        });
      });
    }

    // Reject any other errors
    return Promise.reject(error);
  }
);

export default api;
