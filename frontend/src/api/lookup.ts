import { ApiClient } from './client';

export type Currency = { code: string; name: string };

export const LookupApi = {
  currencies: () => ApiClient.request<Currency[]>('/api/currencies', { method: 'GET' }),
};
