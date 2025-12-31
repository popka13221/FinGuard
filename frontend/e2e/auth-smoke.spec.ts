import { expect, test } from '@playwright/test';

test('smoke: register → verify(654321) → logout → login → dashboard', async ({ page }) => {
  const email = `e2e_${Date.now()}@example.com`;
  const password = 'StrongPass1!';
  const fullName = 'E2E User';

  await page.goto('/app/login.html');
  await expect(page.locator('.auth-wrap')).toBeVisible();

  await page.click('#tab-register');
  await expect(page.locator('#regCurrency')).toBeEnabled();

  await page.fill('#regEmail', email);
  await page.fill('#regPassword', password);
  await page.fill('#regFullName', fullName);

  await page.click('#btn-register');
  await expect(page.locator('#regOtpSection')).toBeVisible();

  await page.fill('#regOtpCode', '654321');
  await page.click('#btn-reg-otp');

  await page.waitForURL('**/app/dashboard.html');
  await expect(page.locator('#userEmail')).toHaveText(email);

  await Promise.all([
    page.waitForURL((url) => url.pathname === '/' || url.pathname === '/index.html'),
    page.click('#btn-logout'),
  ]);

  await page.goto('/app/login.html');
  await expect(page.locator('.auth-wrap')).toBeVisible();

  await page.fill('#loginEmail', email);
  await page.fill('#loginPassword', password);
  await page.click('#btn-login');

  await page.waitForURL('**/app/dashboard.html');
  await expect(page.locator('#userEmail')).toHaveText(email);
});

