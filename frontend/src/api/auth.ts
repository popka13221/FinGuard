import { ApiClient } from './client';

export type AuthResponse = { token: string };
export type ProfileResponse = { id: number; email: string; fullName: string; baseCurrency: string; role: string };

export const AuthApi = {
  login: (body: { email: string; password: string }) =>
    ApiClient.request<AuthResponse>('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body: { email: string; password: string; fullName: string; baseCurrency: string }) =>
    ApiClient.request<AuthResponse>('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  profile: () => ApiClient.request<ProfileResponse>('/api/auth/me', { method: 'GET' }),
  logout: () => ApiClient.request<void>('/api/auth/logout', { method: 'POST' }),
  forgot: (body: { email: string }) =>
    ApiClient.request<void>('/api/auth/forgot', { method: 'POST', body: JSON.stringify(body) }),
  reset: (body: { token: string; password: string }) =>
    ApiClient.request<void>('/api/auth/reset', { method: 'POST', body: JSON.stringify(body) }),
};
