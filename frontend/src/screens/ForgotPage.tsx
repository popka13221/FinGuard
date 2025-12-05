import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { AuthApi } from '../api/auth';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import '../theme.css';

const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

const resendSeconds = 60;
const maxAttempts = 5;
const storageKeys = {
  email: 'fg_forgot_email',
  until: 'fg_forgot_cooldown_until',
  stage: 'fg_forgot_stage',
};

const ForgotPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [errors, setErrors] = useState<{ email?: string; token?: string; form?: string }>({});
  const [showToken, setShowToken] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const [isChecking, setIsChecking] = useState(false);
  const [attempts, setAttempts] = useState(0);

  useEffect(() => {
    const fromQuery = searchParams.get('email');
    if (fromQuery) setEmail(fromQuery);

    const savedEmail = localStorage.getItem(storageKeys.email);
    const savedUntil = localStorage.getItem(storageKeys.until);
    const savedStage = localStorage.getItem(storageKeys.stage);
    if (!email && savedEmail) {
      setEmail(savedEmail);
    }
    if (savedStage === 'sent') {
      setShowToken(true);
    }
    if (savedUntil) {
      const left = Math.max(Math.floor((Number(savedUntil) - Date.now()) / 1000), 0);
      if (left > 0) {
        setCooldown(left);
      } else {
        localStorage.removeItem(storageKeys.until);
      }
    }
  }, [searchParams]);

  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | undefined;
    if (cooldown > 0) {
      timer = setInterval(() => setCooldown((c) => (c > 0 ? c - 1 : 0)), 1000);
    } else {
      localStorage.removeItem(storageKeys.until);
    }
    return () => {
      if (timer) clearInterval(timer);
    };
  }, [cooldown]);

  const validateEmail = () => {
    if (!email.trim()) {
      setErrors({ email: 'Введите email' });
      return false;
    }
    if (!emailRegex.test(email.trim().toLowerCase())) {
      setErrors({ email: 'Введите корректный email' });
      return false;
    }
    setErrors({});
    return true;
  };

  const handleSend = async () => {
    if (isSubmitting || cooldown > 0) return;
    if (!validateEmail()) return;
    setIsSubmitting(true);
    const res = await AuthApi.forgot({ email: email.trim().toLowerCase() });
    setIsSubmitting(false);
    if (res.ok) {
      setShowToken(true);
      setCooldown(resendSeconds);
      localStorage.setItem(storageKeys.email, email.trim().toLowerCase());
      localStorage.setItem(storageKeys.stage, 'sent');
      const until = Date.now() + resendSeconds * 1000;
      localStorage.setItem(storageKeys.until, String(until));
    } else {
      const code = res.data && (res.data as any).code;
      if (code === '400002') {
        setErrors({ email: 'Введите корректный email' });
      } else {
        setErrors({ form: 'Не получилось отправить код. Попробуйте позже.' });
      }
    }
  };

  const goToReset = async () => {
    if (isChecking) return;
    if (attempts >= maxAttempts) {
      setErrors({ form: 'Слишком много неверных попыток. Запросите новый код.' });
      return;
    }
    if (!token.trim()) {
      setErrors({ token: 'Введите код из письма' });
      return;
    }
    if (token.trim().length < 6) {
      setErrors({ token: 'Код слишком короткий' });
      return;
    }
    setErrors({});
    setIsChecking(true);
    const res = await AuthApi.confirmReset({ token: token.trim() });
    setIsChecking(false);
    if (res.ok && res.data) {
      setAttempts(0);
      const resetSessionToken = (res.data as any).resetSessionToken;
      const expiresInSeconds = (res.data as any).expiresInSeconds;
      if (resetSessionToken) {
        sessionStorage.setItem('spa_reset_session', resetSessionToken);
        if (expiresInSeconds) {
          const expiresAt = Date.now() + expiresInSeconds * 1000;
          sessionStorage.setItem('spa_reset_session_expires', String(expiresAt));
        }
      }
      const url = `/reset?confirmed=1${email ? `&email=${encodeURIComponent(email.trim())}` : ''}`;
      navigate(url);
      return;
    }
    const code = res.data && (res.data as any).code;
    if (code === '100005') {
      const nextAttempts = attempts + 1;
      setAttempts(nextAttempts);
      if (nextAttempts >= maxAttempts) {
        setErrors({ form: 'Слишком много неверных попыток. Запросите новый код.' });
      } else {
        setErrors({ token: 'Код неверный или устарел. Запросите новый.' });
      }
    } else if (code === '429001') {
      setErrors({ form: 'Слишком много попыток. Подождите и попробуйте снова.' });
    } else {
      setErrors({ form: 'Не удалось подтвердить код. Попробуйте позже.' });
    }
  };

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: 520 }}>
        <header className="card" style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
          <div>
            <h2 style={{ margin: 0 }}>FinGuard</h2>
            <div className="muted">Восстановление доступа · Шаг 1</div>
          </div>
          <div className="actions">
            <Button variant="ghost" onClick={() => navigate('/auth')}>К входу</Button>
          </div>
        </header>

        <div className="card">
          <div className="hero">
            <h3 style={{ margin: 0 }}>Забыли пароль?</h3>
            <p className="muted">Отправьте код на email и введите его ниже.</p>
          </div>

          <div className="stack">
            <Input
              label="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              autoComplete="email"
              aria-label="Email"
              error={errors.email}
            />

            {showToken && (
              <Input
                label="Код из письма"
                value={token}
                onChange={(e) => setToken(e.target.value)}
                placeholder="Вставьте код"
                aria-label="Код из письма"
                error={errors.token}
              />
            )}

            {errors.form && (
              <div className="alert" role="alert" aria-live="polite">
                {errors.form}
              </div>
            )}

            <div className="actions">
              <Button variant="secondary" onClick={handleSend} disabled={isSubmitting || cooldown > 0}>
                {cooldown > 0 ? `Отправить повторно (${cooldown}s)` : 'Отправить код'}
              </Button>
              <Button variant="ghost" onClick={goToReset} disabled={isChecking || attempts >= maxAttempts}>Ввести код</Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPage;
