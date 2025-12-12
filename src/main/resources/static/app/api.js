const emailKey = 'finguard_email';

const Api = (() => {
  function readCookie(name) {
    return document.cookie.split(';').map(c => c.trim()).filter(c => c.startsWith(name + '=')).map(c => c.substring(name.length + 1))[0] || '';
  }

  async function ensureCsrfToken() {
    const existing = readCookie('XSRF-TOKEN');
    if (existing) return existing;
    try {
      const resp = await fetch('/api/auth/csrf', { credentials: 'include' });
      if (resp.ok) {
        const data = await resp.json().catch(() => ({}));
        return data.token || readCookie('XSRF-TOKEN') || '';
      }
    } catch (_) {
      return '';
    }
    return '';
  }

  function clearToken() {
    sessionStorage.removeItem(emailKey);
  }

  function getEmail() {
    return sessionStorage.getItem(emailKey) || '';
  }

  function setEmail(email) {
    if (email) sessionStorage.setItem(emailKey, email);
  }

  async function call(path, method, body, auth = true) {
    const headers = { 'Content-Type': 'application/json' };
    const upper = (method || 'GET').toUpperCase();
    const needsCsrf = upper !== 'GET' && upper !== 'HEAD' && upper !== 'OPTIONS';
    let csrfToken = readCookie('XSRF-TOKEN');
    if (needsCsrf && !csrfToken) {
      csrfToken = await ensureCsrfToken();
    }
    if (csrfToken) {
      headers['X-XSRF-TOKEN'] = csrfToken;
    }

    const resp = await fetch(path, {
      method,
      headers,
      credentials: 'include',
      body: body ? JSON.stringify(body) : undefined
    });

    const text = await resp.text();
    let json;
    try { json = JSON.parse(text); } catch (e) { json = text; }
    return { ok: resp.ok, status: resp.status, data: json };
  }

  return { call, clearToken, getEmail, setEmail };
})();

window.Api = Api;
