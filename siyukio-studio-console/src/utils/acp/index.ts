import type { AxiosRequestConfig } from 'axios';
import axios from 'axios';

export const acpBaseUrl = import.meta.env.VITE_ACP_BASE_URL || '';

export class ApiError {
  code: number;
  message: string;
  constructor(code: number, message: string) {
    this.code = code;
    this.message = message;
  }
}

// Hook types
export type RequestPreHook = (path: string, data: any, options?: AxiosRequestConfig) => void;
export type RequestCompleteHook = (path: string, data: any, options?: AxiosRequestConfig) => void;
export type RequestErrorHook = (apiError: ApiError, path: string, data: any) => void;

// Default empty implementations
let defaultPreHook: RequestPreHook = () => {};
let defaultCompleteHook: RequestCompleteHook = () => {};
let defaultErrorHook: RequestErrorHook = () => {};

// Setters for external configuration
export const setRequestPreHook = (hook: RequestPreHook) => {
  defaultPreHook = hook;
};

export const setRequestCompleteHook = (hook: RequestCompleteHook) => {
  defaultCompleteHook = hook;
};

export const setRequestErrorHook = (hook: RequestErrorHook) => {
  defaultErrorHook = hook;
};

const authCache = {
  accessToken: '',
  refreshAt: 0,
  maxTime: 600000,
};

let defaultAuthProvider = (): Promise<string> => {
  return Promise.resolve('');
};

export const postRequest = async <T = any>(path: string, data: any = {}, config?: AxiosRequestConfig): Promise<T> => {
  defaultPreHook(path, data, config);

  const url = acpBaseUrl + path;
  try {
    const response = await axios.post<T>(url, data, config);
    const responseData = response.data as any;
    const { error } = responseData;
    if (error) {
      const apiError = new ApiError(error.code, error.message);
      defaultErrorHook(apiError, path, data);
      throw apiError;
    }
    return responseData;
  } catch (error: any) {
    if (error instanceof ApiError) {
      throw error;
    }
    console.error('POST error:', error);
    throw error;
  } finally {
    defaultCompleteHook(path, data, config);
  }
};

export const postRequestWithAuth = async <T = any>(
  path: string,
  data: any = {},
  config?: AxiosRequestConfig,
): Promise<T> => {
  config = config ?? {};
  config.headers = config.headers ?? {};
  config.headers.Authorization = await defaultAuthProvider();
  return postRequest(path, data, config);
};

export const getAccessToken = async () => {
  return await defaultAuthProvider();
};

export const setAccessToken = (accessToken: string) => {
  authCache.accessToken = accessToken;
  authCache.refreshAt = Date.now();
};

export const setDefaultAuthProvider = (authProvider: () => Promise<string>) => {
  defaultAuthProvider = authProvider;
};

export const createAndSetDefaultAuthProvider = (refreshApi: string, refreshToken: string) => {
  defaultAuthProvider = async () => {
    if (Date.now() - authCache.refreshAt <= authCache.maxTime && authCache.accessToken) {
      return authCache.accessToken;
    }
    const tokenResponse = await postRequest<any>(refreshApi, {
      refreshToken,
    });

    if (tokenResponse.error) {
      throw new Error(`${tokenResponse.error.message}`);
    }
    authCache.accessToken = tokenResponse.accessToken;
    authCache.refreshAt = Date.now();
    return tokenResponse.accessToken;
  };
};
