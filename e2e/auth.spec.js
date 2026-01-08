const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('anonymous dashboard redirects to login', async ({ page }) => {
  await page.goto('/app/dashboard.html');
  await expect(page).toHaveURL(/\/app\/login\.html$/);
});

test('register -> verify -> dashboard -> logout', async ({ page }) => {
  const email = uniqueEmail('e2e-auth');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-logout');
  await expect(page).toHaveURL(/\/$/);
});
