import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useTheme } from '../hooks/useTheme';
import { AuthApi } from '../api/auth';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import '../theme.css';

const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;

const ResetPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { toggle } = useTheme();

  const [token, setToken] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [errors, setErrors] = useState<{ password?: string; confirm?: string; form?: string }>({});
  const [success, setSuccess] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const t = searchParams.get('token');
    const email = searchParams.get('email');
    if (t) setToken(t);
    if (email) {
      sessionStorage.setItem('spa_reset_email', email);
    }
  }, [searchParams]);

  const validate = () => {
    const next: typeof errors = {};
    if (!token.trim()) {
      next.form = 'Код не найден. Вернитесь и запросите новый.';
    } else if (token.trim().length < 6) {
      next.form = 'Код слишком короткий. Запросите новый.';
    }
    if (!password) {
      next.password = 'Введите новый пароль';
    } else if (!strongRegex.test(password)) {
      next.password = 'Пароль должен быть не короче 10 символов, содержать верхний/нижний регистр, цифру и спецсимвол';
    }
    if (!confirm) {
      next.confirm = 'Повторите пароль';
    } else if (confirm !== password) {
      next.confirm = 'Пароли не совпадают';
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const submit = async () => {
    if (isSubmitting) return;
    setSuccess('');
    if (!validate()) return;
    setIsSubmitting(true);
    const res = await AuthApi.reset({ token: token.trim(), password: password.trim() });
    setIsSubmitting(false);
    if (res.ok) {
      setSuccess('Пароль обновлён. Теперь можно войти.');
      setErrors({});
      setPassword('');
      setConfirm('');
    } else {
      const code = res.data && (res.data as any).code;
      if (code === '100003' || code === '400003') {
        setErrors({ password: 'Пароль слишком слабый: нужен верхний/нижний регистр, цифра и спецсимвол.' });
      } else if (code === '100005') {
        setErrors({ form: 'Код неверный или устарел. Запросите новый.' });
        setSuccess('');
      } else {
        setErrors({ form: 'Не удалось обновить пароль. Попробуйте позже.' });
      }
    }
  };

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: 520 }}>
        <header className="card" style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
          <div>
            <h2 style={{ margin: 0 }}>FinGuard</h2>
            <div className="muted">Восстановление доступа · Шаг 2</div>
          </div>
          <div className="actions">
            <Button variant="ghost" onClick={() => navigate('/forgot')}>Запросить код</Button>
            <Button variant="ghost" onClick={toggle}>Тема</Button>
          </div>
        </header>

        <div className="card">
          <div className="hero">
            <h3 style={{ margin: 0 }}>Смена пароля</h3>
            <p className="muted">Введите код из письма и задайте новый пароль.</p>
          </div>

          <div className="stack">
            <Input
              label="Новый пароль"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              placeholder="Новый пароль"
              autoComplete="new-password"
              aria-label="Новый пароль"
              error={errors.password}
            />
            <div className="field">
              <label htmlFor="reset-confirm">Повторите пароль</label>
              <input
                id="reset-confirm"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                type="password"
                placeholder="Ещё раз"
                autoComplete="new-password"
                aria-label="Подтверждение пароля"
                className={errors.confirm ? 'error' : ''}
              />
              {errors.confirm && <div className="error-text">{errors.confirm}</div>}
            </div>

            {errors.form && (
              <div className="alert" role="alert" aria-live="polite">
                {errors.form}
              </div>
            )}
            {success && (
              <div className="alert success" role="status" aria-live="polite">
                {success}
              </div>
            )}

            <div className="actions">
              <Button onClick={submit} disabled={isSubmitting}>Сменить пароль</Button>
              <Button variant="ghost" onClick={() => navigate('/auth')}>Перейти ко входу</Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResetPage;
