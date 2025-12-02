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
      validateReset: vi.fn(),
      reset: vi.fn(),
    },
  };
});

const mockedAuth = AuthApi as unknown as {
  AuthApi: {
    forgot: ReturnType<typeof vi.fn>;
    validateReset: ReturnType<typeof vi.fn>;
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

  it('sends forgot request, then validates token before navigation', async () => {
    mockedAuth.AuthApi.forgot.mockResolvedValue({ ok: true });
    mockedAuth.AuthApi.validateReset.mockResolvedValue({ ok: true });
    renderWithRouter('/forgot');

    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'user@example.com' } });
    fireEvent.click(screen.getByRole('button', { name: /Отправить код/i }));
    await waitFor(() => expect(mockedAuth.AuthApi.forgot).toHaveBeenCalledTimes(1));

    fireEvent.change(screen.getByLabelText(/Код из письма/i), { target: { value: '123456' } });
    fireEvent.click(screen.getByRole('button', { name: /Ввести код/i }));
    await waitFor(() => expect(mockedAuth.AuthApi.validateReset).toHaveBeenCalledWith({ token: '123456' }));
  });

  it('reset shows error for weak password and does not call API on validation fail', async () => {
    renderWithRouter('/reset?token=123456');
    fireEvent.change(screen.getByLabelText(/Новый пароль/i), { target: { value: 'weak' } });
    fireEvent.change(screen.getByLabelText(/Повторите пароль/i), { target: { value: 'weak' } });
    fireEvent.click(screen.getByRole('button', { name: /Сменить пароль/i }));
    expect(await screen.findByText(/Пароль должен быть не короче/i)).toBeInTheDocument();
    expect(mockedAuth.AuthApi.reset).not.toHaveBeenCalled();
  });

  it('reset calls API and handles invalid token error', async () => {
    mockedAuth.AuthApi.reset.mockResolvedValue({ ok: false, data: { code: '100005' } });
    renderWithRouter('/reset?token=123456');

    fireEvent.change(screen.getByLabelText(/Новый пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.change(screen.getByLabelText(/Повторите пароль/i), { target: { value: 'StrongPass1!' } });
    fireEvent.click(screen.getByRole('button', { name: /Сменить пароль/i }));

    await waitFor(() => expect(mockedAuth.AuthApi.reset).toHaveBeenCalledWith({ token: '123456', password: 'StrongPass1!' }));
    expect(await screen.findByText(/Код неверный или устарел/i)).toBeInTheDocument();
  });
});
