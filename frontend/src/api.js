// src/api.js
import axios from 'axios';

let apiInstance;

export const initializeApi = () => {
  if (apiInstance) return apiInstance;

  const api = axios.create({
    baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:6061',
    withCredentials: true, // Ensure credentials are included
  });

  apiInstance = api;
  return apiInstance;
};