import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AuthApi } from '../api/auth';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import '../theme.css';

const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;

const formatTimer = (seconds: number) => {
  const m = Math.floor(seconds / 60)
    .toString()
    .padStart(2, '0');
  const s = Math.max(seconds % 60, 0)
    .toString()
    .padStart(2, '0');
  return `${m}:${s}`;
};

const ResetPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const maxAttempts = 5;
  const [code, setCode] = useState('');
  const [resetSessionToken, setResetSessionToken] = useState('');
  const [timer, setTimer] = useState(0);
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [errors, setErrors] = useState<{ code?: string; password?: string; confirm?: string; form?: string }>({});
  const [info, setInfo] = useState('');
  const [success, setSuccess] = useState('');
  const [isConfirming, setIsConfirming] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [initialToken, setInitialToken] = useState('');
  const [attempts, setAttempts] = useState(0);
  const [confirmedFlag, setConfirmedFlag] = useState(false);

  useEffect(() => {
    const t = searchParams.get('token');
    const email = searchParams.get('email');
    const confirmed = searchParams.get('confirmed');
    if (t) {
      setCode(t);
      setInitialToken(t);
    }
    if (email) {
      sessionStorage.setItem('spa_reset_email', email);
    }
    if (confirmed) {
      setConfirmedFlag(true);
    }
  }, [searchParams]);

  useEffect(() => {
    if (!resetSessionToken) return;
    const id = setInterval(() => {
      setTimer((t) => (t > 0 ? t - 1 : 0));
    }, 1000);
    return () => clearInterval(id);
  }, [resetSessionToken]);

  useEffect(() => {
    if (resetSessionToken && timer === 0) {
      setResetSessionToken('');
      setInfo('');
      setErrors({ form: 'Сессия сброса истекла. Введите код снова.' });
    }
  }, [resetSessionToken, timer]);

  const confirmCode = async (valueOverride?: string) => {
    if (confirmedFlag && resetSessionToken) return;
    if (attempts >= maxAttempts) {
      setErrors({ form: 'Слишком много неверных попыток. Запросите новый код.' });
      return;
    }
    if (isConfirming) return;
    setSuccess('');
    setInfo('');
    const value = (valueOverride ?? code).trim();
    const next: typeof errors = {};
    if (!value) {
      next.code = 'Введите код из письма';
    } else if (value.length < 6) {
      next.code = 'Код слишком короткий';
    }
    if (Object.keys(next).length > 0) {
      setErrors(next);
      return;
    }
    setErrors({});
    setIsConfirming(true);
    const res = await AuthApi.confirmReset({ token: value });
    setIsConfirming(false);
    if (res.ok && res.data && res.data.resetSessionToken) {
      setAttempts(0);
      setResetSessionToken(res.data.resetSessionToken);
      setTimer(res.data.expiresInSeconds || 0);
      setInfo('Код подтверждён. Введите новый пароль.');
    } else {
      const code = res.data && (res.data as any).code;
      if (code === '100005') {
        const nextAttempts = attempts + 1;
        setAttempts(nextAttempts);
        if (nextAttempts >= maxAttempts) {
          setErrors({ form: 'Слишком много неверных попыток. Запросите новый код.' });
        } else {
          setErrors({ code: 'Код неверный или устарел. Запросите новый.' });
        }
      } else if (code === '429001') {
        setErrors({ form: 'Слишком много попыток. Подождите и попробуйте снова.' });
      } else {
        setErrors({ form: 'Не получилось подтвердить код. Попробуйте позже.' });
      }
      setResetSessionToken('');
      setTimer(0);
      setConfirmedFlag(false);
    }
    setInitialToken('');
  };

  useEffect(() => {
    if (initialToken && !resetSessionToken && !isConfirming && !confirmedFlag) {
      void confirmCode(initialToken);
    }
  }, [initialToken, resetSessionToken, isConfirming, confirmedFlag]);

  const validatePasswords = () => {
    const next: typeof errors = {};
    if (!resetSessionToken) {
      next.form = 'Сначала подтвердите код из письма.';
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
    if (!validatePasswords()) return;
    setIsSubmitting(true);
    const res = await AuthApi.reset({ resetSessionToken, password: password.trim() });
    setIsSubmitting(false);
    if (res.ok) {
      setSuccess('Пароль обновлён. Сейчас перейдём на вход.');
      setErrors({});
      setPassword('');
      setConfirm('');
      setTimeout(() => navigate('/auth'), 900);
    } else {
      const code = res.data && (res.data as any).code;
      if (code === '100003' || code === '400003') {
        setErrors({ password: 'Пароль слишком слабый: нужен верхний/нижний регистр, цифра и спецсимвол.' });
      } else if (code === '100005') {
        setErrors({ form: 'Сессия сброса устарела или неверна. Введите код снова.' });
        setResetSessionToken('');
        setTimer(0);
        setInitialToken('');
      } else {
        setErrors({ form: 'Не удалось обновить пароль. Попробуйте позже.' });
      }
    }
  };

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: 520 }}>
        <header className="card" style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <img src="/app/assets/white-big-logo.svg" alt="Smart Wallet" style={{ height: 48 }} />
            <div>
              <h2 style={{ margin: 0 }}>Smart Wallet</h2>
              <div className="muted">Восстановление доступа · Шаг 2</div>
            </div>
          </div>
        </header>

        <div className="card">
          <div className="hero">
            <h3 style={{ margin: 0 }}>Смена пароля</h3>
            <p className="muted">Подтвердите код из письма, затем задайте новый пароль.</p>
          </div>

          <div className="stack">
            {!resetSessionToken && (
              <>
                <Input
                  label="Код из письма"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  placeholder="Вставьте код"
                  aria-label="Код из письма"
                  error={errors.code}
                />
                {errors.form && (
                  <div className="alert" role="alert" aria-live="polite">
                    {errors.form}
                  </div>
                )}
                <div className="actions">
                  <Button onClick={() => confirmCode()} disabled={isConfirming || attempts >= maxAttempts}>Подтвердить код</Button>
                  <Button variant="ghost" onClick={() => navigate('/forgot')}>Запросить новый</Button>
                </div>
              </>
            )}

            {resetSessionToken && (
              <>
                <div className="muted" role="status" aria-live="polite">
                  Сессия сброса активна: {formatTimer(timer)}
                </div>
                {info && (
                  <div className="alert success" role="status" aria-live="polite">
                    {info}
                  </div>
                )}
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
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ResetPage;
