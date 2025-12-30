import { ApiClient } from './client';

export type AuthResponse = { token: string };
export type RegistrationResponse = { verificationRequired: boolean; message: string; token: string | null };
export type ProfileResponse = { id: number; email: string; fullName: string; baseCurrency: string; role: string };
export type OtpChallenge = { otpRequired: boolean; expiresInSeconds: number };

export const AuthApi = {
  login: (body: { email: string; password: string }) =>
    ApiClient.request<AuthResponse | OtpChallenge>('/api/auth/login', { method: 'POST', body: JSON.stringify(body) }),
  register: (body: { email: string; password: string; fullName: string; baseCurrency: string }) =>
    ApiClient.request<RegistrationResponse>('/api/auth/register', { method: 'POST', body: JSON.stringify(body) }),
  verifyEmail: (body: { email: string; token: string }) =>
    ApiClient.request<AuthResponse>('/api/auth/verify', { method: 'POST', body: JSON.stringify(body) }),
  requestVerification: (body: { email: string }) =>
    ApiClient.request<void>('/api/auth/verify/request', { method: 'POST', body: JSON.stringify(body) }),
  verifyOtp: (body: { email: string; code: string }) =>
    ApiClient.request<AuthResponse>('/api/auth/login/otp', { method: 'POST', body: JSON.stringify(body) }),
  profile: () => ApiClient.request<ProfileResponse>('/api/auth/me', { method: 'GET' }),
  logout: () => ApiClient.request<void>('/api/auth/logout', { method: 'POST' }),
  forgot: (body: { email: string }) =>
    ApiClient.request<void>('/api/auth/forgot', { method: 'POST', body: JSON.stringify(body) }),
  confirmReset: (body: { token: string }) =>
    ApiClient.request<{ resetSessionToken: string; expiresInSeconds: number }>('/api/auth/reset/confirm', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  reset: (body: { resetSessionToken: string; password: string }) =>
    ApiClient.request<void>('/api/auth/reset', { method: 'POST', body: JSON.stringify(body) }),
};
