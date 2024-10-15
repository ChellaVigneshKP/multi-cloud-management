import axios from 'axios';
const api = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:6061', // Use env var or default to localhost
  withCredentials: true,  // To include cookies
});

export default api;
