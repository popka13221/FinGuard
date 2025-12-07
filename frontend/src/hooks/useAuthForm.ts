import { useState } from 'react';

type Errors = Record<string, string>;

export function useAuthForm(isRegister: boolean) {
  const [errors, setErrors] = useState<Errors>({});

  const validate = (values: Record<string, string>) => {
    const next: Errors = {};
    const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
    const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;
    if (!values.email) next.email = 'Введите email';
    else if (!emailRegex.test(values.email)) next.email = 'Введите корректный email';
    if (!values.password) next.password = 'Введите пароль';
    else if (isRegister && !strongRegex.test(values.password)) next.password = 'Пароль должен быть не короче 10 символов, содержать верхний/нижний регистр, цифру и спецсимвол';
    if (isRegister) {
      if (!values.fullName) next.fullName = 'Введите имя';
      if (!values.baseCurrency) next.baseCurrency = 'Укажите базовую валюту';
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const applyErrorCode = (code: string, message?: string, retryAfterSeconds?: number) => {
    const next: Errors = {};
    const withRetry = (text: string) =>
      retryAfterSeconds && retryAfterSeconds > 0 ? `${text} Подождите ~${Math.max(Math.ceil(retryAfterSeconds), 1)} сек.` : text;
    const defaultMessage = message || 'Ошибка запроса. Попробуйте позже.';
    switch (code) {
      case '100001':
        next.email = ' ';
        next.password = ' ';
        next.form = 'Неверный email или пароль';
        break;
      case '100002':
        next.email = message || 'Такой email уже зарегистрирован';
        break;
      case '100003':
        next.password = message || 'Пароль слишком слабый: нужен верхний/нижний регистр, цифра и спецсимвол';
        break;
      case '100004':
        next.password = ' ';
        next.form = message || 'Аккаунт временно заблокирован. Попробуйте позже.';
        break;
      case '429001':
        next.form = withRetry('Слишком много попыток. Попробуйте позже.');
        break;
      case '429002':
        next.form = withRetry('Мы уже отправили код. Проверьте почту и попробуйте позже.');
        break;
      case '400002':
        next.email = 'Некорректный email';
        break;
      case '400003':
        next.password = 'Пароль не соответствует требованиям';
        break;
      default:
        next.form = defaultMessage;
    }
    setErrors(next);
  };

  const setFormError = (message: string) => {
    setErrors(message ? { form: message } : {});
  };

  return { errors, validate, applyErrorCode, setFormError };
}
