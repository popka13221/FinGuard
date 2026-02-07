(() => {
  const selectors = {
    root: 'body.dashboard',
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
    cryptoDataSource: '#cryptoDataSource',
    cryptoBase: '#cryptoBase',
    marketsDataSource: '#marketsDataSource',
    totalBalance: '#totalBalance',
    creditValue: '#creditValue',
    totalsByCurrency: '#totalsByCurrency',
    accountsList: '#accountsList',
    accountsFeedback: '#accountsFeedback',
    transactionsList: '#transactionsList',
    transactionsFeedback: '#transactionsFeedback',
    txPeriodButtons: '.tx-period-btn',
    addTransactionBtn: '#btn-add-transaction',
    balanceError: '#balanceError',
    fxStatus: '#fxStatus',
    fxDataSource: '#fxDataSource',
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
    addTransactionOverlay: '#add-transaction-overlay',
    addTransactionMenu: '#add-transaction-menu',
    addTransactionAccount: '#newTxAccount',
    addTransactionType: '#newTxType',
    addTransactionCategory: '#newTxCategory',
    addTransactionAmount: '#newTxAmount',
    addTransactionDate: '#newTxDate',
    addTransactionDescription: '#newTxDescription',
    addTransactionCancelBtn: '#btn-add-transaction-cancel',
    addTransactionCreateBtn: '#btn-add-transaction-create',
    addTransactionCloseBtn: '#btn-add-transaction-close',
    addTransactionError: '#addTransactionError',
    walletsList: '#walletsList',
    walletsFeedback: '#walletsFeedback',
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
    incomeExpenseDetails: '#incomeExpenseDetails',
    paymentRentAmount: '#paymentRentAmount',
    paymentSpotifyAmount: '#paymentSpotifyAmount',
    paymentMobileAmount: '#paymentMobileAmount',
    walletAnalysisPanel: '#walletAnalysisPanel',
    walletAnalysisBanner: '#walletAnalysisBanner',
    analysisBannerTitle: '#analysisBannerTitle',
    analysisBannerSubtitle: '#analysisBannerSubtitle',
    analysisDataSource: '#analysisDataSource',
    analysisUpdatedAt: '#analysisUpdatedAt',
    analysisProgressBar: '#analysisProgressBar',
    analysisProgressFill: '#analysisProgressFill',
    analysisStage: '#analysisStage',
    analysisProgressText: '#analysisProgressText',
    analysisEmptyCta: '#analysisEmptyCta',
    analysisCardPortfolio: '#analysisCardPortfolio',
    analysisCardGrowth: '#analysisCardGrowth',
    analysisCardOutflow: '#analysisCardOutflow',
    analysisCardRecurring: '#analysisCardRecurring',
    analysisPortfolioSource: '#analysisPortfolioSource',
    analysisGrowthSource: '#analysisGrowthSource',
    analysisOutflowSource: '#analysisOutflowSource',
    analysisRecurringSource: '#analysisRecurringSource',
    analysisPortfolioValue: '#analysisPortfolioValue',
    analysisGrowthValue: '#analysisGrowthValue',
    analysisOutflowValue: '#analysisOutflowValue',
    analysisRecurringValue: '#analysisRecurringValue',
    analysisPortfolioMeta: '#analysisPortfolioMeta',
    analysisGrowthMeta: '#analysisGrowthMeta',
    analysisOutflowMeta: '#analysisOutflowMeta',
    analysisRecurringMeta: '#analysisRecurringMeta'
  };

  const LANG_STORAGE_KEY = 'finguard:lang';
  const TX_PERIOD_STORAGE_KEY = 'finguard:txPeriodDays';
  const I18N = {
    ru: {
      dashboard_page_title: 'FinGuard | Дашборд',
      dashboard_title: 'Личный кабинет',
      logout: 'Выйти',
      base_currency_button: 'Валюта: {value}',
      balance: 'Баланс',
      credit: 'Кредит',
      credit_placeholder: 'Кредит: —',
      income_expense_month: 'Доход / Расход (мес.)',
      income_expense_details: 'Доход: — · Расход: —',
      income_label: 'Доход',
      expense_label: 'Расход',
      your_cards: 'Ваши карты',
      add: 'Добавить',
      loading_balance: 'Загружаем баланс…',
      upcoming_payments: 'Ближайшие платежи',
      payment_rent: 'Аренда',
      payment_streaming: 'Стриминг',
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
      markets_badge: 'Markets',
      crypto_badge: 'Crypto',
      fx_badge: 'FX',
      fx_radar_title: 'FX Radar',
      markets_title: 'Курсы и рынки',
      markets_subtitle: 'Сводка по крипте и валютам',
      coin_rates: 'Курсы монет',
      loading: 'Загрузка…',
      updating: 'Обновляем…',
      demo_data: 'Демо-данные',
      no_data: 'Нет данных',
      updated: 'Обновлено',
      updated_at: 'Обновлено {value}',
      source_pending: 'Ожидаем данные',
      source_live: 'Live',
      source_demo: 'Demo',
      source_synthetic: 'Synthetic',
      source_hybrid: 'Live + Synthetic',
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
      empty_accounts_hint: 'Добавьте первый счёт, чтобы видеть баланс и аналитику.',
      cta_add_account: 'Добавить счёт',
      account: 'Счет',
      account_remove: 'Удалить',
      account_delete_confirm: 'Удалить счет “{name}”?',
      account_delete_failed: 'Не удалось удалить счет.',
      recent_transactions_title: 'Последние транзакции',
      transactions_empty: 'Транзакций пока нет.',
      empty_transactions_hint: 'Добавьте транзакцию, чтобы заполнить ленту операций.',
      transactions_load_failed: 'Не удалось загрузить транзакции.',
      transactions_load_failed_short: 'Не удалось загрузить транзакции',
      add_transaction_menu_aria: 'Добавить транзакцию',
      add_transaction_title: 'Добавить транзакцию',
      add_transaction_subtitle: 'Доход или расход — баланс пересчитается автоматически.',
      transaction_account_label: 'Счёт',
      transaction_account_aria: 'Счёт',
      transaction_type_label: 'Тип',
      transaction_type_aria: 'Тип',
      transaction_type_expense: 'Расход',
      transaction_type_income: 'Доход',
      transaction_category_label: 'Категория',
      transaction_category_aria: 'Категория',
      transaction_amount_label: 'Сумма',
      transaction_amount_placeholder: '0.00',
      transaction_date_label: 'Дата',
      transaction_date_aria: 'Дата и время',
      transaction_description_label: 'Комментарий',
      transaction_description_placeholder: 'Например: Кофе',
      transaction_no_accounts: 'Сначала создайте счёт.',
      transaction_no_categories: 'Нет подходящих категорий.',
      transaction_select_account: 'Выберите счёт.',
      transaction_select_category: 'Выберите категорию.',
      transaction_enter_amount: 'Введите сумму.',
      transaction_invalid_amount: 'Некорректная сумма.',
      transaction_create_failed: 'Не удалось добавить транзакцию.',
      archived: 'Архив',
      balance_load_failed: 'Не удалось загрузить баланс.',
      balance_load_failed_short: 'Не удалось загрузить баланс',
      fx_no_data: 'Нет данных по валютам.',
      fx_no_currencies: 'Нет доступных валют.',
      wallets_title: 'Крипто-кошельки',
      wallets_empty: 'Кошельки не добавлены.',
      empty_wallets_hint: 'Добавьте watch-only кошелёк, чтобы видеть оценку крипто-портфеля.',
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
      wallet_delete_confirm: 'Удалить кошелёк “{name}”?',
      wallet_delete_failed: 'Не удалось удалить кошелёк.',
      wallet_enter_address: 'Введите адрес.',
      wallet_invalid_address: 'Некорректный адрес.',
      wallet_create_failed: 'Не удалось добавить кошелёк.',
      base_currency_menu_aria: 'Изменить базовую валюту',
      base_currency_title: 'Базовая валюта',
      base_currency_subtitle: 'Все суммы будут отображаться в выбранной валюте.',
      base_currency_update_failed: 'Не удалось обновить базовую валюту.',
      base_currency_conversion_failed: 'Не удалось конвертировать суммы в базовую валюту.',
      cta_add_wallet: 'Добавить кошелёк',
      cta_add_transaction: 'Добавить транзакцию',
      cta_retry: 'Повторить',
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
      expense_other: 'Прочее',
      analysis_banner_badge: 'Instant Value',
      analysis_banner_title_idle: 'Подключите кошелёк и получите анализ',
      analysis_banner_subtitle_idle: 'После добавления кошелька метрики и инсайты появятся автоматически.',
      analysis_banner_title_running: 'Анализируем кошелёк {name}',
      analysis_banner_subtitle_running: 'Собираем транзакции, строим динамику и recurring-паттерны.',
      analysis_banner_title_partial: 'Первые инсайты уже готовы',
      analysis_banner_subtitle_partial: 'Вы видите промежуточные метрики, полный отчёт скоро появится.',
      analysis_banner_title_done: 'Анализ готов',
      analysis_banner_subtitle_done: 'Все ключевые метрики обновлены.',
      analysis_banner_title_failed: 'Анализ временно недоступен',
      analysis_banner_subtitle_failed: 'Попробуем обновить анализ автоматически.',
      analysis_stage_fetch_tx: 'Сканируем транзакции',
      analysis_stage_enrich_tx: 'Классифицируем операции',
      analysis_stage_build_snapshots: 'Строим динамику портфеля',
      analysis_stage_detect_recurring: 'Ищем recurring-платежи',
      analysis_stage_build_insights: 'Формируем инсайты',
      analysis_stage_done: 'Готово',
      analysis_status_queued: 'В очереди',
      analysis_status_running: 'В процессе',
      analysis_status_partial: 'Частично готово',
      analysis_status_done: 'Готово',
      analysis_status_failed: 'Ошибка',
      analysis_card_portfolio: 'Portfolio value',
      analysis_card_growth: '7d growth',
      analysis_card_outflow: 'Top outflow',
      analysis_card_recurring: 'Recurring spend',
      analysis_card_waiting: 'Ожидаем метрики…',
      analysis_card_updated_live: 'Live: обновлено {value}',
      analysis_card_estimated: 'Оценка на основе истории',
      analysis_no_wallet_stage: 'Добавьте кошелёк для старта',
      analysis_polling_error: 'Не удалось обновить статус анализа',
      analysis_top_outflow_label: 'Крупнейший отток: {name}',
      analysis_top_outflow_estimated: 'Оценка оттока пока нет новых операций',
      analysis_recurring_live: 'Recurring по последним операциям',
      analysis_recurring_estimated: 'Синтетическая оценка до завершения анализа'
    },
    en: {
      dashboard_page_title: 'FinGuard | Dashboard',
      dashboard_title: 'Dashboard',
      logout: 'Log out',
      base_currency_button: 'Currency: {value}',
      balance: 'Balance',
      credit: 'Credit',
      credit_placeholder: 'Credit: —',
      income_expense_month: 'Income / Expense (mo.)',
      income_expense_details: 'Income: — · Expense: —',
      income_label: 'Income',
      expense_label: 'Expense',
      your_cards: 'Your cards',
      add: 'Add',
      loading_balance: 'Loading balance…',
      upcoming_payments: 'Upcoming payments',
      payment_rent: 'Rent',
      payment_streaming: 'Streaming',
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
      markets_badge: 'Markets',
      crypto_badge: 'Crypto',
      fx_badge: 'FX',
      fx_radar_title: 'FX Radar',
      markets_title: 'Rates & markets',
      markets_subtitle: 'Crypto & FX summary',
      coin_rates: 'Coin rates',
      loading: 'Loading…',
      updating: 'Updating…',
      demo_data: 'Demo data',
      no_data: 'No data',
      updated: 'Updated',
      updated_at: 'Updated {value}',
      source_pending: 'Waiting for data',
      source_live: 'Live',
      source_demo: 'Demo',
      source_synthetic: 'Synthetic',
      source_hybrid: 'Live + Synthetic',
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
      empty_accounts_hint: 'Add your first account to unlock balances and analytics.',
      cta_add_account: 'Add account',
      account: 'Account',
      account_remove: 'Delete',
      account_delete_confirm: 'Delete account “{name}”?',
      account_delete_failed: 'Failed to delete account.',
      recent_transactions_title: 'Recent transactions',
      transactions_empty: 'No transactions yet.',
      empty_transactions_hint: 'Add a transaction to populate your activity feed.',
      transactions_load_failed: 'Failed to load transactions.',
      transactions_load_failed_short: 'Failed to load transactions',
      add_transaction_menu_aria: 'Add transaction',
      add_transaction_title: 'Add transaction',
      add_transaction_subtitle: 'Income or expense — balance is recalculated automatically.',
      transaction_account_label: 'Account',
      transaction_account_aria: 'Account',
      transaction_type_label: 'Type',
      transaction_type_aria: 'Type',
      transaction_type_expense: 'Expense',
      transaction_type_income: 'Income',
      transaction_category_label: 'Category',
      transaction_category_aria: 'Category',
      transaction_amount_label: 'Amount',
      transaction_amount_placeholder: '0.00',
      transaction_date_label: 'Date',
      transaction_date_aria: 'Date and time',
      transaction_description_label: 'Description',
      transaction_description_placeholder: 'e.g. Coffee',
      transaction_no_accounts: 'Create an account first.',
      transaction_no_categories: 'No matching categories.',
      transaction_select_account: 'Select an account.',
      transaction_select_category: 'Select a category.',
      transaction_enter_amount: 'Enter amount.',
      transaction_invalid_amount: 'Invalid amount.',
      transaction_create_failed: 'Failed to add transaction.',
      archived: 'Archived',
      balance_load_failed: 'Failed to load balance.',
      balance_load_failed_short: 'Failed to load balance',
      fx_no_data: 'No FX data.',
      fx_no_currencies: 'No currencies available.',
      wallets_title: 'Crypto wallets',
      wallets_empty: 'No wallets added yet.',
      empty_wallets_hint: 'Add a watch-only wallet to estimate your crypto portfolio value.',
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
      wallet_delete_confirm: 'Delete wallet “{name}”?',
      wallet_delete_failed: 'Failed to delete wallet.',
      wallet_enter_address: 'Enter an address.',
      wallet_invalid_address: 'Invalid address.',
      wallet_create_failed: 'Failed to add wallet.',
      base_currency_menu_aria: 'Change base currency',
      base_currency_title: 'Base currency',
      base_currency_subtitle: 'All amounts are shown in the selected currency.',
      base_currency_update_failed: 'Failed to update base currency.',
      base_currency_conversion_failed: 'Failed to convert amounts to base currency.',
      cta_add_wallet: 'Add wallet',
      cta_add_transaction: 'Add transaction',
      cta_retry: 'Retry',
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
      expense_other: 'Other',
      analysis_banner_badge: 'Instant Value',
      analysis_banner_title_idle: 'Connect a wallet to unlock analysis',
      analysis_banner_subtitle_idle: 'After wallet connect, metrics and insights appear automatically.',
      analysis_banner_title_running: 'Analyzing wallet {name}',
      analysis_banner_subtitle_running: 'Fetching transactions, building trends, and detecting recurring spend.',
      analysis_banner_title_partial: 'First insights are ready',
      analysis_banner_subtitle_partial: 'You are seeing partial metrics while full analysis completes.',
      analysis_banner_title_done: 'Analysis ready',
      analysis_banner_subtitle_done: 'All key metrics are updated.',
      analysis_banner_title_failed: 'Analysis temporarily unavailable',
      analysis_banner_subtitle_failed: 'We will retry analysis automatically.',
      analysis_stage_fetch_tx: 'Fetching transactions',
      analysis_stage_enrich_tx: 'Enriching activity',
      analysis_stage_build_snapshots: 'Building portfolio snapshots',
      analysis_stage_detect_recurring: 'Detecting recurring spend',
      analysis_stage_build_insights: 'Building insights',
      analysis_stage_done: 'Done',
      analysis_status_queued: 'Queued',
      analysis_status_running: 'Running',
      analysis_status_partial: 'Partially ready',
      analysis_status_done: 'Done',
      analysis_status_failed: 'Failed',
      analysis_card_portfolio: 'Portfolio value',
      analysis_card_growth: '7d growth',
      analysis_card_outflow: 'Top outflow',
      analysis_card_recurring: 'Recurring spend',
      analysis_card_waiting: 'Waiting for metrics…',
      analysis_card_updated_live: 'Live: updated {value}',
      analysis_card_estimated: 'Estimated from current activity',
      analysis_no_wallet_stage: 'Add a wallet to start analysis',
      analysis_polling_error: 'Failed to refresh analysis status',
      analysis_top_outflow_label: 'Top outflow: {name}',
      analysis_top_outflow_estimated: 'Estimated outflow while activity is building',
      analysis_recurring_live: 'Recurring from recent activity',
      analysis_recurring_estimated: 'Synthetic estimate until analysis is complete'
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

  function loadTxPeriodDays() {
    try {
      const stored = localStorage.getItem(TX_PERIOD_STORAGE_KEY);
      if (stored === '7' || stored === '30') return Number(stored);
    } catch (_) {
      // ignore
    }
    return 30;
  }

  let currentLang = loadLang();
  let txPeriodDays = loadTxPeriodDays();

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
    setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
    setDataSourceBadge(selectors.fxDataSource, dataSourceState.fx);
    syncMarketDataSource();
    refreshAnalysisPanel();
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
  let lastAccountsTotalInBase = NaN;
  let cryptoWalletTotalInBase = NaN;
  let lastBalanceSnapshot = null;
  let lastBalanceConversion = null;
  const txListLimit = 20;
  let txCategories = [];
  let txCategoriesById = new Map();
  const DATA_SOURCE = {
    pending: 'pending',
    live: 'live',
    demo: 'demo',
    synthetic: 'synthetic',
    hybrid: 'hybrid'
  };
  const dataSourceState = {
    crypto: DATA_SOURCE.pending,
    fx: DATA_SOURCE.pending
  };
  const reducedMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
  const analysisPollIntervalMs = 2000;
  const analysisState = {
    activeWalletId: null,
    activeWalletName: '',
    status: null,
    pollTimer: 0,
    inFlight: false,
    summaryTotal: NaN,
    summaryBase: 'USD',
    pollError: false
  };
  let recentTransactionsCache = [];

  function q(selector) {
    return document.querySelector(selector);
  }

  function setUiState(target, state) {
    const el = typeof target === 'string' ? q(target) : target;
    if (!el) return;
    el.dataset.uiState = state;
  }

  function setPanelFeedback(selector, message, isError) {
    const el = q(selector);
    if (!el) return;
    if (!message) {
      el.classList.remove('show', 'amount-negative');
      el.textContent = '';
      return;
    }
    el.classList.add('show');
    el.classList.toggle('amount-negative', Boolean(isError));
    el.textContent = message;
  }

  function renderSkeletonList(count) {
    return `<div class="skeleton-list" aria-hidden="true">${Array.from({ length: Math.max(count, 1) }, () => '<div class="skeleton-item"></div>').join('')}</div>`;
  }

  function renderEmptyState(message, actionLabel, action) {
    const safeMsg = escapeHtml(message || '');
    const safeAction = escapeHtml(action || '');
    if (!actionLabel || !action) {
      return `<div class="empty-block muted">${safeMsg}</div>`;
    }
    return `
      <div class="empty-block">
        <div class="muted">${safeMsg}</div>
        <button type="button" class="ghost inline-cta" data-action="${safeAction}">${escapeHtml(actionLabel)}</button>
      </div>
    `;
  }

  function renderErrorState(message, actionLabel, action) {
    const safeMsg = escapeHtml(message || '');
    const safeAction = escapeHtml(action || '');
    return `
      <div class="error-block">
        <div class="amount-negative">${safeMsg}</div>
        ${actionLabel && action ? `<button type="button" class="ghost inline-cta" data-action="${safeAction}">${escapeHtml(actionLabel)}</button>` : ''}
      </div>
    `;
  }

  function pulseElement(selector) {
    const el = typeof selector === 'string' ? q(selector) : selector;
    if (!el || reducedMotionQuery.matches) return;
    el.classList.remove('motion-pulse');
    void el.offsetWidth;
    el.classList.add('motion-pulse');
  }

  function sourceLabel(source) {
    if (source === DATA_SOURCE.live) return t('source_live');
    if (source === DATA_SOURCE.demo) return t('source_demo');
    if (source === DATA_SOURCE.synthetic) return t('source_synthetic');
    if (source === DATA_SOURCE.hybrid) return t('source_hybrid');
    return t('source_pending');
  }

  function setDataSourceBadge(selector, source) {
    const el = q(selector);
    if (!el) return;
    const normalized = Object.values(DATA_SOURCE).includes(source) ? source : DATA_SOURCE.pending;
    el.dataset.dataSource = normalized;
    el.textContent = sourceLabel(normalized);
  }

  function syncMarketDataSource() {
    const crypto = dataSourceState.crypto;
    const fx = dataSourceState.fx;
    let combined = DATA_SOURCE.pending;
    if (crypto === DATA_SOURCE.demo && fx === DATA_SOURCE.demo) {
      combined = DATA_SOURCE.demo;
    } else if (crypto === DATA_SOURCE.live && fx === DATA_SOURCE.hybrid) {
      combined = DATA_SOURCE.hybrid;
    } else if (crypto === DATA_SOURCE.live && fx === DATA_SOURCE.live) {
      combined = DATA_SOURCE.live;
    } else if (crypto === DATA_SOURCE.live || fx === DATA_SOURCE.live || fx === DATA_SOURCE.hybrid) {
      combined = DATA_SOURCE.hybrid;
    } else if (crypto === DATA_SOURCE.synthetic || fx === DATA_SOURCE.synthetic) {
      combined = DATA_SOURCE.synthetic;
    } else if (crypto === DATA_SOURCE.demo || fx === DATA_SOURCE.demo) {
      combined = DATA_SOURCE.demo;
    }
    setDataSourceBadge(selectors.marketsDataSource, combined);
  }

  function getFocusableElements(container) {
    if (!container) return [];
    return Array.from(container.querySelectorAll(
      'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
    )).filter((el) => !el.disabled && !el.hidden && el.offsetParent !== null);
  }

  function trapFocusKeydown(event, container) {
    if (event.key !== 'Tab') return;
    const focusable = getFocusableElements(container);
    if (!focusable.length) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }

  function openOverlay(overlay, menu, opener) {
    if (!overlay || !menu) return;
    overlay.hidden = false;
    overlay.classList.add('is-open');
    overlay.dataset.uiState = 'open';
    menu.setAttribute('aria-hidden', 'false');
    menu.dataset.opener = opener && opener.id ? opener.id : '';
    const focusable = getFocusableElements(menu);
    const target = focusable[0] || menu;
    if (target) target.focus();
  }

  function closeOverlay(overlay, menu, opener) {
    if (!overlay || !menu) return;
    overlay.classList.remove('is-open');
    overlay.hidden = true;
    overlay.dataset.uiState = 'closed';
    menu.setAttribute('aria-hidden', 'true');
    if (opener && typeof opener.focus === 'function') {
      opener.focus();
      return;
    }
    const openerId = menu.dataset.opener || '';
    const openerEl = openerId ? document.getElementById(openerId) : null;
    if (openerEl) openerEl.focus();
  }

  function bindActionCtas() {
    document.addEventListener('click', (event) => {
      const target = event.target instanceof HTMLElement ? event.target.closest('[data-action]') : null;
      if (!(target instanceof HTMLElement)) return;
      const action = target.dataset.action;
      if (!action) return;
      if (action === 'open-add-account') {
        q(selectors.addAccountBtn)?.click();
      } else if (action === 'open-add-wallet') {
        q(selectors.addWalletBtn)?.click();
      } else if (action === 'open-add-transaction') {
        q(selectors.addTransactionBtn)?.click();
      } else if (action === 'retry-balance') {
        loadBalance();
      } else if (action === 'retry-wallets') {
        loadWallets();
      } else if (action === 'retry-transactions') {
        loadRecentTransactions();
      } else if (action === 'retry-fx') {
        loadFxRates();
      }
    });
  }

  function initMotionController() {
    const root = q(selectors.root);
    if (!root) return;
    const cards = Array.from(document.querySelectorAll('.dashboard .card'));
    let rafId = 0;
    let targetX = 50;
    let targetY = 24;
    let currentX = 50;
    let currentY = 24;

    const applyMotionPreference = () => {
      const reduced = reducedMotionQuery.matches;
      root.dataset.motionLevel = reduced ? 'reduced' : 'high';
      if (reduced) {
        root.style.setProperty('--mouse-x', '50%');
        root.style.setProperty('--mouse-y', '20%');
        cards.forEach((card) => {
          card.classList.remove('is-hovered');
          card.style.removeProperty('--pointer-x');
          card.style.removeProperty('--pointer-y');
        });
      }
    };
    applyMotionPreference();

    if (typeof reducedMotionQuery.addEventListener === 'function') {
      reducedMotionQuery.addEventListener('change', applyMotionPreference);
    } else if (typeof reducedMotionQuery.addListener === 'function') {
      reducedMotionQuery.addListener(applyMotionPreference);
    }

    cards.forEach((card) => {
      card.addEventListener('pointerenter', () => {
        if (root.dataset.motionLevel === 'reduced') return;
        card.classList.add('is-hovered');
      });

      card.addEventListener('pointermove', (event) => {
        if (root.dataset.motionLevel === 'reduced') return;
        const rect = card.getBoundingClientRect();
        if (!rect.width || !rect.height) return;
        const x = ((event.clientX - rect.left) / rect.width) * 100;
        const y = ((event.clientY - rect.top) / rect.height) * 100;
        card.style.setProperty('--pointer-x', `${Math.max(0, Math.min(100, x)).toFixed(1)}%`);
        card.style.setProperty('--pointer-y', `${Math.max(0, Math.min(100, y)).toFixed(1)}%`);
      }, { passive: true });

      card.addEventListener('pointerleave', () => {
        card.classList.remove('is-hovered');
        card.style.removeProperty('--pointer-x');
        card.style.removeProperty('--pointer-y');
      });
    });

    const animateAtmosphere = () => {
      rafId = 0;
      if (root.dataset.motionLevel === 'reduced' || document.hidden) return;
      currentX += (targetX - currentX) * 0.045;
      currentY += (targetY - currentY) * 0.045;
      root.style.setProperty('--mouse-x', `${currentX.toFixed(2)}%`);
      root.style.setProperty('--mouse-y', `${currentY.toFixed(2)}%`);
      if (Math.abs(targetX - currentX) > 0.08 || Math.abs(targetY - currentY) > 0.08) {
        rafId = requestAnimationFrame(animateAtmosphere);
      }
    };

    document.addEventListener('pointermove', (event) => {
      if (root.dataset.motionLevel === 'reduced') return;
      const width = Math.max(window.innerWidth, 1);
      const height = Math.max(window.innerHeight, 1);
      targetX = 30 + (event.clientX / width) * 40;
      targetY = 18 + (event.clientY / height) * 36;
      if (!rafId && !document.hidden) {
        rafId = requestAnimationFrame(animateAtmosphere);
      }
    }, { passive: true });

    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        root.classList.add('motion-paused');
        if (rafId) {
          cancelAnimationFrame(rafId);
          rafId = 0;
        }
      } else {
        root.classList.remove('motion-paused');
        if (root.dataset.motionLevel !== 'reduced' && !rafId) {
          rafId = requestAnimationFrame(animateAtmosphere);
        }
      }
    });

    if (root.dataset.motionLevel !== 'reduced') {
      rafId = requestAnimationFrame(animateAtmosphere);
    }
  }

  function initScrollReveal() {
    const targets = Array.from(document.querySelectorAll('.dashboard [data-motion="reveal"]'));
    if (!targets.length) return;

    if (reducedMotionQuery.matches || typeof window.IntersectionObserver !== 'function') {
      targets.forEach((el) => el.classList.add('in-view'));
      return;
    }

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (!entry.isIntersecting) return;
        entry.target.classList.add('in-view');
        observer.unobserve(entry.target);
      });
    }, {
      root: null,
      threshold: 0.12,
      rootMargin: '0px 0px -10% 0px'
    });

    targets.forEach((el, index) => {
      el.style.setProperty('--reveal-delay', `${Math.min(index * 55, 240)}ms`);
      observer.observe(el);
    });
  }

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
      stopAnalysisPolling();
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

  function analysisStageLabel(stage) {
    const normalized = String(stage || '').toUpperCase();
    if (normalized === 'ENRICH_TX') return t('analysis_stage_enrich_tx');
    if (normalized === 'BUILD_SNAPSHOTS') return t('analysis_stage_build_snapshots');
    if (normalized === 'DETECT_RECURRING') return t('analysis_stage_detect_recurring');
    if (normalized === 'BUILD_INSIGHTS') return t('analysis_stage_build_insights');
    if (normalized === 'DONE') return t('analysis_stage_done');
    return t('analysis_stage_fetch_tx');
  }

  function analysisStatusLabel(status) {
    const normalized = String(status || '').toUpperCase();
    if (normalized === 'RUNNING') return t('analysis_status_running');
    if (normalized === 'PARTIAL') return t('analysis_status_partial');
    if (normalized === 'DONE') return t('analysis_status_done');
    if (normalized === 'FAILED') return t('analysis_status_failed');
    return t('analysis_status_queued');
  }

  function isTerminalAnalysisStatus(status) {
    const normalized = String(status || '').toUpperCase();
    return normalized === 'DONE' || normalized === 'FAILED';
  }

  function normalizeAnalysisStatus(payload) {
    const status = String(payload && payload.status ? payload.status : 'QUEUED').toUpperCase();
    const progressRaw = toNumber(payload && payload.progressPct);
    return {
      status,
      progressPct: Number.isFinite(progressRaw) ? Math.max(0, Math.min(100, Math.round(progressRaw))) : 0,
      stage: String(payload && payload.stage ? payload.stage : 'FETCH_TX').toUpperCase(),
      startedAt: payload && payload.startedAt ? String(payload.startedAt) : '',
      updatedAt: payload && payload.updatedAt ? String(payload.updatedAt) : '',
      finishedAt: payload && payload.finishedAt ? String(payload.finishedAt) : '',
      partialReady: Boolean(payload && payload.partialReady)
    };
  }

  function pickLatestWallet(wallets) {
    if (!Array.isArray(wallets) || wallets.length === 0) return null;
    return wallets.reduce((best, candidate) => {
      if (!candidate) return best;
      if (!best) return candidate;
      const bestId = Number(best.id);
      const nextId = Number(candidate.id);
      if (Number.isFinite(nextId) && Number.isFinite(bestId)) {
        return nextId > bestId ? candidate : best;
      }
      return best;
    }, null);
  }

  function stopAnalysisPolling() {
    if (analysisState.pollTimer) {
      clearTimeout(analysisState.pollTimer);
      analysisState.pollTimer = 0;
    }
    analysisState.inFlight = false;
  }

  function scheduleAnalysisPolling(delayMs) {
    if (!analysisState.activeWalletId || isTerminalAnalysisStatus(analysisState.status && analysisState.status.status)) {
      return;
    }
    if (analysisState.pollTimer) {
      clearTimeout(analysisState.pollTimer);
      analysisState.pollTimer = 0;
    }
    const waitMs = Math.max(0, Number(delayMs) || analysisPollIntervalMs);
    analysisState.pollTimer = window.setTimeout(() => {
      analysisState.pollTimer = 0;
      pollWalletAnalysisStatus();
    }, waitMs);
  }

  async function pollWalletAnalysisStatus() {
    if (!analysisState.activeWalletId || analysisState.inFlight) {
      return;
    }
    analysisState.inFlight = true;
    const walletIdAtRequest = analysisState.activeWalletId;
    try {
      const res = await Api.call(`/api/crypto/wallets/${encodeURIComponent(walletIdAtRequest)}/analysis/status`, 'GET', null, true);
      if (walletIdAtRequest !== analysisState.activeWalletId) {
        return;
      }
      if (!res.ok || !res.data || typeof res.data !== 'object') {
        analysisState.pollError = true;
        refreshAnalysisPanel();
        scheduleAnalysisPolling(analysisPollIntervalMs * 2);
        return;
      }
      analysisState.pollError = false;
      analysisState.status = normalizeAnalysisStatus(res.data);
      refreshAnalysisPanel();
      if (!isTerminalAnalysisStatus(analysisState.status.status)) {
        scheduleAnalysisPolling(analysisPollIntervalMs);
      }
    } catch (_) {
      if (walletIdAtRequest === analysisState.activeWalletId) {
        analysisState.pollError = true;
        refreshAnalysisPanel();
        scheduleAnalysisPolling(analysisPollIntervalMs * 2);
      }
    } finally {
      analysisState.inFlight = false;
    }
  }

  function animateMetricValue(target, nextValue, formatter) {
    const el = typeof target === 'string' ? q(target) : target;
    if (!el || !Number.isFinite(nextValue)) {
      if (el) {
        el.textContent = '—';
        delete el.dataset.numericValue;
      }
      return;
    }
    const formatted = typeof formatter === 'function' ? formatter(nextValue) : String(nextValue);
    if (reducedMotionQuery.matches) {
      el.textContent = formatted;
      el.dataset.numericValue = String(nextValue);
      return;
    }
    const prev = toNumber(el.dataset.numericValue);
    if (!Number.isFinite(prev)) {
      el.textContent = formatted;
      el.dataset.numericValue = String(nextValue);
      return;
    }
    const start = performance.now();
    const duration = 460;
    const delta = nextValue - prev;
    const tick = (now) => {
      const progress = Math.min(1, (now - start) / duration);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = prev + (delta * eased);
      el.textContent = typeof formatter === 'function' ? formatter(value) : String(value);
      if (progress < 1) {
        requestAnimationFrame(tick);
        return;
      }
      el.textContent = formatted;
      el.dataset.numericValue = String(nextValue);
    };
    requestAnimationFrame(tick);
  }

  function setAnalysisCardState(selector, state) {
    const el = q(selector);
    if (!el) return;
    setUiState(el, state);
  }

  function setAnalysisValueState(selector, value) {
    const el = q(selector);
    if (!el) return;
    el.classList.remove('is-positive', 'is-negative');
    if (Number.isFinite(value)) {
      if (value > 0) el.classList.add('is-positive');
      if (value < 0) el.classList.add('is-negative');
    }
  }

  function formatSignedPct(value) {
    if (!Number.isFinite(value)) return '—';
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  }

  function firstNonEmpty(parts) {
    for (const part of parts) {
      const value = String(part || '').trim();
      if (value) return value;
    }
    return '';
  }

  function setText(selector, value) {
    const el = q(selector);
    if (!el) return;
    el.textContent = value;
  }

  function buildAnalysisInsightsModel() {
    const portfolio = Number.isFinite(analysisState.summaryTotal) ? Math.max(0, analysisState.summaryTotal) : NaN;
    const base = normalizeCurrency(analysisState.summaryBase || baseCurrency) || 'USD';
    const walletSeed = hashString(`${analysisState.activeWalletId || 0}:${Math.round((Number.isFinite(portfolio) ? portfolio : 0) * 100)}`);
    const random = seededRandom(walletSeed || 1);
    const ready = Boolean(analysisState.status && (analysisState.status.partialReady || analysisState.status.status === 'DONE' || analysisState.status.status === 'PARTIAL'));

    const growthBase = (random() * 9.2) - 1.8;
    const growth = Number.isFinite(portfolio) ? growthBase + (ready ? 0.6 : -0.25) : NaN;

    const expenses = recentTransactionsCache
      .map((tx) => {
        const type = String(tx && tx.type ? tx.type : '').toUpperCase();
        const amount = toNumber(tx && tx.amount);
        if (type !== 'EXPENSE' || !Number.isFinite(amount) || amount <= 0) return null;
        const description = tx && tx.description ? String(tx.description) : '';
        const category = tx && tx.categoryId != null ? txCategoriesById.get(Number(tx.categoryId)) : null;
        const categoryName = category && category.name ? String(category.name) : '';
        const key = firstNonEmpty([description, categoryName, `cat:${tx && tx.categoryId != null ? tx.categoryId : ''}`]).toLowerCase();
        return {
          amount,
          label: firstNonEmpty([description, categoryName, t('expense_label')]),
          key
        };
      })
      .filter(Boolean);

    let topOutflow = null;
    expenses.forEach((item) => {
      if (!topOutflow || item.amount > topOutflow.amount) {
        topOutflow = item;
      }
    });
    const syntheticOutflow = Number.isFinite(portfolio) ? portfolio * (0.006 + random() * 0.017) : NaN;

    const recurringGroups = expenses.reduce((acc, item) => {
      if (!item.key) return acc;
      if (!acc.has(item.key)) acc.set(item.key, []);
      acc.get(item.key).push(item.amount);
      return acc;
    }, new Map());
    let recurringLive = 0;
    let recurringFound = false;
    recurringGroups.forEach((amounts) => {
      if (!Array.isArray(amounts) || amounts.length < 2) return;
      const avg = amounts.reduce((sum, value) => sum + value, 0) / amounts.length;
      recurringLive += avg;
      recurringFound = true;
    });
    if (recurringFound) {
      recurringLive *= 4.2;
    }
    const recurringSynthetic = Number.isFinite(portfolio) ? portfolio * (0.004 + random() * 0.012) : NaN;

    return {
      base,
      portfolio,
      growth,
      outflowValue: topOutflow ? topOutflow.amount : syntheticOutflow,
      outflowLabel: topOutflow ? topOutflow.label : '',
      outflowLive: Boolean(topOutflow),
      recurringValue: recurringFound ? recurringLive : recurringSynthetic,
      recurringLive: recurringFound
    };
  }

  function renderAnalysisCards() {
    const model = buildAnalysisInsightsModel();
    const hasWallet = Boolean(analysisState.activeWalletId);
    const updatedValue = formatFxUpdated(
      analysisState.status && (analysisState.status.finishedAt || analysisState.status.updatedAt || analysisState.status.startedAt)
    );

    if (!hasWallet) {
      setAnalysisCardState(selectors.analysisCardPortfolio, 'loading');
      setAnalysisCardState(selectors.analysisCardGrowth, 'loading');
      setAnalysisCardState(selectors.analysisCardOutflow, 'loading');
      setAnalysisCardState(selectors.analysisCardRecurring, 'loading');
      setText(selectors.analysisPortfolioValue, '—');
      setText(selectors.analysisGrowthValue, '—');
      setText(selectors.analysisOutflowValue, '—');
      setText(selectors.analysisRecurringValue, '—');
      setText(selectors.analysisPortfolioMeta, t('analysis_card_waiting'));
      setText(selectors.analysisGrowthMeta, t('analysis_card_waiting'));
      setText(selectors.analysisOutflowMeta, t('analysis_card_waiting'));
      setText(selectors.analysisRecurringMeta, t('analysis_card_waiting'));
      setDataSourceBadge(selectors.analysisPortfolioSource, DATA_SOURCE.pending);
      setDataSourceBadge(selectors.analysisGrowthSource, DATA_SOURCE.pending);
      setDataSourceBadge(selectors.analysisOutflowSource, DATA_SOURCE.pending);
      setDataSourceBadge(selectors.analysisRecurringSource, DATA_SOURCE.pending);
      return;
    }

    setAnalysisCardState(selectors.analysisCardPortfolio, Number.isFinite(model.portfolio) ? 'ready' : 'loading');
    setAnalysisCardState(selectors.analysisCardGrowth, Number.isFinite(model.growth) ? 'ready' : 'loading');
    setAnalysisCardState(selectors.analysisCardOutflow, Number.isFinite(model.outflowValue) ? 'ready' : 'loading');
    setAnalysisCardState(selectors.analysisCardRecurring, Number.isFinite(model.recurringValue) ? 'ready' : 'loading');

    animateMetricValue(selectors.analysisPortfolioValue, model.portfolio, (value) => formatMoney(value, model.base));
    animateMetricValue(selectors.analysisGrowthValue, model.growth, formatSignedPct);
    animateMetricValue(selectors.analysisOutflowValue, model.outflowValue, (value) => formatMoney(-Math.abs(value), model.base));
    animateMetricValue(selectors.analysisRecurringValue, model.recurringValue, (value) => formatMoney(value, model.base));

    setAnalysisValueState(selectors.analysisPortfolioValue, model.portfolio);
    setAnalysisValueState(selectors.analysisGrowthValue, model.growth);
    setAnalysisValueState(selectors.analysisOutflowValue, -Math.abs(model.outflowValue));
    setAnalysisValueState(selectors.analysisRecurringValue, model.recurringValue);

    setText(
      selectors.analysisPortfolioMeta,
      updatedValue ? t('analysis_card_updated_live', { value: updatedValue }) : t('analysis_card_estimated')
    );
    setText(selectors.analysisGrowthMeta, t('analysis_card_estimated'));
    setText(
      selectors.analysisOutflowMeta,
      model.outflowLive ? t('analysis_top_outflow_label', { name: model.outflowLabel }) : t('analysis_top_outflow_estimated')
    );
    setText(
      selectors.analysisRecurringMeta,
      model.recurringLive ? t('analysis_recurring_live') : t('analysis_recurring_estimated')
    );

    setDataSourceBadge(selectors.analysisPortfolioSource, Number.isFinite(model.portfolio) ? DATA_SOURCE.live : DATA_SOURCE.pending);
    setDataSourceBadge(selectors.analysisGrowthSource, DATA_SOURCE.synthetic);
    setDataSourceBadge(selectors.analysisOutflowSource, model.outflowLive ? DATA_SOURCE.live : DATA_SOURCE.synthetic);
    setDataSourceBadge(selectors.analysisRecurringSource, model.recurringLive ? DATA_SOURCE.live : DATA_SOURCE.synthetic);
  }

  function renderAnalysisBanner() {
    const panel = q(selectors.walletAnalysisPanel);
    const banner = q(selectors.walletAnalysisBanner);
    const title = q(selectors.analysisBannerTitle);
    const subtitle = q(selectors.analysisBannerSubtitle);
    const stageEl = q(selectors.analysisStage);
    const progressTextEl = q(selectors.analysisProgressText);
    const progressBar = q(selectors.analysisProgressBar);
    const progressFill = q(selectors.analysisProgressFill);
    const updatedEl = q(selectors.analysisUpdatedAt);
    const cta = q(selectors.analysisEmptyCta);
    if (!panel || !title || !subtitle || !stageEl || !progressTextEl || !progressFill) return;

    if (!analysisState.activeWalletId) {
      setUiState(panel, 'empty');
      if (banner) banner.dataset.uiState = 'idle';
      title.textContent = t('analysis_banner_title_idle');
      subtitle.textContent = t('analysis_banner_subtitle_idle');
      stageEl.textContent = t('analysis_no_wallet_stage');
      progressTextEl.textContent = '0%';
      progressFill.style.width = '0%';
      if (progressBar) progressBar.setAttribute('aria-valuenow', '0');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.pending);
      if (updatedEl) updatedEl.textContent = t('source_pending');
      if (cta) cta.hidden = false;
      return;
    }

    if (cta) cta.hidden = true;
    const status = analysisState.status || {
      status: 'QUEUED',
      progressPct: 0,
      stage: 'FETCH_TX',
      startedAt: '',
      updatedAt: '',
      finishedAt: '',
      partialReady: false
    };
    const statusName = String(status.status || 'QUEUED').toUpperCase();
    const progressPct = Math.max(0, Math.min(100, Number(status.progressPct) || 0));
    const safeWalletName = analysisState.activeWalletName || `#${analysisState.activeWalletId}`;

    setUiState(panel, 'ready');
    if (banner) banner.dataset.uiState = statusName.toLowerCase();
    if (statusName === 'DONE') {
      title.textContent = t('analysis_banner_title_done');
      subtitle.textContent = t('analysis_banner_subtitle_done');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.live);
    } else if (statusName === 'PARTIAL') {
      title.textContent = t('analysis_banner_title_partial');
      subtitle.textContent = t('analysis_banner_subtitle_partial');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.hybrid);
    } else if (statusName === 'FAILED') {
      title.textContent = t('analysis_banner_title_failed');
      subtitle.textContent = t('analysis_banner_subtitle_failed');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.demo);
    } else {
      title.textContent = t('analysis_banner_title_running', { name: safeWalletName });
      subtitle.textContent = t('analysis_banner_subtitle_running');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.pending);
    }

    const stageLabel = `${analysisStageLabel(status.stage)} · ${analysisStatusLabel(statusName)}`;
    stageEl.textContent = analysisState.pollError
      ? `${stageLabel} · ${t('analysis_polling_error')}`
      : stageLabel;
    progressTextEl.textContent = `${progressPct}%`;
    progressFill.style.width = `${progressPct}%`;
    if (progressBar) progressBar.setAttribute('aria-valuenow', String(progressPct));
    if (updatedEl) {
      const updated = formatFxUpdated(status.finishedAt || status.updatedAt || status.startedAt);
      updatedEl.textContent = updated ? t('updated_at', { value: updated }) : t('source_pending');
    }
  }

  function refreshAnalysisPanel() {
    renderAnalysisBanner();
    renderAnalysisCards();
  }

  function syncAnalysisWallets(wallets, payload) {
    const summary = payload && typeof payload === 'object' ? payload : {};
    analysisState.summaryTotal = toNumber(summary.totalValueInBase);
    analysisState.summaryBase = normalizeCurrency(summary.baseCurrency || baseCurrency) || 'USD';

    const latestWallet = pickLatestWallet(wallets);
    if (!latestWallet || latestWallet.id == null) {
      analysisState.activeWalletId = null;
      analysisState.activeWalletName = '';
      analysisState.status = null;
      analysisState.pollError = false;
      stopAnalysisPolling();
      refreshAnalysisPanel();
      return;
    }

    const nextWalletId = Number(latestWallet.id);
    const changed = analysisState.activeWalletId !== nextWalletId;
    analysisState.activeWalletId = nextWalletId;
    analysisState.activeWalletName = String(latestWallet.label || walletNetworkLabel(latestWallet.network || ''));

    refreshAnalysisPanel();
    if (changed) {
      analysisState.status = null;
      analysisState.pollError = false;
      stopAnalysisPolling();
      pollWalletAnalysisStatus();
      return;
    }
    if (!analysisState.status || !isTerminalAnalysisStatus(analysisState.status.status)) {
      scheduleAnalysisPolling(analysisPollIntervalMs);
    }
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

  function renderIncomeExpenseSummary(summary) {
    const netEl = document.querySelector(selectors.incomeExpenseNet);
    const detailsEl = document.querySelector(selectors.incomeExpenseDetails);
    if (!netEl && !detailsEl) return;

    const base = normalizeCurrency(summary && summary.baseCurrency ? summary.baseCurrency : baseCurrency) || 'USD';
    const income = toNumber(summary && summary.income);
    const expense = toNumber(summary && summary.expense);
    const net = toNumber(summary && summary.net);

    if (netEl) {
      netEl.textContent = Number.isFinite(net) ? formatMoney(net, base) : '—';
    }
    if (detailsEl) {
      detailsEl.textContent = Number.isFinite(income) && Number.isFinite(expense)
        ? `${t('income_label')}: ${formatMoney(income, base)} · ${t('expense_label')}: ${formatMoney(expense, base)}`
        : t('income_expense_details');
    }
  }

  function computeAccountsTotalInBase() {
    if (Number.isFinite(lastAccountsTotalInBase)) {
      return lastAccountsTotalInBase;
    }
    if (!lastBalanceSnapshot || !lastBalanceConversion || !lastBalanceConversion.ok) {
      return NaN;
    }
    const totals = Array.isArray(lastBalanceSnapshot.totalsByCurrency) ? lastBalanceSnapshot.totalsByCurrency : [];
    let hasAny = false;
    const sum = totals.reduce((acc, item) => {
      const value = convertToBaseAmount(item?.total, item?.currency, lastBalanceConversion);
      if (!Number.isFinite(value)) {
        return acc;
      }
      hasAny = true;
      return acc + value;
    }, 0);
    if (!totals.length) {
      return 0;
    }
    return hasAny ? sum : NaN;
  }

  function computeCurrentTotalInBase() {
    const accounts = computeAccountsTotalInBase();
    const wallets = toNumber(cryptoWalletTotalInBase);
    const walletOk = Number.isFinite(wallets);
    const accountsOk = Number.isFinite(accounts);
    if (!accountsOk && !walletOk) {
      return NaN;
    }
    return (accountsOk ? accounts : 0) + (walletOk ? wallets : 0);
  }

  async function loadReportSummary() {
    const netEl = document.querySelector(selectors.incomeExpenseNet);
    if (netEl) netEl.textContent = t('loading');

    const params = new URLSearchParams();
    params.set('period', 'MONTH');
    const res = await Api.call(`/api/reports/summary?${params}`, 'GET', null, true);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      if (netEl) netEl.textContent = '—';
      return null;
    }
    const payload = res.data;
    const serverBase = normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD';
    if (serverBase && serverBase !== normalizeCurrency(baseCurrency)) {
      baseCurrency = serverBase;
      updateCurrencyLabels();
    }
    renderIncomeExpenseSummary(payload);
    return payload;
  }

  async function loadReportByCategory() {
    const target = document.querySelector(selectors.expenseChart);
    if (target) {
      setUiState(target, 'loading');
      target.innerHTML = renderSkeletonList(2);
    }

    const params = new URLSearchParams();
    params.set('period', 'MONTH');
    params.set('limit', '5');
    const res = await Api.call(`/api/reports/by-category?${params}`, 'GET', null, true);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      if (target) {
        setUiState(target, 'error');
        target.innerHTML = `<div class="muted">${t('no_data')}</div>`;
      }
      return null;
    }
    const payload = res.data;
    const serverBase = normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD';
    if (serverBase && serverBase !== normalizeCurrency(baseCurrency)) {
      baseCurrency = serverBase;
      updateCurrencyLabels();
    }

    const expenses = Array.isArray(payload.expenses) ? payload.expenses : [];
    const palette = ['#4f8bff', '#10b981', '#f97316', '#3cc7c4', '#9aa0aa', '#8b5cf6'];
    const items = expenses
      .map((item, idx) => {
        const value = toNumber(item && item.total);
        return {
          label: item && item.categoryName ? String(item.categoryName) : '',
          value: Number.isFinite(value) ? Math.max(value, 0) : NaN,
          color: palette[idx % palette.length]
        };
      })
      .filter((item) => item.label && Number.isFinite(item.value) && item.value > 0);

    if (!items.length) {
      if (target) {
        setUiState(target, 'empty');
        target.innerHTML = `<div class="muted">${t('no_data')}</div>`;
      }
      return payload;
    }

    if (target) setUiState(target, 'ready');
    renderBarChart(selectors.expenseChart, items, baseCurrency);
    return payload;
  }

  async function loadBalanceTrend() {
    const target = document.querySelector(selectors.balanceChart);
    if (target) {
      setUiState(target, 'loading');
      target.innerHTML = renderSkeletonList(2);
    }

    const now = new Date();
    const start = new Date(now);
    start.setMonth(now.getMonth() - 5);
    start.setDate(1);
    start.setHours(0, 0, 0, 0);

    const params = new URLSearchParams();
    params.set('from', start.toISOString());
    params.set('to', now.toISOString());
    const res = await Api.call(`/api/reports/cash-flow?${params}`, 'GET', null, true);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      if (target) setUiState(target, 'ready');
      renderLineChart(selectors.balanceChart, demoData.balance, baseCurrency);
      return null;
    }
    const payload = res.data;
    const points = Array.isArray(payload.points) ? payload.points : [];

    const byMonth = {};
    points.forEach((point) => {
      const rawDate = point && point.date ? String(point.date) : '';
      if (rawDate.length < 7) return;
      const key = rawDate.slice(0, 7);
      const net = toNumber(point && point.net);
      if (!Number.isFinite(net)) return;
      byMonth[key] = (byMonth[key] || 0) + net;
    });

    const monthKeys = [];
    const cursor = new Date(now);
    cursor.setDate(1);
    cursor.setHours(0, 0, 0, 0);
    cursor.setMonth(cursor.getMonth() - 5);
    for (let i = 0; i < 6; i += 1) {
      monthKeys.push(`${cursor.getFullYear()}-${String(cursor.getMonth() + 1).padStart(2, '0')}`);
      cursor.setMonth(cursor.getMonth() + 1);
    }

    const monthNet = monthKeys.map((key) => toNumber(byMonth[key]) || 0);
    const currentTotal = computeCurrentTotalInBase();
    const netSum = monthNet.reduce((acc, v) => acc + v, 0);

    let running = Number.isFinite(currentTotal) ? (currentTotal - netSum) : 0;
    const series = monthNet.map((v) => {
      running += v;
      return running;
    });

    if (target) setUiState(target, 'ready');
    renderLineChart(selectors.balanceChart, series, baseCurrency);
    return payload;
  }

  async function loadReports() {
    await loadReportSummary();
    await loadReportByCategory();
    await loadBalanceTrend();
  }

  function renderAccountsList(accounts, conversion) {
    const list = document.querySelector(selectors.accountsList);
    if (!list) return;
    setPanelFeedback(selectors.accountsFeedback, '');
    if (!accounts || accounts.length === 0) {
      setUiState(list, 'empty');
      list.innerHTML = renderEmptyState(
        `${t('no_accounts')} ${t('empty_accounts_hint')}`,
        t('cta_add_account'),
        'open-add-account'
      );
      return;
    }
    setUiState(list, 'ready');
    const base = normalizeCurrency(baseCurrency) || 'USD';
    list.innerHTML = accounts.map((acc) => {
      const value = convertToBaseAmount(acc.balance || 0, acc.currency, conversion);
      const amountText = Number.isFinite(value) ? formatMoney(value, base) : formatMoney(acc.balance || 0, acc.currency);
      const balanceValue = toNumber(acc.balance);
      const signClass = Number.isFinite(balanceValue) && balanceValue < 0 ? 'amount-negative' : 'amount-positive';
      const rawName = acc && acc.name ? String(acc.name) : t('account');
      const safeName = escapeHtml(rawName);
      const safeCurrency = escapeHtml(acc.currency || baseCurrency);
      const safeId = escapeHtml(acc && acc.id != null ? String(acc.id) : '');
      return `
        <div class="list-item">
          <div>
            <div style="font-weight:800;">${safeName}</div>
            <small>${safeCurrency}${acc.archived ? ` · ${t('archived')}` : ''}</small>
          </div>
          <div class="account-actions">
            <div class="${signClass}">${amountText}</div>
            <button type="button" class="ghost account-remove" data-account-id="${safeId}" data-account-name="${safeName}" title="${t('account_remove')}" aria-label="${t('account_remove')}">✕</button>
          </div>
        </div>
      `;
    }).join('');

    list.querySelectorAll('[data-account-id]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.accountId;
        if (!id) return;
        const name = btn.dataset.accountName || t('account');
        const ok = window.confirm(t('account_delete_confirm', { name }));
        if (!ok) return;

        showBalanceError('');
        const res = await Api.call(`/api/accounts/${encodeURIComponent(id)}`, 'DELETE', null, true);
        if (!res.ok) {
          const message = res.data && typeof res.data === 'object' ? (res.data.message || '') : '';
          showBalanceError(message || t('account_delete_failed'));
          setPanelFeedback(selectors.accountsFeedback, message || t('account_delete_failed'), true);
          return;
        }
        setPanelFeedback(selectors.accountsFeedback, '');
        await loadBalance();
        await loadReports();
      });
    });
  }

  function applyTxPeriodButtons() {
    document.querySelectorAll(selectors.txPeriodButtons).forEach((btn) => {
      const period = Number(btn.dataset.period);
      btn.classList.toggle('active', Number.isFinite(period) && period === txPeriodDays);
    });
  }

  function bindTxPeriodButtons() {
    const buttons = Array.from(document.querySelectorAll(selectors.txPeriodButtons));
    if (!buttons.length) return;
    applyTxPeriodButtons();
    buttons.forEach((btn) => {
      btn.addEventListener('click', () => {
        const period = Number(btn.dataset.period);
        if (!Number.isFinite(period) || (period !== 7 && period !== 30)) return;
        txPeriodDays = period;
        try {
          localStorage.setItem(TX_PERIOD_STORAGE_KEY, String(period));
        } catch (_) {
          // ignore
        }
        applyTxPeriodButtons();
        loadRecentTransactions();
      });
    });
  }

  async function loadTransactionCategories() {
    const res = await Api.call('/api/categories', 'GET', null, true);
    if (!res.ok || !Array.isArray(res.data)) {
      txCategories = [];
      txCategoriesById = new Map();
      return;
    }
    txCategories = res.data
      .filter((item) => item && item.id != null && item.name)
      .map((item) => ({
        id: Number(item.id),
        name: String(item.name),
        type: String(item.type || ''),
        system: Boolean(item.system)
      }));
    txCategories.sort((a, b) => {
      if (a.system !== b.system) return a.system ? -1 : 1;
      return String(a.name).localeCompare(String(b.name), getLocale());
    });
    txCategoriesById = new Map(txCategories.map((item) => [item.id, item]));
  }

  function isCategoryCompatible(categoryType, transactionType) {
    const cat = String(categoryType || '').toUpperCase();
    const tx = String(transactionType || '').toUpperCase();
    if (!cat || !tx) return false;
    if (cat === 'BOTH') return true;
    return cat === tx;
  }

  function toLocalDateTimeInputValue(date) {
    const d = date instanceof Date ? date : new Date();
    if (Number.isNaN(d.getTime())) return '';
    const pad = (v) => String(v).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  function formatTxTimestamp(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    return date.toLocaleString(getLocale(), { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
  }

  function renderTransactionsList(transactions) {
    const list = document.querySelector(selectors.transactionsList);
    if (!list) return;
    if (!Array.isArray(transactions) || transactions.length === 0) {
      setUiState(list, 'empty');
      list.innerHTML = renderEmptyState(
        `${t('transactions_empty')} ${t('empty_transactions_hint')}`,
        t('cta_add_transaction'),
        'open-add-transaction'
      );
      return;
    }
    setUiState(list, 'ready');

    const accounts = lastBalanceSnapshot && Array.isArray(lastBalanceSnapshot.accounts) ? lastBalanceSnapshot.accounts : [];
    const accountById = new Map(
      accounts
        .filter((acc) => acc && acc.id != null)
        .map((acc) => [Number(acc.id), acc])
    );

    list.innerHTML = transactions.map((tx) => {
      const type = String(tx && tx.type ? tx.type : '').toUpperCase();
      const isIncome = type === 'INCOME';
      const amount = toNumber(tx && tx.amount);
      const accountId = tx && tx.accountId != null ? Number(tx.accountId) : NaN;
      const account = accountById.get(accountId);
      const accountName = account && account.name ? String(account.name) : '';

      const categoryId = tx && tx.categoryId != null ? Number(tx.categoryId) : NaN;
      const category = txCategoriesById.get(categoryId);
      const categoryName = category ? String(category.name) : (Number.isFinite(categoryId) ? `#${categoryId}` : '');

      const currency = normalizeCurrency(tx && tx.currency) || normalizeCurrency(account && account.currency) || normalizeCurrency(baseCurrency) || 'USD';
      const signed = Number.isFinite(amount) ? (isIncome ? amount : -amount) : NaN;
      let amountText = Number.isFinite(signed) ? formatMoney(signed, currency) : '—';
      if (isIncome && Number.isFinite(amount) && !amountText.trim().startsWith('+')) {
        amountText = `+${amountText}`;
      }

      const signClass = isIncome ? 'amount-positive' : 'amount-negative';
      const safeCategory = escapeHtml(categoryName || t('transaction_category_label'));
      const safeAccount = escapeHtml(accountName || t('transaction_account_label'));
      const safeWhen = escapeHtml(formatTxTimestamp(tx && tx.transactionDate) || '');
      const description = tx && tx.description ? String(tx.description) : '';
      const safeDesc = description ? escapeHtml(description) : '';
      const descPart = safeDesc ? `<span>·</span><span class="tx-desc">${safeDesc}</span>` : '';
      const accountPart = safeAccount ? `<span>·</span><span>${safeAccount}</span>` : '';
      const meta = safeWhen ? `${safeWhen}${accountPart}${descPart}` : `${safeAccount}${descPart}`;

      return `
        <div class="list-item">
          <div>
            <div style="font-weight:800;">${safeCategory}</div>
            <small class="tx-meta">${meta}</small>
          </div>
          <div class="${signClass}">${amountText}</div>
        </div>
      `;
    }).join('');
  }

  async function loadRecentTransactions() {
    const list = document.querySelector(selectors.transactionsList);
    if (list) {
      setUiState(list, 'loading');
      list.innerHTML = renderSkeletonList(3);
    }
    setPanelFeedback(selectors.transactionsFeedback, '');

    const now = new Date();
    const from = new Date(now.getTime() - txPeriodDays * 24 * 60 * 60 * 1000);
    const params = new URLSearchParams();
    params.set('from', from.toISOString());
    params.set('to', now.toISOString());
    params.set('limit', String(txListLimit));

    const res = await Api.call(`/api/transactions?${params}`, 'GET', null, true);
    if (!res.ok || !Array.isArray(res.data)) {
      if (list) {
        setUiState(list, 'error');
        list.innerHTML = renderErrorState(t('transactions_load_failed_short'), t('cta_retry'), 'retry-transactions');
      }
      setPanelFeedback(selectors.transactionsFeedback, t('transactions_load_failed'), true);
      recentTransactionsCache = [];
      refreshAnalysisPanel();
      return;
    }
    setPanelFeedback(selectors.transactionsFeedback, '');
    recentTransactionsCache = res.data.slice();
    renderTransactionsList(res.data);
    refreshAnalysisPanel();
    pulseElement(selectors.transactionsList);
  }

  function showAddTransactionError(message) {
    const box = document.querySelector(selectors.addTransactionError);
    if (!box) return;
    if (message) {
      box.style.display = 'block';
      box.textContent = message;
    } else {
      box.style.display = 'none';
      box.textContent = '';
    }
  }

  function populateTransactionAccountSelect() {
    const select = document.querySelector(selectors.addTransactionAccount);
    if (!select) return;
    select.innerHTML = '';
    const accounts = lastBalanceSnapshot && Array.isArray(lastBalanceSnapshot.accounts) ? lastBalanceSnapshot.accounts : [];
    const active = accounts
      .filter((acc) => acc && acc.id != null && !acc.archived)
      .map((acc) => ({
        id: Number(acc.id),
        name: String(acc.name || t('account')),
        currency: normalizeCurrency(acc.currency)
      }))
      .sort((a, b) => a.name.localeCompare(b.name, getLocale()));

    if (!active.length) {
      const option = document.createElement('option');
      option.value = '';
      option.textContent = t('transaction_no_accounts');
      option.disabled = true;
      option.selected = true;
      select.appendChild(option);
      return;
    }

    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = t('transaction_select_account');
    placeholder.disabled = true;
    placeholder.selected = true;
    select.appendChild(placeholder);

    active.forEach((acc) => {
      const option = document.createElement('option');
      option.value = String(acc.id);
      option.textContent = acc.currency ? `${acc.name} (${acc.currency})` : acc.name;
      select.appendChild(option);
    });
  }

  function populateTransactionCategorySelect(transactionType) {
    const select = document.querySelector(selectors.addTransactionCategory);
    if (!select) return;
    select.innerHTML = '';

    const filtered = txCategories.filter((cat) => isCategoryCompatible(cat.type, transactionType));

    if (!filtered.length) {
      const option = document.createElement('option');
      option.value = '';
      option.textContent = t('transaction_no_categories');
      option.disabled = true;
      option.selected = true;
      select.appendChild(option);
      return;
    }

    const placeholder = document.createElement('option');
    placeholder.value = '';
    placeholder.textContent = t('transaction_select_category');
    placeholder.disabled = true;
    placeholder.selected = true;
    select.appendChild(placeholder);

    filtered.forEach((cat) => {
      const option = document.createElement('option');
      option.value = String(cat.id);
      option.textContent = cat.name;
      select.appendChild(option);
    });
  }

  function bindAddTransactionMenu() {
    const btn = document.querySelector(selectors.addTransactionBtn);
    const menu = document.querySelector(selectors.addTransactionMenu);
    const overlay = document.querySelector(selectors.addTransactionOverlay);
    const cancelBtn = document.querySelector(selectors.addTransactionCancelBtn);
    const closeBtn = document.querySelector(selectors.addTransactionCloseBtn);
    const createBtn = document.querySelector(selectors.addTransactionCreateBtn);
    const typeSelect = document.querySelector(selectors.addTransactionType);
    if (!btn || !menu || !overlay || !cancelBtn || !closeBtn || !createBtn || !typeSelect) return;
    if (btn.dataset.modalBound === '1') return;
    btn.dataset.modalBound = '1';

    let open = false;
    let submitting = false;
    let opener = null;

    const close = () => {
      open = false;
      closeOverlay(overlay, menu, opener || btn);
      opener = null;
    };

    const openDialog = () => {
      open = true;
      opener = document.activeElement instanceof HTMLElement ? document.activeElement : btn;
      openOverlay(overlay, menu, opener || btn);
      showAddTransactionError('');

      populateTransactionAccountSelect();
      typeSelect.value = 'EXPENSE';
      populateTransactionCategorySelect(typeSelect.value);

      const amountEl = document.querySelector(selectors.addTransactionAmount);
      const dateEl = document.querySelector(selectors.addTransactionDate);
      const descEl = document.querySelector(selectors.addTransactionDescription);
      if (amountEl) amountEl.value = '';
      if (descEl) descEl.value = '';
      if (dateEl) dateEl.value = toLocalDateTimeInputValue(new Date());

      createBtn.disabled = false;
      const accountEl = document.querySelector(selectors.addTransactionAccount);
      if (accountEl) accountEl.focus();
    };

    btn.addEventListener('click', (e) => {
      e.preventDefault();
      openDialog();
    });
    btn.addEventListener('keydown', (e) => {
      if (e.key !== 'Enter' && e.key !== ' ') return;
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

    typeSelect.addEventListener('change', () => {
      populateTransactionCategorySelect(typeSelect.value);
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
      showAddTransactionError('');

      const accountEl = document.querySelector(selectors.addTransactionAccount);
      const categoryEl = document.querySelector(selectors.addTransactionCategory);
      const amountEl = document.querySelector(selectors.addTransactionAmount);
      const dateEl = document.querySelector(selectors.addTransactionDate);
      const descEl = document.querySelector(selectors.addTransactionDescription);

      const accountId = Number(accountEl?.value);
      const categoryId = Number(categoryEl?.value);
      const type = String(typeSelect.value || '').toUpperCase();
      const amount = parseOptionalNumber(amountEl?.value || '');
      const dateValue = (dateEl?.value || '').trim();
      const description = (descEl?.value || '').trim();

      if (accountEl) accountEl.classList.toggle('error', !Number.isFinite(accountId));
      if (categoryEl) categoryEl.classList.toggle('error', !Number.isFinite(categoryId));
      if (amountEl) amountEl.classList.toggle('error', amount == null || Number.isNaN(amount) || amount <= 0);

      if (!Number.isFinite(accountId)) {
        showAddTransactionError(t('transaction_select_account'));
        if (accountEl) accountEl.focus();
        return;
      }
      if (!Number.isFinite(categoryId)) {
        showAddTransactionError(t('transaction_select_category'));
        if (categoryEl) categoryEl.focus();
        return;
      }
      if (amount == null) {
        showAddTransactionError(t('transaction_enter_amount'));
        if (amountEl) amountEl.focus();
        return;
      }
      if (Number.isNaN(amount) || amount <= 0) {
        showAddTransactionError(t('transaction_invalid_amount'));
        if (amountEl) amountEl.focus();
        return;
      }

      let txDate = new Date();
      if (dateValue) {
        const parsed = new Date(dateValue);
        if (!Number.isNaN(parsed.getTime())) {
          txDate = parsed;
        }
      }

      submitting = true;
      createBtn.disabled = true;
      try {
        const res = await Api.call('/api/transactions', 'POST', {
          accountId,
          categoryId,
          type,
          amount,
          transactionDate: txDate.toISOString(),
          description: description || null
        }, true);
        if (!res.ok) {
          const message = res.data && typeof res.data === 'object'
            ? (res.data.message || '')
            : '';
          showAddTransactionError(message || t('transaction_create_failed'));
          return;
        }
        close();
        await loadBalance();
        await loadReports();
        await loadRecentTransactions();
      } finally {
        submitting = false;
        createBtn.disabled = false;
      }
    };

    createBtn.addEventListener('click', submit);
    menu.addEventListener('keydown', (e) => {
      trapFocusKeydown(e, menu);
      if (e.key === 'Enter') {
        e.preventDefault();
        submit();
      }
    });
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
      setUiState(container, 'empty');
      container.innerHTML = `<div class="muted">${t('fx_no_data')}</div>`;
      return;
    }
    setUiState(container, 'ready');
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
      setUiState(list, 'empty');
      list.innerHTML = `<div class="muted">${t('fx_no_currencies')}</div>`;
      return;
    }
    setUiState(list, 'ready');
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
      setUiState(list, 'loading');
      list.innerHTML = renderSkeletonList(3);
    }
    const totalBalanceEl = document.querySelector(selectors.totalBalance);
    if (totalBalanceEl) totalBalanceEl.textContent = t('loading');
    showBalanceError('');
    setPanelFeedback(selectors.accountsFeedback, '');

    const res = await Api.call('/api/accounts/balance', 'GET', null, true);
    if (!res.ok) {
      showBalanceError(t('balance_load_failed'));
      if (list) {
        setUiState(list, 'error');
        list.innerHTML = renderErrorState(t('balance_load_failed_short'), t('cta_retry'), 'retry-balance');
      }
      setPanelFeedback(selectors.accountsFeedback, t('balance_load_failed'), true);
      lastAccountsTotalInBase = NaN;
      return;
    }
    const payload = res.data && typeof res.data === 'object' ? res.data : {};
    const serverBase = normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD';
    if (serverBase && serverBase !== normalizeCurrency(baseCurrency)) {
      baseCurrency = serverBase;
      updateCurrencyLabels();
    }
    const serverTotalInBase = toNumber(payload.totalInBase);
    lastAccountsTotalInBase = Number.isFinite(serverTotalInBase) ? serverTotalInBase : NaN;
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
    pulseElement(selectors.totalBalance);
  }

  async function loadFxRates() {
    const statusEl = document.querySelector(selectors.fxStatus);
    const baseEl = document.querySelector(selectors.fxBase);
    const topContainer = document.querySelector(selectors.fxTopVolatile);
    const base = resolveFxBase();
    const baseSelect = document.querySelector(selectors.fxBaseSelect);
    fxBase = base;
    if (statusEl) statusEl.textContent = t('updating');
    if (baseEl) baseEl.textContent = base;
    if (baseSelect) baseSelect.value = base;
    if (topContainer) {
      setUiState(topContainer, 'loading');
      topContainer.innerHTML = renderSkeletonList(3);
    }
    dataSourceState.fx = DATA_SOURCE.pending;
    setDataSourceBadge(selectors.fxDataSource, dataSourceState.fx);
    syncMarketDataSource();

    const quotes = fxCurrencies.map((item) => item.code).filter((code) => code !== base);
    const params = new URLSearchParams();
    params.set('base', base);
    quotes.forEach((code) => params.append('quote', code));
    const query = params.toString();
    const res = await Api.call(`/api/fx/rates?${query}`, 'GET', null, false);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      if (topContainer) {
        setUiState(topContainer, 'error');
        topContainer.innerHTML = renderErrorState(t('fx_no_data'), t('cta_retry'), 'retry-fx');
      } else {
        renderFxTop([]);
      }
      renderFxDetail(null);
      renderFxList([]);
      if (statusEl) statusEl.textContent = t('no_data');
      dataSourceState.fx = DATA_SOURCE.demo;
      setDataSourceBadge(selectors.fxDataSource, dataSourceState.fx);
      syncMarketDataSource();
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
    dataSourceState.fx = DATA_SOURCE.hybrid;
    setDataSourceBadge(selectors.fxDataSource, dataSourceState.fx);
    syncMarketDataSource();
    pulseElement(selectors.fxStatus);
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
    dataSourceState.crypto = DATA_SOURCE.pending;
    setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
    syncMarketDataSource();

    const params = new URLSearchParams();
    params.set('base', base);
    const res = await Api.call(`/api/crypto/rates?${params}`, 'GET', null, false);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      renderCryptoFallback(base);
      if (statusEl) statusEl.textContent = t('demo_data');
      dataSourceState.crypto = DATA_SOURCE.demo;
      setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
      syncMarketDataSource();
      return;
    }
    const payload = res.data;
    const rates = Array.isArray(payload.rates) ? payload.rates : [];
    if (!rates.length) {
      renderCryptoFallback(base);
      if (statusEl) statusEl.textContent = t('no_data');
      dataSourceState.crypto = DATA_SOURCE.demo;
      setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
      syncMarketDataSource();
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
    dataSourceState.crypto = hasAny ? DATA_SOURCE.live : DATA_SOURCE.demo;
    setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
    syncMarketDataSource();
    pulseElement(selectors.cryptoStatus);
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
    setPanelFeedback(selectors.walletsFeedback, '');
    if (!Array.isArray(wallets) || wallets.length === 0) {
      setUiState(list, 'empty');
      list.innerHTML = renderEmptyState(
        `${t('wallets_empty')} ${t('empty_wallets_hint')}`,
        t('cta_add_wallet'),
        'open-add-wallet'
      );
      return;
    }
    setUiState(list, 'ready');
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
        const res = await Api.call(`/api/crypto/wallets/${encodeURIComponent(id)}`, 'DELETE', null, true);
        if (!res.ok) {
          const message = res.data && typeof res.data === 'object' ? (res.data.message || '') : '';
          setPanelFeedback(selectors.walletsFeedback, message || t('wallet_delete_failed'), true);
          return;
        }
        setPanelFeedback(selectors.walletsFeedback, '');
        await loadWallets();
      });
    });
  }

  async function loadWallets() {
    const list = document.querySelector(selectors.walletsList);
    if (list) {
      setUiState(list, 'loading');
      list.innerHTML = renderSkeletonList(2);
    }
    setPanelFeedback(selectors.walletsFeedback, '');
    const res = await Api.call('/api/crypto/wallets/summary', 'GET', null, true);
    if (!res.ok) {
      if (list) {
        setUiState(list, 'error');
        list.innerHTML = renderErrorState(t('wallets_loading_failed'), t('cta_retry'), 'retry-wallets');
      }
      setPanelFeedback(selectors.walletsFeedback, t('wallets_loading_failed'), true);
      cryptoWalletTotalInBase = NaN;
      rerenderBalanceSnapshot();
      refreshAnalysisPanel();
      return;
    }
    const payload = res.data && typeof res.data === 'object' ? res.data : {};
    const wallets = Array.isArray(payload.wallets) ? payload.wallets : [];
    renderWallets(wallets);
    const total = toNumber(payload.totalValueInBase);
    cryptoWalletTotalInBase = Number.isFinite(total) ? total : NaN;
    syncAnalysisWallets(wallets, payload);
    rerenderBalanceSnapshot();
    pulseElement(selectors.walletsList);
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
    if (btn.dataset.modalBound === '1') return;
    btn.dataset.modalBound = '1';

    let open = false;
    let submitting = false;
    let opener = null;

    const close = () => {
      open = false;
      closeOverlay(overlay, menu, opener || btn);
      opener = null;
    };

    const openDialog = () => {
      open = true;
      opener = document.activeElement instanceof HTMLElement ? document.activeElement : btn;
      openOverlay(overlay, menu, opener || btn);
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
    btn.addEventListener('keydown', (e) => {
      if (e.key !== 'Enter' && e.key !== ' ') return;
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
      trapFocusKeydown(e, menu);
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
    if (btn.dataset.modalBound === '1') return;
    btn.dataset.modalBound = '1';

    let open = false;
    let submitting = false;
    let opener = null;

    const close = () => {
      open = false;
      closeOverlay(overlay, menu, opener || btn);
      opener = null;
    };

    const openDialog = () => {
      open = true;
      opener = document.activeElement instanceof HTMLElement ? document.activeElement : btn;
      openOverlay(overlay, menu, opener || btn);
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
    btn.addEventListener('keydown', (e) => {
      if (e.key !== 'Enter' && e.key !== ' ') return;
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
        await loadReports();
      } finally {
        submitting = false;
        saveBtn.disabled = false;
      }
    };

    saveBtn.addEventListener('click', submit);
    menu.addEventListener('keydown', (e) => {
      trapFocusKeydown(e, menu);
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
    if (btn.dataset.modalBound === '1') return;
    btn.dataset.modalBound = '1';

    let open = false;
    let submitting = false;
    let opener = null;

    const close = () => {
      open = false;
      closeOverlay(overlay, menu, opener || btn);
      opener = null;
    };

    const openDialog = () => {
      open = true;
      opener = document.activeElement instanceof HTMLElement ? document.activeElement : btn;
      openOverlay(overlay, menu, opener || btn);
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
    btn.addEventListener('keydown', (e) => {
      if (e.key !== 'Enter' && e.key !== ' ') return;
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
      trapFocusKeydown(e, menu);
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

  // Bind primary controls early so keyboard interactions are available immediately.
  bindAddAccountMenu();
  bindAddWalletMenu();
  bindAddTransactionMenu();
  bindBaseCurrencyMenu();
  bindTxPeriodButtons();
  window.addEventListener('beforeunload', stopAnalysisPolling);

  document.addEventListener('DOMContentLoaded', async () => {
    const dashboardRoot = q(selectors.root);
    if (dashboardRoot) dashboardRoot.dataset.uiState = 'loading';
    try {
      Theme.apply();
      applyLanguage(currentLang);
      bindLangToggle();
      initMotionController();
      initScrollReveal();
      bindActionCtas();
      bindTxPeriodButtons();
      bindAddAccountMenu();
      bindAddWalletMenu();
      bindAddTransactionMenu();

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
      await Promise.all([
        loadTransactionCategories(),
        loadFxRates(),
        loadCryptoRates(),
        loadWallets(),
        updateDemoAmounts()
      ]);
      await Promise.all([
        loadReports(),
        loadRecentTransactions()
      ]);
      if (dashboardRoot) dashboardRoot.dataset.uiState = 'ready';
    } catch (e) {
      console.error('Dashboard init failed', e);
      if (dashboardRoot) dashboardRoot.dataset.uiState = 'error';
    }
  });
})();
