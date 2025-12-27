const Theme = (() => {
  function apply() {
    if (document.body) document.body.classList.remove('light');
    try {
      localStorage.removeItem('finguard_theme');
    } catch (_) {
      // ignore
    }
  }

  function toggle() {
    apply();
  }

  function init() {
    apply();
  }

  return { apply, toggle, init };
})();

window.Theme = Theme;
