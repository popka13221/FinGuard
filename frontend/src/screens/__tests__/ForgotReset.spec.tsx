import React from 'react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import ForgotPage from '../ForgotPage';
import ResetPage from '../ResetPage';
import * as AuthApi from '../../api/auth';

vi.mock('../../api/auth', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    AuthApi: {
      ...actual.AuthApi,
      forgot: vi.fn(),
      confirmReset: vi.fn(),
      reset: vi.fn(),
    },
  };
});

const mockedAuth = AuthApi as unknown as {
  AuthApi: {
    forgot: ReturnType<typeof vi.fn>;
    confirmReset: ReturnType<typeof vi.fn>;
    reset: ReturnType<typeof vi.fn>;
  };
};

function renderWithRouter(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/forgot" element={<ForgotPage />} />
        <Route path="/reset" element={<ResetPage />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('Forgot/Reset flow', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('shows error on invalid email and does not call API', async () => {
    renderWithRouter('/forgot');
    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'bad' } });
    fireEvent.click(screen.getByRole('button', { name: /Отправить код/i }));
    expect(await screen.findByText(/Введите корректный email/i)).toBeInTheDocument();
    expect(mockedAuth.AuthApi.forgot).not.toHaveBeenCalled();
  });

  it('sends forgot request and reveals code input', async () => {
    mockedAuth.AuthApi.forgot.mockResolvedValue({ ok: true });
    renderWithRouter('/forgot');

    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'user@example.com' } });
    fireEvent.click(screen.getByRole('button', { name: /Отправить код/i }));
    await waitFor(() => expect(mockedAuth.AuthApi.forgot).toHaveBeenCalledTimes(1));
    expect(await screen.findByLabelText(/Код из письма/i)).toBeInTheDocument();
  });

  it('auto-confirms reset token from query and redirects to request new on expiry', async () => {
    mockedAuth.AuthApi.confirmReset.mockResolvedValue({ ok: false, data: { code: '100005' } });
    renderWithRouter('/reset?token=badtoken&email=user@example.com');

    await waitFor(() => expect(mockedAuth.AuthApi.confirmReset).toHaveBeenCalledWith({ token: 'badtoken' }));
    expect(await screen.findByText(/Код устарел\. Запросите новый/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Email/i)).toHaveValue('user@example.com');
  });

  it('reset validates password strength and does not call API on validation fail', async () => {
    mockedAuth.AuthApi.confirmReset.mockResolvedValue({ ok: true, data: { resetSessionToken: 'session-token', expiresInSeconds: 120 } });
    renderWithRouter('/reset?token=123456');
    await screen.findByText(/Сессия сброса активна/i);

    fireEvent.change(screen.getByLabelText(/Новый пароль/i), { target: { value: 'weak' } });
    fireEvent.change(screen.getByLabelText(/Повторите пароль/i), { target: { value: 'weak' } });
    fireEvent.click(screen.getByRole('button', { name: /Сменить пароль/i }));
    expect(await screen.findByText(/Пароль должен быть не короче/i)).toBeInTheDocument();
    expect(mockedAuth.AuthApi.reset).not.toHaveBeenCalled();
  });

  it('reset calls API and handles invalid session error', async () => {
    mockedAuth.AuthApi.confirmReset.mockResolvedValue({ ok: true, data: { resetSessionToken: 'session-token', expiresInSeconds: 120 } });
    mockedAuth.AuthApi.reset.mockResolvedValue({ ok: false, data: { code: '100005' } });
    renderWithRouter('/reset?token=123456');

    await screen.findByText(/Сессия сброса активна/i);
    fireEvent.change(screen.getByLabelText(/Новый пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.change(screen.getByLabelText(/Повторите пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.click(screen.getByRole('button', { name: /Сменить пароль/i }));

    await waitFor(() => expect(mockedAuth.AuthApi.reset).toHaveBeenCalledWith({ resetSessionToken: 'session-token', password: 'StrongPass1!' }));
    expect(await screen.findByText(/Сессия сброса устарела/i)).toBeInTheDocument();
  });
});
