(() => {
  const selectors = {
    root: 'body.dashboard',
    userEmail: '#userEmail',
    logoutBtn: '#btn-logout',
    refreshBtn: '#btn-refresh',
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
    balancePeriodButtons: '.balance-period-btn',
    balanceMetricSelect: '#balanceMetricSelect',
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
    analysisIncomeValue: '#analysisIncomeValue',
    analysisExpenseValue: '#analysisExpenseValue',
    upcomingSection: '#upcomingSection',
    upcomingPaymentsList: '#upcomingPaymentsList',
    walletAnalysisPanel: '#walletAnalysisPanel',
    walletAnalysisBanner: '#walletAnalysisBanner',
    analysisBannerTitle: '#analysisBannerTitle',
    analysisBannerSubtitle: '#analysisBannerSubtitle',
    analysisDataSource: '#analysisDataSource',
    analysisUpdatedAt: '#analysisUpdatedAt',
    analysisProgressWrap: '#analysisProgressWrap',
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
    analysisGrowthSpark: '#analysisGrowthSpark',
    analysisOutflowValue: '#analysisOutflowValue',
    analysisRecurringValue: '#analysisRecurringValue',
    analysisPortfolioMeta: '#analysisPortfolioMeta',
    analysisGrowthMeta: '#analysisGrowthMeta',
    analysisOutflowMeta: '#analysisOutflowMeta',
    analysisRecurringMeta: '#analysisRecurringMeta',
    analysisQuickCard: '#analysisQuickCard',
    analysisMiniValue: '#analysisMiniValue',
    analysisMiniGrowth: '#analysisMiniGrowth',
    analysisHeroChange: '#analysisHeroChange',
    analysisDetailOverlay: '#analysis-detail-overlay',
    analysisDetailMenu: '#analysis-detail-menu',
    analysisDetailCloseBtn: '#btn-analysis-detail-close',
    analysisDetailSource: '#analysisDetailSource',
    analysisDetailUpdated: '#analysisDetailUpdated',
    analysisDetailCopyBtn: '#analysisDetailCopyBtn',
    analysisDetailExplorerLink: '#analysisDetailExplorerLink',
    analysisDetailMetricTabs: '#analysisDetailMetricTabs',
    analysisDetailWindowTabs: '#analysisDetailWindowTabs',
    analysisDetailWalletName: '#analysisDetailWalletName',
    analysisDetailWalletNetwork: '#analysisDetailWalletNetwork',
    analysisDetailWalletAddress: '#analysisDetailWalletAddress',
    analysisDetailWalletValue: '#analysisDetailWalletValue',
    analysisDetailWalletBalance: '#analysisDetailWalletBalance',
    analysisDetailMetricPortfolio: '#analysisDetailMetricPortfolio',
    analysisDetailMetricBalance: '#analysisDetailMetricBalance',
    analysisDetailMetricGrowth: '#analysisDetailMetricGrowth',
    analysisDetailMetricInflow: '#analysisDetailMetricInflow',
    analysisDetailMetricOutflow: '#analysisDetailMetricOutflow',
    analysisDetailPortfolio: '#analysisDetailPortfolio',
    analysisDetailGrowth: '#analysisDetailGrowth',
    analysisDetailOutflow: '#analysisDetailOutflow',
    analysisDetailRecurring: '#analysisDetailRecurring',
    analysisDetailBalance: '#analysisDetailBalance',
    analysisDetailNet: '#analysisDetailNet',
    analysisDetailSeriesMeta: '#analysisDetailSeriesMeta',
    analysisDetailSeriesChart: '#analysisDetailSeriesChart',
    analysisDetailAllocationList: '#analysisDetailAllocationList',
    analysisDetailInsightsList: '#analysisDetailInsightsList',
    getStartedSection: '#getStartedSection'
  };

  const LANG_STORAGE_KEY = 'finguard:lang';
  const TX_PERIOD_STORAGE_KEY = 'finguard:txPeriodDays';
  const I18N = {
    ru: {
      dashboard_page_title: 'FinGuard | Дашборд',
      dashboard_title: 'Личный кабинет',
      logout: 'Выйти',
      refresh: 'Обновить',
      base_currency_button: 'Валюта: {value}',
      balance: 'Баланс',
      credit: 'Кредит',
      credit_placeholder: 'Кредит: —',
      income_expense_month: 'Доход / Расход (мес.)',
      income_month: 'Доход (мес.)',
      spend_month: 'Расход (мес.)',
      income_30d: 'Доход (30д)',
      spend_30d: 'Расход (30д)',
      cashflow_30d: 'Кэшфлоу (30д)',
      debt_label: 'Долг',
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
      payment_name: 'Платёж',
      payment_due_prefix: 'Срок',
      net_worth_title: 'Капитал',
      demo: 'Демо',
      balance_trend_title: 'Динамика баланса',
      last_6_months: 'Последние 6 месяцев,',
      trend_label: 'Тренд:',
      balance_chart_aria: 'График баланса',
      expense_breakdown_title: 'Структура расходов',
      current_month: 'Текущий месяц,',
      expense_chart_aria: 'Диаграмма расходов',
      expense_empty_title: 'Нет данных за текущий месяц',
      markets_badge: 'Рынки',
      crypto_badge: 'Crypto',
      fx_badge: 'FX',
      fx_radar_title: 'FX Radar',
      markets_title: 'Рынки',
      markets_subtitle: 'Сводка по крипте и валютам',
      coin_rates: 'Курсы монет',
      loading: 'Загрузка…',
      updating: 'Обновляем…',
      demo_data: 'Оценочные данные',
      no_data: 'Нет данных',
      updated: 'Обновлено',
      updated_at: 'Обновлено {value}',
      source_pending: 'Ожидаем данные',
      source_live: 'Онлайн',
      source_demo: 'Оценка',
      source_synthetic: 'Оценка',
      source_hybrid: 'Онлайн',
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
      recent_transactions_title: 'Транзакции',
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
      tx_col_date: 'Дата',
      tx_col_merchant: 'Описание',
      tx_col_category: 'Категория',
      tx_col_amount: 'Сумма',
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
      wallets_show_all: 'Показать все ({value})',
      wallets_show_less: 'Свернуть',
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
      copy: 'Копировать',
      open_explorer: 'Эксплорер',
      back_to_dashboard: 'Назад',
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
      cta_connect_account: 'Подключить счёт',
      cta_retry: 'Повторить',
      period_7d: 'за 7д',
      period_7d_compact: '• 7d',
      period_30d_short: '30д',
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
      analysis_banner_badge: 'Аналитика кошелька',
      analysis_open_link: 'Аналитика кошелька →',
      analysis_banner_title_idle: 'Подключите кошелёк',
      analysis_banner_subtitle_idle: 'После подключения аналитика появится автоматически.',
      analysis_banner_title_running: 'Синхронизация {name}',
      analysis_banner_subtitle_running: 'Собираем данные кошелька.',
      analysis_banner_title_partial: 'Часть данных готова',
      analysis_banner_subtitle_partial: 'Откройте аналитику кошелька для деталей.',
      analysis_banner_title_done: 'Аналитика кошелька обновлена',
      analysis_banner_subtitle_done: 'Все метрики синхронизированы.',
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
      analysis_card_portfolio: 'Стоимость портфеля',
      analysis_card_growth: 'Изм. за 7д',
      analysis_card_outflow: 'Крупный отток',
      analysis_card_recurring: 'Регулярные траты',
      analysis_card_waiting: 'Ожидаем метрики…',
      analysis_card_updated_live: 'Live: обновлено {value}',
      analysis_card_estimated: 'Оценка на основе истории',
      analysis_no_wallet_stage: 'Добавьте кошелёк для старта',
      analysis_polling_error: 'Не удалось обновить статус анализа',
      analysis_top_outflow_label: 'Крупнейший отток: {name}',
      analysis_top_outflow_estimated: 'Оценка оттока пока нет новых операций',
      analysis_recurring_live: 'Регулярные по последним операциям',
      analysis_recurring_estimated: 'Оценка до завершения анализа',
      analysis_recurring_meta_live: 'След.: {value} · {confidence}%',
      analysis_recurring_meta_estimated: 'Оценка · {confidence}%',
      analysis_quick_open_hint: 'Открыть полный отчёт',
      analysis_quick_title: 'Все данные сейчас',
      analysis_quick_not_ready: 'Данные появятся после подключения кошелька.',
      analysis_quick_refreshing: 'Обновляем данные…',
      analysis_quick_updated: 'Обновлено {value}',
      analysis_quick_wallets: 'Кошельки',
      analysis_quick_transactions: 'Транзакции',
      analysis_mini_portfolio_label: 'Портфель',
      analysis_mini_growth_label: '7д',
      analysis_detail_menu_aria: 'Детальная аналитика кошелька',
      analysis_detail_title: 'Аналитика кошелька',
      analysis_detail_subtitle: 'Полная детализация активного кошелька, структуры и инсайтов.',
      analysis_detail_wallet_title: 'Активный кошелёк',
      analysis_detail_wallet_balance: 'Баланс',
      analysis_detail_inflow_30d: 'Приток (30д)',
      analysis_detail_outflow_30d: 'Отток (30д)',
      analysis_detail_wallet_missing: 'Подключите кошелёк, чтобы открыть полный анализ.',
      analysis_detail_metrics_title: 'Ключевые метрики',
      analysis_detail_series_title: 'Аналитика портфеля',
      analysis_detail_series_live: 'Ряд данных ({value})',
      analysis_detail_series_estimated: 'Оценочный ряд ({value})',
      analysis_detail_allocation_title: 'Структура портфеля',
      analysis_detail_allocation_empty: 'Пока нет структуры по активам.',
      analysis_detail_insights_title: 'Инсайты',
      analysis_detail_insights_empty: 'Инсайты появятся после сбора операций.',
      analysis_insights_need_data: 'Нужно минимум 10 транзакций для построения инсайтов.',
      analysis_insights_step_connect: 'Подключите кошелёк',
      analysis_insights_step_import: 'Импортируйте историю',
      analysis_insights_step_wait: 'Дождитесь синхронизации',
      analysis_col_asset: 'Актив',
      analysis_col_amount: 'Кол-во',
      analysis_col_value: 'Стоимость',
      analysis_col_share: 'Доля',
      analysis_detail_updated: 'Обновлено {value}',
      analysis_detail_series_meta: '{metric} · {value}',
      analysis_confidence_label: 'Точность',
      analysis_next_label: 'След.',
      analysis_insight_fallback: 'Инсайт',
      no_meaningful_change: 'Нет значимых изменений',
      no_meaningful_change_window: 'Нет значимых изменений ({value})',
      get_started_title: 'С чего начать',
      get_started_subtitle: 'Сделайте один шаг, чтобы открыть аналитику.',
      get_started_connect: 'Подключить счёт',
      get_started_add_tx: 'Добавить транзакцию',
      get_started_import: 'Импортировать историю'
    },
    en: {
      dashboard_page_title: 'FinGuard | Dashboard',
      dashboard_title: 'Dashboard',
      logout: 'Log out',
      refresh: 'Refresh',
      base_currency_button: 'Currency: {value}',
      balance: 'Balance',
      credit: 'Credit',
      credit_placeholder: 'Credit: —',
      income_expense_month: 'Income / Expense (mo.)',
      income_month: 'Income (month)',
      spend_month: 'Spend (month)',
      income_30d: 'Income (30d)',
      spend_30d: 'Spend (30d)',
      cashflow_30d: 'Cashflow (30d)',
      debt_label: 'Debt',
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
      payment_name: 'Payment',
      payment_due_prefix: 'Due',
      net_worth_title: 'Net worth',
      demo: 'Demo',
      balance_trend_title: 'Balance trend',
      last_6_months: 'Last 6 months,',
      trend_label: 'Trend:',
      balance_chart_aria: 'Balance chart',
      expense_breakdown_title: 'Expense breakdown',
      current_month: 'Current month,',
      expense_chart_aria: 'Expense chart',
      expense_empty_title: 'No data for the current month',
      markets_badge: 'Markets',
      crypto_badge: 'Crypto',
      fx_badge: 'FX',
      fx_radar_title: 'FX Radar',
      markets_title: 'Markets',
      markets_subtitle: 'Crypto & FX summary',
      coin_rates: 'Coin rates',
      loading: 'Loading…',
      updating: 'Updating…',
      demo_data: 'Estimated data',
      no_data: 'No data',
      updated: 'Updated',
      updated_at: 'Updated {value}',
      source_pending: 'Waiting for data',
      source_live: 'Live',
      source_demo: 'Estimated',
      source_synthetic: 'Estimated',
      source_hybrid: 'Live',
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
      recent_transactions_title: 'Transactions',
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
      tx_col_date: 'Date',
      tx_col_merchant: 'Merchant',
      tx_col_category: 'Category',
      tx_col_amount: 'Amount',
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
      wallets_show_all: 'Show all ({value})',
      wallets_show_less: 'Show less',
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
      copy: 'Copy',
      open_explorer: 'Explorer',
      back_to_dashboard: 'Back',
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
      cta_connect_account: 'Connect account',
      cta_retry: 'Retry',
      period_7d: 'in 7d',
      period_7d_compact: '• 7d',
      period_30d_short: '30d',
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
      analysis_banner_badge: 'Wallet intelligence',
      analysis_open_link: 'Wallet intelligence →',
      analysis_banner_title_idle: 'Connect wallet',
      analysis_banner_subtitle_idle: 'Analysis appears automatically after connection.',
      analysis_banner_title_running: 'Syncing {name}',
      analysis_banner_subtitle_running: 'Collecting wallet activity.',
      analysis_banner_title_partial: 'Partial data ready',
      analysis_banner_subtitle_partial: 'Open Wallet intelligence for full details.',
      analysis_banner_title_done: 'Wallet intelligence updated',
      analysis_banner_subtitle_done: 'All metrics are synced.',
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
      analysis_recurring_estimated: 'Estimated until analysis is complete',
      analysis_recurring_meta_live: 'Next: {value} · {confidence}%',
      analysis_recurring_meta_estimated: 'Estimate · {confidence}%',
      analysis_quick_open_hint: 'Open full report',
      analysis_quick_title: 'All data now',
      analysis_quick_not_ready: 'Connect a wallet to see all data.',
      analysis_quick_refreshing: 'Refreshing data…',
      analysis_quick_updated: 'Updated {value}',
      analysis_quick_wallets: 'Wallets',
      analysis_quick_transactions: 'Transactions',
      analysis_mini_portfolio_label: 'Portfolio',
      analysis_mini_growth_label: '7d',
      analysis_detail_menu_aria: 'Wallet analysis details',
      analysis_detail_title: 'Wallet intelligence',
      analysis_detail_subtitle: 'Deep breakdown of active wallet metrics, allocation, and insights.',
      analysis_detail_wallet_title: 'Active wallet',
      analysis_detail_wallet_balance: 'Balance',
      analysis_detail_inflow_30d: 'Inflow (30d)',
      analysis_detail_outflow_30d: 'Outflow (30d)',
      analysis_detail_wallet_missing: 'Connect a wallet to open full analysis.',
      analysis_detail_metrics_title: 'Key metrics',
      analysis_detail_series_title: 'Portfolio analytics',
      analysis_detail_series_live: 'Live series ({value})',
      analysis_detail_series_estimated: 'Estimated series ({value})',
      analysis_detail_allocation_title: 'Portfolio allocation',
      analysis_detail_allocation_empty: 'No allocation data yet.',
      analysis_detail_insights_title: 'Insights',
      analysis_detail_insights_empty: 'Insights appear after activity is processed.',
      analysis_insights_need_data: 'We need at least 10 transactions to generate insights.',
      analysis_insights_step_connect: 'Connect account',
      analysis_insights_step_import: 'Import history',
      analysis_insights_step_wait: 'Wait for sync',
      analysis_col_asset: 'Asset',
      analysis_col_amount: 'Amount',
      analysis_col_value: 'Value',
      analysis_col_share: 'Share',
      analysis_detail_updated: 'Updated {value}',
      analysis_detail_series_meta: '{metric} · {value}',
      analysis_confidence_label: 'Confidence',
      analysis_next_label: 'Next',
      analysis_insight_fallback: 'Insight',
      no_meaningful_change: 'No meaningful change',
      no_meaningful_change_window: 'No meaningful change ({value})',
      get_started_title: 'Get started',
      get_started_subtitle: 'Complete one step to unlock analytics.',
      get_started_connect: 'Connect account',
      get_started_add_tx: 'Add transaction',
      get_started_import: 'Import history'
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

  const cryptoAssets = [
    { code: 'BTC', name: 'Bitcoin', priceSelector: selectors.btcPrice, sparkSelector: selectors.btcSpark, color: '#f7931a' },
    { code: 'ETH', name: 'Ethereum', priceSelector: selectors.ethPrice, sparkSelector: selectors.ethSpark, color: '#4f8bff' },
    { code: 'SOL', name: 'Solana', priceSelector: selectors.solPrice, sparkSelector: selectors.solSpark, color: '#10b981' }
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
  let lastReportSummary = null;
  let reportSummaryLoaded = false;
  let reportSummaryConfirmed = false;
  const txListLimit = 20;
  let dashboardOverview = null;
  let lastDashboardUpcoming = [];
  let balanceTrendWindow = '30d';
  let balanceTrendMetric = 'net';
  let balanceTrendCache = null;
  let txCategories = [];
  let txCategoriesById = new Map();
  const DATA_SOURCE = {
    pending: 'pending',
    live: 'live',
    hybrid: 'hybrid'
  };
  const dataSourceState = {
    crypto: DATA_SOURCE.pending,
    fx: DATA_SOURCE.pending
  };
  const reducedMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
  const analysisPollIntervalMs = 2000;
  const INSIGHT_CONFIDENCE_THRESHOLD = 0.55;
  const INSIGHT_MIN_ABS_VALUE = 0.000001;
  const CHART_VARIANCE_THRESHOLD = 0.015;
  const DETAIL_CHART_VARIANCE_THRESHOLD = 0.022;
  const CHART_MIN_POINTS = 4;
  const analysisState = {
    activeWalletId: null,
    activeWalletName: '',
    activeWallet: null,
    status: null,
    pollTimer: 0,
    inFlight: false,
    summaryTotal: NaN,
    summaryBase: 'USD',
    pollError: false,
    apiSummary: null,
    apiInsights: null,
    apiSeries: null,
    seriesWindow: '30d',
    detailMetric: 'net',
    detailWindow: '30d',
    walletsCount: 0,
    detailOpen: false,
    detailBusy: false,
    lastDataFetchAt: 0
  };
  let recentTransactionsCache = [];
  const dashboardDataState = {
    accountsLoaded: false,
    walletsLoaded: false,
    transactionsLoaded: false,
    hasAccounts: false,
    hasWallets: false,
    hasTransactions: false
  };
  let walletListExpanded = false;

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

  function renderEmptyState(message, actionLabel, action, secondaryLabel, secondaryAction) {
    const safeMsg = escapeHtml(message || '');
    const safeAction = escapeHtml(action || '');
    const safeSecondaryAction = escapeHtml(secondaryAction || '');
    const hasPrimary = Boolean(actionLabel && action);
    const hasSecondary = Boolean(secondaryLabel && secondaryAction);
    if (!hasPrimary && !hasSecondary) {
      return `<div class="empty-block muted">${safeMsg}</div>`;
    }
    const primary = hasPrimary
      ? `<button type="button" class="ghost inline-cta inline-cta-primary" data-action="${safeAction}">${escapeHtml(actionLabel)}</button>`
      : '';
    const secondary = hasSecondary
      ? `<button type="button" class="ghost inline-cta inline-cta-secondary" data-action="${safeSecondaryAction}">${escapeHtml(secondaryLabel)}</button>`
      : '';
    return `
      <div class="empty-block">
        <div class="muted">${safeMsg}</div>
        <div class="empty-state-actions">
          ${primary}
          ${secondary}
        </div>
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

  function updateGetStartedSection() {
    const section = q(selectors.getStartedSection);
    if (!section) return;
    const hasAnyLoaded = dashboardDataState.accountsLoaded || dashboardDataState.walletsLoaded || dashboardDataState.transactionsLoaded;
    if (!hasAnyLoaded) {
      section.hidden = true;
      updateActionPriority();
      return;
    }
    const missingAccount = !dashboardDataState.hasAccounts;
    const missingTransactions = !dashboardDataState.hasTransactions;
    const missingWallet = !dashboardDataState.hasWallets;
    const show = missingAccount || missingTransactions || missingWallet;
    section.hidden = !show;
    section.querySelectorAll('[data-action]').forEach((button) => {
      const action = button.dataset.action || '';
      if (action === 'open-add-account') {
        button.hidden = !missingAccount;
      } else if (action === 'open-add-transaction') {
        button.hidden = !missingTransactions;
      } else if (action === 'open-import-history') {
        button.hidden = !(missingTransactions || missingWallet);
      }
    });
    updateActionPriority();
  }

  function updateActionPriority() {
    const onboardingSection = q(selectors.getStartedSection);
    const hasAnyLoaded = dashboardDataState.accountsLoaded || dashboardDataState.walletsLoaded || dashboardDataState.transactionsLoaded;
    const onboardingVisible = !hasAnyLoaded || Boolean(onboardingSection && !onboardingSection.hidden);
    const txBtn = q(selectors.addTransactionBtn);
    const accountBtn = q(selectors.addAccountBtn);
    const walletBtn = q(selectors.addWalletBtn);
    const upcomingBtn = q('#btn-upcoming-add');

    // Keep one clear primary action: onboarding when data is missing, transaction action otherwise.
    if (txBtn) txBtn.hidden = onboardingVisible;
    if (accountBtn) accountBtn.hidden = false;
    if (walletBtn) walletBtn.hidden = false;
    if (upcomingBtn) upcomingBtn.hidden = true;
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
    if (crypto === DATA_SOURCE.live && fx === DATA_SOURCE.live) {
      combined = DATA_SOURCE.live;
    } else if (crypto === DATA_SOURCE.live || fx === DATA_SOURCE.live || fx === DATA_SOURCE.hybrid) {
      combined = DATA_SOURCE.hybrid;
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
    const openActionMenu = (selector) => {
      const trigger = q(selector);
      if (!trigger) return;
      if (analysisState.detailOpen) {
        const opener = q(selectors.analysisQuickCard);
        Promise.resolve(setAnalysisDetailOpen(false, opener || trigger))
          .then(() => {
            trigger.click();
          });
        return;
      }
      trigger.click();
    };

    document.addEventListener('click', (event) => {
      const target = event.target instanceof HTMLElement ? event.target.closest('[data-action]') : null;
      if (!(target instanceof HTMLElement)) return;
      const action = target.dataset.action;
      if (!action) return;
      if (action === 'open-add-account') {
        openActionMenu(selectors.addAccountBtn);
      } else if (action === 'open-add-wallet') {
        openActionMenu(selectors.addWalletBtn);
      } else if (action === 'open-add-transaction') {
        openActionMenu(selectors.addTransactionBtn);
      } else if (action === 'open-import-history') {
        if (dashboardDataState.hasAccounts) {
          openActionMenu(selectors.addTransactionBtn);
        } else {
          openActionMenu(selectors.addAccountBtn);
        }
      } else if (action === 'retry-balance') {
        loadBalance();
      } else if (action === 'retry-wallets') {
        loadWallets();
      } else if (action === 'retry-transactions') {
        loadRecentTransactions();
      } else if (action === 'retry-fx') {
        loadReports();
      }
    });
  }

  function initMotionController() {
    const root = q(selectors.root);
    if (!root) return;
    const applyMotionPreference = () => {
      root.dataset.motionLevel = reducedMotionQuery.matches ? 'reduced' : 'calm';
    };

    applyMotionPreference();

    if (typeof reducedMotionQuery.addEventListener === 'function') {
      reducedMotionQuery.addEventListener('change', applyMotionPreference);
    } else if (typeof reducedMotionQuery.addListener === 'function') {
      reducedMotionQuery.addListener(applyMotionPreference);
    }

    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        root.classList.add('motion-paused');
      } else {
        root.classList.remove('motion-paused');
      }
    });
  }

  function initScrollReveal() {
    const targets = Array.from(document.querySelectorAll('.dashboard [data-motion="reveal"]'));
    if (!targets.length) return;
    targets.forEach((el) => {
      el.style.removeProperty('--reveal-delay');
      el.classList.add('in-view');
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

  function bindRefresh() {
    const btn = document.querySelector(selectors.refreshBtn);
    if (!btn) return;
    if (btn.dataset.bound === '1') return;
    btn.dataset.bound = '1';
    btn.addEventListener('click', async () => {
      if (btn.disabled) return;
      btn.disabled = true;
      try {
        await loadDashboardOverview();
        await Promise.all([
          loadBalance(),
          loadWallets(),
          loadRecentTransactions(),
          loadReports(),
          loadUpcomingPayments()
        ]);
        if (analysisState.detailOpen) {
          await refreshAllDataForAnalysisDetail();
          renderAnalysisDetail(null);
        }
      } finally {
        btn.disabled = false;
      }
    });
  }

  function bindTopNavState() {
    const root = q(selectors.root);
    const nav = q('[data-testid="top-nav"]');
    if (!root || !nav) return;
    if (nav.dataset.scrollBound === '1') return;
    nav.dataset.scrollBound = '1';

    let ticking = false;
    const sync = () => {
      const condensed = window.scrollY > 28;
      root.classList.toggle('nav-condensed', condensed);
      const state = condensed ? 'compact' : 'top';
      nav.dataset.navState = state;
      root.dataset.navState = state;
    };
    sync();

    window.addEventListener('scroll', () => {
      if (ticking) return;
      ticking = true;
      window.requestAnimationFrame(() => {
        sync();
        ticking = false;
      });
    }, { passive: true });
  }

  const numberFormatterCache = new Map();
  const dateTimeFormatterCache = new Map();

  function numberFormatter(minimumFractionDigits, maximumFractionDigits) {
    const locale = getLocale();
    const key = `${locale}|${minimumFractionDigits}|${maximumFractionDigits}`;
    const cached = numberFormatterCache.get(key);
    if (cached) return cached;
    const formatter = new Intl.NumberFormat(locale, {
      minimumFractionDigits,
      maximumFractionDigits
    });
    numberFormatterCache.set(key, formatter);
    return formatter;
  }

  function formatDecimalIntl(value, minimumFractionDigits, maximumFractionDigits) {
    const numeric = toNumber(value);
    if (!Number.isFinite(numeric)) return '—';
    return numberFormatter(minimumFractionDigits, maximumFractionDigits).format(numeric);
  }

  function formatDateTimeIntl(value, options) {
    if (!value) return '';
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const locale = getLocale();
    const key = `${locale}|${JSON.stringify(options || {})}`;
    let formatter = dateTimeFormatterCache.get(key);
    if (!formatter) {
      formatter = new Intl.DateTimeFormat(locale, options || {});
      dateTimeFormatterCache.set(key, formatter);
    }
    return formatter.format(date);
  }

  function moneyFractionDigits(currency) {
    const cur = (currency || '').trim().toUpperCase();
    if (cur === 'BTC' || cur === 'ETH') return 8;
    return 2;
  }

  function formatMoney(value, currency) {
    const cur = currency || baseCurrency || 'USD';
    const numeric = toNumber(value);
    if (!Number.isFinite(numeric)) return '—';
    const abs = Math.abs(numeric);
    const sign = numeric < 0 ? '-' : '';
    const digits = moneyFractionDigits(cur);
    return `${sign}${formatDecimalIntl(abs, digits, digits)} ${cur}`;
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
    return formatDecimalIntl(value, digits, digits);
  }

  function formatFxUpdated(isoValue) {
    return formatDateTimeIntl(isoValue, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
  }

  function formatShortDay(isoValue) {
    return formatDateTimeIntl(isoValue, { day: '2-digit', month: 'short' });
  }

  function seriesVarianceRatio(values) {
    if (!Array.isArray(values) || values.length < 2) return 0;
    const finite = values.map((value) => toNumber(value)).filter((value) => Number.isFinite(value));
    if (finite.length < 2) return 0;
    const max = Math.max(...finite);
    const min = Math.min(...finite);
    const span = Math.abs(max - min);
    const avg = finite.reduce((sum, value) => sum + value, 0) / finite.length;
    const baseline = Math.max(Math.abs(avg), Math.abs(finite[0]), Math.abs(finite[finite.length - 1]), 1);
    return span / baseline;
  }

  function buildCompactSparkline(values, width, height, stroke) {
    const finite = values.map((value) => toNumber(value)).filter((value) => Number.isFinite(value));
    if (finite.length < 2) return '';
    const max = Math.max(...finite);
    const min = Math.min(...finite);
    const span = max - min || 1;
    const pad = 4;
    const points = finite.map((value, idx) => {
      const x = pad + ((width - (pad * 2)) * idx) / Math.max(finite.length - 1, 1);
      const y = height - pad - ((value - min) / span) * (height - (pad * 2));
      return `${x},${y}`;
    }).join(' ');
    return `
      <svg viewBox="0 0 ${width} ${height}" class="compact-sparkline" preserveAspectRatio="none" aria-hidden="true">
        <polyline points="${points}" fill="none" stroke="${stroke}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></polyline>
      </svg>
    `;
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
    const etaRaw = toNumber(payload && payload.etaSeconds);
    return {
      status,
      progressPct: Number.isFinite(progressRaw) ? Math.max(0, Math.min(100, Math.round(progressRaw))) : 0,
      stage: String(payload && payload.stage ? payload.stage : 'FETCH_TX').toUpperCase(),
      startedAt: payload && payload.startedAt ? String(payload.startedAt) : '',
      updatedAt: payload && payload.updatedAt ? String(payload.updatedAt) : '',
      finishedAt: payload && payload.finishedAt ? String(payload.finishedAt) : '',
      partialReady: Boolean(payload && payload.partialReady),
      etaSeconds: Number.isFinite(etaRaw) ? Math.max(0, Math.round(etaRaw)) : null,
      lastSuccessfulStage: String(payload && payload.lastSuccessfulStage ? payload.lastSuccessfulStage : '').toUpperCase()
    };
  }

  function normalizeAnalysisSummary(payload) {
    if (!payload || typeof payload !== 'object') return null;
    const allocation = Array.isArray(payload.allocation) ? payload.allocation : [];
    return {
      totalValueInBase: toNumber(payload.totalValueInBase),
      baseCurrency: normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD',
      delta24hPct: toNumber(payload.delta24hPct),
      delta7dPct: toNumber(payload.delta7dPct),
      allocation: allocation.map((item) => ({
        code: item && item.code ? String(item.code) : '',
        valueInBase: toNumber(item && item.valueInBase),
        sharePct: toNumber(item && item.sharePct)
      })),
      inflow30d: toNumber(payload.inflow30d),
      outflow30d: toNumber(payload.outflow30d),
      metricsSource: String(payload.metricsSource || (payload.synthetic ? 'ESTIMATED' : 'LIVE')).toUpperCase(),
      hasMeaningfulData: Boolean(payload.hasMeaningfulData),
      synthetic: Boolean(payload.synthetic),
      asOf: payload.asOf ? String(payload.asOf) : ''
    };
  }

  function normalizeAnalysisInsights(payload) {
    if (!payload || typeof payload !== 'object') return null;
    const insights = Array.isArray(payload.insights) ? payload.insights : [];
    return {
      baseCurrency: normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD',
      asOf: payload.asOf ? String(payload.asOf) : '',
      insights: insights.map((item) => ({
        type: String(item && item.type ? item.type : ''),
        title: String(item && item.title ? item.title : ''),
        value: toNumber(item && item.value),
        unit: String(item && item.unit ? item.unit : ''),
        currency: item && item.currency ? String(item.currency) : '',
        label: item && item.label ? String(item.label) : '',
        avgAmount: toNumber(item && item.avgAmount),
        nextEstimatedChargeAt: item && item.nextEstimatedChargeAt ? String(item.nextEstimatedChargeAt) : '',
        confidence: toNumber(item && item.confidence),
        synthetic: Boolean(item && item.synthetic)
      }))
    };
  }

  function normalizeAnalysisSeries(payload) {
    if (!payload || typeof payload !== 'object') return null;
    const points = Array.isArray(payload.points) ? payload.points : [];
    return {
      baseCurrency: normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD',
      window: String(payload.window || '30d'),
      synthetic: Boolean(payload.synthetic),
      asOf: payload.asOf ? String(payload.asOf) : '',
      points: points.map((item) => ({
        at: item && item.at ? String(item.at) : '',
        valueInBase: toNumber(item && item.valueInBase)
      })).filter((item) => item.at && Number.isFinite(item.valueInBase))
    };
  }

  function normalizeOverview(payload) {
    if (!payload || typeof payload !== 'object') return null;
    const hero = payload.hero && typeof payload.hero === 'object' ? payload.hero : {};
    const stats = payload.stats && typeof payload.stats === 'object' ? payload.stats : {};
    const getStarted = payload.getStarted && typeof payload.getStarted === 'object' ? payload.getStarted : {};
    const walletIntelligence = payload.walletIntelligence && typeof payload.walletIntelligence === 'object'
      ? payload.walletIntelligence
      : {};
    const upcoming = Array.isArray(payload.upcomingPaymentsPreview) ? payload.upcomingPaymentsPreview : [];
    const wallets = Array.isArray(payload.walletsPreview) ? payload.walletsPreview : [];
    const transactions = Array.isArray(payload.transactionsPreview) ? payload.transactionsPreview : [];
    return {
      asOf: payload.asOf ? String(payload.asOf) : '',
      dataFreshness: String(payload.dataFreshness || 'ESTIMATED').toUpperCase(),
      hero: {
        netWorth: toNumber(hero.netWorth),
        baseCurrency: normalizeCurrency(hero.baseCurrency || baseCurrency) || 'USD',
        delta7dPct: toNumber(hero.delta7dPct),
        updatedAt: hero.updatedAt ? String(hero.updatedAt) : '',
        hasMeaningfulData: Boolean(hero.hasMeaningfulData)
      },
      stats: {
        income30d: toNumber(stats.income30d),
        spend30d: toNumber(stats.spend30d),
        cashflow30d: toNumber(stats.cashflow30d),
        debt: toNumber(stats.debt),
        hasMeaningfulData: Boolean(stats.hasMeaningfulData)
      },
      getStarted: {
        visible: Boolean(getStarted.visible),
        connectAccount: Boolean(getStarted.connectAccount),
        addTransaction: Boolean(getStarted.addTransaction),
        importHistory: Boolean(getStarted.importHistory)
      },
      transactionsPreview: transactions,
      walletsPreview: wallets,
      upcomingPaymentsPreview: upcoming.map((item) => ({
        id: String(item && item.id ? item.id : ''),
        title: String(item && item.title ? item.title : ''),
        amount: toNumber(item && item.amount),
        currency: normalizeCurrency(item && item.currency ? item.currency : baseCurrency) || 'USD',
        dueAt: item && item.dueAt ? String(item.dueAt) : '',
        confidence: toNumber(item && item.confidence),
        source: String(item && item.source ? item.source : '')
      })),
      walletIntelligence: {
        activeWalletId: toNumber(walletIntelligence.activeWalletId),
        status: String(walletIntelligence.status || ''),
        progressPct: toNumber(walletIntelligence.progressPct),
        partialReady: Boolean(walletIntelligence.partialReady),
        updatedAt: walletIntelligence.updatedAt ? String(walletIntelligence.updatedAt) : '',
        source: String(walletIntelligence.source || ''),
        etaSeconds: toNumber(walletIntelligence.etaSeconds),
        lastSuccessfulStage: String(walletIntelligence.lastSuccessfulStage || '')
      }
    };
  }

  function hasMeaningfulNumber(value) {
    const numeric = toNumber(value);
    if (!Number.isFinite(numeric)) return false;
    return Math.abs(numeric) > 0.000001;
  }

  function isEstimatedSource(source) {
    const normalized = String(source || '').toUpperCase();
    return normalized === 'ESTIMATED' || normalized === 'DEMO' || normalized === 'SYNTHETIC';
  }

  function isReliableMetricsSource(source) {
    const normalized = String(source || '').toUpperCase();
    return normalized === 'LIVE' || normalized === 'PARTIAL';
  }

  function formatEtaHint(seconds) {
    const safe = toNumber(seconds);
    if (!Number.isFinite(safe) || safe <= 0) return '';
    const rounded = Math.round(safe);
    if (rounded < 60) {
      return currentLang === 'ru' ? `ETA ~${rounded}с` : `ETA ~${rounded}s`;
    }
    const mins = Math.max(1, Math.round(rounded / 60));
    return currentLang === 'ru' ? `ETA ~${mins}м` : `ETA ~${mins}m`;
  }

  function applyOverviewToHeroAndStats(overview) {
    if (!overview) return;
    const hero = overview.hero || {};
    const stats = overview.stats || {};
    if (hero.baseCurrency) {
      baseCurrency = hero.baseCurrency;
      updateCurrencyLabels();
    }
    const totalBalanceEl = q(selectors.totalBalance);
    if (totalBalanceEl) {
      const showNetWorth = hasMeaningfulNumber(hero.netWorth) || Boolean(hero.hasMeaningfulData);
      totalBalanceEl.textContent = showNetWorth ? formatMoney(hero.netWorth, baseCurrency) : '—';
    }
    setHeroChangeValue(hasMeaningfulNumber(hero.delta7dPct) ? hero.delta7dPct : NaN);
    const updated = formatFxUpdated(hero.updatedAt || overview.asOf);
    setText(selectors.analysisUpdatedAt, updated ? t('updated_at', { value: updated }) : t('source_pending'));

    reportSummaryLoaded = true;
    reportSummaryConfirmed = overview.dataFreshness === 'LIVE' || overview.dataFreshness === 'PARTIAL';
    lastReportSummary = {
      baseCurrency,
      income: stats.income30d,
      expense: stats.spend30d,
      net: stats.cashflow30d,
      synthetic: !reportSummaryConfirmed
    };
    renderIncomeExpenseSummary(lastReportSummary);
  }

  function renderUpcomingPayments(items) {
    const section = q(selectors.upcomingSection);
    const list = q(selectors.upcomingPaymentsList);
    if (!section || !list) return;
    const safeItems = Array.isArray(items)
      ? items.filter((item) => {
        if (!item || !hasMeaningfulNumber(item.amount)) return false;
        return !isEstimatedSource(item.source);
      })
      : [];
    if (!safeItems.length) {
      section.hidden = true;
      list.innerHTML = '';
      return;
    }
    section.hidden = false;
    setUiState(list, 'ready');
    list.innerHTML = safeItems.slice(0, 5).map((item) => {
      const dueAt = formatShortDay(item.dueAt);
      const title = escapeHtml(item.title || t('payment_name'));
      const amount = formatMoney(item.amount, normalizeCurrency(item.currency) || baseCurrency);
      const isOverdue = Boolean(item.dueAt && new Date(item.dueAt).getTime() < Date.now());
      const isNegative = toNumber(item.amount) < 0;
      return `
        <div class="list-item payment-row">
          <div class="payment-name">${title}</div>
          <small class="payment-due">${dueAt ? `${t('payment_due_prefix')}: ${escapeHtml(dueAt)}` : '—'}</small>
          <div class="payment-amount ${isOverdue || isNegative ? 'amount-negative' : ''}">${escapeHtml(amount)}</div>
        </div>
      `;
    }).join('');
  }

  async function loadUpcomingPayments() {
    const list = q(selectors.upcomingPaymentsList);
    if (list) {
      setUiState(list, 'loading');
      list.innerHTML = renderSkeletonList(3);
    }
    const res = await Api.call('/api/dashboard/upcoming-payments?limit=5', 'GET', null, true);
    if (!res.ok || !Array.isArray(res.data)) {
      renderUpcomingPayments(lastDashboardUpcoming);
      return;
    }
    const items = res.data.map((item) => ({
      id: String(item && item.id ? item.id : ''),
      title: String(item && item.title ? item.title : ''),
      amount: toNumber(item && item.amount),
      currency: normalizeCurrency(item && item.currency ? item.currency : baseCurrency) || 'USD',
      dueAt: item && item.dueAt ? String(item.dueAt) : '',
      confidence: toNumber(item && item.confidence),
      source: String(item && item.source ? item.source : '')
    }));
    lastDashboardUpcoming = items;
    renderUpcomingPayments(items);
  }

  async function loadDashboardOverview() {
    const res = await Api.call('/api/dashboard/overview', 'GET', null, true);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      return null;
    }
    const parsed = normalizeOverview(res.data);
    if (!parsed) return null;
    dashboardOverview = parsed;
    applyOverviewToHeroAndStats(parsed);
    lastDashboardUpcoming = parsed.upcomingPaymentsPreview.slice();
    renderUpcomingPayments(lastDashboardUpcoming);

    dashboardDataState.accountsLoaded = true;
    dashboardDataState.walletsLoaded = true;
    dashboardDataState.transactionsLoaded = true;
    dashboardDataState.hasAccounts = !parsed.getStarted.connectAccount;
    dashboardDataState.hasWallets = parsed.walletsPreview.length > 0;
    dashboardDataState.hasTransactions = !parsed.getStarted.addTransaction;

    if (Number.isFinite(parsed.walletIntelligence.activeWalletId) && parsed.walletIntelligence.activeWalletId > 0) {
      analysisState.activeWalletId = parsed.walletIntelligence.activeWalletId;
      analysisState.status = normalizeAnalysisStatus({
        status: parsed.walletIntelligence.status || 'QUEUED',
        progressPct: parsed.walletIntelligence.progressPct,
        stage: parsed.walletIntelligence.lastSuccessfulStage || 'FETCH_TX',
        partialReady: parsed.walletIntelligence.partialReady,
        updatedAt: parsed.walletIntelligence.updatedAt,
        etaSeconds: parsed.walletIntelligence.etaSeconds,
        lastSuccessfulStage: parsed.walletIntelligence.lastSuccessfulStage
      });
    }
    updateGetStartedSection();
    refreshAnalysisPanel();
    return parsed;
  }

  function findAnalysisInsight(type) {
    if (!analysisState.apiInsights || !Array.isArray(analysisState.apiInsights.insights)) {
      return null;
    }
    const normalized = String(type || '').toUpperCase();
    return analysisState.apiInsights.insights.find((item) => String(item.type || '').toUpperCase() === normalized) || null;
  }

  function normalizeSeriesWindow(windowValue) {
    const value = String(windowValue || '').toLowerCase();
    if (value === '7d') return '7d';
    if (value === '30d') return '30d';
    if (value === '90d') return '90d';
    if (value === '1y') return '1y';
    return '30d';
  }

  function apiSeriesWindow(windowValue) {
    return normalizeSeriesWindow(windowValue);
  }

  async function fetchAnalysisData(walletId) {
    if (!walletId) return;
    const seriesWindow = apiSeriesWindow(analysisState.seriesWindow || analysisState.detailWindow || '30d');
    const [summaryRes, insightsRes, seriesRes] = await Promise.all([
      Api.call(`/api/crypto/wallets/${encodeURIComponent(walletId)}/analysis/summary`, 'GET', null, true),
      Api.call(`/api/crypto/wallets/${encodeURIComponent(walletId)}/analysis/insights`, 'GET', null, true),
      Api.call(`/api/crypto/wallets/${encodeURIComponent(walletId)}/analysis/series?window=${encodeURIComponent(seriesWindow)}`, 'GET', null, true)
    ]);
    if (walletId !== analysisState.activeWalletId) {
      return;
    }
    if (summaryRes.ok && summaryRes.data && typeof summaryRes.data === 'object') {
      const parsed = normalizeAnalysisSummary(summaryRes.data);
      if (parsed) {
        analysisState.apiSummary = parsed;
      }
    }
    if (insightsRes.ok && insightsRes.data && typeof insightsRes.data === 'object') {
      const parsed = normalizeAnalysisInsights(insightsRes.data);
      if (parsed) {
        analysisState.apiInsights = parsed;
      }
    }
    if (seriesRes.ok && seriesRes.data && typeof seriesRes.data === 'object') {
      const parsed = normalizeAnalysisSeries(seriesRes.data);
      if (parsed) {
        analysisState.apiSeries = parsed;
      }
    }
    analysisState.lastDataFetchAt = Date.now();
  }

  async function maybeRefreshAnalysisData(force) {
    if (!analysisState.activeWalletId) return;
    const now = Date.now();
    const stale = now - Number(analysisState.lastDataFetchAt || 0) > 8000;
    if (!force && !stale) return;
    try {
      await fetchAnalysisData(analysisState.activeWalletId);
      refreshAnalysisPanel();
    } catch (_) {
      refreshAnalysisPanel();
    }
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
      const statusName = String(analysisState.status.status || '').toUpperCase();
      if (analysisState.status.partialReady || statusName === 'DONE' || statusName === 'PARTIAL') {
        await maybeRefreshAnalysisData(true);
      } else {
        await maybeRefreshAnalysisData(false);
      }
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

  function setHeroChangeValue(value) {
    const el = q(selectors.analysisHeroChange);
    if (!el) return;
    el.classList.remove('is-positive', 'is-negative');
    if (!Number.isFinite(value)) {
      el.textContent = '—';
      return;
    }
    el.textContent = formatSignedPct(value);
    if (value > 0) el.classList.add('is-positive');
    if (value < 0) el.classList.add('is-negative');
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

  function renderAnalysisGrowthSpark(series) {
    const sparkEl = q(selectors.analysisGrowthSpark);
    if (!sparkEl) return;
    if (!Array.isArray(series) || series.length < 2) {
      sparkEl.innerHTML = '';
      return;
    }
    renderSparkline(sparkEl, series, '#7cc4ff');
  }

  function updateAnalysisCardsVisibility() {
    const container = q('#analysisCards');
    if (!container) return;
    const cards = [
      ['#analysisCardIncome', selectors.analysisIncomeValue],
      ['#analysisCardSpend', selectors.analysisExpenseValue],
      ['#analysisCardOutflow', selectors.analysisOutflowValue],
      ['#analysisCardRecurring', selectors.analysisRecurringValue]
    ];
    let visible = 0;
    cards.forEach(([cardSelector, valueSelector]) => {
      const card = q(cardSelector);
      const valueEl = q(valueSelector);
      if (!card || !valueEl) return;
      const text = String(valueEl.textContent || '').trim();
      const show = Boolean(text) && text !== '—';
      card.hidden = !show;
      card.classList.toggle('is-quiet', !show);
      if (show) visible += 1;
    });
    container.classList.toggle('is-collapsed', visible === 0);
    container.style.setProperty('--analysis-stats-count', String(Math.max(visible, 1)));
    const hero = q('#walletAnalysisPanel');
    if (hero) hero.classList.toggle('hero-compact', visible === 0);
  }

  function setAnalysisDetailOpen(open, opener) {
    const overlay = q(selectors.analysisDetailOverlay);
    const menu = q(selectors.analysisDetailMenu);
    const card = q(selectors.analysisQuickCard);
    if (!overlay || !menu || !card) return;
    const next = Boolean(open);
    if (next) {
      openOverlay(overlay, menu, opener || card);
      q(selectors.analysisDetailCloseBtn)?.focus();
    } else {
      closeOverlay(overlay, menu, opener || card);
      const focusTarget = opener || card;
      let focusRetries = 0;
      const restoreFocus = () => {
        focusRetries += 1;
        if (focusTarget && typeof focusTarget.focus === 'function' && !focusTarget.disabled) {
          focusTarget.focus();
          return;
        }
        if (focusRetries < 6) {
          window.setTimeout(restoreFocus, 40);
        }
      };
      window.setTimeout(restoreFocus, 0);
    }
    menu.style.removeProperty('--sheet-drag-offset');
    menu.style.removeProperty('transition');
    analysisState.detailOpen = next;
    document.body.classList.toggle('analysis-drawer-open', next);
    card.setAttribute('aria-expanded', next ? 'true' : 'false');
    card.classList.toggle('is-open', next);
    if (next) {
      return Promise.resolve();
    }
    return new Promise((resolve) => {
      window.setTimeout(resolve, 220);
    });
  }

  function formatInsightValue(item, base) {
    const unit = String(item && item.unit ? item.unit : '').toUpperCase();
    const value = toNumber(item && item.value);
    if (!Number.isFinite(value)) return '—';
    if (unit === 'PERCENT') return formatSignedPct(value);
    if (unit === 'BASE_CURRENCY') {
      const currency = normalizeCurrency(item && item.currency ? item.currency : base) || base || 'USD';
      return formatMoney(value, currency);
    }
    if (unit === 'COUNT') {
      return Math.max(0, Math.round(value)).toLocaleString(getLocale());
    }
    return value.toLocaleString(getLocale(), { maximumFractionDigits: 4 });
  }

  function insightComparableValue(item) {
    const unit = String(item && item.unit ? item.unit : '').toUpperCase();
    const value = toNumber(item && item.value);
    if (unit === 'COUNT') return Number.isFinite(value) ? Math.round(value) : NaN;
    if (Number.isFinite(value)) return value;
    return toNumber(item && item.avgAmount);
  }

  function isMeaningfulInsight(item) {
    if (!item || typeof item !== 'object') return false;
    const confidence = toNumber(item.confidence);
    if (Number.isFinite(confidence) && confidence < INSIGHT_CONFIDENCE_THRESHOLD) {
      return false;
    }
    const comparable = insightComparableValue(item);
    if (!Number.isFinite(comparable)) return false;
    const unit = String(item.unit || '').toUpperCase();
    if (unit === 'COUNT') return comparable > 0;
    return Math.abs(comparable) > INSIGHT_MIN_ABS_VALUE;
  }

  function hasConfirmedTransactionMetrics(summary) {
    if (!summary || typeof summary !== 'object') return false;
    if (!reportSummaryLoaded || !reportSummaryConfirmed) return false;
    if (!dashboardDataState.transactionsLoaded || !dashboardDataState.hasTransactions) return false;
    if (summary.synthetic === true) return false;
    return true;
  }

  function localizedWindowLabel(rawValue) {
    const value = String(rawValue || '').toLowerCase();
    if (value === '30d') return t('period_30d_short');
    if (value === '7d') return currentLang === 'ru' ? '7д' : '7d';
    if (value === '90d') return currentLang === 'ru' ? '90д' : '90d';
    if (value === '1y') return currentLang === 'ru' ? '1г' : '1y';
    return value || t('period_30d_short');
  }

  function renderAnalysisDetailSeries(points, base, sourceLabel, windowLabel) {
    const chart = q(selectors.analysisDetailSeriesChart);
    if (!chart) return;
    if (!Array.isArray(points) || points.length < 2) {
      chart.classList.remove('is-compact-chart');
      chart.innerHTML = `<div class="muted">${t('no_data')}</div>`;
      return;
    }
    const values = points.map((point) => toNumber(point && point.valueInBase)).filter((value) => Number.isFinite(value));
    if (values.length < 2) {
      chart.classList.remove('is-compact-chart');
      chart.innerHTML = `<div class="muted">${t('no_data')}</div>`;
      return;
    }
    const width = Math.max(300, Math.round(chart.clientWidth || 860));
    const chartHostHeight = Math.max(260, Math.round(chart.clientHeight || 340));
    const height = Math.max(220, chartHostHeight - 24);
    const padX = 10;
    const padY = 20;
    const max = Math.max(...values);
    const min = Math.min(...values);
    const span = max - min || Math.max(Math.abs(max), 1);
    const minY = padY;
    const maxY = height - padY;
    const coords = values.map((value, idx) => {
      const x = padX + ((width - (padX * 2)) * idx) / Math.max(values.length - 1, 1);
      const yRaw = height - padY - ((value - min) / span) * (height - (padY * 2));
      const y = Math.max(minY, Math.min(maxY, yRaw));
      return { x, y };
    });
    const line = coords.map((point) => `${point.x},${point.y}`).join(' ');
    const area = [
      `${padX},${height - padY}`,
      ...coords.map((point) => `${point.x},${point.y}`),
      `${width - padX},${height - padY}`
    ].join(' ');
    const rising = values[values.length - 1] >= values[0];
    const stroke = rising ? '#6ea7ff' : '#ff9f78';
    const fillId = `analysisSeriesFill${Math.round(values.length * 17)}`;

    chart.classList.remove('is-compact-chart');
    chart.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="xMidYMid meet" class="analysis-detail-series-svg" aria-hidden="true">
        <defs>
          <linearGradient id="${fillId}" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="${stroke}" stop-opacity="0.22" />
            <stop offset="100%" stop-color="${stroke}" stop-opacity="0.02" />
          </linearGradient>
        </defs>
        <polygon points="${area}" fill="url(#${fillId})"></polygon>
        <polyline points="${line}" fill="none" stroke="${stroke}" stroke-width="2.2"></polyline>
      </svg>
      <div class="analysis-detail-series-axis">
        <span>${escapeHtml(t('min'))}: ${escapeHtml(formatMoney(min, base))} · ${escapeHtml(t('max'))}: ${escapeHtml(formatMoney(max, base))}</span>
      </div>
    `;
  }

  function walletExplorerUrl(network, address) {
    const rawAddress = String(address || '').trim();
    if (!rawAddress) return '';
    const normalizedNetwork = String(network || '').toUpperCase();
    if (normalizedNetwork === 'BTC') {
      return `https://www.blockchain.com/explorer/addresses/btc/${encodeURIComponent(rawAddress)}`;
    }
    if (normalizedNetwork === 'ARBITRUM') {
      return `https://arbiscan.io/address/${encodeURIComponent(rawAddress)}`;
    }
    return `https://etherscan.io/address/${encodeURIComponent(rawAddress)}`;
  }

  function detailMetricLabel(metric) {
    const normalized = String(metric || '').toLowerCase();
    if (normalized === 'inflow') return t('income_label');
    if (normalized === 'outflow') return t('expense_label');
    return t('net_worth_title');
  }

  function buildAnalysisDetailSeries(metric) {
    const seriesPayload = analysisState.apiSeries;
    if (!seriesPayload || seriesPayload.synthetic === true) {
      return [];
    }
    const points = analysisState.apiSeries && Array.isArray(analysisState.apiSeries.points)
      ? analysisState.apiSeries.points
      : [];
    if (String(metric || '').toLowerCase() === 'net') {
      return points;
    }
    const normalized = String(metric || '').toLowerCase();
    const values = points.map((item) => toNumber(item && item.valueInBase));
    return points.map((point, idx) => {
      const current = Number.isFinite(values[idx]) ? values[idx] : NaN;
      const prev = idx > 0 && Number.isFinite(values[idx - 1]) ? values[idx - 1] : current;
      const delta = Number.isFinite(current) && Number.isFinite(prev) ? current - prev : NaN;
      if (normalized === 'inflow') {
        return { at: point.at, valueInBase: Number.isFinite(delta) ? Math.max(delta, 0) : NaN };
      }
      return { at: point.at, valueInBase: Number.isFinite(delta) ? Math.max(-delta, 0) : NaN };
    }).filter((item) => item.at && Number.isFinite(item.valueInBase));
  }

  function renderAnalysisDetail(model) {
    const menu = q(selectors.analysisDetailMenu);
    if (!menu) return;
    syncAnalysisDetailTabState();

    const data = model || buildAnalysisInsightsModel();
    const hasWallet = Boolean(analysisState.activeWalletId);
    const wallet = analysisState.activeWallet || null;
    const summaryApi = analysisState.apiSummary || null;
    const base = normalizeCurrency(data && data.base ? data.base : baseCurrency) || 'USD';
    const detailUpdated = q(selectors.analysisDetailUpdated);

    const statusUpdatedAt = analysisState.status && (
      analysisState.status.finishedAt
      || analysisState.status.updatedAt
      || analysisState.status.startedAt
    );
    const fallbackUpdated = (analysisState.apiSummary && analysisState.apiSummary.asOf)
      || (analysisState.apiInsights && analysisState.apiInsights.asOf)
      || (analysisState.apiSeries && analysisState.apiSeries.asOf)
      || '';
    const updated = formatFxUpdated(statusUpdatedAt || fallbackUpdated);

    const detailSource = !hasWallet
      ? DATA_SOURCE.pending
      : (data && data.portfolioLive ? DATA_SOURCE.live : DATA_SOURCE.hybrid);
    setDataSourceBadge(selectors.analysisDetailSource, detailSource);

    if (detailUpdated) {
      const etaHint = formatEtaHint(analysisState.status && analysisState.status.etaSeconds);
      const lastStage = analysisState.status && analysisState.status.lastSuccessfulStage
        ? analysisStageLabel(analysisState.status.lastSuccessfulStage)
        : '';
      if (analysisState.detailBusy) {
        detailUpdated.textContent = t('analysis_quick_refreshing');
      } else if (!hasWallet) {
        detailUpdated.textContent = t('analysis_detail_wallet_missing');
      } else if (updated) {
        const parts = [t('analysis_detail_updated', { value: updated }), etaHint, lastStage].filter(Boolean);
        detailUpdated.textContent = parts.join(' · ');
      } else {
        const parts = [t('source_pending'), etaHint, lastStage].filter(Boolean);
        detailUpdated.textContent = parts.join(' · ');
      }
    }

    const walletName = hasWallet ? (analysisState.activeWalletName || `#${analysisState.activeWalletId}`) : '—';
    setText(selectors.analysisDetailWalletName, walletName);
    setText(selectors.analysisDetailWalletNetwork, hasWallet ? walletNetworkLabel(wallet && wallet.network ? wallet.network : '') : '—');
    setText(selectors.analysisDetailWalletAddress, hasWallet ? (wallet && wallet.address ? String(wallet.address) : '—') : '—');

    const explorer = q(selectors.analysisDetailExplorerLink);
    if (explorer) {
      const href = hasWallet ? walletExplorerUrl(wallet && wallet.network, wallet && wallet.address) : '';
      explorer.href = href || '#';
      explorer.hidden = !href;
    }

    const walletValue = toNumber(wallet && wallet.valueInBase);
    const walletValueBase = normalizeCurrency(wallet && wallet.baseCurrency ? wallet.baseCurrency : base) || base;
    const walletBalance = toNumber(wallet && wallet.balance);
    const walletAsset = walletNativeAsset(wallet && wallet.network ? wallet.network : '');
    const summarySource = summaryApi && summaryApi.metricsSource ? String(summaryApi.metricsSource).toUpperCase() : '';
    const summaryReliable = isReliableMetricsSource(summarySource);
    const inflowValueRaw = summaryReliable ? toNumber(summaryApi && summaryApi.inflow30d) : NaN;
    const outflowValueRaw = summaryReliable ? toNumber(summaryApi && summaryApi.outflow30d) : NaN;
    const inflowValue = Number.isFinite(inflowValueRaw) && Math.abs(inflowValueRaw) > 0.000001 ? inflowValueRaw : NaN;
    const outflowValue = Number.isFinite(outflowValueRaw) && Math.abs(outflowValueRaw) > 0.000001 ? outflowValueRaw : NaN;
    const inflowCard = q(selectors.analysisDetailMetricInflow);
    const outflowCard = q(selectors.analysisDetailMetricOutflow);
    if (inflowCard) inflowCard.hidden = !Number.isFinite(inflowValue);
    if (outflowCard) outflowCard.hidden = !Number.isFinite(outflowValue);

    setText(selectors.analysisDetailWalletValue, Number.isFinite(walletValue) ? formatMoney(walletValue, walletValueBase) : '—');
    setText(selectors.analysisDetailWalletBalance, Number.isFinite(walletBalance) ? formatAssetAmount(walletBalance, walletAsset) : '—');
    setText(selectors.analysisDetailPortfolio, Number.isFinite(data && data.portfolio) ? formatMoney(data.portfolio, base) : '—');
    setText(selectors.analysisDetailGrowth, Number.isFinite(data && data.growth) ? formatSignedPct(data.growth) : '—');
    setText(selectors.analysisDetailRecurring, Number.isFinite(inflowValue) ? formatMoney(inflowValue, base) : '—');
    setText(selectors.analysisDetailOutflow, Number.isFinite(outflowValue) ? formatMoney(-Math.abs(outflowValue), base) : '—');

    const balanceText = String((q(selectors.totalBalance) && q(selectors.totalBalance).textContent) || '—').trim() || '—';
    const netText = String((q(selectors.incomeExpenseNet) && q(selectors.incomeExpenseNet).textContent) || '—').trim() || '—';
    setText(selectors.analysisDetailBalance, balanceText);
    setText(selectors.analysisDetailNet, netText);

    const currentMetric = String(analysisState.detailMetric || 'net').toLowerCase();
    const seriesWindow = normalizeSeriesWindow(analysisState.detailWindow || analysisState.seriesWindow || '30d').toUpperCase();
    const seriesWindowLocalized = localizedWindowLabel(seriesWindow);
    const metricLabel = detailMetricLabel(currentMetric);
    const seriesLabel = updated
      ? t('analysis_detail_series_meta', { metric: metricLabel, value: updated })
      : `${metricLabel} · ${seriesWindowLocalized}`;
    setText(selectors.analysisDetailSeriesMeta, seriesLabel);
    renderAnalysisDetailSeries(buildAnalysisDetailSeries(currentMetric), base, seriesLabel, seriesWindowLocalized);

    const allocationEl = q(selectors.analysisDetailAllocationList);
    if (allocationEl) {
      const allocation = analysisState.apiSummary && Array.isArray(analysisState.apiSummary.allocation)
        ? analysisState.apiSummary.allocation
        : [];
      if (!allocation.length) {
        allocationEl.innerHTML = `<div class="muted">${t('analysis_detail_allocation_empty')}</div>`;
      } else {
        allocationEl.innerHTML = `
          <table class="analysis-detail-table">
            <thead>
              <tr>
                <th>${t('analysis_col_asset')}</th>
                <th>${t('analysis_col_amount')}</th>
                <th>${t('analysis_col_value')}</th>
                <th>${t('analysis_col_share')}</th>
              </tr>
            </thead>
            <tbody>
              ${allocation.map((item, idx) => {
                const code = String(item && item.code ? item.code : '—');
                const value = toNumber(item && item.valueInBase);
                const share = toNumber(item && item.sharePct);
                const valueText = Number.isFinite(value) ? formatMoney(value, base) : '—';
                const shareText = Number.isFinite(share) ? `${share.toFixed(2)}%` : '—';
                const amountRaw = toNumber(item && item.amount);
                const amountText = Number.isFinite(amountRaw)
                  ? formatAssetAmount(amountRaw, code)
                  : (idx === 0 && Number.isFinite(walletBalance) ? formatAssetAmount(walletBalance, walletAsset) : '—');
                return `
                  <tr>
                    <td>${escapeHtml(code)}</td>
                    <td>${escapeHtml(amountText)}</td>
                    <td>${escapeHtml(valueText)}</td>
                    <td>${escapeHtml(shareText)}</td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        `;
      }
    }

    const insightsEl = q(selectors.analysisDetailInsightsList);
    if (insightsEl) {
      const insights = analysisState.apiInsights && Array.isArray(analysisState.apiInsights.insights)
        ? analysisState.apiInsights.insights
        : [];
      const filteredInsights = insights.filter((item) => item && item.synthetic !== true && isMeaningfulInsight(item));
      if (!filteredInsights.length) {
        insightsEl.innerHTML = `
          <div class="analysis-empty-guide compact-empty-state">
            <div class="analysis-empty-title">${t('analysis_detail_insights_empty')}</div>
            <div class="empty-state-actions">
              <button type="button" class="ghost inline-cta inline-cta-primary" data-action="open-import-history">${t('analysis_insights_step_import')}</button>
              <button type="button" class="ghost inline-cta inline-cta-secondary" data-action="open-add-wallet">${t('cta_add_wallet')}</button>
            </div>
          </div>
        `;
      } else {
        insightsEl.innerHTML = filteredInsights.map((item) => {
          const title = firstNonEmpty([item && item.title, item && item.type, t('analysis_insight_fallback')]);
          const label = firstNonEmpty([item && item.label]);
          const avgAmount = toNumber(item && item.avgAmount);
          const nextCharge = formatShortDay(item && item.nextEstimatedChargeAt);
          const confidence = toNumber(item && item.confidence);
          const confidenceText = Number.isFinite(confidence) ? `${Math.max(0, Math.min(99, Math.round(confidence * 100)))}%` : '—';
          const metaParts = [
            label,
            Number.isFinite(avgAmount) ? `${t('average')}: ${formatMoney(avgAmount, base)}` : '',
            nextCharge ? `${t('analysis_next_label')}: ${nextCharge}` : '',
            `${t('analysis_confidence_label')}: ${confidenceText}`
          ].filter(Boolean);
          return `
            <article class="analysis-detail-list-item">
              <div class="analysis-detail-list-title">${escapeHtml(title)}</div>
              <div class="analysis-detail-list-value">${escapeHtml(formatInsightValue(item, base))}</div>
              <div class="muted">${escapeHtml(metaParts.join(' · '))}</div>
            </article>
          `;
        }).join('');
      }
    }
  }

  function updateAnalysisMiniCard(model) {
    const data = model || buildAnalysisInsightsModel();
    const base = normalizeCurrency(data && data.base ? data.base : baseCurrency) || 'USD';
    const valueEl = q(selectors.analysisMiniValue);
    const growthEl = q(selectors.analysisMiniGrowth);
    const card = q(selectors.analysisQuickCard);
    if (!valueEl || !growthEl || !card) return;

    valueEl.textContent = Number.isFinite(data && data.portfolio)
      ? formatMoney(data.portfolio, base)
      : '—';

    growthEl.classList.remove('is-positive', 'is-negative');
    if (Number.isFinite(data && data.growth)) {
      growthEl.textContent = `${t('analysis_mini_growth_label')} ${formatSignedPct(data.growth)}`;
      if (data.growth > 0) growthEl.classList.add('is-positive');
      if (data.growth < 0) growthEl.classList.add('is-negative');
    } else {
      growthEl.textContent = `${t('analysis_mini_growth_label')} —`;
    }

    card.dataset.uiState = analysisState.detailBusy ? 'loading' : 'ready';
    card.setAttribute('title', t('analysis_quick_open_hint'));
  }

  async function refreshAllDataForAnalysisDetail() {
    if (analysisState.detailBusy) return;
    analysisState.detailBusy = true;
    const card = q(selectors.analysisQuickCard);
    if (card) card.disabled = true;
    setText(selectors.analysisDetailUpdated, t('analysis_quick_refreshing'));

    try {
      await Promise.allSettled([
        loadBalance(),
        loadReports(),
        loadRecentTransactions(),
        loadWallets(),
        maybeRefreshAnalysisData(true)
      ]);
    } finally {
      analysisState.detailBusy = false;
      if (card) card.disabled = false;
      refreshAnalysisPanel();
    }
  }

  function syncAnalysisDetailTabState() {
    const metricRoot = q(selectors.analysisDetailMetricTabs);
    if (metricRoot) {
      metricRoot.querySelectorAll('button[data-metric]').forEach((btn) => {
        const metric = String(btn.dataset.metric || '').toLowerCase();
        btn.classList.toggle('active', metric === String(analysisState.detailMetric || 'net').toLowerCase());
      });
    }
    const windowRoot = q(selectors.analysisDetailWindowTabs);
    if (windowRoot) {
      const activeWindow = normalizeSeriesWindow(analysisState.detailWindow || analysisState.seriesWindow || '30d');
      windowRoot.querySelectorAll('button[data-window]').forEach((btn) => {
        const next = normalizeSeriesWindow(btn.dataset.window || '');
        btn.classList.toggle('active', next === activeWindow);
      });
    }
  }

  function bindAnalysisQuickCard() {
    const card = q(selectors.analysisQuickCard);
    const overlay = q(selectors.analysisDetailOverlay);
    const menu = q(selectors.analysisDetailMenu);
    const closeBtn = q(selectors.analysisDetailCloseBtn);
    const copyBtn = q(selectors.analysisDetailCopyBtn);
    const metricTabs = q(selectors.analysisDetailMetricTabs);
    const windowTabs = q(selectors.analysisDetailWindowTabs);
    if (!card || !overlay || !menu || !closeBtn) return;
    if (card.dataset.bound === '1') return;
    card.dataset.bound = '1';
    // Initialize closed state without forcing focus back to the trigger.
    overlay.classList.remove('is-open');
    overlay.hidden = true;
    overlay.dataset.uiState = 'closed';
    menu.setAttribute('aria-hidden', 'true');
    menu.style.removeProperty('--sheet-drag-offset');
    menu.style.removeProperty('transition');
    analysisState.detailOpen = false;
    document.body.classList.remove('analysis-drawer-open');
    card.setAttribute('aria-expanded', 'false');
    card.classList.remove('is-open');
    updateAnalysisMiniCard(null);
    renderAnalysisDetail(null);
    syncAnalysisDetailTabState();

    const close = () => {
      if (!analysisState.detailOpen) return;
      setAnalysisDetailOpen(false, card);
    };
    const isMobileSheet = () => window.matchMedia('(max-width: 900px)').matches;
    const head = menu.querySelector('.analysis-detail-head');
    let dragPointerId = null;
    let dragStartY = 0;
    let dragOffset = 0;
    const clearSheetDrag = () => {
      dragPointerId = null;
      dragStartY = 0;
      dragOffset = 0;
      menu.style.removeProperty('--sheet-drag-offset');
      menu.style.removeProperty('transition');
    };

    card.addEventListener('click', async (event) => {
      event.preventDefault();
      if (analysisState.detailOpen) {
        close();
        return;
      }
      setAnalysisDetailOpen(true, card);
      syncAnalysisDetailTabState();
      renderAnalysisDetail(null);
      await refreshAllDataForAnalysisDetail();
      renderAnalysisDetail(null);
    });
    card.addEventListener('keydown', (event) => {
      if (event.key !== 'Enter' && event.key !== ' ') return;
      event.preventDefault();
      card.click();
    });

    closeBtn.addEventListener('click', close);
    if (copyBtn) {
      copyBtn.addEventListener('click', async () => {
        const value = String((q(selectors.analysisDetailWalletAddress)?.textContent || '')).trim();
        if (!value || value === '—') return;
        try {
          await navigator.clipboard.writeText(value);
        } catch (_) {
          // ignore clipboard errors
        }
      });
    }
    if (metricTabs) {
      metricTabs.querySelectorAll('button[data-metric]').forEach((btn) => {
        btn.addEventListener('click', () => {
          analysisState.detailMetric = String(btn.dataset.metric || 'net').toLowerCase();
          syncAnalysisDetailTabState();
          renderAnalysisDetail(null);
        });
      });
    }
    if (windowTabs) {
      windowTabs.querySelectorAll('button[data-window]').forEach((btn) => {
        btn.addEventListener('click', async () => {
          const nextWindow = normalizeSeriesWindow(btn.dataset.window || '30d');
          if (nextWindow === normalizeSeriesWindow(analysisState.detailWindow || analysisState.seriesWindow || '30d')) {
            syncAnalysisDetailTabState();
            return;
          }
          analysisState.detailWindow = nextWindow;
          analysisState.seriesWindow = nextWindow;
          analysisState.lastDataFetchAt = 0;
          syncAnalysisDetailTabState();
          renderAnalysisDetail(null);
          await maybeRefreshAnalysisData(true);
          renderAnalysisDetail(null);
        });
      });
    }
    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) close();
    });
    if (head && 'PointerEvent' in window) {
      head.addEventListener('pointerdown', (event) => {
        if (!analysisState.detailOpen || !isMobileSheet()) return;
        if (event.pointerType === 'mouse') return;
        dragPointerId = event.pointerId;
        dragStartY = event.clientY;
        dragOffset = 0;
        menu.style.transition = 'none';
        if (typeof head.setPointerCapture === 'function') {
          head.setPointerCapture(event.pointerId);
        }
      });
      head.addEventListener('pointermove', (event) => {
        if (dragPointerId == null || event.pointerId !== dragPointerId) return;
        dragOffset = Math.max(0, event.clientY - dragStartY);
        menu.style.setProperty('--sheet-drag-offset', `${dragOffset}px`);
        if (event.cancelable) event.preventDefault();
      });
      const finishSheetDrag = (event) => {
        if (dragPointerId == null || event.pointerId !== dragPointerId) return;
        const shouldClose = dragOffset > 96;
        if (typeof head.releasePointerCapture === 'function') {
          try { head.releasePointerCapture(event.pointerId); } catch (_) { /* noop */ }
        }
        clearSheetDrag();
        if (shouldClose) close();
      };
      head.addEventListener('pointerup', finishSheetDrag);
      head.addEventListener('pointercancel', finishSheetDrag);
    }
    menu.addEventListener('keydown', (event) => {
      trapFocusKeydown(event, menu);
    });
    document.addEventListener('keydown', (event) => {
      if (analysisState.detailOpen && event.key === 'Escape') {
        close();
      }
    });
    document.addEventListener('visibilitychange', () => {
      if (document.hidden) clearSheetDrag();
    });
  }

  function buildAnalysisInsightsModel() {
    const summaryApi = analysisState.apiSummary;
    const insightsApi = analysisState.apiInsights;
    const seriesApi = analysisState.apiSeries;
    const summaryReliable = Boolean(summaryApi && isReliableMetricsSource(summaryApi.metricsSource) && summaryApi.synthetic !== true);
    const seriesReliable = Boolean(seriesApi && seriesApi.synthetic !== true);
    const portfolioApi = summaryApi ? toNumber(summaryApi.totalValueInBase) : NaN;
    const portfolio = Number.isFinite(portfolioApi)
      ? Math.max(0, portfolioApi)
      : (Number.isFinite(analysisState.summaryTotal) ? Math.max(0, analysisState.summaryTotal) : NaN);
    const base = normalizeCurrency(
      (summaryApi && summaryApi.baseCurrency)
      || (insightsApi && insightsApi.baseCurrency)
      || analysisState.summaryBase
      || baseCurrency
    ) || 'USD';

    const growthInsight = findAnalysisInsight('PORTFOLIO_30D_CHANGE');
    const growthFromApi = growthInsight
      && growthInsight.synthetic !== true
      && String(growthInsight.unit || '').toUpperCase() === 'PERCENT'
      ? toNumber(growthInsight.value)
      : NaN;
    const seriesValues = seriesReliable && Array.isArray(seriesApi.points)
      ? seriesApi.points.map((point) => toNumber(point && point.valueInBase)).filter((value) => Number.isFinite(value))
      : [];
    const growthFromSeries = seriesValues.length >= 2
      ? ((seriesValues[seriesValues.length - 1] - seriesValues[0]) / Math.max(Math.abs(seriesValues[0]), 1)) * 100
      : NaN;
    const growth = Number.isFinite(growthFromSeries)
      ? growthFromSeries
      : (Number.isFinite(growthFromApi)
        ? growthFromApi
        : (summaryReliable && Number.isFinite(toNumber(summaryApi.delta7dPct)) ? toNumber(summaryApi.delta7dPct) : NaN));

    const outflowInsight = findAnalysisInsight('TOP_OUTFLOW');
    const outflowValueApi = outflowInsight && String(outflowInsight.unit || '').toUpperCase() === 'BASE_CURRENCY'
      ? toNumber(outflowInsight.value)
      : NaN;
    const outflowLabelApi = outflowInsight && outflowInsight.label ? String(outflowInsight.label) : '';
    const outflowLiveApi = Boolean(outflowInsight && !outflowInsight.synthetic && Number.isFinite(outflowValueApi));

    const recurringInsight = findAnalysisInsight('RECURRING_SPEND');
    const recurringValueApi = recurringInsight && String(recurringInsight.unit || '').toUpperCase() === 'BASE_CURRENCY'
      ? toNumber(recurringInsight.value)
      : NaN;
    const recurringLiveApi = Boolean(recurringInsight && !recurringInsight.synthetic && Number.isFinite(recurringValueApi));
    const recurringConfidenceApi = toNumber(recurringInsight && recurringInsight.confidence);
    const recurringNextChargeApi = recurringInsight && recurringInsight.nextEstimatedChargeAt
      ? String(recurringInsight.nextEstimatedChargeAt)
      : '';

    const portfolioLive = Boolean(summaryReliable && Number.isFinite(portfolioApi));

    return {
      base,
      portfolio,
      growth,
      growthSeries: seriesValues,
      growthSeriesSynthetic: !seriesReliable,
      portfolioLive,
      growthLive: Number.isFinite(growthFromSeries) || Number.isFinite(growthFromApi) || (summaryReliable && Number.isFinite(toNumber(summaryApi && summaryApi.delta7dPct))),
      outflowValue: Number.isFinite(outflowValueApi) ? outflowValueApi : NaN,
      outflowLabel: outflowLabelApi || '',
      outflowLive: outflowLiveApi,
      recurringValue: Number.isFinite(recurringValueApi) ? recurringValueApi : NaN,
      recurringLive: recurringLiveApi,
      recurringConfidence: Number.isFinite(recurringConfidenceApi)
        ? recurringConfidenceApi
        : (Number.isFinite(recurringValueApi) ? 0.82 : NaN),
      recurringNextChargeAt: recurringNextChargeApi
    };
  }

  function renderAnalysisCards() {
    const model = buildAnalysisInsightsModel();
    const hasWallet = Boolean(analysisState.activeWalletId);
    const updatedValue = formatFxUpdated(
      analysisState.status && (analysisState.status.finishedAt || analysisState.status.updatedAt || analysisState.status.startedAt)
    );
    const monthlyCashflowRaw = toNumber(lastReportSummary && lastReportSummary.net);
    const monthlyCashflow = hasConfirmedTransactionMetrics(lastReportSummary) ? monthlyCashflowRaw : NaN;
    const debtValue = computeDebtInBase();
    const hasDebt = Number.isFinite(debtValue) && debtValue > 0.000001;
    const debtCard = q(selectors.analysisCardRecurring);

    if (!hasWallet) {
      setAnalysisCardState(selectors.analysisCardPortfolio, 'loading');
      setAnalysisCardState(selectors.analysisCardGrowth, 'loading');
      setAnalysisCardState(selectors.analysisCardOutflow, Number.isFinite(monthlyCashflow) ? 'ready' : 'loading');
      setAnalysisCardState(selectors.analysisCardRecurring, hasDebt ? 'ready' : 'loading');
      setHeroChangeValue(NaN);
      setText(selectors.analysisPortfolioValue, '—');
      setText(selectors.analysisGrowthValue, '—');
      setText(selectors.analysisOutflowValue, Number.isFinite(monthlyCashflow) ? formatMoney(monthlyCashflow, model.base) : '—');
      setText(selectors.analysisRecurringValue, hasDebt ? formatMoney(debtValue, model.base) : '—');
      setText(selectors.analysisPortfolioMeta, '');
      setText(selectors.analysisGrowthMeta, '');
      setText(selectors.analysisOutflowMeta, '');
      setText(selectors.analysisRecurringMeta, '');
      setDataSourceBadge(selectors.analysisPortfolioSource, DATA_SOURCE.pending);
      setDataSourceBadge(selectors.analysisGrowthSource, DATA_SOURCE.pending);
      setDataSourceBadge(selectors.analysisOutflowSource, DATA_SOURCE.pending);
      setDataSourceBadge(selectors.analysisRecurringSource, DATA_SOURCE.pending);
      if (debtCard) debtCard.hidden = !hasDebt;
      renderAnalysisGrowthSpark([]);
      updateAnalysisCardsVisibility();
      return model;
    }

    setAnalysisCardState(selectors.analysisCardPortfolio, Number.isFinite(model.portfolio) ? 'ready' : 'loading');
    setAnalysisCardState(selectors.analysisCardGrowth, Number.isFinite(model.growth) ? 'ready' : 'loading');
    setAnalysisCardState(selectors.analysisCardOutflow, Number.isFinite(monthlyCashflow) ? 'ready' : 'loading');
    setAnalysisCardState(selectors.analysisCardRecurring, hasDebt ? 'ready' : 'loading');
    setHeroChangeValue(model.growth);

    animateMetricValue(selectors.analysisPortfolioValue, model.portfolio, (value) => formatMoney(value, model.base));
    animateMetricValue(selectors.analysisGrowthValue, model.growth, formatSignedPct);
    animateMetricValue(selectors.analysisOutflowValue, monthlyCashflow, (value) => formatMoney(value, model.base));
    if (hasDebt) {
      animateMetricValue(selectors.analysisRecurringValue, debtValue, (value) => formatMoney(value, model.base));
    } else {
      setText(selectors.analysisRecurringValue, '—');
    }

    setAnalysisValueState(selectors.analysisPortfolioValue, model.portfolio);
    setAnalysisValueState(selectors.analysisGrowthValue, model.growth);
    setAnalysisValueState(selectors.analysisOutflowValue, monthlyCashflow);
    setAnalysisValueState(selectors.analysisRecurringValue, hasDebt ? -Math.abs(debtValue) : NaN);
    if (debtCard) debtCard.hidden = !hasDebt;

    setText(selectors.analysisPortfolioMeta, updatedValue ? t('analysis_card_updated_live', { value: updatedValue }) : '');
    setText(selectors.analysisGrowthMeta, '');
    setText(selectors.analysisOutflowMeta, '');
    setText(selectors.analysisRecurringMeta, '');

    renderAnalysisGrowthSpark(model.growthSeries);

    setDataSourceBadge(
      selectors.analysisPortfolioSource,
      model.portfolioLive ? DATA_SOURCE.live : (Number.isFinite(model.portfolio) ? DATA_SOURCE.hybrid : DATA_SOURCE.pending)
    );
    setDataSourceBadge(
      selectors.analysisGrowthSource,
      model.growthLive ? (model.growthSeriesSynthetic ? DATA_SOURCE.hybrid : DATA_SOURCE.live) : DATA_SOURCE.pending
    );
    setDataSourceBadge(selectors.analysisOutflowSource, Number.isFinite(monthlyCashflow) ? DATA_SOURCE.live : DATA_SOURCE.pending);
    setDataSourceBadge(selectors.analysisRecurringSource, hasDebt ? DATA_SOURCE.live : DATA_SOURCE.pending);
    updateAnalysisCardsVisibility();
    return model;
  }

  function renderAnalysisBanner() {
    const panel = q(selectors.walletAnalysisPanel);
    const banner = q(selectors.walletAnalysisBanner);
    const title = q(selectors.analysisBannerTitle);
    const subtitle = q(selectors.analysisBannerSubtitle);
    const progressWrap = q(selectors.analysisProgressWrap);
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
      if (progressWrap) progressWrap.hidden = true;
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
    const updatedValue = formatFxUpdated(status.finishedAt || status.updatedAt || status.startedAt);
    const etaHint = formatEtaHint(status.etaSeconds);
    const lastSuccessfulStage = status.lastSuccessfulStage ? analysisStageLabel(status.lastSuccessfulStage) : '';

    setUiState(panel, 'ready');
    if (banner) banner.dataset.uiState = statusName.toLowerCase();
    title.textContent = t('analysis_banner_badge');
    if (statusName === 'DONE') {
      subtitle.textContent = updatedValue ? t('analysis_quick_updated', { value: updatedValue }) : t('analysis_banner_subtitle_done');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.live);
    } else if (statusName === 'PARTIAL') {
      subtitle.textContent = updatedValue ? t('analysis_quick_updated', { value: updatedValue }) : t('analysis_banner_subtitle_partial');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.hybrid);
    } else if (statusName === 'FAILED') {
      subtitle.textContent = t('analysis_banner_subtitle_failed');
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.pending);
    } else {
      subtitle.textContent = t('analysis_banner_title_running', { name: safeWalletName });
      setDataSourceBadge(selectors.analysisDataSource, DATA_SOURCE.pending);
    }

    const stageLabel = `${analysisStageLabel(status.stage)} · ${analysisStatusLabel(statusName)}`;
    const statusMeta = [etaHint, lastSuccessfulStage].filter(Boolean).join(' · ');
    stageEl.textContent = analysisState.pollError
      ? `${stageLabel}${statusMeta ? ` · ${statusMeta}` : ''} · ${t('analysis_polling_error')}`
      : `${stageLabel}${statusMeta ? ` · ${statusMeta}` : ''}`;
    progressTextEl.textContent = `${progressPct}%`;
    progressFill.style.width = `${progressPct}%`;
    if (progressWrap) {
      const showProgress = statusName === 'RUNNING' || statusName === 'QUEUED';
      progressWrap.hidden = !showProgress;
    }
    if (progressBar) progressBar.setAttribute('aria-valuenow', String(progressPct));
    if (updatedEl) {
      const updatedText = updatedValue ? t('updated_at', { value: updatedValue }) : t('source_pending');
      const parts = [updatedText, etaHint, lastSuccessfulStage].filter(Boolean);
      updatedEl.textContent = parts.join(' · ');
    }
  }

  function refreshAnalysisPanel() {
    renderAnalysisBanner();
    const model = renderAnalysisCards();
    updateAnalysisMiniCard(model);
    if (analysisState.detailOpen) {
      renderAnalysisDetail(model);
    }
  }

  function syncAnalysisWallets(wallets, payload) {
    const summary = payload && typeof payload === 'object' ? payload : {};
    analysisState.summaryTotal = toNumber(summary.totalValueInBase);
    analysisState.summaryBase = normalizeCurrency(summary.baseCurrency || baseCurrency) || 'USD';
    analysisState.walletsCount = Array.isArray(wallets) ? wallets.length : 0;

    const latestWallet = pickLatestWallet(wallets);
    if (!latestWallet || latestWallet.id == null) {
      analysisState.activeWalletId = null;
      analysisState.activeWalletName = '';
      analysisState.activeWallet = null;
      analysisState.status = null;
      analysisState.pollError = false;
      analysisState.apiSummary = null;
      analysisState.apiInsights = null;
      analysisState.apiSeries = null;
      analysisState.seriesWindow = '30d';
      analysisState.detailWindow = '30d';
      analysisState.detailMetric = 'net';
      analysisState.lastDataFetchAt = 0;
      stopAnalysisPolling();
      refreshAnalysisPanel();
      return;
    }

    const nextWalletId = Number(latestWallet.id);
    const changed = analysisState.activeWalletId !== nextWalletId;
    analysisState.activeWalletId = nextWalletId;
    analysisState.activeWalletName = String(latestWallet.label || walletNetworkLabel(latestWallet.network || ''));
    analysisState.activeWallet = latestWallet;

    refreshAnalysisPanel();
    if (changed) {
      analysisState.status = null;
      analysisState.pollError = false;
      analysisState.apiSummary = null;
      analysisState.apiInsights = null;
      analysisState.apiSeries = null;
      analysisState.seriesWindow = '30d';
      analysisState.detailWindow = '30d';
      analysisState.detailMetric = 'net';
      analysisState.lastDataFetchAt = 0;
      stopAnalysisPolling();
      pollWalletAnalysisStatus();
      maybeRefreshAnalysisData(true);
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
      const hasDebt = creditOk && Number.isFinite(creditInBase) && creditInBase > 0.000001;
      creditEl.textContent = hasDebt ? `${t('credit')}: ${formatMoney(creditInBase, base)}` : '';
      creditEl.hidden = !hasDebt;
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
      totalsLineEl.hidden = true;
    }

    showBalanceError(totalOk && creditOk && conversion && conversion.ok ? '' : t('base_currency_conversion_failed'));
    updateAnalysisMiniCard(null);
    if (analysisState.detailOpen) {
      renderAnalysisDetail(null);
    }
    updateAnalysisCardsVisibility();
  }

  function rerenderBalanceSnapshot() {
    if (!lastBalanceSnapshot || !lastBalanceConversion) return;
    renderBalance(lastBalanceSnapshot, lastBalanceConversion, cryptoWalletTotalInBase);
  }

  function renderIncomeExpenseSummary(summary) {
    const netEl = document.querySelector(selectors.incomeExpenseNet);
    const detailsEl = document.querySelector(selectors.incomeExpenseDetails);
    const incomeEl = document.querySelector(selectors.analysisIncomeValue);
    const expenseEl = document.querySelector(selectors.analysisExpenseValue);
    const cashflowEl = document.querySelector(selectors.analysisOutflowValue);
    const debtEl = document.querySelector(selectors.analysisRecurringValue);
    const debtCard = document.querySelector(selectors.analysisCardRecurring);
    if (!netEl && !detailsEl && !incomeEl && !expenseEl && !cashflowEl && !debtEl) return;

    const base = normalizeCurrency(summary && summary.baseCurrency ? summary.baseCurrency : baseCurrency) || 'USD';
    const income = toNumber(summary && summary.income);
    const expense = toNumber(summary && summary.expense);
    const net = toNumber(summary && summary.net);
    const confirmedMetrics = hasConfirmedTransactionMetrics(summary);
    const debt = computeDebtInBase();
    const hasDebt = Number.isFinite(debt) && debt > 0.000001;

    if (netEl) {
      netEl.textContent = Number.isFinite(net) ? formatMoney(net, base) : '—';
    }
    if (detailsEl) {
      detailsEl.textContent = confirmedMetrics && Number.isFinite(income) && Number.isFinite(expense)
        ? `${t('income_label')}: ${formatMoney(income, base)} · ${t('expense_label')}: ${formatMoney(expense, base)}`
        : t('income_expense_details');
    }
    if (incomeEl) {
      incomeEl.textContent = confirmedMetrics && Number.isFinite(income) ? formatMoney(income, base) : '—';
    }
    if (expenseEl) {
      expenseEl.textContent = confirmedMetrics && Number.isFinite(expense) ? formatMoney(-Math.abs(expense), base) : '—';
      expenseEl.classList.toggle('amount-negative', confirmedMetrics && Number.isFinite(expense));
    }
    if (cashflowEl) {
      cashflowEl.textContent = confirmedMetrics && Number.isFinite(net) ? formatMoney(net, base) : '—';
      cashflowEl.classList.remove('amount-positive', 'amount-negative');
      if (confirmedMetrics && Number.isFinite(net)) {
        cashflowEl.classList.add(net >= 0 ? 'amount-positive' : 'amount-negative');
      }
    }
    if (debtEl) {
      debtEl.textContent = hasDebt ? formatMoney(debt, base) : '—';
      debtEl.classList.toggle('amount-negative', hasDebt);
    }
    if (debtCard) {
      debtCard.hidden = !hasDebt;
    }
    updateAnalysisMiniCard(null);
    if (analysisState.detailOpen) {
      renderAnalysisDetail(null);
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

  function computeDebtInBase() {
    const accounts = lastBalanceSnapshot && Array.isArray(lastBalanceSnapshot.accounts)
      ? lastBalanceSnapshot.accounts
      : [];
    if (!accounts.length || !lastBalanceConversion) return NaN;
    let hasAny = false;
    let debt = 0;
    accounts.forEach((account) => {
      const balance = toNumber(account && account.balance);
      if (!Number.isFinite(balance)) return;
      hasAny = true;
      if (balance >= 0) return;
      const converted = convertToBaseAmount(Math.abs(balance), account && account.currency, lastBalanceConversion);
      if (Number.isFinite(converted)) {
        debt += converted;
      }
    });
    if (!hasAny) return NaN;
    return debt;
  }

  async function loadReportSummary() {
    const netEl = document.querySelector(selectors.incomeExpenseNet);
    if (netEl) netEl.textContent = t('loading');
    reportSummaryLoaded = false;
    reportSummaryConfirmed = false;

    const params = new URLSearchParams();
    params.set('period', 'MONTH');
    const res = await Api.call(`/api/reports/summary?${params}`, 'GET', null, true);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      if (netEl) netEl.textContent = '—';
      lastReportSummary = null;
      renderIncomeExpenseSummary(null);
      return null;
    }
    const payload = res.data;
    reportSummaryLoaded = true;
    reportSummaryConfirmed = payload.synthetic !== true;
    const serverBase = normalizeCurrency(payload.baseCurrency || baseCurrency) || 'USD';
    if (serverBase && serverBase !== normalizeCurrency(baseCurrency)) {
      baseCurrency = serverBase;
      updateCurrencyLabels();
    }
    renderIncomeExpenseSummary(payload);
    lastReportSummary = payload;
    return payload;
  }

  async function loadReportByCategory() {
    const target = document.querySelector(selectors.expenseChart);
    if (!target || target.classList.contains('analysis-compat-hidden')) {
      return null;
    }
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
        setUiState(target, 'empty');
        target.innerHTML = `
          <div class="compact-empty-state">
            <div class="compact-empty-title">${escapeHtml(t('expense_empty_title'))}</div>
            <div class="compact-empty-actions">
              <button type="button" class="ghost inline-cta" data-action="open-add-transaction">${escapeHtml(t('cta_add_transaction'))}</button>
              <button type="button" class="ghost inline-cta inline-link" data-action="open-add-account">${escapeHtml(t('cta_connect_account'))}</button>
            </div>
          </div>
        `;
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
        target.innerHTML = `
          <div class="compact-empty-state">
            <div class="compact-empty-title">${escapeHtml(t('expense_empty_title'))}</div>
            <div class="compact-empty-actions">
              <button type="button" class="ghost inline-cta" data-action="open-add-transaction">${escapeHtml(t('cta_add_transaction'))}</button>
              <button type="button" class="ghost inline-cta inline-link" data-action="open-add-account">${escapeHtml(t('cta_connect_account'))}</button>
            </div>
          </div>
        `;
      }
      return payload;
    }

    if (target) setUiState(target, 'ready');
    renderBarChart(selectors.expenseChart, items, baseCurrency);
    return payload;
  }

  function emptyTrendState(target, message) {
    if (!target) return;
    setUiState(target, 'empty');
    target.classList.add('is-compact-chart');
    target.innerHTML = `
      <div class="compact-chart-state">
        <div class="muted compact-chart-note">${escapeHtml(message || t('no_meaningful_change_window', { value: localizedWindowLabel(balanceTrendWindow) }))}</div>
      </div>
    `;
  }

  function trendWindowDays(windowValue) {
    const normalized = normalizeSeriesWindow(windowValue);
    if (normalized === '7d') return 7;
    if (normalized === '90d') return 90;
    if (normalized === '1y') return 365;
    return 30;
  }

  function applyBalanceTrendControls() {
    document.querySelectorAll(selectors.balancePeriodButtons).forEach((btn) => {
      const tabWindow = normalizeSeriesWindow(btn.dataset.window || '');
      btn.classList.toggle('active', tabWindow === normalizeSeriesWindow(balanceTrendWindow));
      btn.setAttribute('aria-selected', tabWindow === normalizeSeriesWindow(balanceTrendWindow) ? 'true' : 'false');
    });
    const metricSelect = q(selectors.balanceMetricSelect);
    if (metricSelect) metricSelect.value = String(balanceTrendMetric || 'net').toLowerCase();
  }

  function bindBalanceTrendControls() {
    const periodButtons = Array.from(document.querySelectorAll(selectors.balancePeriodButtons));
    const metricSelect = q(selectors.balanceMetricSelect);
    if (!periodButtons.length && !metricSelect) return;
    if (periodButtons.length && periodButtons[0].dataset.bound === '1') {
      applyBalanceTrendControls();
      return;
    }
    periodButtons.forEach((btn) => {
      btn.dataset.bound = '1';
      btn.addEventListener('click', async () => {
        const nextWindow = normalizeSeriesWindow(btn.dataset.window || '30d');
        if (nextWindow === normalizeSeriesWindow(balanceTrendWindow)) {
          applyBalanceTrendControls();
          return;
        }
        balanceTrendWindow = nextWindow;
        applyBalanceTrendControls();
        await loadBalanceTrend();
      });
    });
    if (metricSelect && metricSelect.dataset.bound !== '1') {
      metricSelect.dataset.bound = '1';
      metricSelect.addEventListener('change', async () => {
        balanceTrendMetric = String(metricSelect.value || 'net').toLowerCase();
        applyBalanceTrendControls();
        await loadBalanceTrend();
      });
    }
    applyBalanceTrendControls();
  }

  async function loadBalanceTrend() {
    const target = document.querySelector(selectors.balanceChart);
    applyBalanceTrendControls();
    if (target) {
      setUiState(target, 'loading');
      target.innerHTML = renderSkeletonList(2);
    }

    const now = new Date();
    const days = trendWindowDays(balanceTrendWindow);
    const start = new Date(now.getTime() - (days - 1) * 24 * 60 * 60 * 1000);
    start.setHours(0, 0, 0, 0);

    const params = new URLSearchParams();
    params.set('from', start.toISOString());
    params.set('to', now.toISOString());
    const res = await Api.call(`/api/reports/cash-flow?${params}`, 'GET', null, true);
    if (!res.ok || !res.data || typeof res.data !== 'object') {
      emptyTrendState(target, t('no_meaningful_change_window', { value: localizedWindowLabel(balanceTrendWindow) }));
      return null;
    }
    const payload = res.data;
    const points = Array.isArray(payload.points) ? payload.points : [];
    if (!points.length) {
      emptyTrendState(target, t('no_meaningful_change_window', { value: localizedWindowLabel(balanceTrendWindow) }));
      return payload;
    }

    const byDay = new Map();
    points.forEach((point) => {
      const rawDate = point && point.date ? String(point.date) : '';
      if (!rawDate) return;
      const dayKey = rawDate.slice(0, 10);
      const income = toNumber(point && point.income);
      const expense = toNumber(point && point.expense);
      const net = toNumber(point && point.net);
      byDay.set(dayKey, {
        income: Number.isFinite(income) ? income : 0,
        expense: Number.isFinite(expense) ? expense : 0,
        net: Number.isFinite(net) ? net : 0
      });
    });

    const dayKeys = [];
    const cursor = new Date(start);
    while (cursor <= now) {
      dayKeys.push(cursor.toISOString().slice(0, 10));
      cursor.setDate(cursor.getDate() + 1);
    }

    const inflowSeries = dayKeys.map((key) => {
      const item = byDay.get(key);
      return item ? Math.max(item.income, 0) : 0;
    });
    const outflowSeries = dayKeys.map((key) => {
      const item = byDay.get(key);
      return item ? Math.max(item.expense, 0) : 0;
    });
    const netSeries = dayKeys.map((key) => {
      const item = byDay.get(key);
      return item ? item.net : 0;
    });

    let seriesToRender = netSeries.slice();
    if (balanceTrendMetric === 'inflow') {
      seriesToRender = inflowSeries;
    } else if (balanceTrendMetric === 'outflow') {
      seriesToRender = outflowSeries;
    } else {
      const currentTotal = computeCurrentTotalInBase();
      if (Number.isFinite(currentTotal)) {
        const totalNet = netSeries.reduce((sum, value) => sum + value, 0);
        let running = currentTotal - totalNet;
        seriesToRender = netSeries.map((delta) => {
          running += delta;
          return running;
        });
      }
    }

    const hasMeaningful = seriesToRender.some((value) => Number.isFinite(value) && Math.abs(value) > 0.000001);
    if (!hasMeaningful) {
      emptyTrendState(target, t('no_meaningful_change_window', { value: localizedWindowLabel(balanceTrendWindow) }));
      return payload;
    }

    if (target) setUiState(target, 'ready');
    balanceTrendCache = {
      at: Date.now(),
      window: normalizeSeriesWindow(balanceTrendWindow),
      metric: String(balanceTrendMetric || 'net').toLowerCase(),
      series: seriesToRender.slice()
    };
    renderLineChart(selectors.balanceChart, seriesToRender, baseCurrency, balanceTrendWindow);
    return payload;
  }

  async function loadReports() {
    if (!dashboardOverview) {
      await loadReportSummary();
    }
    await loadBalanceTrend();
  }

  function renderAccountsList(accounts, conversion) {
    const list = document.querySelector(selectors.accountsList);
    if (!list) return;
    setPanelFeedback(selectors.accountsFeedback, '');
    dashboardDataState.accountsLoaded = true;
    if (!accounts || accounts.length === 0) {
      list.classList.add('is-empty');
      dashboardDataState.hasAccounts = false;
      setUiState(list, 'empty');
      list.innerHTML = renderEmptyState(
        `${t('no_accounts')} ${t('empty_accounts_hint')}`,
        t('cta_add_account'),
        'open-add-account',
        t('get_started_import'),
        'open-import-history'
      );
      updateGetStartedSection();
      return;
    }
    list.classList.remove('is-empty');
    dashboardDataState.hasAccounts = true;
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
        <div class="list-item table-row account-row">
          <div class="account-main">
            <div class="account-name">${safeName}</div>
            <small class="muted">${safeCurrency}${acc.archived ? ` · ${t('archived')}` : ''}</small>
          </div>
          <div class="account-actions">
            <div class="${signClass}">${amountText}</div>
            <button type="button" class="ghost account-remove menu-trigger" data-account-id="${safeId}" data-account-name="${safeName}" title="${t('account_remove')}" aria-label="${t('account_remove')}">⋯</button>
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
        await loadDashboardOverview();
        await Promise.all([
          loadBalance(),
          loadReports(),
          loadRecentTransactions(),
          loadUpcomingPayments()
        ]);
      });
    });
    updateGetStartedSection();
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
    return formatDateTimeIntl(date, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
  }

  function renderTransactionsList(transactions) {
    const list = document.querySelector(selectors.transactionsList);
    if (!list) return;
    dashboardDataState.transactionsLoaded = true;
    if (!Array.isArray(transactions) || transactions.length === 0) {
      list.classList.add('is-empty');
      dashboardDataState.hasTransactions = false;
      setUiState(list, 'empty');
      const hasAccounts = dashboardDataState.hasAccounts;
      const emptyHint = hasAccounts ? t('empty_transactions_hint') : t('transaction_no_accounts');
      const actionLabel = hasAccounts ? t('cta_add_transaction') : t('cta_add_account');
      const action = hasAccounts ? 'open-add-transaction' : 'open-add-account';
      list.innerHTML = `
        <div class="table-empty-row">
          <div class="table-empty-copy">
            <div class="muted">${escapeHtml(t('transactions_empty'))} ${escapeHtml(emptyHint)}</div>
            <div class="empty-state-actions">
              <button type="button" class="ghost inline-cta inline-cta-primary" data-action="${escapeHtml(action)}">${escapeHtml(actionLabel)}</button>
              <button type="button" class="ghost inline-cta inline-cta-secondary" data-action="open-import-history">${escapeHtml(t('get_started_import'))}</button>
            </div>
          </div>
        </div>
      `;
      if (lastReportSummary) {
        renderIncomeExpenseSummary(lastReportSummary);
      }
      updateGetStartedSection();
      return;
    }
    list.classList.remove('is-empty');
    dashboardDataState.hasTransactions = true;
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
      const safeWhen = escapeHtml(formatTxTimestamp(tx && tx.transactionDate) || '—');
      const description = tx && tx.description ? String(tx.description) : '';
      const safeDesc = escapeHtml(description || safeCategory);

      return `
        <div class="list-item table-row tx-row">
          <div class="tx-cell tx-date">${safeWhen}</div>
          <div class="tx-cell tx-merchant">
            <div class="tx-primary">${safeDesc}</div>
            <div class="muted tx-secondary">${safeAccount}</div>
          </div>
          <div class="tx-cell tx-category">${safeCategory}</div>
          <div class="tx-cell tx-amount ${signClass}">${amountText}</div>
        </div>
      `;
    }).join('');
    if (lastReportSummary) {
      renderIncomeExpenseSummary(lastReportSummary);
    }
    updateGetStartedSection();
  }

  async function loadRecentTransactions() {
    const list = document.querySelector(selectors.transactionsList);
    if (list) {
      list.classList.remove('is-empty');
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
      dashboardDataState.transactionsLoaded = true;
      dashboardDataState.hasTransactions = false;
      if (list) {
        setUiState(list, 'error');
        list.innerHTML = renderErrorState(t('transactions_load_failed_short'), t('cta_retry'), 'retry-transactions');
      }
      setPanelFeedback(selectors.transactionsFeedback, t('transactions_load_failed'), true);
      recentTransactionsCache = [];
      updateGetStartedSection();
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
        await loadDashboardOverview();
        await Promise.all([
          loadBalance(),
          loadReports(),
          loadRecentTransactions(),
          loadUpcomingPayments()
        ]);
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
      list.classList.remove('is-empty');
      setUiState(list, 'loading');
      list.innerHTML = renderSkeletonList(3);
    }
    const totalBalanceEl = document.querySelector(selectors.totalBalance);
    if (totalBalanceEl) totalBalanceEl.textContent = t('loading');
    showBalanceError('');
    setPanelFeedback(selectors.accountsFeedback, '');

    const res = await Api.call('/api/accounts/balance', 'GET', null, true);
    if (!res.ok) {
      dashboardDataState.accountsLoaded = true;
      dashboardDataState.hasAccounts = false;
      showBalanceError(t('balance_load_failed'));
      if (list) {
        setUiState(list, 'error');
        list.innerHTML = renderErrorState(t('balance_load_failed_short'), t('cta_retry'), 'retry-balance');
      }
      setPanelFeedback(selectors.accountsFeedback, t('balance_load_failed'), true);
      lastAccountsTotalInBase = NaN;
      updateGetStartedSection();
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
      dataSourceState.fx = DATA_SOURCE.pending;
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
    dataSourceState.fx = DATA_SOURCE.live;
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

  function clearCryptoCards() {
    cryptoAssets.forEach((asset) => {
      const priceEl = document.querySelector(asset.priceSelector);
      if (priceEl) {
        priceEl.textContent = '—';
        priceEl.classList.remove('amount-positive', 'amount-negative');
      }
      const sparkEl = document.querySelector(asset.sparkSelector);
      if (sparkEl) {
        sparkEl.innerHTML = '';
      }
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
      clearCryptoCards();
      if (statusEl) statusEl.textContent = t('no_data');
      dataSourceState.crypto = DATA_SOURCE.pending;
      setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
      syncMarketDataSource();
      return;
    }
    const payload = res.data;
    const rates = Array.isArray(payload.rates) ? payload.rates : [];
    if (!rates.length) {
      clearCryptoCards();
      if (statusEl) statusEl.textContent = t('no_data');
      dataSourceState.crypto = DATA_SOURCE.pending;
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
        updateCryptoCard(asset, priceValue, changeValue, sparklineRaw, baseCode);
        hasAny = true;
      } else {
        const priceEl = document.querySelector(asset.priceSelector);
        if (priceEl) {
          priceEl.textContent = '—';
          priceEl.classList.remove('amount-positive', 'amount-negative');
        }
        const sparkEl = document.querySelector(asset.sparkSelector);
        if (sparkEl) sparkEl.innerHTML = '';
      }
    });

    if (statusEl) {
      const updated = formatFxUpdated(payload.asOf);
      statusEl.textContent = updated ? t('updated_at', { value: updated }) : (hasAny ? t('updated') : t('no_data'));
    }
    dataSourceState.crypto = hasAny ? DATA_SOURCE.live : DATA_SOURCE.pending;
    setDataSourceBadge(selectors.cryptoDataSource, dataSourceState.crypto);
    syncMarketDataSource();
    pulseElement(selectors.cryptoStatus);
  }

  function renderLineChart(target, data, currency, windowValue) {
    const el = typeof target === 'string' ? document.querySelector(target) : target;
    if (!el || !Array.isArray(data) || data.length === 0) return;
    const values = data.map((value) => toNumber(value)).filter((value) => Number.isFinite(value));
    if (values.length < 2) {
      el.classList.remove('is-compact-chart');
      el.innerHTML = `<div class="muted">${escapeHtml(t('no_data'))}</div>`;
      return;
    }

    const variance = seriesVarianceRatio(values);
    const hasMeaningfulMovement = values.length >= CHART_MIN_POINTS && variance >= CHART_VARIANCE_THRESHOLD;
    const deltaCompact = values[values.length - 1] - values[0];
    if (!hasMeaningfulMovement) {
      el.classList.add('is-compact-chart');
      const note = t('no_meaningful_change_window', { value: localizedWindowLabel(windowValue || balanceTrendWindow || '30d') });
      const spark = buildCompactSparkline(values, 220, 48, deltaCompact >= 0 ? '#4f86ff' : '#f17f85');
      el.innerHTML = `
        <div class="compact-chart-state">
          ${spark}
          <div class="muted compact-chart-note">${escapeHtml(note)}</div>
        </div>
      `;
      return;
    }
    el.classList.remove('is-compact-chart');

    const labels = Array.from({ length: values.length }, (_, idx) => {
      const d = new Date();
      d.setDate(d.getDate() - (values.length - 1 - idx));
      if (values.length <= 14) {
        return formatDateTimeIntl(d, { day: '2-digit', month: 'short' });
      }
      return formatDateTimeIntl(d, { month: 'short' });
    });

    const width = Math.max(el.clientWidth || 520, 520);
    const height = 156;
    const padLeft = 14;
    const padRight = 10;
    const padTop = 10;
    const padBottom = 24;
    const max = Math.max(...values);
    const min = Math.min(...values);
    const span = max - min || 1;

    const points = values.map((v, i) => {
      const x = padLeft + (i / Math.max(values.length - 1, 1)) * (width - padLeft - padRight);
      const y = height - padBottom - ((v - min) / span) * (height - padTop - padBottom);
      return { x, y };
    });

    const areaPoints = [
      `${padLeft},${height - padBottom}`,
      ...points.map((p) => `${p.x},${p.y}`),
      `${width - padRight},${height - padBottom}`
    ].join(' ');

    const linePoints = points.map((p) => `${p.x},${p.y}`).join(' ');
    const yTicks = 1;
    const gridLines = Array.from({ length: yTicks + 1 }, (_, i) => {
      const value = min + (span / yTicks) * i;
      const y = height - padBottom - ((value - min) / span) * (height - padTop - padBottom);
      void value;
      return `<line x1="${padLeft}" x2="${width - padRight}" y1="${y}" y2="${y}" class="chart-gridline"></line>`;
    }).join('');

    const xLabels = labels.map((label, idx) => {
      const x = points[idx]?.x || padLeft;
      if (labels.length > 8 && idx % 2 !== 0 && idx !== labels.length - 1) return '';
      return `<text x="${x}" y="${height - padBottom + 18}" text-anchor="middle" class="chart-axis-label">${label}</text>`;
    }).join('');

    const delta = values[values.length - 1] - values[0];
    const avg = values.reduce((a, b) => a + b, 0) / values.length;
    const deltaPct = values[0] !== 0 ? (delta / values[0]) * 100 : 0;
    const formatPercent = (val) => `${val >= 0 ? '+' : ''}${val.toFixed(1)}%`;

    el.innerHTML = `
      <svg viewBox="0 0 ${width} ${height}" preserveAspectRatio="xMidYMid meet" class="chart-svg">
        <defs>
          <linearGradient id="lineFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#4f8bff" stop-opacity="0.32" />
            <stop offset="100%" stop-color="#4f8bff" stop-opacity="0.04" />
          </linearGradient>
        </defs>
        ${gridLines}
        <polygon points="${areaPoints}" class="chart-area" />
        <polyline points="${linePoints}" class="chart-line" stroke-linecap="round" stroke-linejoin="round" />
        ${xLabels}
      </svg>
      <div class="chart-summary-line">
        <span><strong>${t('min')}:</strong> ${formatMoney(min, currency || baseCurrency)}</span>
        <span aria-hidden="true">·</span>
        <span><strong>${t('max')}:</strong> ${formatMoney(max, currency || baseCurrency)}</span>
        <span aria-hidden="true">·</span>
        <span><strong>${t('average')}:</strong> ${formatMoney(avg, currency || baseCurrency)}</span>
        <span aria-hidden="true">·</span>
        <span><strong>${t('change')}:</strong> ${formatMoney(delta, currency || baseCurrency)}</span>
        <span aria-hidden="true">·</span>
        <span><strong>${t('trend_pct')}:</strong> ${formatPercent(deltaPct)}</span>
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
    return `${formatDecimalIntl(amount, 0, digits)} ${code || ''}`.trim();
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
    dashboardDataState.walletsLoaded = true;
    if (!Array.isArray(wallets) || wallets.length === 0) {
      walletListExpanded = false;
      list.classList.add('is-empty');
      dashboardDataState.hasWallets = false;
      setUiState(list, 'empty');
      list.innerHTML = renderEmptyState(
        `${t('wallets_empty')} ${t('empty_wallets_hint')}`,
        t('cta_add_wallet'),
        'open-add-wallet',
        t('get_started_import'),
        'open-import-history'
      );
      updateGetStartedSection();
      return;
    }
    if (!walletListExpanded || wallets.length <= 5) {
      walletListExpanded = false;
    }
    const walletsToRender = walletListExpanded ? wallets : wallets.slice(0, 5);
    const hiddenCount = Math.max(0, wallets.length - walletsToRender.length);
    list.classList.remove('is-empty');
    dashboardDataState.hasWallets = true;
    setUiState(list, 'ready');
    const walletRows = walletsToRender.map((wallet) => {
      const network = (wallet && wallet.network ? String(wallet.network) : '').toUpperCase();
      const networkLabel = walletNetworkLabel(network);
      const label = wallet && wallet.label ? escapeHtml(String(wallet.label)) : '';
      const addressShort = wallet && wallet.address ? escapeHtml(shortAddress(String(wallet.address))) : '';
      const balance = wallet ? wallet.balance : null;
      const valueInBase = wallet ? wallet.valueInBase : null;
      const base = wallet && wallet.baseCurrency ? String(wallet.baseCurrency) : baseCurrency;
      const valueText = Number.isFinite(Number(valueInBase)) ? formatMoney(Number(valueInBase), base) : '';
      const mainValueText = valueText ? `≈ ${valueText}` : '—';
      const safeWalletId = escapeHtml(wallet && wallet.id != null ? String(wallet.id) : '');
      const openLabel = t('analysis_quick_open_hint');
      return `
        <div
          class="list-item wallet-item table-row wallet-open"
          data-wallet-id="${safeWalletId}"
          role="button"
          tabindex="0"
          aria-label="${escapeHtml(openLabel)}"
        >
          <div class="wallet-left">
            <div class="wallet-title">${label || escapeHtml(networkLabel)}</div>
            <small class="muted">${escapeHtml(networkLabel)}${addressShort ? ` · ${addressShort}` : ''}</small>
          </div>
          <div class="wallet-actions">
            <div class="wallet-right">
              <div class="amount-positive">${mainValueText}</div>
              <small class="muted">${formatAssetAmount(balance, walletNativeAsset(network))}</small>
            </div>
            <span class="wallet-chevron" aria-hidden="true">›</span>
          <button type="button" class="ghost wallet-remove menu-trigger" data-wallet-id="${safeWalletId}" title="${t('wallet_remove')}" aria-label="${t('wallet_remove')}">⋯</button>
          </div>
        </div>
      `;
    }).join('');
    const expandRow = wallets.length > 5
      ? `
        <div class="table-row wallets-expand-row">
          <button type="button" class="ghost wallets-expand-toggle" data-action="toggle-wallets-expand">
            ${escapeHtml(walletListExpanded ? t('wallets_show_less') : t('wallets_show_all', { value: hiddenCount }))}
          </button>
        </div>
      `
      : '';
    list.innerHTML = `${walletRows}${expandRow}`;

    list.querySelectorAll('.wallet-open[data-wallet-id]').forEach((row) => {
      const activateWalletFromRow = () => {
        const walletId = Number(row.dataset.walletId);
        if (!Number.isFinite(walletId)) return;
        const selected = wallets.find((item) => Number(item && item.id) === walletId);
        if (!selected) return;
        const changed = analysisState.activeWalletId !== walletId;
        analysisState.activeWalletId = walletId;
        analysisState.activeWalletName = String(selected.label || walletNetworkLabel(selected.network || ''));
        analysisState.activeWallet = selected;
        if (changed) {
          analysisState.status = null;
          analysisState.apiSummary = null;
          analysisState.apiInsights = null;
          analysisState.apiSeries = null;
          analysisState.lastDataFetchAt = 0;
        }
      };

      row.addEventListener('click', (event) => {
        if (event.target instanceof Element && event.target.closest('.wallet-remove')) return;
        activateWalletFromRow();
        q(selectors.analysisQuickCard)?.click();
      });
      row.addEventListener('keydown', (event) => {
        if (event.key !== 'Enter' && event.key !== ' ') return;
        if (event.target instanceof Element && event.target.closest('.wallet-remove')) return;
        event.preventDefault();
        activateWalletFromRow();
        q(selectors.analysisQuickCard)?.click();
      });
    });

    list.querySelectorAll('button.wallet-remove[data-wallet-id]').forEach((btn) => {
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
        await loadDashboardOverview();
        await Promise.all([
          loadWallets(),
          loadUpcomingPayments()
        ]);
      });
    });
    const expandBtn = list.querySelector('.wallets-expand-toggle');
    if (expandBtn) {
      expandBtn.addEventListener('click', () => {
        walletListExpanded = !walletListExpanded;
        renderWallets(wallets);
      });
    }
    updateGetStartedSection();
  }

  async function loadWallets() {
    const list = document.querySelector(selectors.walletsList);
    if (list) {
      list.classList.remove('is-empty');
      setUiState(list, 'loading');
      list.innerHTML = renderSkeletonList(2);
    }
    setPanelFeedback(selectors.walletsFeedback, '');
    const res = await Api.call('/api/crypto/wallets/summary', 'GET', null, true);
    if (!res.ok) {
      dashboardDataState.walletsLoaded = true;
      dashboardDataState.hasWallets = false;
      if (list) {
        setUiState(list, 'error');
        list.innerHTML = renderErrorState(t('wallets_loading_failed'), t('cta_retry'), 'retry-wallets');
      }
      setPanelFeedback(selectors.walletsFeedback, t('wallets_loading_failed'), true);
      cryptoWalletTotalInBase = NaN;
      analysisState.walletsCount = 0;
      updateGetStartedSection();
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
        await loadDashboardOverview();
        await Promise.all([
          loadWallets(),
          loadUpcomingPayments()
        ]);
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
        await loadDashboardOverview();
        await Promise.all([
          loadBalance(),
          loadWallets()
        ]);
        await Promise.all([
          loadReports(),
          loadUpcomingPayments()
        ]);
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
        await loadDashboardOverview();
        await Promise.all([
          loadBalance(),
          loadReports(),
          loadUpcomingPayments()
        ]);
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
      bindTopNavState();
      bindLangToggle();
      initMotionController();
      initScrollReveal();
      bindActionCtas();
      updateActionPriority();
      bindAnalysisQuickCard();
      bindTxPeriodButtons();
      bindBalanceTrendControls();
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
      bindRefresh();
      bindBaseCurrencyMenu();
      await loadDashboardOverview();

      await loadBalance();
      await loadFxCurrencies();
      bindFxControls();
      await Promise.all([
        loadTransactionCategories(),
        loadWallets()
      ]);
      await Promise.all([
        loadReports(),
        loadRecentTransactions(),
        loadUpcomingPayments()
      ]);
      if (dashboardRoot) dashboardRoot.dataset.uiState = 'ready';
    } catch (e) {
      console.error('Dashboard init failed', e);
      if (dashboardRoot) dashboardRoot.dataset.uiState = 'error';
    }
  });
})();
