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
    otpResendButton: '#btn-otp-resend',
    verifySection: '#verifySection',
    verifyCode: '#verifyCode',
    verifyError: '#verifyCodeError',
    verifyStatus: '#verifyStatus',
    verifyHint: '#verifyHint',
    verifyButton: '#btn-verify-code',
    verifyResendButton: '#btn-verify-resend',
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

  const errorHideTimers = {};

  async function resendOtp() {
    if (submitting) return;
    const remaining = remainingOtpCooldownSeconds();
    if (remaining > 0) {
      startOtpCooldownTimer();
      return;
    }
    const email = (value(selectors.loginEmail) || '').trim().toLowerCase();
    const password = (value(selectors.loginPassword) || '').trim();
    if (!email || !password) {
      showError('Введите email и пароль, чтобы отправить новый код.');
      showFieldError('email', email ? '' : 'Введите email');
      showFieldError('password', password ? '' : 'Введите пароль');
      return;
    }
    await login();
  }

  function showError(msg, boxSelector) {
    const key = boxSelector || selectors.errorBox;
    const box = document.querySelector(key);
    if (!box) return;
    if (errorHideTimers[key]) {
      clearTimeout(errorHideTimers[key]);
      delete errorHideTimers[key];
    }
    box.classList.remove('success');
    if (!msg) {
      box.style.display = 'none';
      box.textContent = '';
    } else {
      box.style.display = 'block';
      box.textContent = msg;
      errorHideTimers[key] = window.setTimeout(() => {
        box.style.display = 'none';
        box.textContent = '';
        delete errorHideTimers[key];
      }, 5000);
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
    // сохраняем состояние OTP при возврате на логин, скрываем только визуально при переходе на регистрацию
    if (form === 'register') {
      const otpSection = document.querySelector(selectors.otpSection);
      if (otpSection) otpSection.style.display = 'none';
      const loginBtn = document.querySelector(selectors.loginButton);
      if (loginBtn) loginBtn.style.display = 'inline-block';
    } else if (form === 'login' && otpPending) {
      showOtpSection();
    } else {
      hideOtpSection();
    }
    clearFieldErrors();
    const remaining = remainingLoginCooldownSeconds();
    if (remaining > 0) {
      const formatter = formatCooldownMessage(loginCooldownCode);
      showError(formatter(remaining));
    } else {
      showError('');
    }
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
      case '429001':
        const left = remainingLoginCooldownSeconds() || 60;
        showError(`Слишком много попыток. Подождите ${left} сек.`);
        break;
      case '429002':
        showError('');
        break;
      case '400002':
        showFieldError('email', 'Некорректный email');
        break;
      case '400003':
        showFieldError('password', 'Пароль не соответствует требованиям');
        break;
      case '100006': {
        const email = (value(selectors.loginEmail) || '').trim().toLowerCase();
        const password = (value(selectors.loginPassword) || '').trim();
        verifyPending = true;
        verifyEmail = email;
        verifyPassword = password;
        showVerifySection('Email не подтвержден. Введите код из письма.');
        showVerifyStatus('Мы отправили код на указанный email.', false);
        Api.call('/api/auth/verify/request', 'POST', { email }, false);
        break;
      }
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
  let otpPending = false;
  let otpEmail = '';
  let otpCooldownUntil = 0;
  let otpCodeValue = '';
  let loginCooldownUntil = 0;
  let loginCooldownCode = '';
  let loginCooldownTimer = null;
  let otpCooldownTimer = null;
  let verifyPending = false;
  let verifyEmail = '';
  let verifyPassword = '';
  let verifyCooldownUntil = 0;
  let verifyCooldownTimer = null;

  function loadOtpState() {
    try {
      const raw = localStorage.getItem('otp_state') || '';
      if (!raw) return;
      const parsed = JSON.parse(raw);
      otpPending = Boolean(parsed.pending);
      otpEmail = parsed.email || '';
      otpCooldownUntil = parsed.cooldownUntil || 0;
      otpCodeValue = parsed.codeValue || '';
    } catch (e) {
      otpPending = false;
      otpEmail = '';
      otpCooldownUntil = 0;
      otpCodeValue = '';
    }
  }

  function saveOtpState() {
    localStorage.setItem('otp_state', JSON.stringify({
      pending: otpPending,
      email: otpEmail,
      cooldownUntil: otpCooldownUntil,
      codeValue: otpCodeValue
    }));
  }

  function loadLoginCooldown() {
    const raw = localStorage.getItem('login_cooldown_until') || '0';
    loginCooldownUntil = Number(raw) || 0;
    loginCooldownCode = localStorage.getItem('login_cooldown_code') || '';
  }

  function saveLoginCooldown(until, code) {
    loginCooldownUntil = until;
    localStorage.setItem('login_cooldown_until', String(until));
    if (code) {
      loginCooldownCode = code;
      localStorage.setItem('login_cooldown_code', code);
    }
  }

  function clearLoginCooldown() {
    loginCooldownUntil = 0;
    loginCooldownCode = '';
    localStorage.removeItem('login_cooldown_until');
    localStorage.removeItem('login_cooldown_code');
  }

  function remainingLoginCooldownSeconds() {
    const now = Date.now();
    if (loginCooldownUntil <= now) {
      clearLoginCooldown();
      return 0;
    }
    return Math.ceil((loginCooldownUntil - now) / 1000);
  }

  function formatCooldownMessage(code) {
    return (sec) => {
      return `Слишком много попыток. Подождите ${sec} сек.`;
    };
  }

  function remainingOtpCooldownSeconds() {
    const now = Date.now();
    if (otpCooldownUntil <= now) {
      if (otpCooldownUntil !== 0) {
        otpCooldownUntil = 0;
        saveOtpState();
      } else {
        otpCooldownUntil = 0;
      }
      return 0;
    }
    return Math.max(1, Math.ceil((otpCooldownUntil - now) / 1000));
  }

  function updateOtpResendButton() {
    const btn = document.querySelector(selectors.otpResendButton);
    if (!btn) return;
    const remaining = remainingOtpCooldownSeconds();
    const baseText = 'Отправить новый код';
    if (remaining > 0) {
      btn.disabled = true;
      btn.textContent = `${baseText} (${remaining} сек.)`;
    } else {
      btn.disabled = submitting;
      btn.textContent = baseText;
    }
  }

  function startOtpCooldownTimer() {
    updateOtpResendButton();
    if (otpCooldownTimer) {
      clearInterval(otpCooldownTimer);
      otpCooldownTimer = null;
    }
    const left = remainingOtpCooldownSeconds();
    if (left > 0) {
      otpCooldownTimer = setInterval(() => {
        const remaining = remainingOtpCooldownSeconds();
        updateOtpResendButton();
        if (remaining <= 0 && otpCooldownTimer) {
          clearInterval(otpCooldownTimer);
          otpCooldownTimer = null;
        }
      }, 1000);
    }
  }

  function showVerifySection(message) {
    const section = document.querySelector(selectors.verifySection);
    const hint = document.querySelector(selectors.verifyHint);
    const error = document.querySelector(selectors.verifyError);
    const status = document.querySelector(selectors.verifyStatus);
    const registerBtn = document.querySelector(selectors.registerButton);
    if (section) section.style.display = 'grid';
    if (hint && message) hint.textContent = message;
    if (error) { error.style.display = 'none'; error.textContent = ''; }
    if (status) { status.style.display = 'none'; status.textContent = ''; status.classList.remove('success'); }
    if (registerBtn) registerBtn.style.display = 'none';
    startVerifyCooldownTimer();
  }

  function hideVerifySection() {
    const section = document.querySelector(selectors.verifySection);
    if (section) section.style.display = 'none';
    verifyPending = false;
    verifyEmail = '';
    verifyPassword = '';
    verifyCooldownUntil = 0;
    updateVerifyResendButton();
    const registerBtn = document.querySelector(selectors.registerButton);
    if (registerBtn) registerBtn.style.display = '';
  }

  function showVerifyError(msg) {
    const el = document.querySelector(selectors.verifyError);
    if (!el) return;
    el.style.display = msg ? 'block' : 'none';
    el.textContent = msg || '';
  }

  function showVerifyStatus(msg, success) {
    const el = document.querySelector(selectors.verifyStatus);
    if (!el) return;
    if (!msg) {
      el.style.display = 'none';
      el.textContent = '';
      el.classList.remove('success');
      return;
    }
    el.style.display = 'block';
    el.textContent = msg;
    el.classList.toggle('success', !!success);
  }

  function remainingVerifyCooldownSeconds() {
    const now = Date.now();
    if (verifyCooldownUntil <= now) {
      verifyCooldownUntil = 0;
      return 0;
    }
    return Math.max(1, Math.ceil((verifyCooldownUntil - now) / 1000));
  }

  function updateVerifyResendButton() {
    const btn = document.querySelector(selectors.verifyResendButton);
    if (!btn) return;
    const remaining = remainingVerifyCooldownSeconds();
    const baseText = 'Отправить код еще раз';
    if (remaining > 0) {
      btn.disabled = true;
      btn.textContent = `${baseText} (${remaining} сек.)`;
    } else {
      btn.disabled = submitting;
      btn.textContent = baseText;
    }
  }

  function startVerifyCooldownTimer() {
    updateVerifyResendButton();
    if (verifyCooldownTimer) {
      clearInterval(verifyCooldownTimer);
      verifyCooldownTimer = null;
    }
    const left = remainingVerifyCooldownSeconds();
    if (left > 0) {
      verifyCooldownTimer = setInterval(() => {
        const remaining = remainingVerifyCooldownSeconds();
        updateVerifyResendButton();
        if (remaining <= 0 && verifyCooldownTimer) {
          clearInterval(verifyCooldownTimer);
          verifyCooldownTimer = null;
        }
      }, 1000);
    }
  }

  function startLoginCooldownTimer(formatter) {
    if (loginCooldownCode === '429002') {
      if (loginCooldownTimer) {
        clearInterval(loginCooldownTimer);
        loginCooldownTimer = null;
      }
      showError('');
      return;
    }
    const format = typeof formatter === 'function'
      ? formatter
      : formatCooldownMessage(loginCooldownCode);
    if (loginCooldownTimer) {
      clearInterval(loginCooldownTimer);
      loginCooldownTimer = null;
    }
    const tick = () => {
      const remaining = remainingLoginCooldownSeconds();
      if (remaining > 0) {
        showError(format(remaining));
      } else {
        showError('');
        saveLoginCooldown(0);
        if (loginCooldownTimer) {
          clearInterval(loginCooldownTimer);
          loginCooldownTimer = null;
        }
      }
    };
    tick();
    if (remainingLoginCooldownSeconds() > 0) {
      loginCooldownTimer = setInterval(tick, 1000);
    }
  }

  function showOtpSection() {
    const otpSection = document.querySelector(selectors.otpSection);
    const otpInput = document.querySelector(selectors.otpCode);
    const otpError = document.querySelector(selectors.otpError);
    const loginBtn = document.querySelector(selectors.loginButton);
    if (otpSection) otpSection.style.display = 'grid';
    if (loginBtn) loginBtn.style.display = 'none';
    if (otpError) { otpError.textContent = ''; otpError.style.display = 'none'; }
    if (otpInput) otpInput.value = otpCodeValue || '';
    otpPending = true;
    saveOtpState();
    startOtpCooldownTimer();
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
    otpPending = false;
    otpEmail = '';
    otpCodeValue = '';
    otpCooldownUntil = 0;
    saveOtpState();
    startOtpCooldownTimer();
  }

  async function submitOtp(email) {
    const codeInput = document.querySelector(selectors.otpCode);
    const otpError = document.querySelector(selectors.otpError);
    if (!codeInput) return;
    const code = (codeInput.value || '').trim();
    otpCodeValue = code;
    saveOtpState();
    if (!code) {
      if (otpError) { otpError.textContent = 'Введите код из письма'; otpError.style.display = 'block'; }
      return;
    }
    submitting = true;
    setSubmitting(true);
    const result = await Api.call('/api/auth/login/otp', 'POST', { email, code }, false);
    submitting = false;
    setSubmitting(false);
    if (result.status === 429) {
      const retry = result.data && result.data.retryAfterSeconds ? Number(result.data.retryAfterSeconds) : 60;
      otpCooldownUntil = Date.now() + retry * 1000;
      otpPending = true;
      otpEmail = email;
      saveOtpState();
      const msg = (result.data && result.data.message)
        ? result.data.message
        : `Слишком много попыток. Попробуйте через ${retry} сек.`;
      showError(msg);
      if (otpError) { otpError.textContent = ''; otpError.style.display = 'none'; }
      showOtpSection();
      startOtpCooldownTimer();
      return;
    }
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(email);
      hideOtpSection();
      window.location.href = '/app/dashboard.html';
    } else {
      if (otpError) { otpError.textContent = 'Код неверный или истек. Попробуйте снова.'; otpError.style.display = 'block'; }
    }
  }

  async function submitVerifyCode() {
    if (!verifyPending || !verifyEmail) {
      showVerifyStatus('Для подтверждения повторите регистрацию или вход.', false);
      return;
    }
    const codeInput = document.querySelector(selectors.verifyCode);
    const code = (codeInput && codeInput.value || '').trim();
    if (!code) {
      showVerifyError('Введите код из письма');
      return;
    }
    showVerifyError('');
    const btn = document.querySelector(selectors.verifyButton);
    if (btn) btn.disabled = true;
    const res = await Api.call('/api/auth/verify', 'POST', { token: code }, false);
    if (btn) btn.disabled = false;
    if (res.ok) {
      showVerifyStatus('Email подтвержден. Входим...', true);
      // Если бек отдал токены и cookies, просто идем в дашборд
      if (res.data && res.data.token) {
        window.location.href = '/app/dashboard.html';
        return;
      }
      // fallback: пробуем логин stored creds
      if (verifyPassword && verifyEmail) {
        const loginResp = await Api.call('/api/auth/login', 'POST', { email: verifyEmail, password: verifyPassword }, false);
        if (loginResp.ok) {
          window.location.href = '/app/dashboard.html';
          return;
        }
      }
      const me = await Api.call('/api/auth/me', 'GET', null, true);
      if (me.ok) {
        window.location.href = '/app/dashboard.html';
      } else {
        window.location.href = `/app/login.html?verified=1&email=${encodeURIComponent(verifyEmail)}`;
      }
      hideVerifySection();
    } else {
      const codeErr = res.data && res.data.code ? res.data.code : '';
      if (codeErr === '100005') {
        showVerifyStatus('Код недействителен или истек. Запросите новый.', false);
      } else {
        showVerifyStatus('Не удалось подтвердить код. Попробуйте снова.', false);
      }
    }
  }

  async function resendVerifyCode() {
    if (!verifyEmail) {
      showVerifyStatus('Введите email и повторите регистрацию или вход.', false);
      return;
    }
    const btn = document.querySelector(selectors.verifyResendButton);
    if (btn) btn.disabled = true;
    const res = await Api.call('/api/auth/verify/request', 'POST', { email: verifyEmail }, false);
    if (btn) btn.disabled = false;
    if (res.ok) {
      showVerifyStatus('Отправили новый код. Проверьте почту.', true);
      const retry = res.data && res.data.retryAfterSeconds ? Number(res.data.retryAfterSeconds) : 60;
      verifyCooldownUntil = Date.now() + retry * 1000;
      startVerifyCooldownTimer();
    } else {
      showVerifyStatus('Не удалось отправить код. Попробуйте позже.', false);
    }
  }

  async function login() {
    if (submitting) return;
    const validation = validateLogin();
    if (!validation.valid) return;
    let cooldownLeft = remainingLoginCooldownSeconds();
    if (cooldownLeft > 0 && loginCooldownCode === '429002') {
      // не блокируем запрос для уже отправленного кода: сервер сам решит, а мы позволим ввести код
      clearLoginCooldown();
      cooldownLeft = 0;
    }
    if (cooldownLeft > 0) {
      const formatter = formatCooldownMessage(loginCooldownCode);
      showError(formatter(cooldownLeft));
      startLoginCooldownTimer(formatter);
      return;
    }
    clearLoginCooldown();
    const now = Date.now();
    if (otpPending && validation.payload.email === otpEmail && now < otpCooldownUntil) {
      showOtpSection();
      startOtpCooldownTimer();
      return;
    }
    submitting = true;
    setSubmitting(true);
    const result = await Api.call('/api/auth/login', 'POST', validation.payload, false);
    submitting = false;
    setSubmitting(false);
    if (result.status === 429) {
      const retry = result.data && result.data.retryAfterSeconds ? Number(result.data.retryAfterSeconds) : 60;
      const code = result.data && result.data.code ? result.data.code : '429001';
      const until = Date.now() + retry * 1000;
      saveLoginCooldown(until, code);
      const formatter = formatCooldownMessage(code);
      if (code === '429002') {
        otpPending = true;
        otpEmail = validation.payload.email;
        otpCooldownUntil = until;
        otpCodeValue = otpCodeValue || '';
        saveOtpState();
        const otpError = document.querySelector(selectors.otpError);
        if (otpError) { otpError.textContent = ''; otpError.style.display = 'none'; }
        showOtpSection();
        showError('');
        startOtpCooldownTimer();
        return;
      } else {
        // не OTP-кулдаун — убираем секцию кода
        otpPending = false;
        otpCooldownUntil = 0;
        otpEmail = '';
        otpCodeValue = '';
        saveOtpState();
        hideOtpSection();
        const msg = result.data && result.data.message ? result.data.message : formatter(retry);
        showError(msg);
      }
      startLoginCooldownTimer(formatter);
      return;
    }
    if (result.status === 202 && result.data && result.data.otpRequired) {
      otpPending = true;
      otpEmail = validation.payload.email;
      otpCodeValue = '';
      otpCooldownUntil = Date.now() + 60000;
      saveOtpState();
      showOtpSection(result.data.expiresInSeconds);
      startOtpCooldownTimer();
      return;
    }
    if (result.ok && result.data && result.data.token) {
      Api.setEmail(validation.payload.email);
      hideOtpSection();
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
    if (result.ok) {
      const email = validation.payload.email;
      const password = validation.payload.password;
      Api.setEmail(email);
      await Api.call('/api/auth/verify/request', 'POST', { email }, false);
      verifyCooldownUntil = Date.now() + 60000;
      startVerifyCooldownTimer();
      verifyPending = true;
      verifyEmail = email;
      verifyPassword = password;
      const hint = (result.data && result.data.message) || 'Мы отправили код на указанный email. Введите его, чтобы завершить регистрацию.';
      showVerifySection(hint);
    } else {
      const code = result.data && result.data.code ? result.data.code : '----';
      handleErrorCode(code);
    }
  }

  function setSubmitting(state) {
    const buttons = [
      selectors.loginButton,
      selectors.registerButton,
      selectors.otpButton,
      selectors.otpResendButton,
      selectors.verifyButton,
      selectors.verifyResendButton
    ].map((sel) => document.querySelector(sel));
    buttons.forEach((btn) => {
      if (!btn) return;
      if (btn.matches(selectors.otpResendButton)) {
        btn.disabled = state || remainingOtpCooldownSeconds() > 0;
      } else if (btn.matches(selectors.verifyResendButton)) {
        btn.disabled = state || remainingVerifyCooldownSeconds() > 0;
      } else {
        btn.disabled = state;
      }
    });
    if (!state) {
      updateOtpResendButton();
      updateVerifyResendButton();
    }
  }

  function bindAuthActions() {
    const loginBtn = document.querySelector(selectors.loginButton);
    if (loginBtn) loginBtn.addEventListener('click', login);
    const registerBtn = document.querySelector(selectors.registerButton);
    if (registerBtn) registerBtn.addEventListener('click', register);
    const otpBtn = document.querySelector(selectors.otpButton);
    if (otpBtn) otpBtn.addEventListener('click', () => submitOtp((value(selectors.loginEmail) || '').trim().toLowerCase()));
    const otpResendBtn = document.querySelector(selectors.otpResendButton);
    if (otpResendBtn) otpResendBtn.addEventListener('click', resendOtp);
    const verifyBtn = document.querySelector(selectors.verifyButton);
    if (verifyBtn) verifyBtn.addEventListener('click', submitVerifyCode);
    const verifyResendBtn = document.querySelector(selectors.verifyResendButton);
    if (verifyResendBtn) verifyResendBtn.addEventListener('click', resendVerifyCode);
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
    loadOtpState();
    loadLoginCooldown();
    const now = Date.now();
    if (otpPending && otpEmail && now < otpCooldownUntil) {
      showOtpSection();
    } else {
      hideOtpSection();
    }
    if (loginCooldownCode !== '429002' && otpPending) {
      hideOtpSection();
      otpPending = false;
      otpEmail = '';
      otpCooldownUntil = 0;
      otpCodeValue = '';
      saveOtpState();
    }
    if (!otpPending && loginCooldownCode === '429002') {
      otpPending = true;
      otpEmail = value(selectors.loginEmail).trim().toLowerCase() || otpEmail;
      showOtpSection();
      saveOtpState();
    }
    clearPasswords();
    bindAuthActions();
    if (loginCooldownCode === '429002') {
        clearLoginCooldown();
        showError('');
    }
    const remaining = remainingLoginCooldownSeconds();
    if (remaining === 0) {
      showError('');
    }
    startOtpCooldownTimer();
    startLoginCooldownTimer();
    switchForm('login');

    const params = new URLSearchParams(window.location.search);
    const verified = params.get('verified');
    const verifiedEmail = params.get('email') || '';
    if (verifiedEmail) {
      const emailInput = document.querySelector(selectors.loginEmail);
      if (emailInput) emailInput.value = verifiedEmail;
    }
    if (verified === '1') {
      const box = document.querySelector(selectors.errorBox);
      if (box) {
        box.classList.add('success');
        box.style.display = 'block';
        box.textContent = 'Email подтвержден. Теперь можно войти.';
      }
    }
    startVerifyCooldownTimer();
  });
})();
