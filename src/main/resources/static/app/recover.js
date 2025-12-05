(() => {
  const selectors = {
    themeToggle: '#btn-theme',
    forgotForm: '#form-forgot',
    forgotEmail: '#fpEmail',
    forgotEmailError: '#fpEmailError',
    forgotStatus: '#forgotStatus',
    forgotButton: '#btn-forgot',
    forgotTokenRow: '#inline-token',
    forgotToken: '#fpToken',
    forgotTokenError: '#fpTokenError',
    forgotNext: '#btn-continue-reset',
    resetForm: '#form-reset',
    resetPassword: '#resetPassword',
    resetPasswordError: '#resetPasswordError',
    resetPasswordConfirm: '#resetPasswordConfirm',
    resetConfirmError: '#resetConfirmError',
    resetStatus: '#resetStatus',
    resetButton: '#btn-reset',
    showResetPassword: '#show-reset-password',
    showResetConfirm: '#show-reset-confirm'
  };

  const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;
  const strongRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$/;
  const resendCooldown = 60;
  let resetSessionToken = '';
  let resetCountdown = 0;
  let resetCountdownTimer;

  let submittingForgot = false;
  let submittingReset = false;
  let cooldownTimer;
  let cooldownRemaining = 0;
  let forgotButtonText = '';

  const qs = (sel) => document.querySelector(sel);
  const val = (sel) => {
    const el = qs(sel);
    return el ? el.value : '';
  };

  function setAlert(sel, msg, type) {
    const box = qs(sel);
    if (!box) return;
    if (!msg) {
      box.style.display = 'none';
      box.textContent = '';
      box.classList.remove('success');
      return;
    }
    box.textContent = msg;
    box.style.display = 'block';
    box.classList.toggle('success', type === 'success');
  }

  function formatTimer(seconds) {
    const m = String(Math.floor(seconds / 60)).padStart(2, '0');
    const s = String(Math.max(seconds % 60, 0)).padStart(2, '0');
    return `${m}:${s}`;
  }

  function clearFieldErrors() {
    [selectors.forgotEmail, selectors.forgotToken, selectors.resetPassword, selectors.resetPasswordConfirm].forEach((sel) => {
      const el = qs(sel);
      if (el) el.classList.remove('error');
    });
    ['fpEmailError', 'fpTokenError', 'resetPasswordError', 'resetConfirmError'].forEach((id) => {
      const el = qs('#' + id);
      if (el) {
        el.textContent = '';
        el.style.display = 'none';
      }
    });
  }

  function showFieldError(id, msg) {
    const input = qs('#' + id);
    const error = qs('#' + id + 'Error');
    if (input) input.classList.add('error');
    if (error) {
      error.textContent = msg;
      error.style.display = msg ? 'block' : 'none';
    }
  }

  function togglePassword(inputId, btn) {
    const input = qs(inputId);
    if (!input) return;
    if (input.type === 'password') {
      input.type = 'text';
      if (btn) btn.textContent = 'Скрыть';
    } else {
      input.type = 'password';
      if (btn) btn.textContent = 'Показать';
    }
  }

  function startResetCountdown(seconds) {
    if (resetCountdownTimer) {
      clearInterval(resetCountdownTimer);
      resetCountdownTimer = null;
    }
    resetCountdown = Math.max(seconds, 0);
    if (resetCountdown <= 0) {
      return;
    }
    setAlert(selectors.resetStatus, `Сессия сброса активна: ${formatTimer(resetCountdown)}`, 'success');
    resetCountdownTimer = setInterval(() => {
      resetCountdown -= 1;
      if (resetCountdown <= 0) {
        clearInterval(resetCountdownTimer);
        resetCountdownTimer = null;
        resetSessionToken = '';
        setAlert(selectors.resetStatus, 'Сессия сброса истекла. Введите код снова.', 'error');
      } else {
        setAlert(selectors.resetStatus, `Сессия сброса активна: ${formatTimer(resetCountdown)}`, 'success');
      }
    }, 1000);
  }

  function setSubmitting(buttonSelectors, state) {
    buttonSelectors.forEach((sel) => {
      const btn = qs(sel);
      if (btn) btn.disabled = state;
    });
  }

  function updateCooldownLabel() {
    const btn = qs(selectors.forgotButton);
    if (!btn) return;
    if (cooldownRemaining > 0) {
      btn.textContent = `Отправить код повторно (${cooldownRemaining}s)`;
    } else {
      btn.textContent = forgotButtonText || 'Отправить код';
    }
  }

  function startCooldown() {
    const btn = qs(selectors.forgotButton);
    if (!btn) return;
    if (!forgotButtonText) forgotButtonText = btn.textContent || 'Отправить код';
    cooldownRemaining = resendCooldown;
    btn.disabled = true;
    updateCooldownLabel();
    if (cooldownTimer) clearInterval(cooldownTimer);
    cooldownTimer = setInterval(() => {
      cooldownRemaining -= 1;
      if (cooldownRemaining <= 0) {
        clearInterval(cooldownTimer);
        cooldownTimer = null;
        btn.disabled = false;
        cooldownRemaining = 0;
      }
      updateCooldownLabel();
    }, 1000);
  }

  function validateForgot() {
    clearFieldErrors();
    const email = (val(selectors.forgotEmail) || '').trim().toLowerCase();
    if (!email) {
      showFieldError('fpEmail', 'Введите email');
      return { valid: false, email };
    }
    if (!emailRegex.test(email)) {
      showFieldError('fpEmail', 'Введите корректный email');
      return { valid: false, email };
    }
    return { valid: true, email };
  }

  function showTokenInput() {
    const row = qs(selectors.forgotTokenRow);
    if (row) {
      row.style.display = 'grid';
      row.classList.add('active');
    }
  }

  async function continueToReset() {
    clearFieldErrors();
    const tokenVal = (val(selectors.forgotToken) || '').trim();
    const emailVal = (val(selectors.forgotEmail) || '').trim().toLowerCase();
    if (!tokenVal) {
      showFieldError('fpToken', 'Введите код из письма');
      return;
    }
    if (tokenVal.length < 6) {
      showFieldError('fpToken', 'Код слишком короткий');
      return;
    }
    const url = '/app/reset.html?token=' + encodeURIComponent(tokenVal) + (emailVal ? `&email=${encodeURIComponent(emailVal)}` : '');
    window.location.href = url;
  }

  async function confirmResetSession(tokenVal, emailVal) {
    if (!tokenVal) {
      setAlert(selectors.resetStatus, 'Код не найден. Вернитесь и запросите новый.', 'error');
      return;
    }
    const res = await Api.call('/api/auth/reset/confirm', 'POST', { token: tokenVal }, false);
    if (!res.ok) {
      const code = res.data && res.data.code ? res.data.code : '';
      if (code === '100005') {
        const qs = new URLSearchParams();
        qs.set('reason', 'expired');
        if (emailVal) qs.set('email', emailVal);
        window.location.href = `/app/forgot.html?${qs.toString()}`;
      } else if (code === '429001') {
        setAlert(selectors.resetStatus, 'Слишком много попыток. Попробуйте позже.', 'error');
      } else {
        setAlert(selectors.resetStatus, 'Не удалось подтвердить код. Попробуйте снова.', 'error');
      }
      resetSessionToken = '';
      return;
    }
    resetSessionToken = res.data && res.data.resetSessionToken ? res.data.resetSessionToken : '';
    const ttl = res.data && res.data.expiresInSeconds ? res.data.expiresInSeconds : 0;
    startResetCountdown(ttl);
  }

  function validateReset() {
    clearFieldErrors();
    setAlert(selectors.resetStatus, '');
    let valid = true;
    const sessionToken = (resetSessionToken || '').trim();
    const password = (val(selectors.resetPassword) || '').trim();
    const confirm = (val(selectors.resetPasswordConfirm) || '').trim();

    if (!sessionToken) {
      setAlert(selectors.resetStatus, 'Сначала подтвердите код из письма.', 'error');
      valid = false;
    }

    if (!password) {
      showFieldError('resetPassword', 'Введите новый пароль');
      valid = false;
    } else if (!strongRegex.test(password)) {
      showFieldError('resetPassword', 'Пароль должен быть не короче 10 символов, содержать верхний/нижний регистр, цифру и спецсимвол');
      valid = false;
    }

    if (!confirm) {
      showFieldError('resetPasswordConfirm', 'Повторите пароль');
      valid = false;
    } else if (confirm !== password) {
      showFieldError('resetPasswordConfirm', 'Пароли не совпадают');
      valid = false;
    }

    return { valid, payload: { resetSessionToken: sessionToken, password } };
  }

  async function submitForgot() {
    if (submittingForgot) return;
    const validation = validateForgot();
    if (!validation.valid) return;
    submittingForgot = true;
    setSubmitting([selectors.forgotButton], true);
    const result = await Api.call('/api/auth/forgot', 'POST', { email: validation.email }, false);
    submittingForgot = false;
    if (!cooldownTimer) setSubmitting([selectors.forgotButton], false);
    if (result.ok) {
      showTokenInput();
      startCooldown();
    } else {
      const code = result.data && result.data.code ? result.data.code : '';
      if (code === '400002') {
        showFieldError('fpEmail', 'Введите корректный email');
      } else {
        setAlert(selectors.forgotStatus, 'Не получилось отправить письмо. Попробуйте ещё раз позже.');
      }
    }
  }

  function handleResetError(code) {
    switch (code) {
      case '100003':
      case '400003':
        showFieldError('resetPassword', 'Пароль слишком слабый: нужен верхний/нижний регистр, цифра и спецсимвол.');
        break;
      case '100005':
        resetSessionToken = '';
        setAlert(selectors.resetStatus, 'Сессия сброса устарела или уже использована. Запросите новый код.', 'error');
        break;
      default:
        setAlert(selectors.resetStatus, 'Не удалось обновить пароль. Проверьте данные или повторите позже.');
    }
  }

  async function submitReset() {
    if (submittingReset) return;
    const validation = validateReset();
    if (!validation.valid) return;
    submittingReset = true;
    setSubmitting([selectors.resetButton], true);
    const result = await Api.call('/api/auth/reset', 'POST', validation.payload, false);
    submittingReset = false;
    setSubmitting([selectors.resetButton], false);
    if (result.ok) {
      setAlert(selectors.resetStatus, 'Пароль обновлен. Сейчас перенаправим на экран входа.', 'success');
      setTimeout(() => {
        window.location.href = '/app/login.html';
      }, 900);
      [selectors.resetPassword, selectors.resetPasswordConfirm].forEach((sel) => {
        const input = qs(sel);
        if (input) input.value = '';
      });
    } else {
      const code = result.data && result.data.code ? result.data.code : '';
      handleResetError(code);
    }
  }

  function prefillFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const email = params.get('email');
    const reason = params.get('reason');
    if (reason === 'expired') {
      setAlert(selectors.forgotStatus, 'Код устарел. Запросите новый.', 'error');
      showTokenInput();
    }
    if (token) {
      confirmResetSession(token, email || '');
    }
    if (email) {
      const emailInput = qs(selectors.forgotEmail);
      if (emailInput) emailInput.value = email;
    }
  }

  function bindActions() {
    const forgotBtn = qs(selectors.forgotButton);
    if (forgotBtn) forgotBtn.addEventListener('click', submitForgot);
    const continueBtn = qs(selectors.forgotNext);
    if (continueBtn) continueBtn.addEventListener('click', continueToReset);
    const resetBtn = qs(selectors.resetButton);
    if (resetBtn) resetBtn.addEventListener('click', submitReset);
    const showResetPw = qs(selectors.showResetPassword);
    if (showResetPw) showResetPw.addEventListener('click', (e) => togglePassword(selectors.resetPassword, e.target));
    const showResetConfirm = qs(selectors.showResetConfirm);
    if (showResetConfirm) showResetConfirm.addEventListener('click', (e) => togglePassword(selectors.resetPasswordConfirm, e.target));
  }

  document.addEventListener('DOMContentLoaded', () => {
    Theme.init(selectors.themeToggle);
    prefillFromQuery();
    bindActions();
  });
})();
