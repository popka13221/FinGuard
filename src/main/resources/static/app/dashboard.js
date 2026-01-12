(() => {
  const selectors = {
    userEmail: '#userEmail',
    logoutBtn: '#btn-logout',
    baseCurrencyBtn: '#btn-base-currency',
    langBtn: '#btn-lang',
    balanceChart: '#balanceChart',
    expenseChart: '#expenseChart',
    btcSpark: '#btcSpark',
    ethSpark: '#ethSpark',
    solSpark: '#solSpark',
    btcPrice: '#btcPrice',
    ethPrice: '#ethPrice',
    solPrice: '#solPrice',
    cryptoStatus: '#cryptoStatus',
    cryptoBase: '#cryptoBase',
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
    fxSortButtons: '.fx-sort button',
    addAccountBtn: '#btn-add-account',
    addAccountOverlay: '#add-account-overlay',
    addAccountMenu: '#add-account-menu',
    addAccountName: '#newAccountName',
    addAccountCurrency: '#newAccountCurrency',
    addAccountBalance: '#newAccountBalance',
    addAccountCancelBtn: '#btn-add-account-cancel',
    addAccountCreateBtn: '#btn-add-account-create',
    addAccountCloseBtn: '#btn-add-account-close',
    addAccountError: '#addAccountError',
    walletsList: '#walletsList',
    addWalletBtn: '#btn-add-wallet',
    addWalletOverlay: '#add-wallet-overlay',
    addWalletMenu: '#add-wallet-menu',
    addWalletLabel: '#newWalletLabel',
    addWalletNetwork: '#newWalletNetwork',
    addWalletAddress: '#newWalletAddress',
    addWalletCancelBtn: '#btn-add-wallet-cancel',
    addWalletCreateBtn: '#btn-add-wallet-create',
    addWalletCloseBtn: '#btn-add-wallet-close',
    addWalletError: '#addWalletError',
    baseCurrencyOverlay: '#base-currency-overlay',
    baseCurrencyMenu: '#base-currency-menu',
    baseCurrencySelect: '#baseCurrencySelect',
    baseCurrencyCancelBtn: '#btn-base-currency-cancel',
    baseCurrencySaveBtn: '#btn-base-currency-save',
    baseCurrencyCloseBtn: '#btn-base-currency-close',
    baseCurrencyError: '#baseCurrencyError',
    incomeExpenseNet: '#incomeExpenseNet',
    paymentRentAmount: '#paymentRentAmount',
    paymentSpotifyAmount: '#paymentSpotifyAmount',
    paymentMobileAmount: '#paymentMobileAmount'
  };

  const LANG_STORAGE_KEY = 'finguard:lang';
  const I18N = {
    ru: {
      dashboard_title: 'Личный кабинет',
      logout: 'Выйти',
      base_currency_button: 'Валюта: {value}',
      balance: 'Баланс',
      credit: 'Кредит',
      credit_placeholder: 'Кредит: —',
      income_expense_month: 'Доход / Расход (мес.)',
      income_expense_details: 'Доход: 4 200 · Расход: 2 350',
      your_cards: 'Ваши карты',
      add: 'Добавить',
      loading_balance: 'Загружаем баланс…',
      upcoming_payments: 'Ближайшие платежи',
      payment_rent: 'Аренда',
      payment_due_jan15: 'Срок: 15 янв',
      payment_due_jan19: 'Срок: 19 янв',
      payment_mobile: 'Мобильная связь',
      payment_due_jan22: 'Срок: 22 янв',
      demo: 'Демо',
      balance_trend_title: 'Динамика баланса',
      last_6_months: 'Последние 6 месяцев,',
      trend_label: 'Тренд:',
      balance_chart_aria: 'График баланса',
      expense_breakdown_title: 'Структура расходов',
      current_month: 'Текущий месяц,',
      expense_chart_aria: 'Диаграмма расходов',
      markets_title: 'Курсы и рынки',
      markets_subtitle: 'Сводка по крипте и валютам',
      coin_rates: 'Курсы монет',
      loading: 'Загрузка…',
      updating: 'Обновляем…',
      demo_data: 'Демо-данные',
      no_data: 'Нет данных',
      updated: 'Обновлено',
      updated_at: 'Обновлено {value}',
      base_label: 'База:',
      all_currencies: 'Все валюты',
      hide: 'Скрыть',
      most_volatile: 'Самые волатильные',
      loading_rates: 'Загружаем курсы…',
      select_currency_details: 'Выберите валюту для деталей',
      search_currency_placeholder: 'Поиск валюты',
      search_currency_aria: 'Поиск валюты',
      base_currency_aria: 'Базовая валюта',
      volatility: 'Волатильность',
      add_account_menu_aria: 'Добавить счёт',
      add_account_title: 'Добавить счёт',
      add_account_subtitle: 'Создайте новый счёт для учёта баланса.',
      account_name_label: 'Название',
      account_name_placeholder: 'Например: Visa',
      currency_label: 'Валюта',
      currency_aria: 'Валюта',
      initial_balance_label: 'Начальный баланс',
      initial_balance_placeholder: '0.00',
      cancel: 'Отмена',
      create: 'Создать',
      save: 'Сохранить',
      close_dialog: 'Закрыть',
      create_account_failed: 'Не удалось создать счёт.',
      enter_account_name: 'Введите название счёта.',
      select_currency: 'Выберите валюту.',
      invalid_balance: 'Некорректный баланс.',
      total: 'Всего',
      balance_by_currency: 'Баланс по валютам',
      no_accounts: 'Счета пока не добавлены.',
      account: 'Счет',
      archived: 'Архив',
      balance_load_failed: 'Не удалось загрузить баланс.',
      balance_load_failed_short: 'Не удалось загрузить баланс',
      fx_no_data: 'Нет данных по валютам.',
      fx_no_currencies: 'Нет доступных валют.',
      wallets_title: 'Крипто-кошельки',
      wallets_empty: 'Кошельки не добавлены.',
      wallets_loading_failed: 'Не удалось загрузить кошельки.',
      add_wallet_menu_aria: 'Добавить кошелёк',
      add_wallet_title: 'Добавить кошелёк',
      add_wallet_subtitle: 'Только чтение: добавьте адрес, мы подтянем баланс.',
      wallet_label: 'Название',
      wallet_label_placeholder: 'Например: Ledger',
      wallet_network: 'Сеть',
      wallet_network_aria: 'Сеть',
      wallet_address: 'Адрес',
      wallet_address_placeholder: '0x… / bc1…',
      wallet_remove: 'Удалить',
      wallet_enter_address: 'Введите адрес.',
      wallet_invalid_address: 'Некорректный адрес.',
      wallet_create_failed: 'Не удалось добавить кошелёк.',
      base_currency_menu_aria: 'Изменить базовую валюту',
      base_currency_title: 'Базовая валюта',
      base_currency_subtitle: 'Все суммы будут отображаться в выбранной валюте.',
      base_currency_update_failed: 'Не удалось обновить базовую валюту.',
      base_currency_conversion_failed: 'Не удалось конвертировать суммы в базовую валюту.',
      period_7d: 'за 7д',
      period_24h: 'за 24ч',
      min: 'Мин',
      max: 'Макс',
      change: 'Изменение',
      average: 'Среднее',
      trend_pct: 'Тренд %',
      expense_housing: 'Жильё',
      expense_food: 'Еда',
      expense_transport: 'Транспорт',
      expense_subscriptions: 'Подписки',
      expense_other: 'Прочее'
    },
    en: {
      dashboard_title: 'Dashboard',
      logout: 'Log out',
      base_currency_button: 'Currency: {value}',
      balance: 'Balance',
      credit: 'Credit',
      credit_placeholder: 'Credit: —',
      income_expense_month: 'Income / Expense (mo.)',
      income_expense_details: 'Income: 4 200 · Expense: 2 350',
      your_cards: 'Your cards',
      add: 'Add',
      loading_balance: 'Loading balance…',
      upcoming_payments: 'Upcoming payments',
      payment_rent: 'Rent',
      payment_due_jan15: 'Due: Jan 15',
      payment_due_jan19: 'Due: Jan 19',
      payment_mobile: 'Mobile service',
      payment_due_jan22: 'Due: Jan 22',
      demo: 'Demo',
      balance_trend_title: 'Balance trend',
      last_6_months: 'Last 6 months,',
      trend_label: 'Trend:',
      balance_chart_aria: 'Balance chart',
      expense_breakdown_title: 'Expense breakdown',
      current_month: 'Current month,',
      expense_chart_aria: 'Expense chart',
      markets_title: 'Rates & markets',
      markets_subtitle: 'Crypto & FX summary',
      coin_rates: 'Coin rates',
      loading: 'Loading…',
      updating: 'Updating…',
      demo_data: 'Demo data',
      no_data: 'No data',
      updated: 'Updated',
      updated_at: 'Updated {value}',
      base_label: 'Base:',
      all_currencies: 'All currencies',
      hide: 'Hide',
      most_volatile: 'Most volatile',
      loading_rates: 'Loading rates…',
      select_currency_details: 'Select a currency for details',
      search_currency_placeholder: 'Search currency',
      search_currency_aria: 'Search currency',
      base_currency_aria: 'Base currency',
      volatility: 'Volatility',
      add_account_menu_aria: 'Add account',
      add_account_title: 'Add account',
      add_account_subtitle: 'Create a new account to track balances.',
      account_name_label: 'Name',
      account_name_placeholder: 'e.g. Visa',
      currency_label: 'Currency',
      currency_aria: 'Currency',
      initial_balance_label: 'Initial balance',
      initial_balance_placeholder: '0.00',
      cancel: 'Cancel',
      create: 'Create',
      save: 'Save',
      close_dialog: 'Close',
      create_account_failed: 'Failed to create account.',
      enter_account_name: 'Enter an account name.',
      select_currency: 'Select a currency.',
      invalid_balance: 'Invalid balance.',
      total: 'Total',
      balance_by_currency: 'Balance by currency',
      no_accounts: 'No accounts added yet.',
      account: 'Account',
      archived: 'Archived',
      balance_load_failed: 'Failed to load balance.',
      balance_load_failed_short: 'Failed to load balance',
      fx_no_data: 'No FX data.',
      fx_no_currencies: 'No currencies available.',
      wallets_title: 'Crypto wallets',
      wallets_empty: 'No wallets added yet.',
      wallets_loading_failed: 'Failed to load wallets.',
      add_wallet_menu_aria: 'Add wallet',
      add_wallet_title: 'Add wallet',
      add_wallet_subtitle: 'Watch-only: add an address and we will fetch its balance.',
      wallet_label: 'Label',
      wallet_label_placeholder: 'e.g. Ledger',
      wallet_network: 'Network',
      wallet_network_aria: 'Network',
      wallet_address: 'Address',
      wallet_address_placeholder: '0x… / bc1…',
      wallet_remove: 'Remove',
      wallet_enter_address: 'Enter an address.',
      wallet_invalid_address: 'Invalid address.',
      wallet_create_failed: 'Failed to add wallet.',
      base_currency_menu_aria: 'Change base currency',
      base_currency_title: 'Base currency',
      base_currency_subtitle: 'All amounts are shown in the selected currency.',
      base_currency_update_failed: 'Failed to update base currency.',
      base_currency_conversion_failed: 'Failed to convert amounts to base currency.',
      period_7d: 'in 7d',
      period_24h: 'in 24h',
      min: 'Min',
      max: 'Max',
      change: 'Change',
      average: 'Average',
      trend_pct: 'Trend %',
      expense_housing: 'Housing',
      expense_food: 'Food',
      expense_transport: 'Transport',
      expense_subscriptions: 'Subscriptions',
      expense_other: 'Other'
    }
  };

  function detectBrowserLang() {
    const rawList = Array.isArray(navigator.languages) && navigator.languages.length
      ? navigator.languages
      : [navigator.language];
    for (const raw of rawList) {
      const normalized = String(raw || '').toLowerCase().split(/[-_]/)[0];
      if (normalized === 'en' || normalized === 'ru') return normalized;
    }
    return 'en';
  }

  function loadLang() {
    try {
      const stored = localStorage.getItem(LANG_STORAGE_KEY);
      if (stored === 'en' || stored === 'ru') return stored;
    } catch (_) {
      // ignore
    }
    return detectBrowserLang();
  }

  let currentLang = loadLang();

  function t(key, vars) {
    const langTable = I18N[currentLang] || I18N.ru;
    let value = langTable[key] ?? I18N.ru[key] ?? key;
    if (vars && typeof value === 'string') {
      Object.entries(vars).forEach(([name, raw]) => {
        value = value.replaceAll(`{${name}}`, String(raw));
      });
    }
    return value;
  }

  function getLocale() {
    return currentLang === 'en' ? 'en-US' : 'ru-RU';
  }

  function applyTranslations() {
    document.querySelectorAll('[data-i18n]').forEach((el) => {
      const key = el.dataset.i18n;
      if (!key) return;
      el.textContent = t(key);
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach((el) => {
      const key = el.dataset.i18nPlaceholder;
      if (!key) return;
      el.setAttribute('placeholder', t(key));
    });
    document.querySelectorAll('[data-i18n-aria]').forEach((el) => {
      const key = el.dataset.i18nAria;
      if (!key) return;
      el.setAttribute('aria-label', t(key));
    });
    document.querySelectorAll('[data-i18n-title]').forEach((el) => {
      const key = el.dataset.i18nTitle;
      if (!key) return;
      el.setAttribute('title', t(key));
    });
  }

  function updateLangToggle() {
    const btn = document.querySelector(selectors.langBtn);
    if (!btn) return;
    if (currentLang === 'en') {
      btn.textContent = 'RU';
      btn.setAttribute('title', 'Русский');
      btn.setAttribute('aria-label', 'Switch language to Russian');
    } else {
      btn.textContent = 'EN';
      btn.setAttribute('title', 'English');
      btn.setAttribute('aria-label', 'Переключить язык на английский');
    }
  }

  function applyLanguage(lang) {
    currentLang = lang === 'en' ? 'en' : 'ru';
    document.documentElement.lang = currentLang;
    applyTranslations();
    updateLangToggle();
  }

  function bindLangToggle() {
    const btn = document.querySelector(selectors.langBtn);
    if (!btn) return;
    btn.addEventListener('click', () => {
      const next = currentLang === 'en' ? 'ru' : 'en';
      try {
        localStorage.setItem(LANG_STORAGE_KEY, next);
      } catch (_) {
        // ignore
      }
      window.location.reload();
    });
  }

  applyLanguage(currentLang);
  bindLangToggle();

  const demoData = {
    balance: [18200, 18750, 19340, 18900, 20200, 20950],
    expenses: [
      { labelKey: 'expense_housing', value: 720, color: '#4f8bff' },
      { labelKey: 'expense_food', value: 540, color: '#10b981' },
      { labelKey: 'expense_transport', value: 310, color: '#f97316' },
      { labelKey: 'expense_subscriptions', value: 260, color: '#3cc7c4' },
      { labelKey: 'expense_other', value: 520, color: '#9aa0aa' }
    ],
    crypto: {
      btc: [61200, 61850, 62500, 61900, 64000, 66200],
      eth: [3020, 3100, 3150, 3080, 3180, 3120],
      sol: [128, 134, 140, 137, 143, 145]
    }
  };

  const cryptoAssets = [
    { code: 'BTC', name: 'Bitcoin', priceSelector: selectors.btcPrice, sparkSelector: selectors.btcSpark, color: '#f7931a', fallbackSeries: demoData.crypto.btc },
    { code: 'ETH', name: 'Ethereum', priceSelector: selectors.ethPrice, sparkSelector: selectors.ethSpark, color: '#4f8bff', fallbackSeries: demoData.crypto.eth },
    { code: 'SOL', name: 'Solana', priceSelector: selectors.solPrice, sparkSelector: selectors.solSpark, color: '#10b981', fallbackSeries: demoData.crypto.sol }
  ];

  const fxFallbackCurrencies = [
    { code: 'USD', name: 'US Dollar' },
    { code: 'EUR', name: 'Euro' },
    { code: 'RUB', name: 'Russian Ruble' },
    { code: 'CNY', name: 'Chinese Yuan' }
  ];
  const fxFallbackBase = 'USD';
  const fxExcluded = new Set(['BTC', 'ETH']);
  const fxSortModes = { volatility: 'volatility', alpha: 'alpha' };
  let supportedCurrencies = fxFallbackCurrencies.concat([
    { code: 'BTC', name: 'Bitcoin' },
    { code: 'ETH', name: 'Ethereum' }
  ]);
  let fxCurrencies = fxFallbackCurrencies.slice();
  let fxBase = '';
  let fxSortMode = fxSortModes.volatility;
  let fxSearchQuery = '';
  let fxSelectedCode = '';
  let fxItems = [];

  let baseCurrency = 'USD';
  let balanceRenderId = 0;
  let cryptoWalletTotalInBase = NaN;
  let lastBalanceSnapshot = null;
  let lastBalanceConversion = null;

  function normalizeCurrency(code) {
    return (code || '').trim().toUpperCase();
  }

  function toNumber(value) {
    if (typeof value === 'number') return value;
    if (typeof value === 'string') {
      const trimmed = value.trim();
      if (!trimmed) return NaN;
      return Number(trimmed);
    }
    return NaN;
  }

  function sumTotals(totals) {
    return (Array.isArray(totals) ? totals : []).reduce((acc, item) => {
      const value = toNumber(item?.total);
      return Number.isFinite(value) ? acc + value : acc;
    }, 0);
  }

  function isCryptoCurrency(code) {
    const normalized = normalizeCurrency(code);
    return normalized === 'BTC' || normalized === 'ETH';
  }

  function uniqueCurrencies(values) {
    return Array.from(new Set((Array.isArray(values) ? values : [])
      .map((v) => normalizeCurrency(v))
      .filter(Boolean)));
  }

  async function fetchFxRates(base, quotes) {
    const normalizedBase = normalizeCurrency(base) || 'USD';
    const uniqueQuotes = uniqueCurrencies(quotes)
      .filter((code) => code && code !== normalizedBase)
      .filter((code) => !isCryptoCurrency(code));
    if (uniqueQuotes.length === 0) {
      return { ok: true, base: normalizedBase, rates: {}, asOf: null };
    }

    const params = new URLSearchParams();
    params.set('base', normalizedBase);
    uniqueQuotes.forEach((code) => params.append('quote', code));
    const res = await Api.call(`/api/fx/rates?${params}`, 'GET', null, false);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      return { ok: false, base: normalizedBase, rates: {}, asOf: null };
    }
    const rates = res.data.rates && typeof res.data.rates === 'object' ? res.data.rates : {};
    return { ok: true, base: normalizedBase, rates, asOf: res.data.asOf };
  }

  async function fetchCryptoUsdPrices() {
    const res = await Api.call('/api/crypto/rates?base=USD', 'GET', null, false);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      return { ok: false, prices: {}, asOf: null };
    }
    const payload = res.data;
    const list = Array.isArray(payload.rates) ? payload.rates : [];
    const prices = {};
    list.forEach((item) => {
      const code = item && item.code ? String(item.code).toUpperCase() : '';
      const price = toNumber(item && item.price);
      if (code && Number.isFinite(price) && price > 0) {
        prices[code] = price;
      }
    });
    return { ok: Object.keys(prices).length > 0, prices, asOf: payload.asOf };
  }

  async function buildConversionContext(base, currencies) {
    const normalizedBase = normalizeCurrency(base) || 'USD';
    const currencyList = uniqueCurrencies(currencies);
    const hasCrypto = currencyList.some((code) => isCryptoCurrency(code)) || isCryptoCurrency(normalizedBase);
    const baseIsCrypto = isCryptoCurrency(normalizedBase);

    let fx = { ok: true, base: normalizedBase, rates: {}, asOf: null };
    let cryptoUsd = { ok: true, prices: {}, asOf: null };

    if (baseIsCrypto) {
      const fiat = currencyList.filter((code) => !isCryptoCurrency(code) && code !== 'USD');
      fx = await fetchFxRates('USD', fiat);
      cryptoUsd = await fetchCryptoUsdPrices();
    } else {
      const fiat = currencyList.filter((code) => !isCryptoCurrency(code) && code !== normalizedBase);
      if (hasCrypto && normalizedBase !== 'USD' && !fiat.includes('USD')) {
        fiat.push('USD');
      }
      fx = await fetchFxRates(normalizedBase, fiat);
      if (hasCrypto) {
        cryptoUsd = await fetchCryptoUsdPrices();
      }
    }

    return {
      base: normalizedBase,
      baseIsCrypto,
      fx,
      cryptoUsd,
      ok: fx.ok && (!hasCrypto || cryptoUsd.ok)
    };
  }

  function convertToBaseAmount(amount, currency, ctx) {
    const base = normalizeCurrency(ctx && ctx.base ? ctx.base : baseCurrency) || 'USD';
    const from = normalizeCurrency(currency);
    const value = toNumber(amount);
    if (!Number.isFinite(value)) {
      return NaN;
    }
    if (!from || from === base) {
      return value;
    }
    if (!ctx || !ctx.ok) {
      return NaN;
    }

    if (ctx.baseIsCrypto) {
      const baseUsd = toNumber(ctx.cryptoUsd && ctx.cryptoUsd.prices ? ctx.cryptoUsd.prices[base] : NaN);
      if (!Number.isFinite(baseUsd) || baseUsd <= 0) {
        return NaN;
      }
      if (isCryptoCurrency(from)) {
        const fromUsd = toNumber(ctx.cryptoUsd && ctx.cryptoUsd.prices ? ctx.cryptoUsd.prices[from] : NaN);
        if (!Number.isFinite(fromUsd) || fromUsd <= 0) {
          return NaN;
        }
        return (value * fromUsd) / baseUsd;
      }
      if (from === 'USD') {
        return value / baseUsd;
      }
      const quotePerUsd = toNumber(ctx.fx && ctx.fx.rates ? ctx.fx.rates[from] : NaN);
      if (!Number.isFinite(quotePerUsd) || quotePerUsd <= 0) {
        return NaN;
      }
      const usdAmount = value / quotePerUsd;
      return usdAmount / baseUsd;
    }

    if (!isCryptoCurrency(from)) {
      const quotePerBase = toNumber(ctx.fx && ctx.fx.rates ? ctx.fx.rates[from] : NaN);
      if (!Number.isFinite(quotePerBase) || quotePerBase <= 0) {
        return NaN;
      }
      return value / quotePerBase;
    }

    const fromUsd = toNumber(ctx.cryptoUsd && ctx.cryptoUsd.prices ? ctx.cryptoUsd.prices[from] : NaN);
    if (!Number.isFinite(fromUsd) || fromUsd <= 0) {
      return NaN;
    }
    const usdValue = value * fromUsd;
    if (base === 'USD') {
      return usdValue;
    }
    const usdPerBase = toNumber(ctx.fx && ctx.fx.rates ? ctx.fx.rates.USD : NaN);
    if (!Number.isFinite(usdPerBase) || usdPerBase <= 0) {
      return NaN;
    }
    return usdValue / usdPerBase;
  }

  function updateCurrencyLabels() {
    const bal = document.querySelector('#balanceCurrency');
    const exp = document.querySelector('#expenseCurrency');
    if (bal) bal.textContent = baseCurrency;
    if (exp) exp.textContent = baseCurrency;
    const baseBtn = document.querySelector(selectors.baseCurrencyBtn);
    if (baseBtn) baseBtn.textContent = t('base_currency_button', { value: baseCurrency });
    const totalExpense = document.querySelector('#expenseTotal');
    if (totalExpense) totalExpense.textContent = `${t('total')}: ${formatMoney(2350, baseCurrency)}`;
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

  function moneyFractionDigits(currency) {
    const cur = (currency || '').trim().toUpperCase();
    if (cur === 'BTC' || cur === 'ETH') return 8;
    return 2;
  }

  function formatMoney(value, currency) {
    const cur = currency || baseCurrency || 'USD';
    const abs = Math.abs(value || 0);
    const sign = (value || 0) < 0 ? '-' : '';
    const digits = moneyFractionDigits(cur);
    return `${sign}${abs.toLocaleString(getLocale(), { minimumFractionDigits: digits, maximumFractionDigits: digits })} ${cur}`;
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#39;');
  }

  function formatCryptoPrice(value, currency) {
    if (typeof value !== 'number' || Number.isNaN(value)) return '—';
    const cur = (currency || 'USD').toUpperCase();
    const digits = value < 1 ? 6 : 2;
    try {
      return value.toLocaleString(getLocale(), {
        style: 'currency',
        currency: cur,
        minimumFractionDigits: digits,
        maximumFractionDigits: digits
      });
    } catch (_) {
      return `${value.toLocaleString(getLocale(), { minimumFractionDigits: digits, maximumFractionDigits: digits })} ${cur}`;
    }
  }

  function resolveFxBase() {
    const normalized = (fxBase || baseCurrency || '').toUpperCase();
    return fxCurrencies.some((item) => item.code === normalized) ? normalized : fxFallbackBase;
  }

  function formatFxRate(value) {
    if (typeof value !== 'number' || Number.isNaN(value)) return '—';
    const digits = value < 1 ? 4 : 2;
    return value.toLocaleString(getLocale(), { minimumFractionDigits: digits, maximumFractionDigits: digits });
  }

  function formatFxUpdated(isoValue) {
    if (!isoValue) return '';
    const date = new Date(isoValue);
    if (Number.isNaN(date.getTime())) return '';
    return date.toLocaleString(getLocale(), { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
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

  function renderBalance(summary, conversion, cryptoTotalInBase) {
    const totals = Array.isArray(summary.totalsByCurrency) ? summary.totalsByCurrency : [];
    const accounts = Array.isArray(summary.accounts) ? summary.accounts : [];
    const totalBalanceEl = document.querySelector(selectors.totalBalance);
    const creditEl = document.querySelector(selectors.creditValue);
    const totalsLineEl = document.querySelector(selectors.totalsByCurrency);
    const base = normalizeCurrency(baseCurrency) || 'USD';

    let totalOk = true;
    const totalInBase = totals.reduce((acc, item) => {
      const value = convertToBaseAmount(item?.total, item?.currency, conversion);
      if (!Number.isFinite(value)) {
        totalOk = false;
        return acc;
      }
      return acc + value;
    }, 0);

    const cryptoValue = toNumber(cryptoTotalInBase);
    const cryptoOk = Number.isFinite(cryptoValue);
    if (totalBalanceEl) {
      if (!totals.length) {
        totalBalanceEl.textContent = formatMoney(cryptoOk ? cryptoValue : 0, base);
      } else if (totalOk) {
        totalBalanceEl.textContent = formatMoney(totalInBase + (cryptoOk ? cryptoValue : 0), base);
      } else {
        const baseItem = totals.find((item) => normalizeCurrency(item?.currency) === base);
        const fallback = baseItem || totals[0];
        const fallbackCurrency = normalizeCurrency(fallback?.currency) || base;
        const fallbackAmount = toNumber(fallback?.total);
        const canAddCrypto = cryptoOk && normalizeCurrency(fallbackCurrency) === base;
        totalBalanceEl.textContent = formatMoney(
          (Number.isFinite(fallbackAmount) ? fallbackAmount : 0) + (canAddCrypto ? cryptoValue : 0),
          fallbackCurrency
        );
      }
    }

    let creditOk = true;
    const creditInBase = accounts.reduce((acc, account) => {
      const balance = toNumber(account?.balance);
      if (!Number.isFinite(balance) || balance >= 0) {
        return acc;
      }
      const value = convertToBaseAmount(Math.abs(balance), account?.currency, conversion);
      if (!Number.isFinite(value)) {
        creditOk = false;
        return acc;
      }
      return acc + value;
    }, 0);
    if (creditEl) {
      creditEl.textContent = creditOk
        ? `${t('credit')}: ${formatMoney(creditInBase || 0, base)}`
        : `${t('credit')}: —`;
    }

    const totalsText = totals.length
      ? totals.map((item) => {
        const cur = normalizeCurrency(item?.currency);
        const value = convertToBaseAmount(item?.total, cur, conversion);
        if (Number.isFinite(value)) {
          return `${cur}: ${formatMoney(value, base)}`;
        }
        return formatMoney(item?.total || 0, cur || base);
      }).join(' · ')
      : '';
    if (totalsLineEl) {
      totalsLineEl.textContent = totalsText ? `${t('balance_by_currency')}: ${totalsText}` : '';
      totalsLineEl.style.display = totalsText ? 'block' : 'none';
    }

    showBalanceError(totalOk && creditOk && conversion && conversion.ok ? '' : t('base_currency_conversion_failed'));
  }

  function rerenderBalanceSnapshot() {
    if (!lastBalanceSnapshot || !lastBalanceConversion) return;
    renderBalance(lastBalanceSnapshot, lastBalanceConversion, cryptoWalletTotalInBase);
  }

  function renderAccountsList(accounts, conversion) {
    const list = document.querySelector(selectors.accountsList);
    if (!list) return;
    if (!accounts || accounts.length === 0) {
      list.innerHTML = `<div class="muted">${t('no_accounts')}</div>`;
      return;
    }
    const base = normalizeCurrency(baseCurrency) || 'USD';
    list.innerHTML = accounts.map((acc) => {
      const value = convertToBaseAmount(acc.balance || 0, acc.currency, conversion);
      const amountText = Number.isFinite(value) ? formatMoney(value, base) : formatMoney(acc.balance || 0, acc.currency);
      const balanceValue = toNumber(acc.balance);
      const signClass = Number.isFinite(balanceValue) && balanceValue < 0 ? 'amount-negative' : 'amount-positive';
      const safeName = escapeHtml(acc.name || t('account'));
      const safeCurrency = escapeHtml(acc.currency || baseCurrency);
      return `
        <div class="list-item">
          <div>
            <div style="font-weight:800;">${safeName}</div>
            <small>${safeCurrency}${acc.archived ? ` · ${t('archived')}` : ''}</small>
          </div>
          <div class="${signClass}">${amountText}</div>
        </div>
      `;
    }).join('');
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
      container.innerHTML = `<div class="muted">${t('fx_no_data')}</div>`;
      return;
    }
    container.innerHTML = items.map((item, idx) => {
      const changeClass = pickChangeClass(item.metrics.change);
      const stroke = item.metrics.change >= 0 ? '#10b981' : '#f97316';
      const safeName = escapeHtml(item.name || '');
      return `
        <button type="button" class="fx-card" data-code="${item.code}" style="--delay:${idx * 70}ms;">
          <div class="fx-card-main">
            <div class="fx-card-code">${item.code}</div>
            <div class="fx-card-name">${safeName}</div>
          </div>
          <div class="fx-card-side">
            <div class="fx-card-rate">${formatFxRate(item.rate)}</div>
            <div class="${changeClass}">${formatChangePct(item.metrics.changePct)} ${t('period_7d')}</div>
          </div>
          <div class="fx-card-spark">${sparkSvg(item.series, 160, 22, stroke)}</div>
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
      list.innerHTML = `<div class="muted">${t('fx_no_currencies')}</div>`;
      return;
    }
    list.innerHTML = items.map((item) => {
      const active = item.code === fxSelectedCode ? 'is-active' : '';
      const safeName = escapeHtml(item.name || '');
      return `
        <button type="button" class="fx-list-item ${active}" data-code="${item.code}">
          <div class="fx-list-left">
            <div class="fx-list-code">${item.code}</div>
            <div class="fx-list-name">${safeName}</div>
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
      if (nameEl) nameEl.textContent = t('no_data');
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
      changeEl.textContent = `${formatChangePct(item.metrics.changePct)} ${t('period_7d')}`;
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
        toggle.textContent = browse.classList.contains('is-open') ? t('hide') : t('all_currencies');
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
        const all = data
          .filter((item) => item && item.code && item.name)
          .map((item) => ({ code: item.code.toUpperCase(), name: item.name }))
          .filter((item) => item.code.length === 3);
        if (all.length) {
          supportedCurrencies = all;
          const filtered = all.filter((item) => !fxExcluded.has(item.code));
          if (filtered.length) {
            fxCurrencies = filtered;
          }
        }
      }
    } catch (_) {
      fxCurrencies = fxFallbackCurrencies.slice();
      supportedCurrencies = fxFallbackCurrencies.concat([
        { code: 'BTC', name: 'Bitcoin' },
        { code: 'ETH', name: 'Ethereum' }
      ]);
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
    populateAccountCurrencySelect();
    populateBaseCurrencySelect();
  }

  async function loadBalance() {
    const list = document.querySelector(selectors.accountsList);
    if (list) {
      list.innerHTML = `<div class="muted">${t('loading_balance')}</div>`;
    }
    const totalBalanceEl = document.querySelector(selectors.totalBalance);
    if (totalBalanceEl) totalBalanceEl.textContent = t('loading');
    showBalanceError('');

    const res = await Api.call('/api/accounts/balance', 'GET', null, true);
    if (!res.ok) {
      showBalanceError(t('balance_load_failed'));
      if (list) list.innerHTML = `<div class="amount-negative">${t('balance_load_failed_short')}</div>`;
      return;
    }
    const payload = res.data && typeof res.data === 'object' ? res.data : {};
    const accounts = Array.isArray(payload.accounts) ? payload.accounts : [];
    const totalsByCurrency = Array.isArray(payload.totalsByCurrency) ? payload.totalsByCurrency : [];
    const renderId = (balanceRenderId += 1);
    const currencyList = [
      ...accounts.map((item) => item && item.currency),
      ...totalsByCurrency.map((item) => item && item.currency)
    ];
    const conversion = await buildConversionContext(baseCurrency, currencyList);
    if (renderId !== balanceRenderId) return;
    lastBalanceSnapshot = { accounts, totalsByCurrency };
    lastBalanceConversion = conversion;
    renderAccountsList(accounts, conversion);
    rerenderBalanceSnapshot();
  }

  async function loadFxRates() {
    const statusEl = document.querySelector(selectors.fxStatus);
    const baseEl = document.querySelector(selectors.fxBase);
    const base = resolveFxBase();
    const baseSelect = document.querySelector(selectors.fxBaseSelect);
    fxBase = base;
    if (statusEl) statusEl.textContent = t('updating');
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
      if (statusEl) statusEl.textContent = t('no_data');
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
      statusEl.textContent = updated ? t('updated_at', { value: updated }) : t('updated');
    }
  }

  function sampleSeries(series, maxPoints) {
    if (!Array.isArray(series) || series.length === 0) return [];
    if (!maxPoints || series.length <= maxPoints) return series.slice();
    const step = (series.length - 1) / (maxPoints - 1);
    return Array.from({ length: maxPoints }, (_, idx) => series[Math.round(idx * step)]);
  }

  function updateCryptoCard(asset, price, changePct, series, base) {
    const priceEl = document.querySelector(asset.priceSelector);
    if (priceEl) {
      const changeValue = Number.isFinite(changePct) ? changePct : 0;
      priceEl.textContent = formatCryptoPrice(price, base);
      priceEl.classList.remove('amount-positive', 'amount-negative');
      priceEl.classList.add(changeValue < 0 ? 'amount-negative' : 'amount-positive');
      priceEl.title = Number.isFinite(changePct) ? `${formatChangePct(changePct)} ${t('period_24h')}` : '';
    }
    const normalized = sampleSeries(series, 14);
    renderSparkline(asset.sparkSelector, normalized, asset.color);
  }

  function renderCryptoFallback(base) {
    cryptoAssets.forEach((asset) => {
      const fallbackSeries = Array.isArray(asset.fallbackSeries) ? asset.fallbackSeries : [];
      const last = fallbackSeries[fallbackSeries.length - 1];
      updateCryptoCard(asset, typeof last === 'number' ? last : NaN, 0, fallbackSeries, base);
    });
  }

  async function loadCryptoRates() {
    const statusEl = document.querySelector(selectors.cryptoStatus);
    const baseEl = document.querySelector(selectors.cryptoBase);
    const base = (baseCurrency || 'USD').toUpperCase();
    if (statusEl) statusEl.textContent = t('updating');
    if (baseEl) baseEl.textContent = base;

    const params = new URLSearchParams();
    params.set('base', base);
    const res = await Api.call(`/api/crypto/rates?${params}`, 'GET', null, false);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      renderCryptoFallback(base);
      if (statusEl) statusEl.textContent = t('demo_data');
      return;
    }
    const payload = res.data;
    const rates = Array.isArray(payload.rates) ? payload.rates : [];
    if (!rates.length) {
      renderCryptoFallback(base);
      if (statusEl) statusEl.textContent = t('no_data');
      return;
    }
    const baseCode = typeof payload.baseCurrency === 'string' && payload.baseCurrency ? payload.baseCurrency : base;
    if (baseEl) baseEl.textContent = baseCode;

    const byCode = new Map();
    rates.forEach((item) => {
      const code = item && item.code ? String(item.code).toUpperCase() : '';
      if (code) byCode.set(code, item);
    });

    let hasAny = false;
    cryptoAssets.forEach((asset) => {
      const item = byCode.get(asset.code);
      const priceValue = item ? Number(item.price) : NaN;
      const changeValue = item ? Number(item.changePct24h) : NaN;
      const sparklineRaw = item && Array.isArray(item.sparkline)
        ? item.sparkline.map(Number).filter(Number.isFinite)
        : [];
      if (Number.isFinite(priceValue)) {
        updateCryptoCard(asset, priceValue, changeValue, sparklineRaw.length ? sparklineRaw : asset.fallbackSeries, baseCode);
        hasAny = true;
      } else {
        const fallbackSeries = asset.fallbackSeries || [];
        updateCryptoCard(asset, fallbackSeries[fallbackSeries.length - 1], 0, fallbackSeries, baseCode);
      }
    });

    if (statusEl) {
      const updated = formatFxUpdated(payload.asOf);
      statusEl.textContent = updated ? t('updated_at', { value: updated }) : (hasAny ? t('updated') : t('no_data'));
    }
  }

  function renderLineChart(target, data, currency) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(data) || data.length === 0) return;
    const labels = Array.from({ length: data.length }, (_, idx) => {
      const d = new Date();
      d.setMonth(d.getMonth() - (data.length - 1 - idx));
      return d.toLocaleString(getLocale(), { month: 'short' });
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
          <div class="muted">${t('min')}</div>
          <div class="stat-value">${formatMoney(min, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">${t('max')}</div>
          <div class="stat-value">${formatMoney(max, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">${t('change')}</div>
          <div class="stat-value">${formatMoney(delta, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">${t('average')}</div>
          <div class="stat-value">${formatMoney(avg, currency || baseCurrency)}</div>
        </div>
        <div class="stat-chip">
          <div class="muted">${t('trend_pct')}</div>
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
      const safeLabel = escapeHtml(item.label || '');
      return `<button class="legend-item" data-slice="${idx}" aria-label="${safeLabel}">
                <span class="legend-dot" style="background:${color};"></span>
                <span>${safeLabel}</span>
                <span class="legend-pct">${pct}%</span>
              </button>`;
    }).join('');

    el.innerHTML = `
      <div class="pie-wrap">
        <svg viewBox="0 0 ${size} ${size}" class="pie-chart" aria-label="${t('expense_breakdown_title')}">
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
          <text x="${center}" y="${center + 12}" text-anchor="middle" class="pie-muted">${t('total')}</text>
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

  function showAddAccountError(message) {
    const box = document.querySelector(selectors.addAccountError);
    if (!box) return;
    if (message) {
      box.style.display = 'block';
      box.textContent = message;
    } else {
      box.style.display = 'none';
      box.textContent = '';
    }
  }

  function shortAddress(value) {
    if (!value) return '';
    const raw = String(value);
    if (raw.length <= 16) return raw;
    return `${raw.slice(0, 6)}…${raw.slice(-4)}`;
  }

  function formatAssetAmount(value, asset) {
    const amount = Number(value);
    if (!Number.isFinite(amount)) return '—';
    const code = (asset || '').toUpperCase();
    const digits = code === 'BTC' ? 8 : 6;
    return `${amount.toLocaleString(getLocale(), { minimumFractionDigits: 0, maximumFractionDigits: digits })} ${code || ''}`.trim();
  }

  function walletNetworkLabel(network) {
    const code = (network || '').toUpperCase();
    if (code === 'ARBITRUM') return 'Arbitrum (ETH)';
    if (code === 'EVM') return 'Total (ETH + Arbitrum)';
    return code;
  }

  function walletNativeAsset(network) {
    const code = (network || '').toUpperCase();
    if (code === 'ARBITRUM') return 'ETH';
    if (code === 'EVM') return 'ETH';
    return code;
  }

  function renderWallets(wallets) {
    const list = document.querySelector(selectors.walletsList);
    if (!list) return;
    if (!Array.isArray(wallets) || wallets.length === 0) {
      list.innerHTML = `<div class="muted">${t('wallets_empty')}</div>`;
      return;
    }
    list.innerHTML = wallets.map((wallet) => {
      const network = (wallet && wallet.network ? String(wallet.network) : '').toUpperCase();
      const networkLabel = walletNetworkLabel(network);
      const label = wallet && wallet.label ? escapeHtml(String(wallet.label)) : '';
      const addressShort = wallet && wallet.address ? escapeHtml(shortAddress(String(wallet.address))) : '';
      const balance = wallet ? wallet.balance : null;
      const valueInBase = wallet ? wallet.valueInBase : null;
      const base = wallet && wallet.baseCurrency ? String(wallet.baseCurrency) : baseCurrency;
      const valueText = Number.isFinite(Number(valueInBase)) ? formatMoney(Number(valueInBase), base) : '';
      const mainValueText = valueText ? `≈ ${valueText}` : '—';
      return `
        <div class="list-item wallet-item">
          <div class="wallet-left">
            <div style="font-weight:800;">${label || escapeHtml(networkLabel)}</div>
            <small>${escapeHtml(networkLabel)}${addressShort ? ` · ${addressShort}` : ''}</small>
          </div>
          <div class="wallet-actions">
            <div class="wallet-right">
              <div class="amount-positive">${mainValueText}</div>
              <small class="muted">${formatAssetAmount(balance, walletNativeAsset(network))}</small>
            </div>
            <button type="button" class="ghost wallet-remove" data-wallet-id="${wallet.id || ''}" title="${t('wallet_remove')}" aria-label="${t('wallet_remove')}">✕</button>
          </div>
        </div>
      `;
    }).join('');

    list.querySelectorAll('[data-wallet-id]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.walletId;
        if (!id) return;
        await Api.call(`/api/crypto/wallets/${encodeURIComponent(id)}`, 'DELETE', null, true);
        await loadWallets();
      });
    });
  }

  async function loadWallets() {
    const list = document.querySelector(selectors.walletsList);
    if (list) {
      list.innerHTML = `<div class="muted">${t('loading')}</div>`;
    }
    const res = await Api.call('/api/crypto/wallets/summary', 'GET', null, true);
    if (!res.ok) {
      if (list) list.innerHTML = `<div class="amount-negative">${t('wallets_loading_failed')}</div>`;
      cryptoWalletTotalInBase = NaN;
      rerenderBalanceSnapshot();
      return;
    }
    const payload = res.data && typeof res.data === 'object' ? res.data : {};
    const wallets = Array.isArray(payload.wallets) ? payload.wallets : [];
    renderWallets(wallets);
    const total = toNumber(payload.totalValueInBase);
    cryptoWalletTotalInBase = Number.isFinite(total) ? total : NaN;
    rerenderBalanceSnapshot();
  }

  function showAddWalletError(message) {
    const box = document.querySelector(selectors.addWalletError);
    if (!box) return;
    if (message) {
      box.style.display = 'block';
      box.textContent = message;
    } else {
      box.style.display = 'none';
      box.textContent = '';
    }
  }

  function bindAddWalletMenu() {
    const btn = document.querySelector(selectors.addWalletBtn);
    const menu = document.querySelector(selectors.addWalletMenu);
    const overlay = document.querySelector(selectors.addWalletOverlay);
    const cancelBtn = document.querySelector(selectors.addWalletCancelBtn);
    const closeBtn = document.querySelector(selectors.addWalletCloseBtn);
    const createBtn = document.querySelector(selectors.addWalletCreateBtn);
    if (!btn || !menu || !overlay || !cancelBtn || !closeBtn || !createBtn) return;

    let open = false;
    let submitting = false;

    const close = () => {
      open = false;
      overlay.style.display = 'none';
      menu.style.display = 'none';
    };

    const openDialog = () => {
      open = true;
      overlay.style.display = 'flex';
      menu.style.display = 'grid';
      showAddWalletError('');
      const labelEl = document.querySelector(selectors.addWalletLabel);
      const networkEl = document.querySelector(selectors.addWalletNetwork);
      const addressEl = document.querySelector(selectors.addWalletAddress);
      if (labelEl) labelEl.value = '';
      if (networkEl) networkEl.value = 'BTC';
      if (addressEl) addressEl.value = '';
      if (addressEl) addressEl.focus();
      createBtn.disabled = false;
    };

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openDialog();
    });
    cancelBtn.addEventListener('click', close);
    closeBtn.addEventListener('click', close);
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close();
    });
    document.addEventListener('keydown', (e) => {
      if (open && e.key === 'Escape') {
        close();
      }
    });

    const submit = async () => {
      if (!open || submitting) return;
      showAddWalletError('');

      const labelEl = document.querySelector(selectors.addWalletLabel);
      const networkEl = document.querySelector(selectors.addWalletNetwork);
      const addressEl = document.querySelector(selectors.addWalletAddress);
      const label = (labelEl?.value || '').trim();
      const network = (networkEl?.value || '').trim();
      const address = (addressEl?.value || '').trim();

      if (addressEl) addressEl.classList.toggle('error', !address);
      if (!address) {
        showAddWalletError(t('wallet_enter_address'));
        if (addressEl) addressEl.focus();
        return;
      }

      submitting = true;
      createBtn.disabled = true;
      try {
        const res = await Api.call('/api/crypto/wallets', 'POST', {
          network,
          address,
          label: label || null
        }, true);
        if (!res.ok) {
          const message = res.data && typeof res.data === 'object'
            ? (res.data.message || '')
            : '';
          showAddWalletError(message || t('wallet_create_failed'));
          return;
        }
        close();
        await loadWallets();
      } finally {
        submitting = false;
        createBtn.disabled = false;
      }
    };

    createBtn.addEventListener('click', submit);
    menu.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        submit();
      }
    });
  }

  function populateAccountCurrencySelect(preferredCode) {
    const select = document.querySelector(selectors.addAccountCurrency);
    if (!select) return;
    const existing = preferredCode || select.value;
    select.innerHTML = '';
    fxCurrencies.forEach((item) => {
      const option = document.createElement('option');
      option.value = item.code;
      option.textContent = `${item.code} — ${item.name}`;
      select.appendChild(option);
    });
    const preferred = (existing || baseCurrency || fxFallbackBase || '').toUpperCase();
    if (preferred && fxCurrencies.some((item) => item.code === preferred)) {
      select.value = preferred;
    } else if (fxCurrencies.length) {
      select.value = fxCurrencies[0].code;
    }
  }

  function showBaseCurrencyError(message) {
    const box = document.querySelector(selectors.baseCurrencyError);
    if (!box) return;
    if (message) {
      box.style.display = 'block';
      box.textContent = message;
    } else {
      box.style.display = 'none';
      box.textContent = '';
    }
  }

  function populateBaseCurrencySelect(preferredCode) {
    const select = document.querySelector(selectors.baseCurrencySelect);
    if (!select) return;
    const existing = preferredCode || select.value;
    select.innerHTML = '';
    supportedCurrencies.forEach((item) => {
      const option = document.createElement('option');
      option.value = item.code;
      option.textContent = `${item.code} — ${item.name}`;
      select.appendChild(option);
    });
    const preferred = (existing || baseCurrency || fxFallbackBase || '').toUpperCase();
    if (preferred && supportedCurrencies.some((item) => item.code === preferred)) {
      select.value = preferred;
    } else if (supportedCurrencies.length) {
      select.value = supportedCurrencies[0].code;
    }
  }

  async function updateDemoAmounts() {
    const base = normalizeCurrency(baseCurrency) || 'USD';
    const entries = [
      { selector: selectors.incomeExpenseNet, amount: 1850, currency: 'USD' },
      { selector: selectors.paymentRentAmount, amount: -700, currency: 'USD' },
      { selector: selectors.paymentSpotifyAmount, amount: -4.99, currency: 'USD' },
      { selector: selectors.paymentMobileAmount, amount: -15, currency: 'USD' }
    ];

    const conversion = await buildConversionContext(base, entries.map((item) => item.currency));
    entries.forEach((item) => {
      const el = document.querySelector(item.selector);
      if (!el) return;
      const value = convertToBaseAmount(item.amount, item.currency, conversion);
      el.textContent = Number.isFinite(value) ? formatMoney(value, base) : formatMoney(item.amount, item.currency);
    });
  }

  function bindBaseCurrencyMenu() {
    const btn = document.querySelector(selectors.baseCurrencyBtn);
    const menu = document.querySelector(selectors.baseCurrencyMenu);
    const overlay = document.querySelector(selectors.baseCurrencyOverlay);
    const cancelBtn = document.querySelector(selectors.baseCurrencyCancelBtn);
    const closeBtn = document.querySelector(selectors.baseCurrencyCloseBtn);
    const saveBtn = document.querySelector(selectors.baseCurrencySaveBtn);
    if (!btn || !menu || !overlay || !cancelBtn || !closeBtn || !saveBtn) return;

    let open = false;
    let submitting = false;

    const close = () => {
      open = false;
      overlay.style.display = 'none';
      menu.style.display = 'none';
    };

    const openDialog = () => {
      open = true;
      overlay.style.display = 'flex';
      menu.style.display = 'grid';
      showBaseCurrencyError('');
      populateBaseCurrencySelect(baseCurrency);
      const select = document.querySelector(selectors.baseCurrencySelect);
      if (select) select.focus();
      saveBtn.disabled = false;
    };

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openDialog();
    });
    cancelBtn.addEventListener('click', close);
    closeBtn.addEventListener('click', close);
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close();
    });
    document.addEventListener('keydown', (e) => {
      if (open && e.key === 'Escape') close();
    });

    const submit = async () => {
      if (!open || submitting) return;
      showBaseCurrencyError('');

      const select = document.querySelector(selectors.baseCurrencySelect);
      const selected = normalizeCurrency(select?.value);
      if (select) select.classList.toggle('error', !selected);
      if (!selected) {
        showBaseCurrencyError(t('select_currency'));
        if (select) select.focus();
        return;
      }

      submitting = true;
      saveBtn.disabled = true;
      try {
        const res = await Api.call('/api/auth/me/base-currency', 'PATCH', { baseCurrency: selected }, true);
        if (!res.ok || !res.data || typeof res.data !== 'object') {
          const message = res.data && typeof res.data === 'object'
            ? (res.data.message || '')
            : '';
          showBaseCurrencyError(message || t('base_currency_update_failed'));
          return;
        }
        baseCurrency = res.data.baseCurrency || selected;
        fxBase = '';
        updateCurrencyLabels();
        close();
        await Promise.all([
          loadBalance(),
          loadFxRates(),
          loadCryptoRates(),
          loadWallets(),
          updateDemoAmounts()
        ]);
        renderLineChart(selectors.balanceChart, demoData.balance, baseCurrency);
        renderBarChart(
          selectors.expenseChart,
          demoData.expenses.map((item) => ({ ...item, label: t(item.labelKey) })),
          baseCurrency
        );
      } finally {
        submitting = false;
        saveBtn.disabled = false;
      }
    };

    saveBtn.addEventListener('click', submit);
    menu.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        submit();
      }
    });
  }

  function bindAddAccountMenu() {
    const btn = document.querySelector(selectors.addAccountBtn);
    const menu = document.querySelector(selectors.addAccountMenu);
    const overlay = document.querySelector(selectors.addAccountOverlay);
    const cancelBtn = document.querySelector(selectors.addAccountCancelBtn);
    const closeBtn = document.querySelector(selectors.addAccountCloseBtn);
    const createBtn = document.querySelector(selectors.addAccountCreateBtn);
    if (!btn || !menu || !overlay || !cancelBtn || !closeBtn || !createBtn) return;

    let open = false;
    let submitting = false;

    const close = () => {
      open = false;
      overlay.style.display = 'none';
      menu.style.display = 'none';
    };

    const openDialog = () => {
      open = true;
      overlay.style.display = 'flex';
      menu.style.display = 'grid';
      showAddAccountError('');
      populateAccountCurrencySelect();
      const nameEl = document.querySelector(selectors.addAccountName);
      const balanceEl = document.querySelector(selectors.addAccountBalance);
      if (nameEl) nameEl.value = '';
      if (balanceEl) balanceEl.value = '';
      if (nameEl) nameEl.focus();
      createBtn.disabled = false;
    };

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openDialog();
    });
    cancelBtn.addEventListener('click', close);
    closeBtn.addEventListener('click', close);
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close();
    });
    document.addEventListener('keydown', (e) => {
      if (open && e.key === 'Escape') {
        close();
      }
    });

    const parseOptionalNumber = (raw) => {
      const value = (raw || '').trim();
      if (!value) return null;
      const normalized = value.replace(',', '.');
      const num = Number(normalized);
      return Number.isFinite(num) ? num : NaN;
    };

    const submit = async () => {
      if (!open || submitting) return;
      showAddAccountError('');

      const nameEl = document.querySelector(selectors.addAccountName);
      const currencyEl = document.querySelector(selectors.addAccountCurrency);
      const balanceEl = document.querySelector(selectors.addAccountBalance);
      const name = (nameEl?.value || '').trim();
      const currency = (currencyEl?.value || '').trim();
      const initialBalance = parseOptionalNumber(balanceEl?.value || '');

      if (nameEl) nameEl.classList.toggle('error', !name);
      if (currencyEl) currencyEl.classList.toggle('error', !currency);
      if (balanceEl) balanceEl.classList.toggle('error', Number.isNaN(initialBalance));

      if (!name) {
        showAddAccountError(t('enter_account_name'));
        if (nameEl) nameEl.focus();
        return;
      }
      if (!currency) {
        showAddAccountError(t('select_currency'));
        if (currencyEl) currencyEl.focus();
        return;
      }
      if (Number.isNaN(initialBalance)) {
        showAddAccountError(t('invalid_balance'));
        if (balanceEl) balanceEl.focus();
        return;
      }

      submitting = true;
      createBtn.disabled = true;
      try {
        const res = await Api.call('/api/accounts', 'POST', {
          name,
          currency,
          initialBalance
        }, true);
        if (!res.ok) {
          const message = res.data && typeof res.data === 'object'
            ? (res.data.message || '')
            : '';
          showAddAccountError(message || t('create_account_failed'));
          return;
        }
        close();
        await loadBalance();
      } finally {
        submitting = false;
        createBtn.disabled = false;
      }
    };

    createBtn.addEventListener('click', submit);
    menu.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        submit();
      }
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
    bindBaseCurrencyMenu();
    await loadBalance();
    await loadFxCurrencies();
    bindFxControls();
    await loadFxRates();
    await loadCryptoRates();
    await loadWallets();
    await updateDemoAmounts();
    renderLineChart(selectors.balanceChart, demoData.balance, baseCurrency);
    renderBarChart(
      selectors.expenseChart,
      demoData.expenses.map((item) => ({ ...item, label: t(item.labelKey) })),
      baseCurrency
    );
    bindAddAccountMenu();
    bindAddWalletMenu();
    if (root) root.style.visibility = 'visible';
  });
})();
