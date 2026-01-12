(() => {
  const selectors = {
    themeToggle: '#btn-theme',
    email: '#verifyEmail',
    token: '#verifyToken',
    emailError: '#verifyEmailError',
    tokenError: '#verifyTokenError',
    status: '#verifyStatus',
    btnVerify: '#btn-verify',
    btnRequest: '#btn-request'
  };

  function t(key, vars) {
    if (window.I18n && typeof window.I18n.t === 'function') {
      return window.I18n.t(key, vars);
    }
    return key;
  }

  const emailRegex = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

  const qs = (sel) => document.querySelector(sel);
  const val = (sel) => {
    const el = qs(sel);
    return el ? el.value : '';
  };

  function showFieldError(sel, msg) {
    const input = qs(sel);
    const error = qs(sel + 'Error');
    if (input && msg) input.classList.add('error');
    if (input && !msg) input.classList.remove('error');
    if (error) {
      error.textContent = msg || '';
      error.style.display = msg ? 'block' : 'none';
    }
  }

  function setStatus(msg, success) {
    const box = qs(selectors.status);
    if (!box) return;
    if (!msg) {
      box.style.display = 'none';
      box.textContent = '';
      box.classList.remove('success');
      return;
    }
    box.textContent = msg;
    box.style.display = 'block';
    box.classList.toggle('success', !!success);
  }

  function validateEmail(email) {
    if (!email) {
      showFieldError(selectors.email, t('msg_enter_email'));
      return false;
    }
    if (!emailRegex.test(email)) {
      showFieldError(selectors.email, t('msg_enter_valid_email'));
      return false;
    }
    showFieldError(selectors.email, '');
    return true;
  }

  async function requestCode() {
    const emailVal = (val(selectors.email) || '').trim().toLowerCase();
    if (!validateEmail(emailVal)) return;
    setStatus('');
    const btn = qs(selectors.btnRequest);
    if (btn) btn.disabled = true;
    const res = await Api.call('/api/auth/verify/request', 'POST', { email: emailVal }, false);
    if (btn) btn.disabled = false;
    if (res.ok) {
      setStatus(t('msg_verify_sent_if_needed'), true);
    } else {
      const code = res.data && res.data.code ? res.data.code : '';
      if (code === '429001') {
        setStatus(t('msg_forgot_too_many_attempts'), false);
      } else if (code === '400002') {
        showFieldError(selectors.email, t('msg_enter_valid_email'));
      } else {
        setStatus(t('msg_verify_send_failed'), false);
      }
    }
  }

  function validateToken(token) {
    if (!token) {
      showFieldError(selectors.token, t('msg_enter_email_code'));
      return false;
    }
    showFieldError(selectors.token, '');
    return true;
  }

  async function verify() {
    const emailVal = (val(selectors.email) || '').trim().toLowerCase();
    const tokenVal = (val(selectors.token) || '').trim();
    setStatus('');
    if (!validateEmail(emailVal) | !validateToken(tokenVal)) return;
    const btn = qs(selectors.btnVerify);
    if (btn) btn.disabled = true;
    const res = await Api.call('/api/auth/verify', 'POST', { email: emailVal, token: tokenVal }, false);
    if (btn) btn.disabled = false;
    if (res.ok) {
      setStatus(t('msg_verify_success'), true);
      const target = `/app/login.html?verified=1&email=${encodeURIComponent(emailVal)}`;
      setTimeout(() => { window.location.href = target; }, 600);
    } else {
      const code = res.data && res.data.code ? res.data.code : '';
      if (code === '100005') {
        setStatus(t('msg_verify_code_invalid'), false);
      } else if (code === '429001') {
        setStatus(t('msg_forgot_too_many_attempts'), false);
      } else {
        setStatus(t('msg_verify_failed_retry'), false);
      }
    }
  }

  function prefillFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token') || '';
    const email = params.get('email') || '';
    if (email) {
      const el = qs(selectors.email);
      if (el) el.value = email;
    }
    if (token) {
      const tokenInput = qs(selectors.token);
      if (tokenInput) tokenInput.value = token;
      verify();
    }
  }

  function bindActions() {
    const btnReq = qs(selectors.btnRequest);
    if (btnReq) btnReq.addEventListener('click', requestCode);
    const btnVer = qs(selectors.btnVerify);
    if (btnVer) btnVer.addEventListener('click', verify);
  }

  document.addEventListener('DOMContentLoaded', () => {
    Theme.init(selectors.themeToggle);
    bindActions();
    prefillFromQuery();
  });
})();
