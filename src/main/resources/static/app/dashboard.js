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

    const width = Math.max(el.clientWidth || 520, 520);
    const height = 200;
    const padLeft = 110;
    const padRight = 16;
    const padTop = 18;
    const padBottom = 34;
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
    const yTicks = 4;
    const gridLines = Array.from({ length: yTicks + 1 }, (_, i) => {
      const value = min + (span / yTicks) * i;
      const y = height - padBottom - ((value - min) / span) * (height - padTop - padBottom);
      return `
        <line x1="${padLeft}" x2="${width - padRight}" y1="${y}" y2="${y}" class="chart-gridline"></line>
        <text x="${padLeft - 10}" y="${y + 4}" text-anchor="end" class="chart-axis-label">${formatMoney(value, currency || baseCurrency)}</text>
      `;
    }).join('');

    const xLabels = labels.map((label, idx) => {
      const x = points[idx]?.x || padLeft;
      return `<text x="${x}" y="${height - padBottom + 18}" text-anchor="middle" class="chart-axis-label">${label}</text>`;
    }).join('');

    const delta = data[data.length - 1] - data[0];
    const avg = data.reduce((a, b) => a + b, 0) / (data.length || 1);
    const deltaPct = data[0] !== 0 ? (delta / data[0]) * 100 : 0;
    const formatPercent = (val) => `${val >= 0 ? '+' : ''}${val.toFixed(1)}%`;

    el.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="xMidYMid meet" class="chart-svg">
        <defs>
          <linearGradient id="lineFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#4f8bff" stop-opacity="0.32" />
            <stop offset="100%" stop-color="#3cc7c4" stop-opacity="0.08" />
          </linearGradient>
          <linearGradient id="lineStroke" x1="0" x2="1" y1="0" y2="0">
            <stop offset="0%" stop-color="#4f8bff" />
            <stop offset="100%" stop-color="#3cc7c4" />
          </linearGradient>
        </defs>
        ${gridLines}
        <polygon points="${areaPoints}" class="chart-area" />
        <polyline points="${linePoints}" class="chart-line" stroke-linecap="round" stroke-linejoin="round" />
        ${xLabels}
      </svg>
      <div class="mini-stats">
        <div class="stat-chip">
          <div class="muted">Мин</div>
          <div class="stat-value">${formatMoney(min, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">Макс</div>
          <div class="stat-value">${formatMoney(max, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">Изменение</div>
          <div class="stat-value">${formatMoney(delta, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">Среднее</div>
          <div class="stat-value">${formatMoney(avg, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">Тренд %</div>
          <div class="stat-value">${formatPercent(deltaPct)}</div>
        </div>
      </div>
    `;
  }

  function renderBarChart(target, items, currency) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(items) || items.length === 0) return;
    const currencyLabel = currency || baseCurrency;
    const total = items.reduce((acc, item) => acc + (item.value || 0), 0) || 1;
    const size = 200;
    const radius = 78;
    const center = size / 2;
    let offset = 0;
    const slices = items.map((item) => {
      const value = item.value || 0;
      const pct = value / total;
      const fullSpan = pct * Math.PI * 2;
      const startAngle = (offset / total) * Math.PI * 2;
      const endAngle = startAngle + fullSpan;
      offset += value;
      return { item, startAngle, endAngle };
    });
    const legend = items.map((item, idx) => {
      const color = item.color || ['#4f8bff', '#10b981', '#f97316', '#3cc7c4', '#9aa0aa'][idx % 5];
      const pct = Math.round(((item.value || 0) / total) * 100);
      return `<button class="legend-item" data-slice="${idx}" aria-label="${item.label}">
                <span class="legend-dot" style="background:${color};"></span>
                <span>${item.label}</span>
                <span class="legend-pct">${pct}%</span>
              </button>`;
    }).join('');

    el.innerHTML = `
      <div class="pie-wrap">
        <svg viewBox="0 0 ${size} ${size}" class="pie-chart" aria-label="Структура расходов">
          ${slices.map((slice, idx) => {
            const color = slice.item.color || ['#4f8bff', '#10b981', '#f97316', '#3cc7c4', '#9aa0aa'][idx % 5];
            const start = {
              x: center + radius * Math.cos(slice.startAngle - Math.PI / 2),
              y: center + radius * Math.sin(slice.startAngle - Math.PI / 2),
            };
            const end = {
              x: center + radius * Math.cos(slice.endAngle - Math.PI / 2),
              y: center + radius * Math.sin(slice.endAngle - Math.PI / 2),
            };
            const largeArc = slice.endAngle - slice.startAngle > Math.PI ? 1 : 0;
            const d = `M ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArc} 1 ${end.x} ${end.y}`;
            return `<path d="${d}" fill="none" stroke="${color}" stroke-width="16" stroke-linecap="butt"
              data-base-stroke="16" data-slice="${idx}" class="pie-slice"></path>`;
          }).join('')}
          <text x="${center}" y="${center - 6}" text-anchor="middle" class="pie-total">${formatMoney(total, currencyLabel)}</text>
          <text x="${center}" y="${center + 12}" text-anchor="middle" class="pie-muted">Всего</text>
        </svg>
        <div class="chart-legend grid-compact">${legend}</div>
      </div>
    `;

    // hover / focus interactions
    const sliceEls = Array.from(el.querySelectorAll('.pie-slice'));
    const legendEls = Array.from(el.querySelectorAll('.legend-item'));
    const activate = (idx) => {
      sliceEls.forEach((s, i) => {
        const baseStroke = s.dataset.baseStroke || '18';
        if (i === idx) {
          s.style.opacity = '1';
          s.style.strokeWidth = baseStroke;
          s.style.filter = 'drop-shadow(0 0 10px rgba(79,139,255,0.3))';
        } else {
          s.style.opacity = '0.35';
          s.style.strokeWidth = baseStroke;
          s.style.filter = 'none';
        }
      });
      legendEls.forEach((l, i) => l.classList.toggle('active', i === idx));
    };
    const reset = () => {
      sliceEls.forEach((s) => {
        const baseStroke = s.dataset.baseStroke || '18';
        s.style.opacity = '1';
        s.style.strokeWidth = baseStroke;
        s.style.filter = 'none';
      });
      legendEls.forEach((l) => l.classList.remove('active'));
    };
    legendEls.forEach((l, idx) => {
      l.addEventListener('mouseenter', () => activate(idx));
      l.addEventListener('focus', () => activate(idx));
      l.addEventListener('mouseleave', reset);
      l.addEventListener('blur', reset);
    });
    sliceEls.forEach((s, idx) => {
      s.addEventListener('mouseenter', () => activate(idx));
      s.addEventListener('mouseleave', reset);
    });
  }

  function bindAddAccountMenu() {
    const btn = document.querySelector('#btn-add-account');
    const menu = document.querySelector('#add-account-menu');
    if (!btn || !menu) return;
    let open = false;
    const toggle = (state) => {
      open = state ?? !open;
      menu.style.display = open ? 'grid' : 'none';
    };
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggle();
    });
    document.addEventListener('click', (e) => {
      if (open && !menu.contains(e.target) && e.target !== btn) {
        toggle(false);
      }
    });
    menu.querySelectorAll('.dropdown-item').forEach((item) => {
      item.addEventListener('click', () => toggle(false));
    });
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
    bindAddAccountMenu();
    renderSparkline(selectors.btcSpark, demoData.crypto.btc, '#f7931a');
    renderSparkline(selectors.ethSpark, demoData.crypto.eth, '#4f8bff');
    renderSparkline(selectors.solSpark, demoData.crypto.sol, '#10b981');
    if (root) root.style.visibility = 'visible';
  });
})();
