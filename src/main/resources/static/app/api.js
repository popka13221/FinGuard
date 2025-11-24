const emailKey = 'finguard_email';

const Api = (() => {
  function baseUrl() {
    return window.location.origin.replace(/\/$/, '');
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

    const resp = await fetch(baseUrl() + path, {
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
