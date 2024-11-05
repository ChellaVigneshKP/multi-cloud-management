import axios from 'axios';
import Cookies from 'js-cookie';

// Define base API URL
const BASE_API_URL = process.env.REACT_APP_API_BASE_URL || 'http://192.168.1.8:6061';

function getCsrfToken() {
  return Cookies.get('XSRF-TOKEN');
}

console.log(getCsrfToken());
// Create Axios instance
const api = axios.create({
  baseURL: BASE_API_URL,
  withCredentials: true,  // Include cookies in requests
});

// Manage refresh token mechanism
let isRefreshing = false;
let subscribers = [];

// Notify all subscribers with the refreshed token
function onAccessTokenFetched() {
  subscribers.forEach((callback) => callback());
  subscribers = [];
}

// Add request to subscribers
function addSubscriber(callback) {
  subscribers.push(callback);
}

api.interceptors.request.use((config) => {
  const csrfToken = getCsrfToken();
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken;  // Add CSRF token to headers
  }
  return config;
});
// Axios response interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized error for token refresh
    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (!isRefreshing) {
        isRefreshing = true;
        try {
          // Attempt to refresh the token using the backend refresh endpoint
          await axios.post(`${BASE_API_URL}/auth/refresh-token`, {}, { withCredentials: true });
          onAccessTokenFetched();
        } catch (err) {
          window.location.href = '/login';  // Redirect to login if refresh fails
          return Promise.reject(err);
        } finally {
          isRefreshing = false;
        }
      }

      // Queue requests until the token refresh completes
      return new Promise((resolve) => {
        addSubscriber(() => resolve(api(originalRequest)));
      });
    }

    return Promise.reject(error);
  }
);

export default api;
