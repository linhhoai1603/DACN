import axios from 'axios';

export const AUTH_TOKEN_KEY = 'authToken';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';

const httpClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000,
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(AUTH_TOKEN_KEY);

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

const toApiError = (error) => {
  if (!error.response) {
    return {
      code: 0,
      status: 'ERROR',
      message: 'Khong the ket noi den may chu. Vui long kiem tra backend.',
      data: null,
    };
  }

  const payload = error.response.data || {};
  return {
    code: payload.code || error.response.status,
    status: payload.status || 'ERROR',
    message: payload.message || 'Yeu cau that bai.',
    data: payload.data ?? null,
  };
};

export const unwrapApiResponse = (response) => {
  const payload = response.data || {};

  if (payload.status === 'ERROR') {
    const apiError = new Error(payload.message || 'Yeu cau that bai.');
    apiError.code = payload.code || response.status;
    apiError.status = payload.status;
    apiError.data = payload.data ?? null;
    throw apiError;
  }

  return {
    code: payload.code || response.status,
    status: payload.status || 'SUCCESS',
    message: payload.message || '',
    data: payload.data ?? null,
  };
};

export const parseApiError = (error) => {
  if (error && typeof error === 'object' && 'code' in error && 'message' in error) {
    return error;
  }

  return toApiError(error);
};

export default httpClient;
