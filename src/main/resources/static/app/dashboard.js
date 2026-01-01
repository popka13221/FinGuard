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
    balanceError: '#balanceError',
    fxStatus: '#fxStatus',
    fxBase: '#fxBase',
    fxTopVolatile: '#fxTopVolatile',
    fxList: '#fxList',
    fxBrowse: '#fxBrowse',
    fxSearch: '#fxSearch',
    fxBaseSelect: '#fxBaseSelect',
    fxDetail: '#fxDetail',
    fxDetailCode: '#fxDetailCode',
    fxDetailName: '#fxDetailName',
    fxDetailRate: '#fxDetailRate',
    fxDetailChange: '#fxDetailChange',
    fxDetailChart: '#fxDetailChart',
    fxToggleList: '#fxToggleList',
    fxSortButtons: '.fx-sort button'
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

  const fxFallbackCurrencies = [
    { code: 'USD', name: 'US Dollar' },
    { code: 'EUR', name: 'Euro' },
    { code: 'RUB', name: 'Russian Ruble' },
    { code: 'CNY', name: 'Chinese Yuan' }
  ];
  const fxFallbackBase = 'USD';
  const fxExcluded = new Set(['BTC', 'ETH']);
  const fxSortModes = { volatility: 'volatility', alpha: 'alpha' };
  let fxCurrencies = fxFallbackCurrencies.slice();
  let fxBase = '';
  let fxSortMode = fxSortModes.volatility;
  let fxSearchQuery = '';
  let fxSelectedCode = '';
  let fxItems = [];

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

  function resolveFxBase() {
    const normalized = (fxBase || baseCurrency || '').toUpperCase();
    return fxCurrencies.some((item) => item.code === normalized) ? normalized : fxFallbackBase;
  }

  function formatFxRate(value) {
    if (typeof value !== 'number' || Number.isNaN(value)) return '—';
    const digits = value < 1 ? 4 : 2;
    return value.toLocaleString('ru-RU', { minimumFractionDigits: digits, maximumFractionDigits: digits });
  }

  function formatFxUpdated(isoValue) {
    if (!isoValue) return '';
    const date = new Date(isoValue);
    if (Number.isNaN(date.getTime())) return '';
    return date.toLocaleString('ru-RU', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
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

  function hashString(value) {
    let hash = 0;
    for (let i = 0; i < value.length; i += 1) {
      hash = ((hash << 5) - hash) + value.charCodeAt(i);
      hash |= 0;
    }
    return Math.abs(hash);
  }

  function seededRandom(seed) {
    let state = seed % 2147483647;
    if (state <= 0) state += 2147483646;
    return () => {
      state = (state * 16807) % 2147483647;
      return (state - 1) / 2147483646;
    };
  }

  function buildFxSeries(rate, key) {
    const base = Math.max(rate, 0.0001);
    const seed = hashString(`${key}:${base.toFixed(6)}`);
    const random = seededRandom(seed);
    const amplitude = 0.004 + (seed % 12) / 1000;
    const drift = (random() - 0.5) * 0.006;
    let value = base * (1 + (random() - 0.5) * amplitude);
    const series = [];
    for (let i = 0; i < 8; i += 1) {
      const daily = (random() - 0.5) * amplitude + drift;
      value = Math.max(value * (1 + daily), base * 0.65);
      series.push(value);
    }
    const scale = base / series[series.length - 1];
    return series.map((v) => v * scale);
  }

  function summarizeSeries(series) {
    const max = Math.max(...series);
    const min = Math.min(...series);
    const avg = series.reduce((acc, v) => acc + v, 0) / (series.length || 1);
    const change = series[series.length - 1] - series[0];
    const changePct = series[0] !== 0 ? (change / series[0]) * 100 : 0;
    const volatility = avg !== 0 ? ((max - min) / avg) * 100 : 0;
    return { max, min, avg, change, changePct, volatility };
  }

  function pickChangeClass(change) {
    if (change > 0) return 'fx-change positive';
    if (change < 0) return 'fx-change negative';
    return 'fx-change neutral';
  }

  function formatChangePct(changePct) {
    if (!Number.isFinite(changePct)) return '—';
    const sign = changePct >= 0 ? '+' : '';
    return `${sign}${changePct.toFixed(2)}%`;
  }

  function sparkSvg(series, width, height, stroke) {
    const max = Math.max(...series);
    const min = Math.min(...series);
    const span = max - min || 1;
    const pad = 6;
    const points = series.map((value, idx) => {
      const x = pad + (idx / Math.max(series.length - 1, 1)) * (width - pad * 2);
      const y = height - pad - ((value - min) / span) * (height - pad * 2);
      return { x, y };
    });
    const line = points.map((p) => `${p.x},${p.y}`).join(' ');
    const area = [
      `${pad},${height - pad}`,
      ...points.map((p) => `${p.x},${p.y}`),
      `${width - pad},${height - pad}`
    ].join(' ');
    const fillId = `fxFill${Math.abs(hashString(line))}${Math.random().toString(36).slice(2, 7)}`;
    return `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="none" class="fx-spark-svg">
        <defs>
          <linearGradient id="${fillId}" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="${stroke}" stop-opacity="0.35" />
            <stop offset="100%" stop-color="${stroke}" stop-opacity="0.05" />
          </linearGradient>
        </defs>
        <polygon points="${area}" fill="url(#${fillId})"></polygon>
        <polyline points="${line}" fill="none" stroke="${stroke}" stroke-width="2.2" stroke-linecap="round"></polyline>
      </svg>
    `;
  }

  function renderFxTop(items) {
    const container = document.querySelector(selectors.fxTopVolatile);
    if (!container) return;
    if (!items.length) {
      container.innerHTML = '<div class="muted">Нет данных по валютам.</div>';
      return;
    }
    container.innerHTML = items.map((item, idx) => {
      const changeClass = pickChangeClass(item.metrics.change);
      const stroke = item.metrics.change >= 0 ? '#10b981' : '#f97316';
      return `
        <button type="button" class="fx-card" data-code="${item.code}" style="--delay:${idx * 70}ms;">
          <div class="fx-card-main">
            <div class="fx-card-code">${item.code}</div>
            <div class="fx-card-name">${item.name}</div>
          </div>
          <div class="fx-card-side">
            <div class="fx-card-rate">${formatFxRate(item.rate)}</div>
            <div class="${changeClass}">${formatChangePct(item.metrics.changePct)} за 7д</div>
          </div>
          <div class="fx-card-spark">${sparkSvg(item.series, 160, 28, stroke)}</div>
        </button>
      `;
    }).join('');
    container.querySelectorAll('.fx-card').forEach((button) => {
      button.addEventListener('click', () => selectFx(button.dataset.code || ''));
    });
  }

  function renderFxList(items) {
    const list = document.querySelector(selectors.fxList);
    if (!list) return;
    if (!items.length) {
      list.innerHTML = '<div class="muted">Нет доступных валют.</div>';
      return;
    }
    list.innerHTML = items.map((item) => {
      const active = item.code === fxSelectedCode ? 'is-active' : '';
      return `
        <button type="button" class="fx-list-item ${active}" data-code="${item.code}">
          <div class="fx-list-left">
            <div class="fx-list-code">${item.code}</div>
            <div class="fx-list-name">${item.name}</div>
          </div>
          <div class="fx-list-right">
            <div class="fx-list-rate">${formatFxRate(item.rate)}</div>
          </div>
        </button>
      `;
    }).join('');
    list.querySelectorAll('.fx-list-item').forEach((button) => {
      button.addEventListener('click', () => selectFx(button.dataset.code || ''));
    });
  }

  function renderFxDetail(item) {
    const codeEl = document.querySelector(selectors.fxDetailCode);
    const nameEl = document.querySelector(selectors.fxDetailName);
    const rateEl = document.querySelector(selectors.fxDetailRate);
    const changeEl = document.querySelector(selectors.fxDetailChange);
    const chartEl = document.querySelector(selectors.fxDetailChart);
    if (!item) {
      if (codeEl) codeEl.textContent = '—';
      if (nameEl) nameEl.textContent = 'Нет данных';
      if (rateEl) rateEl.textContent = '—';
      if (changeEl) changeEl.textContent = '';
      if (chartEl) chartEl.innerHTML = '';
      return;
    }
    const base = resolveFxBase();
    if (codeEl) codeEl.textContent = item.code;
    if (nameEl) nameEl.textContent = `${item.name} · 1 ${base}`;
    if (rateEl) rateEl.textContent = formatFxRate(item.rate);
    if (changeEl) {
      changeEl.className = `fx-detail-change ${pickChangeClass(item.metrics.change)}`;
      changeEl.textContent = `${formatChangePct(item.metrics.changePct)} за 7д`;
    }
    if (chartEl) {
      const stroke = item.metrics.change >= 0 ? '#4f8bff' : '#f97316';
      const width = Math.max(chartEl.clientWidth || 140, 140);
      const height = Math.max(48, Math.min(90, Math.round(width / 2.4)));
      chartEl.innerHTML = sparkSvg(item.series, width, height, stroke);
    }
  }

  function selectFx(code) {
    fxSelectedCode = code;
    const selected = fxItems.find((item) => item.code === code) || fxItems[0];
    renderFxDetail(selected);
    renderFxList(applyFxFilters());
  }

  function applyFxFilters() {
    let list = fxItems.slice();
    if (fxSearchQuery) {
      const q = fxSearchQuery.toLowerCase();
      list = list.filter((item) => item.code.toLowerCase().includes(q) || item.name.toLowerCase().includes(q));
    }
    if (fxSortMode === fxSortModes.alpha) {
      list.sort((a, b) => a.code.localeCompare(b.code));
    } else {
      list.sort((a, b) => b.metrics.volatility - a.metrics.volatility);
    }
    return list;
  }

  function bindFxControls() {
    const toggle = document.querySelector(selectors.fxToggleList);
    const browse = document.querySelector(selectors.fxBrowse);
    if (toggle && browse) {
      toggle.addEventListener('click', () => {
        browse.classList.toggle('is-open');
        toggle.textContent = browse.classList.contains('is-open') ? 'Скрыть' : 'Все валюты';
      });
    }
    const search = document.querySelector(selectors.fxSearch);
    if (search) {
      search.addEventListener('input', (e) => {
        fxSearchQuery = e.target.value || '';
        renderFxList(applyFxFilters());
      });
    }
    const baseSelect = document.querySelector(selectors.fxBaseSelect);
    if (baseSelect) {
      baseSelect.addEventListener('change', (e) => {
        fxBase = e.target.value;
        loadFxRates();
      });
    }
    document.querySelectorAll(selectors.fxSortButtons).forEach((btn) => {
      btn.addEventListener('click', () => {
        fxSortMode = btn.dataset.sort === 'alpha' ? fxSortModes.alpha : fxSortModes.volatility;
        document.querySelectorAll(selectors.fxSortButtons).forEach((item) => item.classList.toggle('active', item === btn));
        renderFxList(applyFxFilters());
      });
    });
  }

  async function loadFxCurrencies() {
    try {
      const resp = await fetch('/api/currencies');
      const data = await resp.json();
      if (Array.isArray(data) && data.length) {
        const filtered = data
          .filter((item) => item && item.code && item.name)
          .map((item) => ({ code: item.code.toUpperCase(), name: item.name }))
          .filter((item) => item.code.length === 3 && !fxExcluded.has(item.code));
        if (filtered.length) {
          fxCurrencies = filtered;
        }
      }
    } catch (_) {
      fxCurrencies = fxFallbackCurrencies.slice();
    }
    const baseSelect = document.querySelector(selectors.fxBaseSelect);
    if (baseSelect) {
      baseSelect.innerHTML = '';
      fxCurrencies.forEach((item) => {
        const option = document.createElement('option');
        option.value = item.code;
        option.textContent = item.code;
        baseSelect.appendChild(option);
      });
      const base = resolveFxBase();
      baseSelect.value = base;
    }
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

  async function loadFxRates() {
    const statusEl = document.querySelector(selectors.fxStatus);
    const baseEl = document.querySelector(selectors.fxBase);
    const base = resolveFxBase();
    const baseSelect = document.querySelector(selectors.fxBaseSelect);
    fxBase = base;
    if (statusEl) statusEl.textContent = 'Обновляем…';
    if (baseEl) baseEl.textContent = base;
    if (baseSelect) baseSelect.value = base;

    const quotes = fxCurrencies.map((item) => item.code).filter((code) => code !== base);
    const params = new URLSearchParams();
    params.set('base', base);
    quotes.forEach((code) => params.append('quote', code));
    const query = params.toString();
    const res = await Api.call(`/api/fx/rates?${query}`, 'GET', null, false);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      renderFxTop([]);
      renderFxDetail(null);
      renderFxList([]);
      if (statusEl) statusEl.textContent = 'Нет данных';
      return;
    }
    const payload = res.data;
    const rates = payload.rates && typeof payload.rates === 'object' ? payload.rates : {};
    fxItems = fxCurrencies
      .filter((item) => item.code !== base)
      .map((item) => {
        const raw = rates[item.code];
        const rateValue = typeof raw === 'number' ? raw : Number(raw);
        return { code: item.code, name: item.name, rate: rateValue };
      })
      .filter((item) => Number.isFinite(item.rate));
    fxItems.forEach((item) => {
      item.series = buildFxSeries(item.rate, `${base}-${item.code}`);
      item.metrics = summarizeSeries(item.series);
    });
    const top = fxItems.slice().sort((a, b) => b.metrics.volatility - a.metrics.volatility).slice(0, 3);
    renderFxTop(top);
    if (!fxSelectedCode || !fxItems.some((item) => item.code === fxSelectedCode)) {
      fxSelectedCode = (top[0] || fxItems[0] || {}).code || '';
    }
    selectFx(fxSelectedCode);
    if (statusEl) {
      const updated = formatFxUpdated(payload.asOf);
      statusEl.textContent = updated ? `Обновлено ${updated}` : 'Обновлено';
    }
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
    const overlay = document.querySelector('#add-account-overlay');
    if (!btn || !menu || !overlay) return;
    let open = false;
    const toggle = (state) => {
      open = state ?? !open;
      if (open) {
        overlay.style.display = 'flex';
        menu.style.display = 'grid';
      } else {
        overlay.style.display = 'none';
        menu.style.display = 'none';
      }
    };
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      toggle();
    });
    document.addEventListener('click', (e) => {
      if (open && (!menu.contains(e.target) && !btn.contains(e.target) && !overlay.contains(e.target))) {
        toggle(false);
      }
    });
    overlay.addEventListener('click', () => open && toggle(false));
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
    await loadFxCurrencies();
    bindFxControls();
    await loadFxRates();
    renderLineChart(selectors.balanceChart, demoData.balance, baseCurrency);
    renderBarChart(selectors.expenseChart, demoData.expenses, baseCurrency);
    bindAddAccountMenu();
    renderSparkline(selectors.btcSpark, demoData.crypto.btc, '#f7931a');
    renderSparkline(selectors.ethSpark, demoData.crypto.eth, '#4f8bff');
    renderSparkline(selectors.solSpark, demoData.crypto.sol, '#10b981');
    if (root) root.style.visibility = 'visible';
  });
})();
