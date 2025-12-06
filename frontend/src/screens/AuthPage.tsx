import React, { useEffect, useState } from 'react';
import { Tabs } from '../components/Tabs';
import { Input } from '../components/Input';
import { Button } from '../components/Button';
import { Select } from '../components/Select';
import { useAuthForm } from '../hooks/useAuthForm';
import { AuthApi } from '../api/auth';
import { ApiClient } from '../api/client';
import { LookupApi, type Currency } from '../api/lookup';
import '../theme.css';

type Mode = 'login' | 'register';

const AuthPage: React.FC = () => {
  const [mode, setMode] = useState<Mode>('login');
  const { errors, validate, applyErrorCode, setFormError } = useAuthForm(mode === 'register');
  const [currencies, setCurrencies] = useState<Currency[]>([]);
  const [isLoadingCurrencies, setIsLoadingCurrencies] = useState<boolean>(true);
  const [currencyError, setCurrencyError] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [otpStep, setOtpStep] = useState<boolean>(false);
  const [otpCode, setOtpCode] = useState<string>('');
  const [otpExpiresIn, setOtpExpiresIn] = useState<number>(0);
  const [otpError, setOtpError] = useState<string>('');

  const [values, setValues] = useState({
    email: '',
    password: '',
    fullName: '',
    baseCurrency: '',
  });

  useEffect(() => {
    let mounted = true;
    setIsLoadingCurrencies(true);
    setCurrencyError('');
    LookupApi.currencies()
      .then((res) => {
        if (!mounted) return;
        if (res.ok && Array.isArray(res.data) && res.data.length > 0) {
          setCurrencies(res.data);
          setValues((prev) => ({
            ...prev,
            baseCurrency: prev.baseCurrency || res.data[0].code,
          }));
        } else {
          setCurrencies([]);
          setCurrencyError('Не удалось загрузить валюты. Попробуйте позже.');
        }
        setIsLoadingCurrencies(false);
      })
      .catch(() => {
        if (!mounted) return;
        setCurrencies([]);
        setCurrencyError('Не удалось загрузить валюты. Попробуйте позже.');
        setIsLoadingCurrencies(false);
      });
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    setOtpStep(false);
    setOtpCode('');
    setOtpError('');
    setOtpExpiresIn(0);
  }, [mode, values.email]);

  const onChange = (field: string, val: string) => {
    setValues((prev) => ({ ...prev, [field]: val }));
  };

  const submit = async () => {
    if (isSubmitting) return;
    setFormError('');
    if (mode === 'register' && (isLoadingCurrencies || currencyError || currencies.length === 0)) {
      setFormError(currencyError || 'Валюты загружаются. Подождите и попробуйте снова.');
      return;
    }
    const payload = {
      email: values.email.trim().toLowerCase(),
      password: values.password,
      fullName: values.fullName,
      baseCurrency: values.baseCurrency.toUpperCase(),
    };
    const isValid = validate(payload);
    if (!isValid) return;
    setIsSubmitting(true);
    const apiCall =
      mode === 'login'
        ? AuthApi.login({ email: payload.email, password: payload.password })
        : AuthApi.register(payload);
    const res = await apiCall;
    setIsSubmitting(false);
    if (mode === 'login' && res.status === 202 && res.data && (res.data as any).otpRequired) {
      setOtpStep(true);
      setOtpExpiresIn((res.data as any).expiresInSeconds || 0);
      setOtpError('');
      return;
    }
    if (res.ok && res.data && (res.data as any).token) {
      ApiClient.setEmail(payload.email);
      window.location.href = '/dashboard';
    } else {
      const code = res.data && res.data.code ? res.data.code : '----';
      applyErrorCode(code, '');
    }
  };

  const submitOtp = async () => {
    if (isSubmitting || !otpStep) return;
    setOtpError('');
    if (!otpCode.trim()) {
      setOtpError('Введите код из письма.');
      return;
    }
    setIsSubmitting(true);
    const res = await AuthApi.verifyOtp({ email: values.email.trim().toLowerCase(), code: otpCode.trim() });
    setIsSubmitting(false);
    if (res.ok && res.data && (res.data as any).token) {
      ApiClient.setEmail(values.email.trim().toLowerCase());
      window.location.href = '/dashboard';
    } else {
      setOtpError('Код неверный или истек. Попробуйте снова.');
    }
  };

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: 580 }}>
        <header className="card" style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <img src="/app/assets/logo.svg" alt="Smart Wallet" style={{ height: 48 }} />
            <h2 style={{ margin: 0 }}>Smart Wallet</h2>
          </div>
          <div className="actions" style={{ gap: 8 }}>
            <div className="pill">Secure Login</div>
          </div>
        </header>

        <div className="card">
          <div className="hero">
            <h3 style={{ margin: 0 }}>Ваш фин. помощник</h3>
            <p className="muted">Войдите или зарегистрируйтесь, чтобы перейти в дашборд.</p>
          </div>

          <Tabs
            value={mode}
            options={[
              { id: 'login', label: 'Вход' },
              { id: 'register', label: 'Регистрация' },
            ]}
            onChange={(val) => setMode(val as Mode)}
          />

          <div className="stack">
            {mode === 'login' ? (
              <>
                <Input
                  label="Email"
                  value={values.email}
                  onChange={(e) => onChange('email', e.target.value)}
                  placeholder="you@example.com"
                  autoComplete="email"
                  aria-label="Email"
                  error={errors.email}
                />
                <Input
                  label="Пароль"
                  value={values.password}
                  onChange={(e) => onChange('password', e.target.value)}
                  type="password"
                  placeholder="Password"
                  autoComplete="off"
                  aria-label="Пароль"
                  error={errors.password}
                />
                {otpStep && (
                  <>
                    <Input
                      label="Код из письма"
                      value={otpCode}
                      onChange={(e) => setOtpCode(e.target.value)}
                      placeholder="123456"
                      aria-label="Код из письма"
                      error={otpError}
                    />
                    <div className="muted">
                      Отправили код на почту. Срок действия: {otpExpiresIn ? Math.max(otpExpiresIn, 1) : 5 * 60} сек.
                    </div>
                  </>
                )}
                {(errors.form || otpError) && (
                  <div className="alert" role="alert" aria-live="polite">
                    {errors.form || otpError}
                  </div>
                )}
                <div className="actions">
                  {!otpStep ? (
                    <Button variant="secondary" onClick={submit} disabled={isSubmitting}>
                      Войти
                    </Button>
                  ) : (
                    <Button variant="secondary" onClick={submitOtp} disabled={isSubmitting}>
                      Подтвердить код
                    </Button>
                  )}
                </div>
              </>
            ) : (
              <>
                <Input
                  label="Email"
                  value={values.email}
                  onChange={(e) => onChange('email', e.target.value)}
                  placeholder="you@example.com"
                  autoComplete="email"
                  aria-label="Email"
                  error={errors.email}
                />
                <Input
                  label="Пароль"
                  value={values.password}
                  onChange={(e) => onChange('password', e.target.value)}
                  type="password"
                  placeholder="Password"
                  autoComplete="off"
                  aria-label="Пароль"
                  error={errors.password}
                />
                <Input
                  label="Имя"
                  value={values.fullName}
                  onChange={(e) => onChange('fullName', e.target.value)}
                  placeholder="Ваше имя"
                  autoComplete="name"
                  aria-label="Имя"
                  error={errors.fullName}
                />
                <Select
                  label="Базовая валюта"
                  value={values.baseCurrency}
                  onChange={(e) => onChange('baseCurrency', e.target.value)}
                  aria-label="Базовая валюта"
                  error={errors.baseCurrency || currencyError}
                  disabled={isLoadingCurrencies || Boolean(currencyError) || currencies.length === 0}
                  options={currencies.map((c) => ({ value: c.code, label: `${c.code} — ${c.name}` }))}
                />
                {errors.form && <div className="alert" role="alert" aria-live="polite">{errors.form}</div>}
                <div className="actions">
                  <Button onClick={submit} disabled={isSubmitting || isLoadingCurrencies || Boolean(currencyError) || currencies.length === 0}>Создать аккаунт</Button>
                </div>
              </>
            )}
            <div className="muted">Токен будет храниться в httpOnly cookie; фронт токен не читает.</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;
