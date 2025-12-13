import React, { useEffect, useState } from 'react';
import { Button } from '../components/Button';
import { AuthApi } from '../api/auth';
import { ApiClient } from '../api/client';
import '../theme.css';

type Status = 'idle' | 'ok' | 'error';
type Tx = { id: number; title: string; category: string; amount: number; date: string };
type Budget = { id: number; title: string; used: number; limit: number };
type Account = { id: number; title: string; balance: number; type: string };

const DashboardPage: React.FC = () => {
  const [email, setEmail] = useState<string>('user');
  const [fullName, setFullName] = useState<string>('Владелец кошелька');
  const [baseCurrency, setBaseCurrency] = useState<string>('USD');
  const [status, setStatus] = useState<Status>('idle');
  const tokenText = 'Токен в httpOnly cookie';

  const accounts: Account[] = [
    { id: 1, title: 'Основной', balance: 18450.32, type: 'Дебет' },
    { id: 2, title: 'Накопительный', balance: 4200.75, type: 'Сбережения' },
    { id: 3, title: 'Кредитка', balance: -1250.12, type: 'Кредит' },
  ];

  const txs: Tx[] = [
    { id: 1, title: 'Кофе', category: 'Еда и напитки', amount: -4.5, date: 'Сегодня' },
    { id: 2, title: 'Получен возврат', category: 'Прочее', amount: 120.0, date: 'Вчера' },
    { id: 3, title: 'Продукты', category: 'Супермаркет', amount: -36.8, date: 'Вчера' },
    { id: 4, title: 'Такси', category: 'Транспорт', amount: -12.0, date: '02 янв' },
  ];

  const budgets: Budget[] = [
    { id: 1, title: 'Еда и кафе', used: 320, limit: 600 },
    { id: 2, title: 'Транспорт', used: 90, limit: 200 },
    { id: 3, title: 'Подписки', used: 45, limit: 80 },
  ];

  const fx = [
    { pair: 'USD / EUR', rate: '0.91' },
    { pair: 'USD / KZT', rate: '459.10' },
    { pair: 'USD / GBP', rate: '0.79' },
  ];

  useEffect(() => {
    AuthApi.profile().then((res) => {
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
    });
  }, []);

  const checkHealth = async (url: string) => {
    const res = await ApiClient.request(url, { method: 'GET' });
    setStatus(res.ok ? 'ok' : 'error');
  };

  const logout = async () => {
    await AuthApi.logout();
    ApiClient.clearEmail();
    window.location.href = '/auth';
  };

  const formatMoney = (value: number) =>
    `${value < 0 ? '-' : ''}${Math.abs(value).toLocaleString('ru-RU', { minimumFractionDigits: 2 })} ${baseCurrency}`;

  const totalBalance = accounts.reduce((acc, item) => acc + item.balance, 0);
  const creditUsed = accounts.filter((a) => a.balance < 0).reduce((acc, a) => acc + Math.abs(a.balance), 0);
  const monthlyIn = 3420;
  const monthlyOut = 1980;
  const savingsRate = monthlyIn === 0 ? 0 : Math.round(((monthlyIn - monthlyOut) / monthlyIn) * 100);

  return (
    <div className="app">
      <div className="container">
        <header className="card" style={{ display: 'flex', justifyContent: 'space-between', gap: 10, flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <img src="/app/assets/white-big-logo.svg" alt="Smart Wallet" style={{ height: 48 }} />
            <div>
              <h2 style={{ margin: 0 }}>Smart Wallet</h2>
              <div className="muted">Ваш финансовый дашборд</div>
            </div>
          </div>
          <div className="actions">
            <div className="pill">Dashboard</div>
          </div>
        </header>

        <div className="card" style={{ marginTop: 12, display: 'flex', gap: 12, flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div className="muted">Добро пожаловать,</div>
            <h3 style={{ margin: 0 }}>{fullName || email}</h3>
            <div className="muted">Базовая валюта: {baseCurrency}</div>
          </div>
          <div className="actions">
            <Button variant="ghost" onClick={() => checkHealth('/health')}>/health</Button>
            <Button variant="ghost" onClick={() => checkHealth('/actuator/health')}>/actuator/health</Button>
            <a className="ghost" style={{ textDecoration: 'none', padding: '12px 14px', borderRadius: 12, border: '1px solid var(--border)', color: 'var(--text)' }} href="/swagger-ui/index.html" target="_blank">Swagger UI</a>
            <Button variant="secondary" onClick={logout}>Выйти</Button>
          </div>
        </div>

        <div className="grid-3" style={{ marginTop: 12 }}>
          <div className="card stat-card">
            <div className="muted">Баланс</div>
            <div className="stat-value">{formatMoney(totalBalance)}</div>
            <div className="chip">Аккаунты: {accounts.length}</div>
          </div>
          <div className="card stat-card">
            <div className="muted">Денежный поток (мес.)</div>
            <div className="stat-value">{formatMoney(monthlyIn - monthlyOut)}</div>
            <div className="muted">Доход: {formatMoney(monthlyIn)} · Расход: {formatMoney(monthlyOut)}</div>
          </div>
          <div className="card stat-card">
            <div className="muted">Ставка сбережений</div>
            <div className="stat-value">{savingsRate}%</div>
            <div className="muted">Кредитная нагрузка: {formatMoney(-creditUsed)}</div>
          </div>
        </div>

        <div className="grid-2" style={{ marginTop: 12 }}>
          <div className="stack">
            <div className="card">
              <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ margin: 0 }}>Счета</h3>
                <div className="pill-soft">Синхронизация позже</div>
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {accounts.map((acc) => (
                  <div key={acc.id} className="list-item">
                    <div>
                      <div style={{ fontWeight: 700 }}>{acc.title}</div>
                      <small>{acc.type}</small>
                    </div>
                    <div className={acc.balance >= 0 ? 'amount-positive' : 'amount-negative'}>{formatMoney(acc.balance)}</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ margin: 0 }}>Последние транзакции</h3>
                <div className="pill-soft">Фильтр: неделя</div>
              </div>
              <div className="list" style={{ marginTop: 10 }}>
                {txs.map((tx) => (
                  <div key={tx.id} className="list-item">
                    <div>
                      <div style={{ fontWeight: 700 }}>{tx.title}</div>
                      <small>{tx.category} · {tx.date}</small>
                    </div>
                    <div className={tx.amount >= 0 ? 'amount-positive' : 'amount-negative'}>{formatMoney(tx.amount)}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="stack">
            <div className="card">
              <h3 style={{ marginTop: 0 }}>Бюджеты и цели</h3>
              <div className="list" style={{ marginTop: 10 }}>
                {budgets.map((b) => {
                  const pct = Math.min(100, Math.round((b.used / b.limit) * 100));
                  return (
                    <div key={b.id} className="progress-wrap">
                      <div className="row" style={{ justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <div style={{ fontWeight: 700 }}>{b.title}</div>
                          <small>{formatMoney(b.used)} из {formatMoney(b.limit)}</small>
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

            <div className="card">
              <h3 style={{ marginTop: 0 }}>Курсы валют</h3>
              <div className="list" style={{ marginTop: 10 }}>
                {fx.map((item, idx) => (
                  <div key={idx} className="list-item">
                    <div>
                      <div style={{ fontWeight: 700 }}>{item.pair}</div>
                      <small>Базовая: {baseCurrency}</small>
                    </div>
                    <div className="amount-positive">{item.rate}</div>
                  </div>
                ))}
              </div>
            </div>

            <div className="card">
              <h3 style={{ marginTop: 0 }}>Статус и токен</h3>
              <div className="row" style={{ alignItems: 'center' }}>
                <div className={`status-pill ${status === 'ok' ? 'status-ok' : status === 'error' ? 'status-bad' : ''}`}>
                  {status === 'ok' ? 'Health OK' : status === 'error' ? 'Health error' : 'Health не проверен'}
                </div>
                <Button variant="ghost" onClick={() => checkHealth('/health')}>/health</Button>
                <Button variant="ghost" onClick={() => checkHealth('/actuator/health')}>/actuator/health</Button>
              </div>
              <div className="muted" style={{ marginTop: 8 }}>JWT</div>
              <div className="token-box">{tokenText}</div>
              <div className="actions" style={{ marginTop: 10 }}>
                <Button variant="ghost" onClick={logout}>Сбросить токен</Button>
                <a className="ghost" style={{ textDecoration: 'none', padding: '12px 14px', borderRadius: 12, border: '1px solid var(--border)', color: 'var(--text)' }} href="/swagger-ui/index.html" target="_blank">Swagger UI</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
