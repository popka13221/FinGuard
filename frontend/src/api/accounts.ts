import { ApiClient } from './client';

export type AccountBalance = {
  id: number;
  name: string;
  currency: string;
  balance: number;
  archived: boolean;
};

export type CurrencyBalance = { currency: string; total: number };

export type UserBalanceResponse = {
  accounts: AccountBalance[];
  totalsByCurrency: CurrencyBalance[];
};

export const AccountsApi = {
  balance: () => ApiClient.request<UserBalanceResponse>('/api/accounts/balance', { method: 'GET' }),
};
