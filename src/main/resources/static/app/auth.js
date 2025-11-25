(() => {
  const selectors = {
    loginForm: '#form-login',
    registerForm: '#form-register',
    tabLogin: '#tab-login',
    tabRegister: '#tab-register',
    loginEmail: '#loginEmail',
    loginPassword: '#loginPassword',
    regEmail: '#regEmail',
    regPassword: '#regPassword',
    regFullName: '#regFullName',
    regCurrency: '#regCurrency',
    emailError: '#emailError',
    passwordError: '#passwordError',
    regEmailError: '#regEmailError',
    regPasswordError: '#regPasswordError',
    fullNameError: '#fullNameError',
    currencyError: '#currencyError',
    errorBox: '#errorBox',
    errorBoxRegister: '#errorBoxRegister',
    loginButton: '#btn-login',
    registerButton: '#btn-register',
    themeToggle: '#btn-theme',
    showPasswordLogin: '#show-login-password',
    showPasswordRegister: '#show-register-password'
  };

  function value(id) {
    const el = document.querySelector(id);
    return el ? el.value : '';
  }

  function showError(msg, boxSelector) {
    const box = document.querySelector(boxSelector || selectors.errorBox);
    if (!box) return;
    if (!msg) {
      box.style.display = 'none';
      box.textContent = '';
    } else {
      box.style.display = 'block';
      box.textContent = msg;
    }
  }

  function clearFieldErrors() {
    const inputs = ['loginEmail', 'loginPassword', 'regEmail', 'regPassword', 'regFullName', 'regCurrency'];
    const errors = ['emailError', 'passwordError', 'regEmailError', 'regPasswordError', 'fullNameError', 'currencyError'];
    inputs.forEach(key => {
      const el = document.querySelector(selectors[key]);
      if (el) el.classList.remove('error');
    });
    errors.forEach(key => {
      const el = document.querySelector(selectors[key]);
      if (el) { el.style.display = 'none'; el.textContent = ''; }
    });
  }

  function showFieldError(field, message) {
    const map = {
      email: ['emailError', 'regEmailError'],
      password: ['passwordError', 'regPasswordError'],
      fullName: ['fullNameError'],
      currency: ['currencyError']
    };
    const inputMap = {
      email: ['loginEmail', 'regEmail'],
      password: ['loginPassword', 'regPassword'],
      fullName: ['regFullName'],
      currency: ['regCurrency']
    };
    (map[field] || []).forEach(key => {
      const el = document.querySelector('#' + key);
      if (el) {
        if (message) {
          el.style.display = 'block';
          el.textContent = message;
        } else {
          el.style.display = 'none';
          el.textContent = '';
        }
      }
    });
    (inputMap[field] || []).forEach(key => {
      const el = document.querySelector('#' + key);
      if (el) el.classList.add('error');
    });
  }

  function togglePassword(inputId, btn) {
    const input = document.querySelector(inputId);
    if (!input) return;
    if (input.type === 'password') {
      input.type = 'text';
      if (btn) btn.textContent = 'Скрыть';
    } else {
      input.type = 'password';
      if (btn) btn.textContent = 'Показать';
    }
  }

  function switchForm(form) {
    const loginForm = document.querySelector(selectors.loginForm);
    const registerForm = document.querySelector(selectors.registerForm);
    const tabLogin = document.querySelector(selectors.tabLogin);
    const tabRegister = document.querySelector(selectors.tabRegister);
    if (!loginForm || !registerForm || !tabLogin || !tabRegister) return;
    clearFieldErrors();
    showError('');
    showError('', selectors.errorBoxRegister);
    if (form === 'register') {
      loginForm.style.display = 'none';
      registerForm.style.display = 'grid';
      tabLogin.classList.remove('active');
      tabRegister.classList.add('active');
    } else {
      registerForm.style.display = 'none';
      loginForm.style.display = 'grid';
      tabRegister.classList.remove('active');
      tabLogin.classList.add('active');
    }
  }

  async function loadCurrencies() {
    try {
      const resp = await fetch('/api/currencies');
      const data = await resp.json();
      const select = document.querySelector(selectors.regCurrency);
      if (!select) return;
      select.innerHTML = '';
      (data || []).forEach((c) => {
        const opt = document.createElement('option');
        opt.value = c.code;
        opt.textContent = `${c.code} — ${c.name}`;
        select.appendChild(opt);
      });
      if (data && data.length > 0) {
        select.value = data[0].code;
      }
    } catch (e) {
      // fallback
      const select = document.querySelector(selectors.regCurrency);
      if (select) {
        select.innerHTML = '<option value="USD">USD — US Dollar</option>';
        select.value = 'USD';
      }
    }
  }
  function handleErrorCode(code, message) {
    showError('');
    showError('', selectors.errorBoxRegister);
    switch (code) {
      case '100001':
        showFieldError('email', '');
        showFieldError('password', '');
        showError('Неверный email или пароль');
        break;
      case '100002':
        showFieldError('email', 'Такой email уже зарегистрирован');
        break;
      case '100003':
        showFieldError('password', 'Пароль слишком слабый: нужен верхний/нижний регистр, цифра и спецсимвол');
        break;
      case '100004':
        showFieldError('password', 'Аккаунт временно заблокирован. Попробуйте позже.');
        break;
      case '400002':
        showFieldError('email', message || 'Некорректный email');
        break;
      case '400003':
        showFieldError('password', message || 'Пароль не соответствует требованиям');
        break;
      default:
        showError(message || 'Ошибка запроса');
    }
  }

  function validateLogin() {
    clearFieldErrors();
    let valid = true;
    const email = (value(selectors.loginEmail) || '').trim().toLowerCase();
    const password = (value(selectors.loginPassword) || '').trim();
    const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

    if (!email) {
      showFieldError('email', 'Введите email');
      valid = false;
    } else if (!emailRegex.test(email)) {
      showFieldError('email', 'Введите корректный email');
      valid = false;
    }

    if (!password) {
      showFieldError('password', 'Введите пароль');
      valid = false;
    }

    return { valid, payload: { email, password } };
  }

  function validateRegister() {
    clearFieldErrors();
    let valid = true;
    const email = (value(selectors.regEmail) || '').trim().toLowerCase();
    const password = (value(selectors.regPassword) || '').trim();
    const fullName = (value(selectors.regFullName) || '').trim();
    const currency = (value(selectors.regCurrency) || '').trim().toUpperCase();
    const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

    if (!email) {
      showFieldError('email', 'Введите email');
      valid = false;
    } else if (!emailRegex.test(email)) {
      showFieldError('email', 'Введите корректный email');
      valid = false;
    }

    if (!password) {
      showFieldError('password', 'Введите пароль');
      valid = false;
    }

    if (!fullName) {
      showFieldError('fullName', 'Введите имя');
      valid = false;
    }
    if (!currency) {
      showFieldError('currency', 'Укажите базовую валюту');
      valid = false;
    }

    return {
      valid,
      payload: {
        email,
        password,
        fullName: fullName || 'User',
        baseCurrency: currency || 'USD'
      }
    };
  }

  async function login() {
    const validation = validateLogin();
    if (!validation.valid) return;
    const result = await Api.call('/api/auth/login', 'POST', validation.payload, false);
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(validation.payload.email);
      window.location.href = '/app/dashboard.html';
    } else {
      const code = result.data && result.data.code ? result.data.code : '----';
      const message = result.data && result.data.message ? result.data.message : 'Ошибка запроса';
      handleErrorCode(code, message);
    }
  }

  async function register() {
    const validation = validateRegister();
    if (!validation.valid) return;
    const result = await Api.call('/api/auth/register', 'POST', validation.payload, false);
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(validation.payload.email);
      window.location.href = '/app/dashboard.html';
    } else {
      const code = result.data && result.data.code ? result.data.code : '----';
      const message = result.data && result.data.message ? result.data.message : 'Ошибка запроса';
      handleErrorCode(code, message);
    }
  }

  function bindAuthActions() {
    const loginBtn = document.querySelector(selectors.loginButton);
    if (loginBtn) loginBtn.addEventListener('click', login);
    const registerBtn = document.querySelector(selectors.registerButton);
    if (registerBtn) registerBtn.addEventListener('click', register);
    const tabLogin = document.querySelector(selectors.tabLogin);
    const tabRegister = document.querySelector(selectors.tabRegister);
    if (tabLogin) tabLogin.addEventListener('click', () => switchForm('login'));
    if (tabRegister) tabRegister.addEventListener('click', () => switchForm('register'));
    const showLoginPw = document.querySelector(selectors.showPasswordLogin);
    if (showLoginPw) showLoginPw.addEventListener('click', (e) => togglePassword('#loginPassword', e.target));
    const showRegPw = document.querySelector(selectors.showPasswordRegister);
    if (showRegPw) showRegPw.addEventListener('click', (e) => togglePassword('#regPassword', e.target));
    loadCurrencies();
  }

  document.addEventListener('DOMContentLoaded', () => {
    Theme.init(selectors.themeToggle);
    bindAuthActions();
    switchForm('login');
  });
})();
