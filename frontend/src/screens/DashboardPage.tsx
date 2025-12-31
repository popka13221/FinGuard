import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Button } from '../components/Button';
import { AuthApi } from '../api/auth';
import { ApiClient } from '../api/client';
import { AccountsApi, type AccountBalance, type CurrencyBalance } from '../api/accounts';
import { FxApi } from '../api/fx';
import { BalanceChart } from '../components/BalanceChart';
import { PieChart, type PieChartItem } from '../components/PieChart';
import '../theme.css';
import './DashboardPage.css';
import { useTheme } from '../hooks/useTheme';

type Breakdown = { id: number; name: string; spent: number; limit: number };
type Goal = { id: number; title: string; progress: number; target: string };
type Payment = { id: number; title: string; amount: number; due: string };
type Activity = { id: number; title: string; tag: string; time: string; amount?: number };
type FxWatchItem = { code: string; name: string };

const fxWatchlist: FxWatchItem[] = [
  { code: 'USD', name: 'US Dollar' },
  { code: 'EUR', name: 'Euro' },
  { code: 'RUB', name: 'Russian Ruble' },
  { code: 'CNY', name: 'Chinese Yuan' },
];
const fxFallbackBase = 'USD';

const DashboardPage: React.FC = () => {
  const [email, setEmail] = useState<string>('user');
  const [fullName, setFullName] = useState<string>('Владелец кошелька');
  const [baseCurrency, setBaseCurrency] = useState<string>('USD');
  const [accounts, setAccounts] = useState<AccountBalance[]>([]);
  const [totalsByCurrency, setTotalsByCurrency] = useState<CurrencyBalance[]>([]);
  const [balanceError, setBalanceError] = useState<string>('');
  const [balanceLoading, setBalanceLoading] = useState<boolean>(true);
  const [fxRates, setFxRates] = useState<Record<string, number>>({});
  const [fxAsOf, setFxAsOf] = useState<string>('');
  const [fxError, setFxError] = useState<string>('');
  const [fxLoading, setFxLoading] = useState<boolean>(true);
  const [isAddMenuOpen, setIsAddMenuOpen] = useState<boolean>(false);
  const isMountedRef = useRef<boolean>(false);
  const addMenuLastFocusRef = useRef<HTMLElement | null>(null);
  const addMenuDialogRef = useRef<HTMLDivElement | null>(null);
  const { theme, toggle: toggleTheme } = useTheme();

  const breakdown: Breakdown[] = [
    { id: 1, name: 'Еда и кафе', spent: 320, limit: 600 },
    { id: 2, name: 'Транспорт', spent: 90, limit: 200 },
    { id: 3, name: 'Подписки', spent: 45, limit: 80 },
    { id: 4, name: 'Здоровье', spent: 60, limit: 150 },
  ];

  const goals: Goal[] = [
    { id: 1, title: 'Фонд 3 месяцев', progress: 62, target: '3 000' },
    { id: 2, title: 'Путешествие', progress: 35, target: '2 500' },
  ];

  const payments: Payment[] = [
    { id: 1, title: 'Аренда', amount: -700, due: '15 янв' },
    { id: 2, title: 'Spotify', amount: -4.99, due: '19 янв' },
    { id: 3, title: 'Мобильная связь', amount: -15, due: '22 янв' },
  ];

  const activities: Activity[] = [
    { id: 1, title: 'Создан бюджет «Еда и кафе»', tag: 'Бюджеты', time: '2 мин назад' },
    { id: 2, title: 'Подключён счёт «Travel»', tag: 'Счета', time: '1 час назад' },
    { id: 3, title: 'Оплачен Netflix', tag: 'Подписки', time: 'Вчера', amount: -10.99 },
  ];

  const balanceTrendSeries = useMemo(() => [18200, 18750, 19340, 18900, 20200, 20950], []);
  const expensePieItems: PieChartItem[] = useMemo(() => {
    const colors = ['#4f8bff', '#10b981', '#f97316', '#3cc7c4', '#9aa0aa'];
    return breakdown.map((b, idx) => ({
      label: b.name,
      value: b.spent,
      color: colors[idx % colors.length],
    }));
  }, [breakdown]);

  useEffect(() => {
    isMountedRef.current = true;
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  const loadBalance = useCallback(async () => {
    if (!isMountedRef.current) return;
    setBalanceLoading(true);
    setBalanceError('');
    try {
      const res = await AccountsApi.balance();
      if (!isMountedRef.current) return;
      if (res.ok && res.data) {
        setAccounts(res.data.accounts || []);
        setTotalsByCurrency(res.data.totalsByCurrency || []);
        setBalanceError('');
      } else {
        setBalanceError('Не удалось загрузить баланс');
      }
    } catch {
      if (!isMountedRef.current) return;
      setBalanceError('Не удалось загрузить баланс');
    } finally {
      if (isMountedRef.current) {
        setBalanceLoading(false);
      }
    }
  }, []);

  const loadFxRates = useCallback(async (base: string, quotes: string[]) => {
    if (!isMountedRef.current) return;
    setFxLoading(true);
    setFxError('');
    try {
      const res = await FxApi.latestRates(base, quotes);
      if (!isMountedRef.current) return;
      if (res.ok && res.data) {
        setFxRates(res.data.rates || {});
        setFxAsOf(res.data.asOf || '');
        setFxError('');
      } else {
        setFxError('Не удалось загрузить курсы');
      }
    } catch {
      if (!isMountedRef.current) return;
      setFxError('Не удалось загрузить курсы');
    } finally {
      if (isMountedRef.current) {
        setFxLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    const loadProfile = async () => {
      const res = await AuthApi.profile();
      if (!isMountedRef.current) return;
      if (!res.ok) {
        window.location.href = '/auth';
        return;
      }
      if (res.data && (res.data as any).email) {
        const profile = res.data as any;
        setEmail(profile.email);
        setFullName(profile.fullName || 'Владелец кошелька');
        setBaseCurrency(profile.baseCurrency || 'USD');
        ApiClient.setEmail(profile.email);
      }
      await loadBalance();
    };

    loadProfile();
  }, [loadBalance]);

  const logout = async () => {
    await AuthApi.logout();
    ApiClient.clearEmail();
    window.location.href = '/auth';
  };

  const formatMoney = (value: number, currency?: string) =>
    `${value < 0 ? '-' : ''}${Math.abs(value).toLocaleString('ru-RU', {
      minimumFractionDigits: 2,
    })} ${currency || baseCurrency}`;

  const fxBase = useMemo(
    () => (fxWatchlist.some((item) => item.code === baseCurrency) ? baseCurrency : fxFallbackBase),
    [baseCurrency]
  );
  const fxQuotes = useMemo(
    () => fxWatchlist.map((item) => item.code).filter((code) => code !== fxBase),
    [fxBase]
  );
  const formatFxRate = (value?: number) => {
    if (typeof value !== 'number') return '—';
    const digits = value < 1 ? 4 : 2;
    return value.toLocaleString('ru-RU', { minimumFractionDigits: digits, maximumFractionDigits: digits });
  };
  const fxUpdatedLabel = fxAsOf
    ? new Date(fxAsOf).toLocaleString('ru-RU', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })
    : '';

  useEffect(() => {
    if (!fxBase) return;
    loadFxRates(fxBase, fxQuotes);
  }, [fxBase, fxQuotes, loadFxRates]);

  const activeAccounts = useMemo(() => accounts.filter((a) => !a.archived), [accounts]);
  const archivedCount = accounts.length - activeAccounts.length;
  const creditUsed = activeAccounts.filter((c) => c.balance < 0).reduce((acc, c) => acc + Math.abs(c.balance), 0);
  const monthlyIn: number = 4200;
  const monthlyOut: number = 2350;
  const savingsRate = monthlyIn === 0 ? 0 : Math.round(((monthlyIn - monthlyOut) / monthlyIn) * 100);
  const baseTotal = totalsByCurrency.find((t) => t.currency === baseCurrency);
  const primaryTotal = baseTotal?.total ?? totalsByCurrency[0]?.total ?? activeAccounts.reduce((acc, item) => acc + item.balance, 0);
  const primaryCurrency = baseTotal?.currency ?? totalsByCurrency[0]?.currency ?? baseCurrency;
  const balanceStatusText = balanceLoading ? 'Обновляем данные…' : balanceError ? 'Ошибка обновления.' : 'Данные обновлены.';
  const fxItems = useMemo(
    () => fxQuotes.map((code) => {
      const meta = fxWatchlist.find((item) => item.code === code);
      return { code, name: meta?.name || code, rate: fxRates[code] };
    }),
    [fxQuotes, fxRates]
  );
  const fxStatusText = fxLoading
    ? 'Обновляем…'
    : fxError
      ? 'Нет данных'
      : fxUpdatedLabel
        ? `Обновлено ${fxUpdatedLabel}`
        : 'Обновлено';

  const closeAddMenu = useCallback(() => {
    setIsAddMenuOpen(false);
    addMenuLastFocusRef.current?.focus?.();
  }, []);
  const openAddMenu = useCallback(() => {
    addMenuLastFocusRef.current = (document.activeElement as HTMLElement | null) ?? null;
    setIsAddMenuOpen(true);
  }, []);

  useEffect(() => {
    if (!isAddMenuOpen) return;
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') closeAddMenu();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [closeAddMenu, isAddMenuOpen]);

  useEffect(() => {
    if (!isAddMenuOpen) return;
    const body = document.body;
    const prevOverflow = body.style.overflow;
    body.style.overflow = 'hidden';
    addMenuDialogRef.current?.focus();
    return () => {
      body.style.overflow = prevOverflow;
    };
  }, [isAddMenuOpen]);

  return (
    <div className="app">
      <div className="container dash-container">
        <header className="card dash-surface dash-topbar">
          <div className="dash-brand">
            <img className="dash-logo" src="/app/assets/white-big-logo.svg" alt="Smart Wallet" />
            <div>
              <div className="dash-brand-name">Smart Wallet</div>
              <div className="muted">{email}</div>
            </div>
          </div>
          <div className="actions dash-actions">
            <div className="chip">Валюта: {baseCurrency}</div>
            <button
              type="button"
              className="icon-btn"
              onClick={toggleTheme}
              aria-label="Переключить тему"
              title={theme === 'light' ? 'Светлая тема' : 'Тёмная тема'}
            >
              {theme === 'light' ? 'Light' : 'Dark'}
            </button>
            <Button variant="ghost" onClick={logout}>Выйти</Button>
          </div>
        </header>

        <section className="hero-area dash-hero" aria-label="Обзор">
          <div className="hero-grid">
            <div>
              <div className="badge">Личный кабинет</div>
              <h1 className="dash-title">Привет, {fullName}</h1>
              <p className="hero-sub">Сводка по счетам, расходам и целям в одном месте.</p>
            </div>
            <div className="dash-hero-right">
              <div className="mini-chart" aria-hidden="true" />
              <div className="muted" style={{ marginTop: 8 }}>
                {balanceStatusText}
              </div>
            </div>
          </div>
        </section>

        <section className="dash-kpis" aria-label="Ключевые показатели">
          <div className="card dash-surface dash-kpi">
            <div className="muted">Баланс</div>
            <div className="dash-kpi-value" data-testid="total-balance">
              {balanceLoading ? 'Загрузка…' : formatMoney(primaryTotal, primaryCurrency)}
            </div>
            <div className="muted">Кредит: {formatMoney(-creditUsed, primaryCurrency)}</div>
            {totalsByCurrency.length > 1 && (
              <div className="dash-kpi-meta">
                {totalsByCurrency.map((t) => (
                  <span key={t.currency} className="dash-currency-chip">
                    {t.currency}: {t.total.toFixed(2)}
                  </span>
                ))}
              </div>
            )}
          </div>

          <div className="card dash-surface dash-kpi">
            <div className="muted">Поток за месяц</div>
            <div className="dash-kpi-value">{formatMoney(monthlyIn - monthlyOut)}</div>
            <div className="muted">Доход: {formatMoney(monthlyIn)} · Расход: {formatMoney(monthlyOut)}</div>
            <div className="dash-kpi-meta">
              <span className="dash-currency-chip">Сбережения: {savingsRate}%</span>
            </div>
          </div>

          <div className="card dash-surface dash-kpi">
            <div className="muted">Счета</div>
            <div className="dash-kpi-value">{activeAccounts.length}</div>
            <div className="muted">Активных: {activeAccounts.length} · Архив: {archivedCount}</div>
            <div className="dash-kpi-meta">
              <span className="dash-currency-chip">Базовая: {baseCurrency}</span>
            </div>
          </div>
        </section>

        <main className="dash-grid">
          <section className="stack">
            <div className="card dash-surface dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Счета</h2>
                <div className="dash-card-actions">
                  <Button onClick={openAddMenu}>Добавить</Button>
                  <Button variant="ghost" onClick={loadBalance} disabled={balanceLoading}>Обновить</Button>
                </div>
              </div>
              <div className="list dash-list">
                {balanceLoading && (
                  <>
                    <div className="muted">Загружаем баланс…</div>
                    <div className="dash-skeleton-list" aria-hidden="true">
                      <div className="dash-skeleton-item" />
                      <div className="dash-skeleton-item" />
                      <div className="dash-skeleton-item" />
                    </div>
                  </>
                )}
                {!balanceLoading && balanceError && <div className="amount-negative">{balanceError}</div>}
                {!balanceLoading && !balanceError && accounts.length === 0 && (
                  <div className="muted">Счета пока не добавлены.</div>
                )}
                {!balanceLoading && !balanceError && accounts.map((card) => (
                  <div key={card.id} className="list-item dash-list-item">
                    <div className="dash-account">
                      <div className="dash-account-name">{card.name}</div>
                      <div className="dash-account-meta">
                        <span className="muted">{card.currency}</span>
                        {card.archived && <span className="dash-tag">Архив</span>}
                      </div>
                    </div>
                    <div className={card.balance >= 0 ? 'amount-positive' : 'amount-negative'}>
                      {formatMoney(card.balance, card.currency)}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card dash-surface dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Баланс · тренд</h2>
                <div className="pill-soft">6 месяцев</div>
              </div>
              <div style={{ marginTop: 10 }}>
                <BalanceChart series={balanceTrendSeries} currency={primaryCurrency} />
              </div>
            </div>

            <div className="card dash-surface dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Расходы</h2>
                <div className="pill-soft">Период: месяц</div>
              </div>
              <div style={{ marginTop: 10 }}>
                <PieChart items={expensePieItems} currency={baseCurrency} />
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {breakdown.map((b) => {
                  const pct = Math.min(100, Math.round((b.spent / b.limit) * 100));
                  return (
                    <div key={b.id} className="progress-wrap">
                      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <div style={{ fontWeight: 800 }}>{b.name}</div>
                          <small>{formatMoney(b.spent)} из {formatMoney(b.limit)}</small>
                        </div>
                        <div className={pct >= 90 ? 'amount-negative' : 'amount-positive'}>{pct}%</div>
                      </div>
                      <div className="progress-bar">
                        <div
                          className="progress-fill"
                          style={{ ['--pct' as any]: pct / 100 } as React.CSSProperties}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </section>

          <section className="stack">
            <div className="card dash-surface dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Цели</h2>
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {goals.map((g) => (
                  <div key={g.id} className="progress-wrap">
                    <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 800 }}>{g.title}</div>
                        <small>Цель: {g.target} {baseCurrency}</small>
                      </div>
                      <div className="chip">{g.progress}%</div>
                    </div>
                    <div className="progress-bar">
                      <div
                        className="progress-fill"
                        style={{ ['--pct' as any]: g.progress / 100 } as React.CSSProperties}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card dash-surface dash-card dash-fx-card">
              <div className="dash-card-header">
                <div>
                  <div className="badge">FX</div>
                  <h2 className="dash-card-title" style={{ marginTop: 6 }}>Курсы валют</h2>
                  <div className="muted">База: {fxBase}</div>
                </div>
                <div className="pill-soft">{fxStatusText}</div>
              </div>
              <div className="dash-fx-grid">
                {fxLoading && (
                  <div className="dash-skeleton-list" aria-hidden="true">
                    <div className="dash-skeleton-item" />
                    <div className="dash-skeleton-item" />
                    <div className="dash-skeleton-item" />
                  </div>
                )}
                {!fxLoading && fxError && <div className="amount-negative">{fxError}</div>}
                {!fxLoading && !fxError && fxItems.map((item) => (
                  <div key={item.code} className="dash-fx-row">
                    <div className="dash-fx-info">
                      <div className="dash-fx-code">{item.code}</div>
                      <div className="dash-fx-name">{item.name}</div>
                    </div>
                    <div className="dash-fx-rate">
                      {formatFxRate(item.rate)}
                      <span className="dash-fx-unit">{item.code}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card dash-surface dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Платежи</h2>
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {payments.map((p) => (
                  <div key={p.id} className="list-item dash-list-item">
                    <div>
                      <div style={{ fontWeight: 800 }}>{p.title}</div>
                      <small>Срок: {p.due}</small>
                    </div>
                    <div className="amount-negative">{formatMoney(p.amount)}</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card dash-surface dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Активность</h2>
              </div>
              <div className="timeline" style={{ marginTop: 10 }}>
                {activities.map((a) => (
                  <div key={a.id} className="timeline-item">
                    <div className="timeline-dot" />
                    <div className="dash-activity-main">
                      <div style={{ fontWeight: 800 }}>{a.title}</div>
                      <div className="dash-activity-meta">
                        <span className="dash-tag">{a.tag}</span>
                        <span className="muted">{a.time}</span>
                      </div>
                    </div>
                    <div className={a.amount && a.amount < 0 ? 'amount-negative' : 'muted'}>
                      {a.amount ? formatMoney(a.amount) : ''}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </section>
        </main>
      </div>

      <div
        id="add-account-overlay"
        className={isAddMenuOpen ? 'dash-overlay dash-overlay-open' : 'dash-overlay'}
        style={{ display: isAddMenuOpen ? 'flex' : 'none' }}
        onMouseDown={(e) => {
          if (e.target === e.currentTarget) closeAddMenu();
        }}
        aria-hidden={!isAddMenuOpen}
      >
        <div
          ref={addMenuDialogRef}
          className="dash-modal"
          role="dialog"
          tabIndex={-1}
          aria-modal="true"
          aria-labelledby="add-account-title"
        >
          <div className="dash-modal-header">
            <div>
              <div className="muted" style={{ fontSize: 12 }}>Новый счёт</div>
              <h3 id="add-account-title" style={{ margin: 0 }}>Выберите способ</h3>
            </div>
            <button type="button" className="icon-btn" onClick={closeAddMenu} aria-label="Закрыть">×</button>
          </div>

          <div className="dash-modal-grid">
            <button type="button" className="dash-option" onClick={closeAddMenu}>
              <div className="dash-option-title">Счёт вручную</div>
              <div className="muted">Быстро добавить наличные / депозит</div>
            </button>
            <button type="button" className="dash-option" onClick={closeAddMenu}>
              <div className="dash-option-title">Карта</div>
              <div className="muted">Подключить карту или банковский счёт</div>
            </button>
            <button type="button" className="dash-option" onClick={closeAddMenu}>
              <div className="dash-option-title">Импорт</div>
              <div className="muted">Загрузить операции из файла</div>
            </button>
          </div>

          <div className="dash-modal-footer">
            <Button variant="ghost" onClick={closeAddMenu}>Закрыть</Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
