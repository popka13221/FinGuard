import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup } from '@testing-library/react';
import AuthPage from '../AuthPage';
import * as AuthApiModule from '../../api/auth';

vi.mock('../../api/auth', async (importOriginal) => {
  const actual = (await importOriginal()) as typeof import('../../api/auth');
  return {
    ...actual,
    AuthApi: {
      ...actual.AuthApi,
      login: vi.fn(),
      verifyOtp: vi.fn(),
      register: vi.fn(),
    },
  };
});

const mockedAuth = AuthApiModule as unknown as {
  AuthApi: {
    login: ReturnType<typeof vi.fn>;
    verifyOtp: ReturnType<typeof vi.fn>;
  };
};

describe('AuthPage OTP flow', () => {
  const setHref = vi.fn();

  beforeEach(() => {
    vi.resetAllMocks();
    sessionStorage.clear();
    Object.defineProperty(window, 'location', {
      value: {
        get href() {
          return '';
        },
        set href(val: string) {
          setHref(val);
        },
      },
      writable: true,
    });
  });

  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  it('shows OTP step when backend returns otpRequired and completes login after code', async () => {
    mockedAuth.AuthApi.login.mockResolvedValue({
      ok: true,
      status: 202,
      data: { otpRequired: true, expiresInSeconds: 299 },
    });
    mockedAuth.AuthApi.verifyOtp.mockResolvedValue({
      ok: true,
      data: { token: 'access' },
    });

    render(<AuthPage />);

    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/Пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.click(screen.getByRole('button', { name: /Войти/i }));

    expect(await screen.findByLabelText(/Код из письма/i)).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText(/Код из письма/i), { target: { value: '654321' } });
    fireEvent.click(screen.getByRole('button', { name: /Подтвердить код/i }));

    await waitFor(() => expect(mockedAuth.AuthApi.verifyOtp).toHaveBeenCalledWith({ email: 'user@example.com', code: '654321' }));
    expect(setHref).toHaveBeenCalledWith('/dashboard');
    expect(sessionStorage.getItem('spa_email')).toBe('user@example.com');
  });

  it('shows error on invalid OTP', async () => {
    mockedAuth.AuthApi.login.mockResolvedValue({
      ok: true,
      status: 202,
      data: { otpRequired: true, expiresInSeconds: 299 },
    });
    mockedAuth.AuthApi.verifyOtp.mockResolvedValue({
      ok: false,
      status: 401,
      data: { code: '100001' },
    });

    render(<AuthPage />);

    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/Пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.click(screen.getByRole('button', { name: /Войти/i }));
    await screen.findByLabelText(/Код из письма/i);

    fireEvent.change(screen.getByLabelText(/Код из письма/i), { target: { value: '000000' } });
    fireEvent.click(screen.getByRole('button', { name: /Подтвердить код/i }));

    await waitFor(() => expect(mockedAuth.AuthApi.verifyOtp).toHaveBeenCalled());
    expect(screen.getAllByText(/Код неверный или истек/i).length).toBeGreaterThan(0);
    expect(setHref).not.toHaveBeenCalled();
  });

  it('does not block login on weak (but non-empty) password and redirects after success', async () => {
    mockedAuth.AuthApi.login.mockResolvedValue({
      ok: true,
      status: 200,
      data: { token: 'access' },
    });

    render(<AuthPage />);

    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/Пароль/i), { target: { value: 'weak' } });
    fireEvent.click(screen.getByRole('button', { name: /Войти/i }));

    await waitFor(() => expect(mockedAuth.AuthApi.login).toHaveBeenCalledWith({ email: 'user@example.com', password: 'weak' }));
    expect(setHref).toHaveBeenCalledWith('/dashboard');
    expect(sessionStorage.getItem('spa_email')).toBe('user@example.com');
  });

  it('keeps OTP submit disabled without code and shows a single inline error on failure', async () => {
    mockedAuth.AuthApi.login.mockResolvedValue({
      ok: true,
      status: 202,
      data: { otpRequired: true, expiresInSeconds: 120 },
    });
    mockedAuth.AuthApi.verifyOtp.mockResolvedValue({
      ok: false,
      status: 401,
      data: { code: '100001' },
    });

    render(<AuthPage />);

    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'user@example.com' } });
    fireEvent.change(screen.getByLabelText(/Пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.click(screen.getByRole('button', { name: /Войти/i }));

    const otpButton = await screen.findByRole('button', { name: /Подтвердить код/i });
    expect(otpButton).toBeDisabled();

    fireEvent.change(screen.getByLabelText(/Код из письма/i), { target: { value: '000000' } });
    expect(otpButton).not.toBeDisabled();
    fireEvent.click(otpButton);

    await waitFor(() => expect(mockedAuth.AuthApi.verifyOtp).toHaveBeenCalled());
    expect(screen.getByText(/Код неверный или истек/i)).toBeInTheDocument();
    expect(screen.queryByRole('alert')).toBeNull();
  });
});
