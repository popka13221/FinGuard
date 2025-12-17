(() => {
  function initTheme() {
    Theme.init('#btn-theme');
  }

  function initReveal() {
    const items = document.querySelectorAll('.reveal');
    const io = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('visible');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15 });
    items.forEach((el) => io.observe(el));
  }

  function initAudience() {
    const buttons = document.querySelectorAll('.aud-btn');
    const heroTitle = document.getElementById('hero-title');
    const heroSubtitle = document.getElementById('hero-subtitle');
    const perspTitle = document.getElementById('persp-title');
    const perspSub = document.getElementById('persp-sub');
    const ctaTitle = document.getElementById('cta-title');
    const ctaSub = document.getElementById('cta-sub');

    const copy = {
      users: {
        heroTitle: 'FinGuard. Финансы под контролем.',
        heroSubtitle: 'Учет, цели и умные правила, которые сами следят.',
        perspTitle: 'FinGuard масштабируется вместе с задачей.',
        perspSub: 'Личный, командный, модульный.',
        ctaTitle: 'Готовы включить guard-режим?',
        ctaSub: 'Демо или запуск локально.'
      },
      devs: {
        heroTitle: 'FinGuard — платформа для правил',
        heroSubtitle: 'UI + движок сигналов. Подключайте свои данные.',
        perspTitle: 'Для разработчиков',
        perspSub: 'Движок правил как модуль.',
        ctaTitle: 'Демо и API',
        ctaSub: 'Контейнер и свои сценарии.'
      },
      teams: {
        heroTitle: 'FinGuard — контроль для команд',
        heroSubtitle: 'Общий дашборд, роли, единые правила.',
        perspTitle: 'Для команд',
        perspSub: 'Общий пул правил и уведомления.',
        ctaTitle: 'Подключите команду',
        ctaSub: 'Демо, роли, сценарии.'
      }
    };

    function setAudience(key) {
      const next = copy[key] || copy.users;
      if (heroTitle) heroTitle.textContent = next.heroTitle;
      if (heroSubtitle) heroSubtitle.textContent = next.heroSubtitle;
      if (perspTitle) perspTitle.textContent = next.perspTitle;
      if (perspSub) perspSub.textContent = next.perspSub;
      if (ctaTitle) ctaTitle.textContent = next.ctaTitle;
      if (ctaSub) ctaSub.textContent = next.ctaSub;
      buttons.forEach((btn) => btn.classList.toggle('active', btn.dataset.audience === key));
    }

    buttons.forEach((btn) => {
      btn.addEventListener('click', () => setAudience(btn.dataset.audience));
    });
    setAudience('users');
  }

  function initSnapScroll() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
    if ('ontouchstart' in window) return;
    const sections = Array.from(document.querySelectorAll('.snap-section'));
    if (sections.length < 2) return;
  }

  document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initReveal();
    initAudience();
  });
})();
