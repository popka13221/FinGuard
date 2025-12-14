(() => {
  const selectors = {
    baseCurrency: '#baseCurrency',
    userEmail: '#userEmail',
    logoutBtn: '#btn-logout'
  };

  function renderProfile(profile) {
    const currency = document.querySelector(selectors.baseCurrency);
    if (currency && profile && profile.baseCurrency) {
      currency.textContent = profile.baseCurrency;
    }
    const email = document.querySelector(selectors.userEmail);
    if (email && profile && profile.email) {
      email.textContent = profile.email;
    }
  }

  function bindLogout() {
    const btn = document.querySelector(selectors.logoutBtn);
    if (!btn) return;
    btn.addEventListener('click', async () => {
      await Api.call('/api/auth/logout', 'POST', null, true);
      Api.clearToken();
      window.location.href = '/';
    });
  }

  document.addEventListener('DOMContentLoaded', async () => {
    const root = document.documentElement;
    if (root) root.style.visibility = 'hidden';
    Theme.apply();
    const res = await Api.call('/api/auth/me', 'GET', null, true);
    if (!res.ok) {
      Api.clearToken();
      window.location.href = '/app/login.html';
      return;
    }
    renderProfile(res.data || {});
    bindLogout();
    if (root) root.style.visibility = 'visible';
  });
})();
