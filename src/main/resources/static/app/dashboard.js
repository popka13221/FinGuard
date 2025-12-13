(() => {
  const selectors = {
    themeToggle: '#btn-theme',
    logoutBtn: '#btn-logout',
    clearTokenBtn: '#btn-clear-token',
    healthBtn: '#btn-health',
    healthBtnInline: '#btn-health-inline',
    actHealthBtn: '#btn-actuator',
    actHealthBtnInline: '#btn-actuator-inline',
    tokenBox: '#tokenBoxApp',
    emailLabel: '#appEmail',
    healthStatus: '#healthStatusApp'
  };

  function setStatus(ok, text) {
    const el = document.querySelector(selectors.healthStatus);
    if (!el) return;
    el.className = 'status-pill ' + (ok ? 'status-ok' : 'status-bad');
    el.textContent = text;
  }

  async function health(url, label) {
    const res = await Api.call(url, 'GET', null, false);
    if (res.ok && res.data && (res.data.status === 'OK' || res.data.status === 'UP')) {
      setStatus(true, `${label} в порядке`);
    } else {
      setStatus(false, `${label}: ошибка`);
    }
  }

  function showToken(token) {
    const box = document.querySelector(selectors.tokenBox);
    if (box) box.textContent = token || 'Токен не установлен';
  }

  function initButtons() {
    const hb = document.querySelector(selectors.healthBtn);
    if (hb) hb.addEventListener('click', () => health('/health', 'Health'));
    const ab = document.querySelector(selectors.actHealthBtn);
    if (ab) ab.addEventListener('click', () => health('/actuator/health', 'Actuator'));
    const hb2 = document.querySelector(selectors.healthBtnInline);
    if (hb2) hb2.addEventListener('click', () => health('/health', 'Health'));
    const ab2 = document.querySelector(selectors.actHealthBtnInline);
    if (ab2) ab2.addEventListener('click', () => health('/actuator/health', 'Actuator'));
    const lb = document.querySelector(selectors.logoutBtn);
    if (lb) lb.addEventListener('click', () => {
      Api.clearToken();
      Api.call('/api/auth/logout', 'POST', null, true);
      window.location.href = '/';
    });
    const cb = document.querySelector(selectors.clearTokenBtn);
    if (cb) cb.addEventListener('click', () => {
      Api.clearToken();
      Api.call('/api/auth/logout', 'POST', null, true);
      window.location.href = '/';
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    const root = document.documentElement;
    if (root) root.style.visibility = 'hidden';
    Theme.init(selectors.themeToggle);
    Api.call('/api/auth/me', 'GET', null, true).then((res) => {
      if (!res.ok) {
        window.location.href = '/app/login.html';
        return;
      }
      const emailLabel = document.querySelector(selectors.emailLabel);
      if (emailLabel && res.data && res.data.email) {
        emailLabel.textContent = res.data.email;
      }
      showToken('Токен в httpOnly cookie');
      initButtons();
      if (root) root.style.visibility = 'visible';
    });
  });
})();
