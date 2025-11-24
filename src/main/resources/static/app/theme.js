const themeKey = 'finguard_theme';

const Theme = (() => {
  let theme = localStorage.getItem(themeKey) || 'dark';

  function apply() {
    if (theme === 'light') {
      document.body.classList.add('light');
    } else {
      document.body.classList.remove('light');
    }
  }

  function toggle() {
    theme = theme === 'light' ? 'dark' : 'light';
    localStorage.setItem(themeKey, theme);
    apply();
  }

  function init(buttonSelector) {
    apply();
    if (buttonSelector) {
      const btn = document.querySelector(buttonSelector);
      if (btn) btn.addEventListener('click', toggle);
    }
  }

  return { apply, toggle, init };
})();

window.Theme = Theme;
