import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import DashboardPage from '../DashboardPage';
import * as AuthApi from '../../api/auth';
import * as AccountsApi from '../../api/accounts';

vi.mock('../../api/auth', async (importOriginal) => {
  const actual = (await importOriginal()) as typeof import('../../api/auth');
  return {
    ...actual,
    AuthApi: {
      ...actual.AuthApi,
      profile: vi.fn(),
      logout: vi.fn(),
    },
  };
});

vi.mock('../../api/accounts', async (importOriginal) => {
  const actual = (await importOriginal()) as typeof import('../../api/accounts');
  return {
    ...actual,
    AccountsApi: {
      ...actual.AccountsApi,
      balance: vi.fn(),
    },
  };
});

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    (AuthApi as any).AuthApi.profile.mockResolvedValue({
      ok: true,
      data: { email: 'user@example.com', fullName: 'User', baseCurrency: 'USD' },
    });
    (AccountsApi as any).AccountsApi.balance.mockResolvedValue({
      ok: true,
      data: {
        accounts: [
          { id: 1, name: 'Main', currency: 'USD', balance: 10.5, archived: false },
          { id: 2, name: 'Archived', currency: 'USD', balance: 5, archived: true },
        ],
        totalsByCurrency: [{ currency: 'USD', total: 10.5 }],
      },
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('renders accounts and totals after successful load', async () => {
    render(<DashboardPage />);
    expect(screen.getByText(/Загружаем баланс/i)).toBeInTheDocument();

    await waitFor(() => expect(screen.getByText('Main')).toBeInTheDocument());
    expect(screen.getByText('Archived')).toBeInTheDocument();
    expect(screen.getByTestId('total-balance')).toBeInTheDocument();
    expect(screen.queryByText(/Не удалось загрузить баланс/i)).not.toBeInTheDocument();
  });

  it('shows error when balance fails', async () => {
    (AccountsApi as any).AccountsApi.balance.mockResolvedValueOnce({ ok: false });
    render(<DashboardPage />);
    await waitFor(() => expect(screen.getByText(/Не удалось загрузить баланс/i)).toBeInTheDocument());
  });

  it('opens and closes add account menu', async () => {
    render(<DashboardPage />);
    await screen.findByText('Main');
    const addBtn = screen.getByRole('button', { name: /Добавить/i });
    fireEvent.click(addBtn);
    const menuItem = await screen.findByRole('button', { name: /Счёт вручную/i });
    expect(menuItem).toBeInTheDocument();
    fireEvent.click(menuItem);
    await waitFor(() => {
      const overlay = menuItem.closest('#add-account-overlay') as HTMLElement | null;
      expect(overlay?.style.display).toBe('none');
    });
  });
});
