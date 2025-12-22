(() => {
  const selectors = {
    userEmail: '#userEmail',
    logoutBtn: '#btn-logout',
    balanceChart: '#balanceChart',
    expenseChart: '#expenseChart',
    btcSpark: '#btcSpark',
    ethSpark: '#ethSpark',
    solSpark: '#solSpark',
    totalBalance: '#totalBalance',
    creditValue: '#creditValue',
    totalsByCurrency: '#totalsByCurrency',
    accountsList: '#accountsList',
    balanceError: '#balanceError'
  };

  const demoData = {
    balance: [18200, 18750, 19340, 18900, 20200, 20950],
    expenses: [
      { label: 'Жильё', value: 720, color: '#4f8bff' },
      { label: 'Еда', value: 540, color: '#10b981' },
      { label: 'Транспорт', value: 310, color: '#f97316' },
      { label: 'Подписки', value: 260, color: '#3cc7c4' },
      { label: 'Прочее', value: 520, color: '#9aa0aa' }
    ],
    crypto: {
      btc: [61200, 61850, 62500, 61900, 64000, 66200],
      eth: [3020, 3100, 3150, 3080, 3180, 3120],
      sol: [128, 134, 140, 137, 143, 145]
    }
  };

  let baseCurrency = 'USD';
  function updateCurrencyLabels() {
    const bal = document.querySelector('#balanceCurrency');
    const exp = document.querySelector('#expenseCurrency');
    if (bal) bal.textContent = baseCurrency;
    if (exp) exp.textContent = baseCurrency;
    const totalExpense = document.querySelector('#expenseTotal');
    if (totalExpense) totalExpense.textContent = `Всего: ${formatMoney(2350, baseCurrency)}`;
  }

  function renderProfile(profile) {
    const email = document.querySelector(selectors.userEmail);
    if (email && profile && profile.email) {
      email.textContent = profile.email;
    }
    if (profile && profile.baseCurrency) {
      baseCurrency = profile.baseCurrency;
    }
    updateCurrencyLabels();
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

  function formatMoney(value, currency) {
    const cur = currency || baseCurrency || 'USD';
    const abs = Math.abs(value || 0);
    const sign = (value || 0) < 0 ? '-' : '';
    return `${sign}${abs.toLocaleString('ru-RU', { minimumFractionDigits: 2 })} ${cur}`;
  }

  function showBalanceError(message) {
    const el = document.querySelector(selectors.balanceError);
    if (!el) return;
    if (message) {
      el.style.display = 'block';
      el.textContent = message;
    } else {
      el.style.display = 'none';
      el.textContent = '';
    }
  }

  function renderBalance(summary) {
    const totals = Array.isArray(summary.totalsByCurrency) ? summary.totalsByCurrency : [];
    const accounts = Array.isArray(summary.accounts) ? summary.accounts : [];
    const totalBalanceEl = document.querySelector(selectors.totalBalance);
    const creditEl = document.querySelector(selectors.creditValue);
    const totalsLineEl = document.querySelector(selectors.totalsByCurrency);

    const mainTotal = totals.length
      ? totals[0]
      : { currency: accounts[0]?.currency || baseCurrency, total: accounts.reduce((acc, a) => acc + (a.balance || 0), 0) };

    if (totalBalanceEl) {
      totalBalanceEl.textContent = formatMoney(mainTotal.total || 0, mainTotal.currency);
    }

    const creditByCurrency = accounts.reduce((acc, account) => {
      if ((account.balance || 0) < 0) {
        const cur = account.currency || baseCurrency;
        acc[cur] = (acc[cur] || 0) + Math.abs(account.balance || 0);
      }
      return acc;
    }, {});
    const creditParts = Object.entries(creditByCurrency).map(([cur, sum]) => formatMoney(sum, cur));
    if (creditEl) {
      creditEl.textContent = `Кредит: ${creditParts.length ? creditParts.join(' · ') : formatMoney(0)}`;
    }

    const totalsText = totals.length ? totals.map((t) => formatMoney(t.total || 0, t.currency)).join(' · ') : '';
    if (totalsLineEl) {
      totalsLineEl.textContent = totalsText ? `Баланс по валютам: ${totalsText}` : '';
      totalsLineEl.style.display = totalsText ? 'block' : 'none';
    }
  }

  function renderAccountsList(accounts) {
    const list = document.querySelector(selectors.accountsList);
    if (!list) return;
    if (!accounts || accounts.length === 0) {
      list.innerHTML = '<div class="muted">Счета пока не добавлены.</div>';
      return;
    }
    list.innerHTML = accounts.map((acc) => `
      <div class="list-item">
        <div>
          <div style="font-weight:800;">${acc.name || 'Счет'}</div>
          <small>${acc.currency || baseCurrency}${acc.archived ? ' · Архив' : ''}</small>
        </div>
        <div class="${(acc.balance || 0) >= 0 ? 'amount-positive' : 'amount-negative'}">${formatMoney(acc.balance || 0, acc.currency)}</div>
      </div>
    `).join('');
  }

  async function loadBalance() {
    const list = document.querySelector(selectors.accountsList);
    if (list) {
      list.innerHTML = '<div class="muted">Загружаем баланс…</div>';
    }
    const totalBalanceEl = document.querySelector(selectors.totalBalance);
    if (totalBalanceEl) totalBalanceEl.textContent = 'Загрузка…';
    showBalanceError('');

    const res = await Api.call('/api/accounts/balance', 'GET', null, true);
    if (!res.ok) {
      showBalanceError('Не удалось загрузить баланс.');
      if (list) list.innerHTML = '<div class="amount-negative">Не удалось загрузить баланс</div>';
      return;
    }
    const payload = res.data && typeof res.data === 'object' ? res.data : {};
    renderBalance(payload);
    renderAccountsList(payload.accounts || []);
  }

  function renderLineChart(target, data, currency) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(data) || data.length === 0) return;
    const labels = Array.from({ length: data.length }, (_, idx) => {
      const d = new Date();
      d.setMonth(d.getMonth() - (data.length - 1 - idx));
      return d.toLocaleString('ru-RU', { month: 'short' });
    });

    const width = Math.max(el.clientWidth || 640, 640);
    const height = 260;
    const padLeft = 56;
    const padRight = 20;
    const padTop = 24;
    const padBottom = 40;
    const max = Math.max(...data);
    const min = Math.min(...data);
    const span = max - min || 1;

    const points = data.map((v, i) => {
      const x = padLeft + (i / Math.max(data.length - 1, 1)) * (width - padLeft - padRight);
      const y = height - padBottom - ((v - min) / span) * (height - padTop - padBottom);
      return { x, y };
    });

    const areaPoints = [
      `${padLeft},${height - padBottom}`,
      ...points.map((p) => `${p.x},${p.y}`),
      `${width - padRight},${height - padBottom}`
    ].join(' ');

    const linePoints = points.map((p) => `${p.x},${p.y}`).join(' ');
    const circles = points.map((p, idx) => `<circle cx="${p.x}" cy="${p.y}" r="4" class="chart-dot" data-idx="${idx}"></circle>`).join('');
    const yTicks = 4;
    const gridLines = Array.from({ length: yTicks + 1 }, (_, i) => {
      const value = min + (span / yTicks) * i;
      const y = height - padBottom - ((value - min) / span) * (height - padTop - padBottom);
      return `
        <line x1="${padLeft}" x2="${width - padRight}" y1="${y}" y2="${y}" class="chart-gridline"></line>
        <text x="${padLeft - 8}" y="${y + 4}" text-anchor="end" class="chart-axis-label">${formatMoney(value, currency || baseCurrency)}</text>
      `;
    }).join('');

    const xLabels = labels.map((label, idx) => {
      const x = points[idx]?.x || padLeft;
      return `<text x="${x}" y="${height - padBottom + 18}" text-anchor="middle" class="chart-axis-label">${label}</text>`;
    }).join('');

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
        ${gridLines}
        <polygon points="${areaPoints}" class="chart-area" />
        <polyline points="${linePoints}" class="chart-line" />
        ${circles}
        ${xLabels}
        <text x="${padLeft}" y="${padTop}" class="chart-axis-title">Баланс, ${currency || baseCurrency}</text>
      </svg>
    `;
  }

  function renderBarChart(target, items, currency) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(items) || items.length === 0) return;
    const width = Math.max(el.clientWidth || 520, 640);
    const height = Math.max(240, Math.min(320, Math.round(width * 0.42)));
    const padLeft = 56;
    const padRight = 20;
    const padTop = 24;
    const padBottom = 40;
    const max = Math.max(...items.map(i => i.value)) || 1;
    const barSpace = (width - padLeft - padRight) / items.length;
    const barWidth = Math.max(32, Math.min(72, barSpace * 0.62));

    const bars = items.map((item, idx) => {
      const x = padLeft + idx * barSpace + (barSpace - barWidth) / 2;
      const h = Math.max(10, ((item.value) / max) * (height - padTop - padBottom));
      const y = height - padBottom - h;
      return `
        <g>
          <rect x="${x}" y="${y}" width="${barWidth}" height="${h}" rx="8" fill="${item.color}" class="chart-bar"></rect>
          <text x="${x + barWidth / 2}" y="${y - 8}" class="chart-value">${formatMoney(item.value, currency || baseCurrency)}</text>
          <text x="${x + barWidth / 2}" y="${height - padBottom + 18}" class="chart-label">${item.label}</text>
        </g>
      `;
    }).join('');

    const steps = 4;
    const grid = Array.from({ length: steps + 1 }, (_, i) => {
      const value = max - (max / steps) * i;
      const y = padTop + (i / steps) * (height - padTop - padBottom);
      return `
        <line x1="${padLeft}" x2="${width - padRight}" y1="${y}" y2="${y}" class="chart-gridline"></line>
        <text x="${padLeft - 8}" y="${y + 4}" text-anchor="end" class="chart-axis-label">${formatMoney(value, currency || baseCurrency)}</text>
      `;
    }).join('');

    el.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" class="chart-svg">
        ${grid}
        ${bars}
        <text x="${padLeft}" y="${padTop}" class="chart-axis-title">Расходы, ${currency || baseCurrency}</text>
      </svg>
    `;
  }

  function renderSparkline(target, series, color) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(series) || series.length === 0) return;
    const width = el.clientWidth || 140;
    const height = 52;
    const pad = 8;
    const max = Math.max(...series);
    const min = Math.min(...series);
    const span = max - min || 1;
    const points = series.map((v, i) => {
      const x = pad + (i / Math.max(series.length - 1, 1)) * (width - pad * 2);
      const y = height - pad - ((v - min) / span) * (height - pad * 2);
      return { x, y };
    });
    const line = points.map(p => `${p.x},${p.y}`).join(' ');
    const area = [
      `${pad},${height - pad}`,
      ...points.map(p => `${p.x},${p.y}`),
      `${width - pad},${height - pad}`
    ].join(' ');
    const rising = series[series.length - 1] >= series[0];
    const strokeColor = rising ? '#10b981' : '#f97316';
    const fillId = `sparkFill${(strokeColor + target).replace(/[^a-zA-Z0-9]/g, '')}`;
    el.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" class="spark-svg">
        <defs>
          <linearGradient id="${fillId}" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="${strokeColor}" stop-opacity="0.32" />
            <stop offset="100%" stop-color="${strokeColor}" stop-opacity="0.05" />
          </linearGradient>
        </defs>
        <polygon points="${area}" fill="url(#${fillId})"></polygon>
        <polyline points="${line}" fill="none" stroke="${strokeColor}" stroke-width="2.6"></polyline>
        ${points.map(p => `<circle cx="${p.x}" cy="${p.y}" r="2" fill="${strokeColor}" />`).join('')}
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
    await loadBalance();
    renderLineChart(selectors.balanceChart, demoData.balance, baseCurrency);
    renderBarChart(selectors.expenseChart, demoData.expenses, baseCurrency);
    renderSparkline(selectors.btcSpark, demoData.crypto.btc, '#f7931a');
    renderSparkline(selectors.ethSpark, demoData.crypto.eth, '#4f8bff');
    renderSparkline(selectors.solSpark, demoData.crypto.sol, '#10b981');
    if (root) root.style.visibility = 'visible';
  });
})();
