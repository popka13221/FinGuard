import React, { useEffect, useState } from 'react';
import { Button } from '../components/Button';
import { AuthApi } from '../api/auth';
import { ApiClient } from '../api/client';
import { AccountsApi, type AccountBalance, type CurrencyBalance } from '../api/accounts';
import '../theme.css';

type Breakdown = { id: number; name: string; spent: number; limit: number };
type Goal = { id: number; title: string; progress: number; target: string };
type Payment = { id: number; title: string; amount: number; due: string };
type Activity = { id: number; title: string; tag: string; time: string; amount?: number };

const DashboardPage: React.FC = () => {
  const [email, setEmail] = useState<string>('user');
  const [fullName, setFullName] = useState<string>('Владелец кошелька');
  const [baseCurrency, setBaseCurrency] = useState<string>('USD');
  const [accounts, setAccounts] = useState<AccountBalance[]>([]);
  const [totalsByCurrency, setTotalsByCurrency] = useState<CurrencyBalance[]>([]);
  const [balanceError, setBalanceError] = useState<string>('');
  const [balanceLoading, setBalanceLoading] = useState<boolean>(true);

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

  useEffect(() => {
    const loadBalance = async () => {
      setBalanceLoading(true);
      setBalanceError('');
      try {
        const res = await AccountsApi.balance();
        if (res.ok && res.data) {
          setAccounts(res.data.accounts || []);
          setTotalsByCurrency(res.data.totalsByCurrency || []);
          setBalanceError('');
        } else {
          setBalanceError('Не удалось загрузить баланс');
        }
      } catch (e) {
        setBalanceError('Не удалось загрузить баланс');
      } finally {
        setBalanceLoading(false);
      }
    };

    const loadProfile = async () => {
      const res = await AuthApi.profile();
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
  }, []);

  const logout = async () => {
    await AuthApi.logout();
    ApiClient.clearEmail();
    window.location.href = '/auth';
  };

  const formatMoney = (value: number, currency?: string) =>
    `${value < 0 ? '-' : ''}${Math.abs(value).toLocaleString('ru-RU', {
      minimumFractionDigits: 2,
    })} ${currency || baseCurrency}`;

  const totalBalance = accounts.reduce((acc, item) => acc + item.balance, 0);
  const creditUsed = accounts.filter((c) => c.balance < 0).reduce((acc, c) => acc + Math.abs(c.balance), 0);
  const monthlyIn: number = 4200;
  const monthlyOut: number = 2350;
  const savingsRate = monthlyIn === 0 ? 0 : Math.round(((monthlyIn - monthlyOut) / monthlyIn) * 100);

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: 1280, paddingTop: 20 }}>
        <header className="nav">
          <div className="brand">
            <div className="brand-dot" />
            <div>
              <div className="muted" style={{ fontSize: 12 }}>Smart Wallet</div>
              <strong>Личный кабинет</strong>
              <div className="muted" style={{ fontSize: 12 }}>{email}</div>
            </div>
          </div>
          <div className="actions">
            <div className="chip">Базовая валюта: {baseCurrency}</div>
            <Button variant="ghost" onClick={logout}>Выйти</Button>
          </div>
        </header>

        <div className="grid-3" style={{ marginTop: 12 }}>
          <div className="card stat-card">
            <div className="muted">Баланс</div>
            <div className="stat-value">
              {balanceLoading ? 'Загрузка…' : formatMoney(totalBalance, totalsByCurrency[0]?.currency || baseCurrency)}
            </div>
            <div className="muted">Кредит: {formatMoney(-creditUsed, totalsByCurrency[0]?.currency || baseCurrency)}</div>
            {totalsByCurrency.length > 1 && (
              <div className="muted" style={{ marginTop: 4 }}>
                Баланс по валютам:{' '}
                {totalsByCurrency.map((t) => `${t.total.toFixed(2)} ${t.currency}`).join(' · ')}
              </div>
            )}
            {balanceError && <div className="amount-negative" style={{ marginTop: 6 }}>{balanceError}</div>}
          </div>
          <div className="card stat-card">
            <div className="muted">Доход / Расход (мес.)</div>
            <div className="stat-value">{formatMoney(monthlyIn - monthlyOut)}</div>
            <div className="muted">Доход: {formatMoney(monthlyIn)} · Расход: {formatMoney(monthlyOut)}</div>
          </div>
          <div className="card stat-card">
            <div className="muted">Ставка сбережений</div>
            <div className="stat-value">{savingsRate}%</div>
            <div className="muted">{fullName}</div>
          </div>
        </div>

        <div className="grid-2" style={{ marginTop: 12 }}>
          <div className="stack">
            <div className="card">
              <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ margin: 0 }}>Ваши карты</h3>
                <div className="pill-soft">Обновление балансов вручную</div>
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {balanceLoading && <div className="muted">Загружаем баланс…</div>}
                {!balanceLoading && balanceError && <div className="amount-negative">{balanceError}</div>}
                {!balanceLoading && !balanceError && accounts.length === 0 && (
                  <div className="muted">Счета пока не добавлены.</div>
                )}
                {!balanceLoading && !balanceError && accounts.map((card) => (
                  <div key={card.id} className="list-item">
                    <div>
                      <div style={{ fontWeight: 800 }}>{card.name}</div>
                      <small>{card.currency}{card.archived ? ' · Архив' : ''}</small>
                    </div>
                    <div className={card.balance >= 0 ? 'amount-positive' : 'amount-negative'}>
                      {formatMoney(card.balance, card.currency)}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ margin: 0 }}>Статьи расходов</h3>
                <div className="pill-soft">Период: месяц</div>
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {breakdown.map((b) => {
                  const pct = Math.min(100, Math.round((b.spent / b.limit) * 100));
                  return (
                    <div key={b.id} className="progress-wrap">
                      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <div style={{ fontWeight: 700 }}>{b.name}</div>
                          <small>{formatMoney(b.spent)} из {formatMoney(b.limit)}</small>
                        </div>
                        <div className={pct >= 90 ? 'amount-negative' : 'amount-positive'}>{pct}%</div>
                      </div>
                      <div className="progress-bar">
                        <div className="progress-fill" style={{ width: `${pct}%` }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          <div className="stack">
            <div className="card">
              <h3 style={{ marginTop: 0 }}>Цели</h3>
              <div className="list" style={{ marginTop: 10 }}>
                {goals.map((g) => (
                  <div key={g.id} className="progress-wrap">
                    <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: 700 }}>{g.title}</div>
                        <small>Цель: {g.target} {baseCurrency}</small>
                      </div>
                      <div className="chip">{g.progress}%</div>
                    </div>
                    <div className="progress-bar">
                      <div className="progress-fill" style={{ width: `${g.progress}%` }} />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <h3 style={{ marginTop: 0 }}>Ближайшие платежи</h3>
              <div className="list" style={{ marginTop: 10 }}>
                {payments.map((p) => (
                  <div key={p.id} className="list-item">
                    <div>
                      <div style={{ fontWeight: 700 }}>{p.title}</div>
                      <small>Срок: {p.due}</small>
                    </div>
                    <div className="amount-negative">{formatMoney(p.amount)}</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <h3 style={{ marginTop: 0 }}>Активность</h3>
              <div className="timeline">
                {activities.map((a) => (
                  <div key={a.id} className="timeline-item">
                    <div className="timeline-dot" />
                    <div>
                      <div style={{ fontWeight: 700 }}>{a.title}</div>
                      <small className="muted">{a.tag}</small>
                    </div>
                    <div className={a.amount && a.amount < 0 ? 'amount-negative' : 'muted'}>
                      {a.amount ? formatMoney(a.amount) : a.time}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
