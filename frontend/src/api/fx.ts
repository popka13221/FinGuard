import { ApiClient } from './client';

export type FxRatesResponse = {
  baseCurrency: string;
  asOf: string;
  rates: Record<string, number>;
};

export const FxApi = {
  latestRates: (base: string, quote: string[] = []) => {
    const params = new URLSearchParams();
    params.set('base', base);
    quote.forEach((code) => params.append('quote', code));
    const query = params.toString();
    return ApiClient.request<FxRatesResponse>(`/api/fx/rates?${query}`, { method: 'GET' });
  },
};
