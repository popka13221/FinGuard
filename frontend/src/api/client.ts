const emailKey = 'spa_email';

export const ApiClient = {
  baseUrl: () => window.location.origin.replace(/\/$/, ''),

  getEmail: () => sessionStorage.getItem(emailKey) || '',
  setEmail: (email: string) => {
    if (email) sessionStorage.setItem(emailKey, email);
  },
  clearEmail: () => sessionStorage.removeItem(emailKey),

  async request<T>(path: string, options: RequestInit = {}): Promise<{ ok: boolean; status: number; data: T | any }> {
    const resp = await fetch(ApiClient.baseUrl() + path, {
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
    const text = await resp.text();
    let json;
    try {
      json = JSON.parse(text);
    } catch {
      json = text;
    }
    return { ok: resp.ok, status: resp.status, data: json };
  },
};
