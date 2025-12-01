import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useTheme } from '../hooks/useTheme';
import { AuthApi } from '../api/auth';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import '../theme.css';

const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

const resendSeconds = 60;

const ForgotPage: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { toggle } = useTheme();

  const [email, setEmail] = useState('');
  const [token, setToken] = useState('');
  const [errors, setErrors] = useState<{ email?: string; token?: string; form?: string }>({});
  const [showToken, setShowToken] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [cooldown, setCooldown] = useState(0);

  useEffect(() => {
    const fromQuery = searchParams.get('email');
    if (fromQuery) setEmail(fromQuery);
  }, [searchParams]);

  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | undefined;
    if (cooldown > 0) {
      timer = setInterval(() => setCooldown((c) => (c > 0 ? c - 1 : 0)), 1000);
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
    } else {
      const code = res.data && (res.data as any).code;
      if (code === '400002') {
        setErrors({ email: 'Введите корректный email' });
      } else {
        setErrors({ form: 'Не получилось отправить код. Попробуйте позже.' });
      }
    }
  };

  const goToReset = () => {
    if (!token.trim()) {
      setErrors({ token: 'Введите код из письма' });
      return;
    }
    if (token.trim().length < 6) {
      setErrors({ token: 'Код слишком короткий' });
      return;
    }
    const url = `/reset?token=${encodeURIComponent(token.trim())}${email ? `&email=${encodeURIComponent(email.trim())}` : ''}`;
    navigate(url);
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
            <Button variant="ghost" onClick={toggle}>Тема</Button>
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
                autoComplete="one-time-code"
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
              <Button variant="ghost" onClick={goToReset}>Ввести код</Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPage;
