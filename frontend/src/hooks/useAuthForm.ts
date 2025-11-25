import { useState } from 'react';

type Errors = Record<string, string>;

export function useAuthForm(isRegister: boolean) {
  const [errors, setErrors] = useState<Errors>({});

  const validate = (values: Record<string, string>) => {
    const next: Errors = {};
    const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
    if (!values.email) next.email = 'Введите email';
    else if (!emailRegex.test(values.email)) next.email = 'Введите корректный email';
    if (!values.password) next.password = 'Введите пароль';
    if (isRegister) {
      if (!values.fullName) next.fullName = 'Введите имя';
      if (!values.baseCurrency) next.baseCurrency = 'Укажите базовую валюту';
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const applyErrorCode = (code: string, message: string) => {
    const next: Errors = {};
    switch (code) {
      case '100001':
        next.email = ' ';
        next.password = ' ';
        next.form = 'Неверный email или пароль';
        break;
      case '100002':
        next.email = 'Такой email уже зарегистрирован';
        break;
      case '100003':
        next.password = 'Пароль слишком слабый: нужен верхний/нижний регистр, цифра и спецсимвол';
        break;
      case '100004':
        next.password = 'Аккаунт временно заблокирован. Попробуйте позже.';
        break;
      case '400002':
        next.email = message || 'Некорректный email';
        break;
      case '400003':
        next.password = message || 'Пароль не соответствует требованиям';
        break;
      default:
        next.form = message || 'Ошибка запроса';
    }
    setErrors(next);
  };

  return { errors, validate, applyErrorCode };
}
