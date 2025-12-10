const emailKey = 'spa_email';

export const ApiClient = {
  baseUrl: () => window.location.origin.replace(/\/$/, ''),
  readCookie: (name: string) =>
    document.cookie
      .split(';')
      .map((c) => c.trim())
      .find((c) => c.startsWith(`${name}=`))
      ?.substring(name.length + 1) || '',

  getEmail: () => sessionStorage.getItem(emailKey) || '',
  setEmail: (email: string) => {
    if (email) sessionStorage.setItem(emailKey, email);
  },
  clearEmail: () => sessionStorage.removeItem(emailKey),

  async request<T>(path: string, options: RequestInit = {}): Promise<{ ok: boolean; status: number; data: T | any }> {
    const headers: Record<string, string> = { 'Content-Type': 'application/json' };
    const method = (options.method || 'GET').toUpperCase();
    const needsCsrf = !['GET', 'HEAD', 'OPTIONS'].includes(method);
    let csrf = ApiClient.readCookie('XSRF-TOKEN');
    if (needsCsrf && !csrf) {
      try {
        const resp = await fetch(ApiClient.baseUrl() + '/api/auth/csrf', { credentials: 'include' });
        if (resp.ok) {
          const data = await resp.json().catch(() => ({} as any));
          csrf = (data as any).token || ApiClient.readCookie('XSRF-TOKEN');
        }
      } catch {
        csrf = ApiClient.readCookie('XSRF-TOKEN');
      }
    }
    if (csrf) {
      headers['X-XSRF-TOKEN'] = csrf;
    }

    const resp = await fetch(ApiClient.baseUrl() + path, {
      credentials: 'include',
      headers: { ...headers, ...(options.headers as Record<string, string> | undefined) },
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
