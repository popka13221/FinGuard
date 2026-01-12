(() => {
  const LANG_STORAGE_KEY = 'finguard:lang';
  const SUPPORTED = new Set(['en', 'ru']);

  const I18N = {
    ru: {
      // Language toggle (button label is handled separately).
      lang_title_en: 'English',
      lang_title_ru: 'Русский',
      lang_aria_to_en: 'Переключить язык на английский',
      lang_aria_to_ru: 'Переключить язык на русский',

      // Landing (index.html)
      landing_title: 'FinGuard — Финансы под контролем',
      landing_nav_overview: 'Обзор',
      landing_nav_rules: 'Правила',
      landing_nav_goals: 'Цели',
      landing_nav_onchain: 'On-chain',
      landing_nav_roadmap: 'Дорожная карта',
      landing_nav_security: 'Безопасность',
      landing_nav_faq: 'FAQ',
      landing_open_demo: 'Открыть демо',
      landing_hero_title: 'FinGuard. Личные финансы под контролем.',
      landing_hero_subtitle: 'Учет, цели и правила, которые предупреждают раньше, чем появляется проблема.',
      landing_microline: 'Правила и уведомления · Валюты и активы · On-chain аудит',

      landing_mock_total_title: 'Итого на счетах',
      landing_mock_total_sub: '$22 650 · сумма по всем счетам',
      landing_mock_rules_title: 'Активные правила',
      landing_mock_rules_sub: 'лимиты · пороги · подписки',
      landing_mock_rates_title: 'Курсы и портфель',
      landing_mock_rates_sub: 'BTC/USD 118936,62 · портфель +2,4%',
      landing_mock_alert1_title: 'Лимит на подписки',
      landing_mock_alert1_sub: 'Потрачено 78% · предупредим при 90%.',
      landing_mock_alert1_cta: 'Настроить',
      landing_mock_alert2_title: 'Порог по USD/RUB',
      landing_mock_alert2_sub: '96,20 достигнут · отправлено в Telegram.',
      landing_mock_alert2_cta: 'История',
      landing_mock_fund_title: 'Финансовая подушка',
      landing_mock_fund_sub: 'Готово 62% · осталось $3,2k · темп $480/мес.',

      landing_intro_title: 'Не просто учет. Это система контроля бюджета.',
      landing_intro_sub: 'FinGuard собирает счета, транзакции и отчеты в одном месте — и добавляет главное: правила. Вы задаете условия, и система сама следит и уведомляет.',
      landing_flow_data: 'Данные',
      landing_flow_rules: 'Правила',
      landing_flow_alerts: 'Уведомления',

      landing_goals_title: 'Главное — три вещи.',
      landing_goal_rules_pill: 'Правила',
      landing_goal_rules_title: 'Правила, которые работают за вас.',
      landing_goal_rules_sub: 'Лимиты, пороги и повторяющиеся списания — сигнал приходит, когда ещё можно спокойно исправить.',
      landing_goal_goals_pill: 'Цели',
      landing_goal_goals_title: 'Цели с понятным прогрессом.',
      landing_goal_goals_sub: 'Срок, сумма, темп — FinGuard показывает, сколько откладывать в месяц, чтобы дойти до цели.',
      landing_goal_assets_pill: 'Валюты и активы',
      landing_goal_assets_title: 'Валюты и активы — в одной панели.',
      landing_goal_assets_sub: 'Курсы и изменения — чтобы видеть ситуацию целиком, не переключаясь между сервисами.',

      landing_rules_title: 'Уведомления, которые не шумят — и срабатывают по делу.',
      landing_rules_card1_title: 'Правила, которые работают сами.',
      landing_rules_card1_sub: 'Условие + расписание — FinGuard проверяет и даёт сигнал, когда важно среагировать.',
      landing_rules_card2_title: 'Лимиты по категориям.',
      landing_rules_card2_sub: '“Еда”, “Транспорт”, “Подписки” — уведомление приходит, когда вы подходите к лимиту.',
      landing_rules_card3_title: 'Пороги по курсам и активам.',
      landing_rules_card3_sub: 'Когда курс достигает порога — вы узнаёте об этом сразу.',
      landing_rules_card4_title: 'Регулярные списания.',
      landing_rules_card4_sub: 'Выделяет регулярные списания отдельно — чтобы подписки не терялись в ленте.',

      landing_how_title: 'Три шага — и контроль включён.',
      landing_how_structure_pill: 'Структура',
      landing_how_structure_sub: 'Создайте счета, категории и базовую валюту.',
      landing_how_transactions_pill: 'Транзакции',
      landing_how_transactions_sub: 'Добавляйте доходы и траты.',
      landing_how_rules_pill: 'Правила',
      landing_how_rules_sub: 'Включите правила и уведомления. FinGuard следит — вы решаете.',

      landing_modules_title: 'Возможности',
      landing_module_accounts_title: 'Счета и транзакции',
      landing_module_accounts_sub: 'История, фильтры и актуальный баланс.',
      landing_module_reports_title: 'Отчеты',
      landing_module_reports_sub: 'Сводка периода, cash-flow, категории.',
      landing_module_goals_title: 'Цели',
      landing_module_goals_sub: 'Прогресс, срок и расчет темпа.',
      landing_module_rules_title: 'Правила',
      landing_module_rules_sub: 'Лимиты, пороги и условия срабатывания.',
      landing_module_notifications_title: 'Уведомления',
      landing_module_notifications_sub: 'Email, Telegram и встроенные уведомления.',
      landing_module_assets_title: 'Валюты и активы',
      landing_module_assets_sub: 'FX и цены активов, изменения, пороги.',

      landing_onchain_title: 'On-chain аудит: проверяемость без раскрытия деталей.',
      landing_onchain_audit_pill: 'Метки аудита',
      landing_onchain_audit_sub: 'Фиксация контрольного отпечатка отчета/правил.',
      landing_onchain_policy_pill: 'Политики кошелька',
      landing_onchain_policy_sub: 'Лимиты и правила могут исполняться на уровне Vault.',
      landing_onchain_safe_goals_pill: 'Сейф-цели',
      landing_onchain_safe_goals_sub: 'Цель может быть escrow с условиями разблокировки.',

      landing_perspective_title: 'Один продукт — несколько сценариев роста.',
      landing_persp_personal_title: 'Для личного использования',
      landing_persp_personal_sub: 'Контроль бюджета и привычек: учет + цели + правила.',
      landing_persp_team_title: 'Для команд',
      landing_persp_team_sub: 'Бюджеты, роли и лимиты — контроль расходов по направлениям.',
      landing_persp_module_title: 'Как модуль для систем',
      landing_persp_module_sub: 'Rules/Alerts engine: события → правила → действия. Подходит для финтеха и внутренних систем контроля.',
      landing_persp_platform_title: 'Как платформа',
      landing_persp_platform_sub: 'Интеграции, расширяемость, документация.',

      landing_roadmap_title: 'Дорожная карта. Поэтапно и прозрачно.',
      landing_roadmap_v1_title: 'Основа',
      landing_roadmap_v1_sub: 'Счета · Транзакции · Отчеты · Базовые правила · Уведомления',
      landing_roadmap_v2_title: 'Инсайты',
      landing_roadmap_v2_sub: 'Цели · Подписки · Расширенные правила · Улучшенная аналитика',
      landing_roadmap_v3_title: 'Интеграции',
      landing_roadmap_v3_sub: 'Импорт CSV · Провайдеры FX · Активы · Webhooks',
      landing_roadmap_v4_title: 'On-chain',
      landing_roadmap_v4_sub: 'Метки аудита · Политики Vault · Escrow-цели',

      landing_security_title: 'Безопасность — часть продукта.',
      landing_security_access_title: 'Доступ',
      landing_security_access_sub: 'Защищенные сессии и контроль действий.',
      landing_security_recovery_title: 'Восстановление',
      landing_security_recovery_sub: 'Надежные потоки верификации и reset.',
      landing_security_privacy_title: 'Приватность',
      landing_security_privacy_sub: 'Контроль данных и минимизация сведений в интеграциях.',

      landing_faq_title: 'FAQ',
      landing_faq_q1: 'Это банк?',
      landing_faq_a1: 'Нет. FinGuard не хранит деньги и не проводит платежи — он помогает учитывать, планировать и контролировать.',
      landing_faq_q2: 'Где хранятся данные?',
      landing_faq_a2: 'Локально или на вашем сервере — вы контролируете данные.',
      landing_faq_q3: 'Как работают правила?',
      landing_faq_a3: 'Вы задаёте правило (лимит/порог/период). FinGuard проверяет по расписанию и отправляет уведомление.',
      landing_faq_q4: 'Какие уведомления есть?',
      landing_faq_a4: 'Встроенные уведомления, а также Email и Telegram (по настройке).',
      landing_faq_q5: 'Есть импорт транзакций?',
      landing_faq_a5: 'Импорт CSV — в разработке. Банковские интеграции — отдельный этап.',
      landing_faq_q6: 'Поддерживаются валюты и активы?',
      landing_faq_a6: 'Да: курсы, изменения, пороги и уведомления.',
      landing_faq_q7: 'Как начать?',
      landing_faq_a7: 'Откройте демо или разверните проект через Docker.',

      landing_cta_title: 'Подключите FinGuard сейчас.',
      landing_footer_tagline: 'FinGuard — страница продукта без лишнего шума.',
      landing_footer_privacy: 'Приватность',
      landing_footer_terms: 'Условия',
      landing_footer_contact: 'Telegram',

      // Auth: login/register
      login_title: 'Smart Wallet | Вход',
      login_hero_title: 'Ваш фин. помощник',
      login_hero_sub: 'Войдите или зарегистрируйтесь, чтобы перейти в дашборд.',
      login_tab_login: 'Вход',
      login_tab_register: 'Регистрация',
      login_label_password: 'Пароль',
      login_password_placeholder: 'Пароль',
      login_password_help: 'Не менее 10 символов, верхний/нижний регистр, цифра и спецсимвол.',
      login_forgot: 'Забыли пароль?',
      login_otp_label: 'Код из письма',
      login_otp_sign_in: 'Войти',
      login_otp_resend: 'Отправить новый код',
      login_button: 'Войти',
      login_after: 'После входа или регистрации откроется дашборд.',

      reg_label_name: 'Имя',
      reg_name_placeholder: 'Ваше имя',
      reg_label_base_currency: 'Базовая валюта',
      reg_otp_hint: 'Мы отправили код на вашу почту.',
      reg_otp_confirm: 'Подтвердить',
      reg_create_account: 'Создать аккаунт',

      // Forgot/reset/verify
      forgot_title: 'Smart Wallet | Восстановление пароля',
      forgot_step: 'Восстановление доступа · Шаг 1',
      forgot_hero_title: 'Забыли пароль?',
      forgot_hero_sub: 'Введите email, мы отправим одноразовый код. После отправки введите код из письма и продолжите.',
      forgot_badge: 'Шаг 1 · Письмо',
      forgot_card_title: 'Отправить код',
      forgot_token_label: 'Код из письма',
      forgot_token_placeholder: 'Вставьте код',
      forgot_continue: 'Продолжить',
      forgot_send: 'Отправить код',
      back_to_login: 'Назад ко входу',

      reset_title: 'Smart Wallet | Смена пароля',
      reset_step: 'Восстановление доступа · Шаг 2',
      reset_hero_title: 'Смена пароля',
      reset_hero_sub: 'Введите код из письма и задайте новый пароль. Код одноразовый и действует ограниченное время.',
      reset_badge: 'Шаг 2 · Новый пароль',
      reset_card_title: 'Сменить пароль по коду',
      reset_new_password: 'Новый пароль',
      reset_new_password_placeholder: 'Новый пароль',
      reset_confirm_password: 'Повторите пароль',
      reset_confirm_password_placeholder: 'Ещё раз',
      reset_submit: 'Сменить пароль',
      go_to_login: 'Перейти ко входу',

      verify_title: 'Smart Wallet | Подтверждение email',
      verify_header: 'Подтверждение email',
      verify_hero_title: 'Подтвердите адрес',
      verify_hero_sub: 'Введите код из письма или перейдите по ссылке из письма. Если письмо не пришло — запросите новый код.',
      verify_badge: 'Код из письма',
      verify_card_title: 'Проверить код',
      verify_code_label: 'Код',
      verify_code_placeholder: 'Вставьте код',
      verify_confirm: 'Подтвердить',
      verify_request: 'Запросить код',

      // Forbidden
      forbidden_title: 'Доступ запрещен',
      forbidden_badge: 'Доступ запрещен',
      forbidden_header: 'Сессия недействительна',
      forbidden_sub: 'Сессия недействительна или у вас нет прав для этой страницы.',
      forbidden_back: 'Вернуться к входу',
      forbidden_hint: 'Если это ошибка, попробуйте перелогиниться.',

      // Dynamic messages (auth/recover/verify)
      msg_enter_email: 'Введите email',
      msg_enter_valid_email: 'Введите корректный email',
      msg_enter_password: 'Введите пароль',
      msg_password_requirements: 'Пароль должен быть не короче 10 символов, содержать верхний/нижний регистр, цифру и спецсимвол',
      msg_email_exists: 'Такой email уже зарегистрирован',
      msg_invalid_credentials: 'Неверный email или пароль',
      msg_account_locked: 'Аккаунт временно заблокирован. Попробуйте позже.',
      msg_email_not_verified: 'Email не подтвержден. Проверьте почту.',
      msg_too_many_attempts_wait: 'Слишком много попыток. Подождите {sec} сек.',
      msg_too_many_attempts_retry: 'Слишком много попыток. Попробуйте через {sec} сек.',
      msg_request_failed_try_later: 'Ошибка запроса. Попробуйте позже.',
      msg_enter_name: 'Введите имя',
      msg_select_base_currency: 'Укажите базовую валюту',
      msg_invalid_password_short: 'Пароль не соответствует требованиям',
      msg_email_and_password_to_resend: 'Введите email и пароль, чтобы отправить новый код.',
      msg_enter_email_and_retry_register: 'Введите email и повторите регистрацию, чтобы получить код.',
      msg_enter_email_code: 'Введите код из письма',
      msg_code_invalid_or_expired: 'Код недействителен или истек. Запросите новый код или попробуйте снова.',
      msg_code_invalid_short: 'Код неверный или истек. Попробуйте снова.',
      msg_verify_failed_retry: 'Не удалось подтвердить код. Попробуйте снова.',

      msg_currencies_loading: 'Загрузка...',
      msg_currencies_load_failed: 'Не удалось загрузить валюты',
      msg_currencies_load_failed_full: 'Не удалось загрузить валюты. Попробуйте позже.',

      msg_send_code_again: 'Отправить код повторно ({sec} сек.)',
      msg_send_code: 'Отправить код',
      msg_show: 'Показать',
      msg_hide: 'Скрыть',
      toggle_password_visibility_aria: 'Показать или скрыть пароль',

      msg_forgot_too_many_attempts: 'Слишком много попыток. Попробуйте позже.',
      msg_forgot_confirm_failed: 'Не удалось подтвердить код. Попробуйте снова.',
      msg_forgot_code_invalid: 'Код неверный или устарел. Запросите новый.',
      msg_forgot_too_many_wrong_attempts: 'Слишком много неверных попыток. Запросите новый код.',
      msg_forgot_enter_email_to_reset: 'Введите email, мы отправим одноразовый код.',
      msg_forgot_enter_code: 'Введите код из письма',
      msg_forgot_code_too_short: 'Код слишком короткий',

      msg_reset_session_active: 'Сессия сброса активна: {time}',
      msg_reset_session_expired: 'Сессия сброса истекла. Введите код снова.',
      msg_reset_password_mismatch: 'Пароли не совпадают',
      msg_reset_password_updated: 'Пароль обновлён. Теперь можно войти.',
      msg_reset_code_missing: 'Код не найден. Вернитесь и запросите новый.',
      msg_reset_enter_email_for_code: 'Укажите email, на который пришел код.',
      msg_reset_confirm_code_first: 'Сначала подтвердите код из письма.',
      msg_enter_new_password: 'Введите новый пароль',
      msg_repeat_password: 'Повторите пароль',
      msg_forgot_send_failed: 'Не получилось отправить письмо. Попробуйте ещё раз позже.',
      msg_reset_session_invalid: 'Сессия сброса устарела или уже использована. Запросите новый код.',
      msg_reset_failed: 'Не удалось обновить пароль. Проверьте данные или повторите позже.',

      msg_verify_sent_if_needed: 'Если email существует и не подтвержден — отправили код. Проверьте почту.',
      msg_verify_send_failed: 'Не удалось отправить код. Попробуйте позже.',
      msg_verify_code_invalid: 'Код недействителен или истек. Запросите новый.',
      msg_verify_success: 'Email подтвержден. Теперь можно войти.'
    },

    en: {
      lang_title_en: 'English',
      lang_title_ru: 'Русский',
      lang_aria_to_en: 'Switch language to English',
      lang_aria_to_ru: 'Switch language to Russian',

      // Landing (index.html)
      landing_title: 'FinGuard — Finance, guarded',
      landing_nav_overview: 'Overview',
      landing_nav_rules: 'Rules',
      landing_nav_goals: 'Goals',
      landing_nav_onchain: 'On-chain',
      landing_nav_roadmap: 'Roadmap',
      landing_nav_security: 'Security',
      landing_nav_faq: 'FAQ',
      landing_open_demo: 'Open demo',
      landing_hero_title: 'FinGuard. Personal finances under control.',
      landing_hero_subtitle: 'Tracking, goals, and rules that warn you before problems show up.',
      landing_microline: 'Rules & alerts · Currencies & assets · On-chain audit',

      landing_mock_total_title: 'Total across accounts',
      landing_mock_total_sub: '$22 650 · total across all accounts',
      landing_mock_rules_title: 'Active rules',
      landing_mock_rules_sub: 'limits · thresholds · subscriptions',
      landing_mock_rates_title: 'Rates & portfolio',
      landing_mock_rates_sub: 'BTC/USD 118,936.62 · portfolio +2.4%',
      landing_mock_alert1_title: 'Subscriptions limit',
      landing_mock_alert1_sub: 'Spent 78% · we will notify at 90%.',
      landing_mock_alert1_cta: 'Configure',
      landing_mock_alert2_title: 'USD/RUB threshold',
      landing_mock_alert2_sub: '96.20 reached · sent to Telegram.',
      landing_mock_alert2_cta: 'History',
      landing_mock_fund_title: 'Emergency fund',
      landing_mock_fund_sub: '62% done · $3.2k left · pace $480/mo',

      landing_intro_title: 'Not just tracking. A budget control system.',
      landing_intro_sub: 'FinGuard brings accounts, transactions, and reports into one place — and adds the most important part: rules. You set conditions, the system monitors and notifies.',
      landing_flow_data: 'Data',
      landing_flow_rules: 'Rules',
      landing_flow_alerts: 'Alerts',

      landing_goals_title: 'The core is three things.',
      landing_goal_rules_pill: 'Rules',
      landing_goal_rules_title: 'Rules that work for you.',
      landing_goal_rules_sub: 'Limits, thresholds, recurring charges — you get a signal while it’s still easy to react.',
      landing_goal_goals_pill: 'Goals',
      landing_goal_goals_title: 'Goals with clear progress.',
      landing_goal_goals_sub: 'Deadline, amount, pace — FinGuard shows how much to save per month to reach the goal.',
      landing_goal_assets_pill: 'Currencies & assets',
      landing_goal_assets_title: 'Currencies & assets in one panel.',
      landing_goal_assets_sub: 'Rates and changes — see the full picture without jumping between services.',

      landing_rules_title: 'Notifications that are quiet — and fire when it matters.',
      landing_rules_card1_title: 'Rules that run on their own.',
      landing_rules_card1_sub: 'Condition + schedule — FinGuard checks and signals when it’s time to react.',
      landing_rules_card2_title: 'Category limits.',
      landing_rules_card2_sub: '“Food”, “Transport”, “Subscriptions” — you get notified as you approach the limit.',
      landing_rules_card3_title: 'FX & asset thresholds.',
      landing_rules_card3_sub: 'When a rate hits a threshold — you know right away.',
      landing_rules_card4_title: 'Recurring charges.',
      landing_rules_card4_sub: 'Highlights recurring spending so subscriptions don’t get lost in the feed.',

      landing_how_title: 'Three steps — and control is on.',
      landing_how_structure_pill: 'Setup',
      landing_how_structure_sub: 'Create accounts, categories, and a base currency.',
      landing_how_transactions_pill: 'Transactions',
      landing_how_transactions_sub: 'Add income and expenses.',
      landing_how_rules_pill: 'Rules',
      landing_how_rules_sub: 'Enable rules and notifications. FinGuard watches — you decide.',

      landing_modules_title: 'Capabilities',
      landing_module_accounts_title: 'Accounts & transactions',
      landing_module_accounts_sub: 'History, filters, and current balance.',
      landing_module_reports_title: 'Reports',
      landing_module_reports_sub: 'Period summary, cash-flow, categories.',
      landing_module_goals_title: 'Goals',
      landing_module_goals_sub: 'Progress, deadline, pace calculation.',
      landing_module_rules_title: 'Rules',
      landing_module_rules_sub: 'Limits, thresholds, trigger conditions.',
      landing_module_notifications_title: 'Notifications',
      landing_module_notifications_sub: 'Email, Telegram, and in-app alerts.',
      landing_module_assets_title: 'Currencies & assets',
      landing_module_assets_sub: 'FX and asset prices, changes, thresholds.',

      landing_onchain_title: 'On-chain audit: verifiable without exposing details.',
      landing_onchain_audit_pill: 'Audit marks',
      landing_onchain_audit_sub: 'Anchor a fingerprint of reports/rules.',
      landing_onchain_policy_pill: 'Wallet policies',
      landing_onchain_policy_sub: 'Limits and rules can be enforced at the Vault level.',
      landing_onchain_safe_goals_pill: 'Safe goals',
      landing_onchain_safe_goals_sub: 'A goal can be an escrow with unlock conditions.',

      landing_perspective_title: 'One product — multiple growth paths.',
      landing_persp_personal_title: 'For personal use',
      landing_persp_personal_sub: 'Budget control and habits: tracking + goals + rules.',
      landing_persp_team_title: 'For teams',
      landing_persp_team_sub: 'Budgets, roles, limits — expense control by area.',
      landing_persp_module_title: 'As a system module',
      landing_persp_module_sub: 'Rules/Alerts engine: events → rules → actions. Fits fintech and internal control systems.',
      landing_persp_platform_title: 'As a platform',
      landing_persp_platform_sub: 'Integrations, extensibility, documentation.',

      landing_roadmap_title: 'Roadmap. Step-by-step and transparent.',
      landing_roadmap_v1_title: 'Foundation',
      landing_roadmap_v1_sub: 'Accounts · Transactions · Reports · Basic rules · Notifications',
      landing_roadmap_v2_title: 'Insights',
      landing_roadmap_v2_sub: 'Goals · Subscriptions · Advanced rules · Better analytics',
      landing_roadmap_v3_title: 'Integrations',
      landing_roadmap_v3_sub: 'CSV import · FX providers · Assets · Webhooks',
      landing_roadmap_v4_title: 'On-chain',
      landing_roadmap_v4_sub: 'Audit marks · Vault policies · Escrow goals',

      landing_security_title: 'Security is part of the product.',
      landing_security_access_title: 'Access',
      landing_security_access_sub: 'Secure sessions and action control.',
      landing_security_recovery_title: 'Recovery',
      landing_security_recovery_sub: 'Reliable verification and reset flows.',
      landing_security_privacy_title: 'Privacy',
      landing_security_privacy_sub: 'Data control and minimal exposure in integrations.',

      landing_faq_title: 'FAQ',
      landing_faq_q1: 'Is this a bank?',
      landing_faq_a1: 'No. FinGuard doesn’t hold money and doesn’t process payments — it helps you track, plan, and control.',
      landing_faq_q2: 'Where is the data stored?',
      landing_faq_a2: 'Locally or on your own server — you control the data.',
      landing_faq_q3: 'How do rules work?',
      landing_faq_a3: 'You define a rule (limit/threshold/period). FinGuard checks on a schedule and sends a notification.',
      landing_faq_q4: 'What notifications are supported?',
      landing_faq_a4: 'In-app notifications, plus Email and Telegram (configurable).',
      landing_faq_q5: 'Is transaction import available?',
      landing_faq_a5: 'CSV import is in progress. Banking integrations are a separate phase.',
      landing_faq_q6: 'Are currencies and assets supported?',
      landing_faq_a6: 'Yes: rates, changes, thresholds, and notifications.',
      landing_faq_q7: 'How do I start?',
      landing_faq_a7: 'Open the demo or run the project via Docker.',

      landing_cta_title: 'Start using FinGuard now.',
      landing_footer_tagline: 'FinGuard — product page without the noise.',
      landing_footer_privacy: 'Privacy',
      landing_footer_terms: 'Terms',
      landing_footer_contact: 'Telegram',

      // Auth: login/register
      login_title: 'Smart Wallet | Sign in',
      login_hero_title: 'Your finance assistant',
      login_hero_sub: 'Sign in or create an account to open the dashboard.',
      login_tab_login: 'Sign in',
      login_tab_register: 'Sign up',
      login_label_password: 'Password',
      login_password_placeholder: 'Password',
      login_password_help: 'At least 10 chars, upper/lower case, a digit, and a symbol.',
      login_forgot: 'Forgot password?',
      login_otp_label: 'Email code',
      login_otp_sign_in: 'Sign in',
      login_otp_resend: 'Resend code',
      login_button: 'Sign in',
      login_after: 'After sign in or sign up the dashboard opens.',

      reg_label_name: 'Name',
      reg_name_placeholder: 'Your name',
      reg_label_base_currency: 'Base currency',
      reg_otp_hint: 'We sent a code to your email.',
      reg_otp_confirm: 'Confirm',
      reg_create_account: 'Create account',

      // Forgot/reset/verify
      forgot_title: 'Smart Wallet | Password recovery',
      forgot_step: 'Account recovery · Step 1',
      forgot_hero_title: 'Forgot your password?',
      forgot_hero_sub: 'Enter your email and we will send a one-time code. After that, enter the code from the email and continue.',
      forgot_badge: 'Step 1 · Email',
      forgot_card_title: 'Send code',
      forgot_token_label: 'Email code',
      forgot_token_placeholder: 'Enter code',
      forgot_continue: 'Continue',
      forgot_send: 'Send code',
      back_to_login: 'Back to sign in',

      reset_title: 'Smart Wallet | New password',
      reset_step: 'Account recovery · Step 2',
      reset_hero_title: 'New password',
      reset_hero_sub: 'Enter the code from the email and choose a new password. The code is one-time and expires soon.',
      reset_badge: 'Step 2 · New password',
      reset_card_title: 'Reset password using a code',
      reset_new_password: 'New password',
      reset_new_password_placeholder: 'New password',
      reset_confirm_password: 'Confirm password',
      reset_confirm_password_placeholder: 'Repeat',
      reset_submit: 'Reset password',
      go_to_login: 'Go to sign in',

      verify_title: 'Smart Wallet | Email verification',
      verify_header: 'Email verification',
      verify_hero_title: 'Verify your email',
      verify_hero_sub: 'Enter the code from the email or follow the link in the email. If you didn’t receive it — request a new code.',
      verify_badge: 'Email code',
      verify_card_title: 'Verify code',
      verify_code_label: 'Code',
      verify_code_placeholder: 'Enter code',
      verify_confirm: 'Verify',
      verify_request: 'Request code',

      // Forbidden
      forbidden_title: 'Access denied',
      forbidden_badge: 'Access denied',
      forbidden_header: 'Session is invalid',
      forbidden_sub: 'Your session is invalid or you don’t have access to this page.',
      forbidden_back: 'Back to sign in',
      forbidden_hint: 'If this is a mistake, try signing in again.',

      // Dynamic messages (auth/recover/verify)
      msg_enter_email: 'Enter email',
      msg_enter_valid_email: 'Enter a valid email',
      msg_enter_password: 'Enter password',
      msg_password_requirements: 'Password must be at least 10 chars and include upper/lower case, a digit, and a symbol',
      msg_email_exists: 'This email is already registered',
      msg_invalid_credentials: 'Invalid email or password',
      msg_account_locked: 'Account is temporarily locked. Try again later.',
      msg_email_not_verified: 'Email is not verified. Check your inbox.',
      msg_too_many_attempts_wait: 'Too many attempts. Wait {sec} sec.',
      msg_too_many_attempts_retry: 'Too many attempts. Try again in {sec} sec.',
      msg_request_failed_try_later: 'Request failed. Try again later.',
      msg_enter_name: 'Enter name',
      msg_select_base_currency: 'Select a base currency',
      msg_invalid_password_short: 'Password does not meet requirements',
      msg_email_and_password_to_resend: 'Enter email and password to resend a code.',
      msg_enter_email_and_retry_register: 'Enter your email and retry registration to get a code.',
      msg_enter_email_code: 'Enter the code from the email',
      msg_code_invalid_or_expired: 'The code is invalid or expired. Request a new code or try again.',
      msg_code_invalid_short: 'The code is invalid or expired. Try again.',
      msg_verify_failed_retry: 'Verification failed. Try again.',

      msg_currencies_loading: 'Loading…',
      msg_currencies_load_failed: 'Failed to load currencies',
      msg_currencies_load_failed_full: 'Failed to load currencies. Try again later.',

      msg_send_code_again: 'Resend code ({sec}s)',
      msg_send_code: 'Send code',
      msg_show: 'Show',
      msg_hide: 'Hide',
      toggle_password_visibility_aria: 'Show or hide password',

      msg_forgot_too_many_attempts: 'Too many attempts. Try again later.',
      msg_forgot_confirm_failed: 'Failed to confirm the code. Try again.',
      msg_forgot_code_invalid: 'The code is invalid or expired. Request a new one.',
      msg_forgot_too_many_wrong_attempts: 'Too many wrong attempts. Request a new code.',
      msg_forgot_enter_email_to_reset: 'Enter your email and we will send a one-time code.',
      msg_forgot_enter_code: 'Enter the code from the email',
      msg_forgot_code_too_short: 'Code is too short',

      msg_reset_session_active: 'Reset session active: {time}',
      msg_reset_session_expired: 'Reset session expired. Enter the code again.',
      msg_reset_password_mismatch: 'Passwords do not match',
      msg_reset_password_updated: 'Password updated. You can sign in now.',
      msg_reset_code_missing: 'Code not found. Go back and request a new one.',
      msg_reset_enter_email_for_code: 'Enter the email that received the code.',
      msg_reset_confirm_code_first: 'Confirm the email code first.',
      msg_enter_new_password: 'Enter a new password',
      msg_repeat_password: 'Repeat password',
      msg_forgot_send_failed: 'Failed to send email. Try again later.',
      msg_reset_session_invalid: 'Reset session is expired or already used. Request a new code.',
      msg_reset_failed: 'Failed to update password. Check input or try again later.',

      msg_verify_sent_if_needed: 'If the email exists and is not verified — we sent a code. Check your inbox.',
      msg_verify_send_failed: 'Failed to send a code. Try again later.',
      msg_verify_code_invalid: 'The code is invalid or expired. Request a new one.',
      msg_verify_success: 'Email verified. You can sign in now.'
    }
  };

  function normalizeLang(raw) {
    if (!raw) return '';
    const value = String(raw).trim().toLowerCase();
    if (!value) return '';
    if (value.includes('-')) return value.split('-')[0];
    if (value.includes('_')) return value.split('_')[0];
    return value;
  }

  function detectBrowserLang() {
    const rawList = Array.isArray(navigator.languages) && navigator.languages.length
      ? navigator.languages
      : [navigator.language];
    for (const raw of rawList) {
      const normalized = normalizeLang(raw);
      if (SUPPORTED.has(normalized)) return normalized;
    }
    return 'en';
  }

  function loadLang() {
    try {
      const stored = localStorage.getItem(LANG_STORAGE_KEY);
      if (SUPPORTED.has(stored)) return stored;
    } catch (_) {
      // ignore
    }
    return detectBrowserLang();
  }

  let currentLang = loadLang();

  function t(key, vars) {
    const table = I18N[currentLang] || I18N.en;
    let value = table[key] ?? I18N.en[key] ?? key;
    if (vars && typeof value === 'string') {
      Object.entries(vars).forEach(([name, raw]) => {
        value = value.replaceAll(`{${name}}`, String(raw));
      });
    }
    return value;
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
    const buttons = document.querySelectorAll('#btn-lang,[data-lang-toggle]');
    if (!buttons.length) return;
    const isEn = currentLang === 'en';
    const next = isEn ? 'ru' : 'en';
    const titleKey = next === 'en' ? 'lang_title_en' : 'lang_title_ru';
    const ariaKey = next === 'en' ? 'lang_aria_to_en' : 'lang_aria_to_ru';
    buttons.forEach((btn) => {
      btn.textContent = next.toUpperCase();
      btn.setAttribute('title', t(titleKey));
      btn.setAttribute('aria-label', t(ariaKey));
    });
  }

  function applyLanguage(lang) {
    currentLang = lang === 'ru' ? 'ru' : 'en';
    document.documentElement.lang = currentLang;
    applyTranslations();
    updateLangToggle();
  }

  function setLang(lang) {
    const next = lang === 'ru' ? 'ru' : 'en';
    try {
      localStorage.setItem(LANG_STORAGE_KEY, next);
    } catch (_) {
      // ignore
    }
    window.location.reload();
  }

  function bindLangToggle() {
    const buttons = document.querySelectorAll('#btn-lang,[data-lang-toggle]');
    if (!buttons.length) return;
    buttons.forEach((btn) => {
      btn.addEventListener('click', () => {
        const next = currentLang === 'en' ? 'ru' : 'en';
        setLang(next);
      });
    });
  }

  function init() {
    applyLanguage(currentLang);
    bindLangToggle();
  }

  const api = {
    t,
    getLang: () => currentLang,
    setLang,
    applyTranslations,
    applyLanguage
  };

  window.I18n = api;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
