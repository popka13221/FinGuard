const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test('forgot password -> confirm code -> reset -> login with new password', async ({ page }) => {
  const email = uniqueEmail('e2e-reset');
  const oldPassword = 'StrongPass1!';
  const newPassword = 'NewStrongPass2@';

  await registerAndLogin(page, { email, password: oldPassword, baseCurrency: 'USD' });
  await page.click('#btn-logout');

  await page.goto('/app/forgot.html');
  await page.fill('#fpEmail', email);
  await page.click('#btn-forgot');
  await expect(page.locator('#inline-token')).toBeVisible();

  await page.fill('#fpToken', '654321');
  await page.click('#btn-continue-reset');
  await expect(page).toHaveURL(/\/app\/reset\.html\?confirmed=1/);

  await page.fill('#resetPassword', newPassword);
  await page.fill('#resetPasswordConfirm', newPassword);
  await page.click('#btn-reset');

  await expect(page).toHaveURL(/\/app\/login\.html$/);

  await page.fill('#loginEmail', email);
  await page.fill('#loginPassword', newPassword);
  await page.click('#btn-login');
  await expect(page).toHaveURL(/\/app\/dashboard\.html$/);
});

