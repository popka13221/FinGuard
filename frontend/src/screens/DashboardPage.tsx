import React, { useEffect, useState } from 'react';
import { Button } from '../components/Button';
import { AuthApi } from '../api/auth';
import { ApiClient } from '../api/client';
import '../theme.css';

type Status = 'idle' | 'ok' | 'error';

const DashboardPage: React.FC = () => {
  const [email, setEmail] = useState<string>('user');
  const [status, setStatus] = useState<Status>('idle');
  const tokenText = 'Токен в httpOnly cookie';

  useEffect(() => {
    AuthApi.profile().then((res) => {
      if (!res.ok) {
        window.location.href = '/auth';
        return;
      }
      if (res.data && (res.data as any).email) {
        setEmail((res.data as any).email);
        ApiClient.setEmail((res.data as any).email);
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

  return (
    <div className="app">
      <div className="container">
        <header className="card" style={{ display: 'flex', justifyContent: 'space-between', gap: 10, flexWrap: 'wrap' }}>
          <div>
            <h2 style={{ margin: 0 }}>FinGuard</h2>
            <div className="muted">Ваш финансовый дашборд</div>
          </div>
          <div className="actions">
            <div className="pill">Dashboard</div>
          </div>
        </header>

        <div className="card" style={{ marginTop: 12, display: 'flex', gap: 12, flexWrap: 'wrap', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div className="muted">Вы вошли как</div>
            <h3 style={{ margin: 0 }}>{email}</h3>
          </div>
          <div className="actions">
            <Button variant="ghost" onClick={() => checkHealth('/health')}>/health</Button>
            <Button variant="ghost" onClick={() => checkHealth('/actuator/health')}>/actuator/health</Button>
            <a className="ghost" style={{ textDecoration: 'none', padding: '12px 14px', borderRadius: 12, border: '1px solid var(--border)', color: 'var(--text)' }} href="/swagger-ui/index.html" target="_blank">Swagger UI</a>
            <Button variant="secondary" onClick={logout}>Выйти</Button>
          </div>
        </div>

        <div className="grid-2" style={{ marginTop: 12 }}>
          <div className="stack">
            <div className="card">
              <h3>Счета</h3>
              <div className="muted">Заглушка. Подключим /api/accounts.</div>
            </div>
            <div className="card">
              <h3>Транзакции</h3>
              <div className="muted">Заглушка. Подключим /api/transactions.</div>
            </div>
          </div>
          <div className="stack">
            <div className="card">
              <h3>Статус и токен</h3>
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
            <div className="card">
              <h3>Логи API</h3>
              <pre style={{ margin: 0 }}>Готово к запросам...</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
