(() => {
  const selectors = {
    baseCurrency: '#baseCurrency',
    userEmail: '#userEmail',
    logoutBtn: '#btn-logout',
    balanceChart: '#balanceChart',
    expenseChart: '#expenseChart'
  };

  const demoData = {
    balance: [18200, 18750, 19340, 18900, 20200, 20950],
    expenses: [
      { label: 'Жильё', value: 720, color: '#4f8bff' },
      { label: 'Еда', value: 540, color: '#10b981' },
      { label: 'Транспорт', value: 310, color: '#f97316' },
      { label: 'Подписки', value: 260, color: '#3cc7c4' },
      { label: 'Прочее', value: 520, color: '#9aa0aa' }
    ]
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

  function renderLineChart(target, data) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(data) || data.length === 0) return;
    const width = el.clientWidth || 540;
    const height = 220;
    const pad = 26;
    const max = Math.max(...data);
    const min = Math.min(...data);
    const span = max - min || 1;

    const points = data.map((v, i) => {
      const x = pad + (i / Math.max(data.length - 1, 1)) * (width - pad * 2);
      const y = height - pad - ((v - min) / span) * (height - pad * 2);
      return { x, y };
    });

    const areaPoints = [
      `${pad},${height - pad}`,
      ...points.map((p) => `${p.x},${p.y}`),
      `${width - pad},${height - pad}`
    ].join(' ');

    const linePoints = points.map((p) => `${p.x},${p.y}`).join(' ');
    const circles = points.map((p, idx) => `<circle cx="${p.x}" cy="${p.y}" r="4" class="chart-dot" data-idx="${idx}"></circle>`).join('');

    el.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" class="chart-svg">
        <defs>
          <linearGradient id="lineFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#4f8bff" stop-opacity="0.35" />
            <stop offset="100%" stop-color="#3cc7c4" stop-opacity="0.05" />
          </linearGradient>
          <linearGradient id="lineStroke" x1="0" x2="1" y1="0" y2="0">
            <stop offset="0%" stop-color="#4f8bff" />
            <stop offset="100%" stop-color="#3cc7c4" />
          </linearGradient>
        </defs>
        <polygon points="${areaPoints}" class="chart-area" />
        <polyline points="${linePoints}" class="chart-line" />
        ${circles}
      </svg>
    `;
  }

  function renderBarChart(target, items) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(items) || items.length === 0) return;
    const width = el.clientWidth || 520;
    const height = 220;
    const pad = 24;
    const max = Math.max(...items.map(i => i.value)) || 1;
    const barSpace = (width - pad * 2) / items.length;
    const barWidth = Math.max(26, barSpace * 0.56);

    const bars = items.map((item, idx) => {
      const x = pad + idx * barSpace + (barSpace - barWidth) / 2;
      const h = Math.max(8, ((item.value) / max) * (height - pad * 2));
      const y = height - pad - h;
      return `
        <g>
          <rect x="${x}" y="${y}" width="${barWidth}" height="${h}" rx="8" fill="${item.color}" class="chart-bar"></rect>
          <text x="${x + barWidth / 2}" y="${y - 8}" class="chart-value">${item.value}</text>
          <text x="${x + barWidth / 2}" y="${height - pad + 16}" class="chart-label">${item.label}</text>
        </g>
      `;
    }).join('');

    el.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" class="chart-svg">
        ${bars}
      </svg>
    `;
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
    renderLineChart(selectors.balanceChart, demoData.balance);
    renderBarChart(selectors.expenseChart, demoData.expenses);
    if (root) root.style.visibility = 'visible';
  });
})();
