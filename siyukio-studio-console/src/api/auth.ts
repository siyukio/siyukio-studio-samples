import { postRequest } from '@/utils/acp';

export const AuthApi = {
  Register: '/auth/register',
  Login: '/auth/login',
  Refresh: '/auth/refresh',
};

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RefisterRequest {
  username: string;
  password: string;
  nickname: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthResponse {
  userId: string;
  nickname: string;
  accessToken: string;
  refreshToken: string;
}

export interface RegisterResponse {
  userId: string;
  nickname: string;
}

export const register = async (request: RefisterRequest) => {
  return await postRequest<RegisterResponse>(AuthApi.Register, request);
};

export const login = async (request: LoginRequest) => {
  return await postRequest<AuthResponse>(AuthApi.Login, request);
};

export const refresh = async (request: RefreshTokenRequest) => {
  return await postRequest<AuthResponse>(AuthApi.Refresh, request);
};
