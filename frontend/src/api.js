import axios from 'axios';
import Cookies from 'js-cookie';

// Define base API URL once
const BASE_API_URL = process.env.REACT_APP_API_BASE_URL || 'http://192.168.1.8:6061';

const api = axios.create({
  baseURL: BASE_API_URL, // Use the constant here
  withCredentials: true,  // To include cookies in requests
});

// Request Interceptor
api.interceptors.request.use(
  (config) => {
    // Get the current access token from cookies
    const token = Cookies.get('apiToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Variables for refresh token management
let isRefreshing = false;
let subscribers = [];

// Function to notify all subscribers with the new token
function onAccessTokenFetched(accessToken) {
  subscribers.forEach((callback) => callback(accessToken));
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
      if (!isRefreshing) {
        isRefreshing = true;
        originalRequest._retry = true;

        try {
          // Attempt to refresh the token
          const refreshToken = Cookies.get('refreshToken');
          if (refreshToken) {
            const response = await axios.post(
              `${BASE_API_URL}/auth/refresh-token`, // Use the constant here
              { refreshToken },
              { withCredentials: true }
            );

            // Set the new tokens in cookies
            Cookies.set('apiToken', response.data.accessToken, { expires: 1, secure: true });
            Cookies.set('refreshToken', response.data.refreshToken, { expires: 7, secure: true });

            // Notify all queued requests with the new token
            onAccessTokenFetched(response.data.accessToken);
            isRefreshing = false;

            // Retry the original request with the new token
            originalRequest.headers['Authorization'] = `Bearer ${response.data.accessToken}`;
            return api(originalRequest);
          }
        } catch (err) {
          // If refresh token fails, clear the cookies and redirect to login
          Cookies.remove('apiToken');
          Cookies.remove('refreshToken');
          window.location.href = '/login';
        } finally {
          isRefreshing = false;
        }
      }

      // Queue subsequent requests until the token refresh completes
      return new Promise((resolve) => {
        addSubscriber((token) => {
          originalRequest.headers['Authorization'] = `Bearer ${token}`;
          resolve(api(originalRequest));
        });
      });
    }

    // Reject any other errors
    return Promise.reject(error);
  }
);

export default api;
