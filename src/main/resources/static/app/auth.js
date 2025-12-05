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
    otpSection: '#otpSection',
    otpCode: '#otpCode',
    otpError: '#otpError',
    otpButton: '#btn-otp',
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
    const errors = ['emailError', 'passwordError', 'otpError', 'regEmailError', 'regPasswordError', 'fullNameError', 'currencyError'];
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
    hideOtpSection();
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

  function clearPasswords() {
    const loginPw = document.querySelector(selectors.loginPassword);
    const regPw = document.querySelector(selectors.regPassword);
    if (loginPw) loginPw.value = '';
    if (regPw) regPw.value = '';
  }

  async function loadCurrencies() {
    const select = document.querySelector(selectors.regCurrency);
    const registerBtn = document.querySelector(selectors.registerButton);
    if (registerBtn) registerBtn.disabled = true;
    if (select) {
      select.disabled = true;
      select.innerHTML = '<option value="">Загрузка...</option>';
    }
    showError('', selectors.errorBoxRegister);
    try {
      const resp = await fetch('/api/currencies');
      const data = await resp.json();
      if (!select) return;
      select.innerHTML = '';
      if (Array.isArray(data) && data.length > 0) {
        data.forEach((c) => {
          const opt = document.createElement('option');
          opt.value = c.code;
          opt.textContent = `${c.code} — ${c.name}`;
          select.appendChild(opt);
        });
        select.value = data[0].code;
        select.disabled = false;
        if (registerBtn) registerBtn.disabled = false;
      } else {
        select.innerHTML = '<option value="">Не удалось загрузить валюты</option>';
        select.disabled = true;
        showError('Не удалось загрузить валюты. Попробуйте позже.', selectors.errorBoxRegister);
      }
    } catch (e) {
      if (select) {
        select.innerHTML = '<option value="">Не удалось загрузить валюты</option>';
        select.disabled = true;
      }
      showError('Не удалось загрузить валюты. Попробуйте позже.', selectors.errorBoxRegister);
    }
  }
  function handleErrorCode(code) {
    const defaultMessage = 'Ошибка запроса. Попробуйте позже.';
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
        showFieldError('email', 'Некорректный email');
        break;
      case '400003':
        showFieldError('password', 'Пароль не соответствует требованиям');
        break;
      default:
        showError(defaultMessage);
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
    } else {
      const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;
      if (!strongRegex.test(password)) {
        showFieldError('password', 'Пароль должен быть не короче 10 символов, содержать верхний/нижний регистр, цифру и спецсимвол');
        valid = false;
      }
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

  let submitting = false;

  function showOtpSection(expiresInSeconds) {
    const otpSection = document.querySelector(selectors.otpSection);
    const otpInput = document.querySelector(selectors.otpCode);
    const otpError = document.querySelector(selectors.otpError);
    const loginBtn = document.querySelector(selectors.loginButton);
    if (otpSection) otpSection.style.display = 'grid';
    if (loginBtn) loginBtn.style.display = 'none';
    if (otpError) { otpError.textContent = ''; otpError.style.display = 'none'; }
    if (otpInput) otpInput.value = '';
  }

  function hideOtpSection() {
    const otpSection = document.querySelector(selectors.otpSection);
    const otpError = document.querySelector(selectors.otpError);
    const otpInput = document.querySelector(selectors.otpCode);
    const loginBtn = document.querySelector(selectors.loginButton);
    if (otpSection) otpSection.style.display = 'none';
    if (otpError) { otpError.textContent = ''; otpError.style.display = 'none'; }
    if (otpInput) otpInput.value = '';
    if (loginBtn) loginBtn.style.display = 'inline-block';
  }

  async function submitOtp(email) {
    const codeInput = document.querySelector(selectors.otpCode);
    const otpError = document.querySelector(selectors.otpError);
    if (!codeInput) return;
    const code = (codeInput.value || '').trim();
    if (!code) {
      if (otpError) { otpError.textContent = 'Введите код из письма'; otpError.style.display = 'block'; }
      return;
    }
    submitting = true;
    setSubmitting(true);
    const result = await Api.call('/api/auth/login/otp', 'POST', { email, code }, false);
    submitting = false;
    setSubmitting(false);
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(email);
      window.location.href = '/app/dashboard.html';
    } else {
      if (otpError) { otpError.textContent = 'Код неверный или истек. Попробуйте снова.'; otpError.style.display = 'block'; }
    }
  }

  async function login() {
    if (submitting) return;
    const validation = validateLogin();
    if (!validation.valid) return;
    submitting = true;
    setSubmitting(true);
    const result = await Api.call('/api/auth/login', 'POST', validation.payload, false);
    submitting = false;
    setSubmitting(false);
    if (result.status === 202 && result.data && result.data.otpRequired) {
      submitting = false;
      setSubmitting(false);
      showOtpSection(result.data.expiresInSeconds);
      return;
    }
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(validation.payload.email);
      window.location.href = '/app/dashboard.html';
    } else {
      const code = result.data && result.data.code ? result.data.code : '----';
      handleErrorCode(code);
    }
  }

  async function register() {
    if (submitting) return;
    const currencySelect = document.querySelector(selectors.regCurrency);
    if (currencySelect && currencySelect.disabled) {
      showError('Не удалось загрузить валюты. Обновите страницу и попробуйте снова.', selectors.errorBoxRegister);
      return;
    }
    const validation = validateRegister();
    if (!validation.valid) return;
    submitting = true;
    setSubmitting(true);
    const result = await Api.call('/api/auth/register', 'POST', validation.payload, false);
    submitting = false;
    setSubmitting(false);
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(validation.payload.email);
      window.location.href = '/app/dashboard.html';
    } else {
      const code = result.data && result.data.code ? result.data.code : '----';
      handleErrorCode(code);
    }
  }

  function setSubmitting(state) {
    const buttons = [selectors.loginButton, selectors.registerButton, selectors.otpButton].map((sel) => document.querySelector(sel));
    buttons.forEach((btn) => {
      if (btn) btn.disabled = state;
    });
  }

  function bindAuthActions() {
    const loginBtn = document.querySelector(selectors.loginButton);
    if (loginBtn) loginBtn.addEventListener('click', login);
    const registerBtn = document.querySelector(selectors.registerButton);
    if (registerBtn) registerBtn.addEventListener('click', register);
    const otpBtn = document.querySelector(selectors.otpButton);
    if (otpBtn) otpBtn.addEventListener('click', () => submitOtp((value(selectors.loginEmail) || '').trim().toLowerCase()));
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
    clearPasswords();
    bindAuthActions();
    switchForm('login');
  });
})();
