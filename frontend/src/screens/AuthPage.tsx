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
    if (res.ok && res.data && (res.data as any).token) {
      ApiClient.setEmail(payload.email);
      window.location.href = '/dashboard';
    } else {
      const code = res.data && res.data.code ? res.data.code : '----';
      applyErrorCode(code, '');
    }
  };

  return (
    <div className="app">
      <div className="container" style={{ maxWidth: 580 }}>
        <header className="card" style={{ marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 10 }}>
          <div>
            <h2 style={{ margin: 0 }}>FinGuard</h2>
          </div>
          <div className="actions">
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
                {errors.form && <div className="alert" role="alert" aria-live="polite">{errors.form}</div>}
                <div className="actions">
                  <Button variant="secondary" onClick={submit} disabled={isSubmitting}>Войти</Button>
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
