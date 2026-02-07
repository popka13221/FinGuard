const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('dashboard empty states expose action CTAs', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-empty');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('#accountsList [data-action="open-add-account"]')).toBeVisible();
  await expect(page.locator('#walletsList [data-action="open-add-wallet"]')).toBeVisible();
  await expect(page.locator('#transactionsList [data-action="open-add-transaction"]')).toBeVisible();
});

test('modal supports keyboard escape and focus restore', async ({ page }, testInfo) => {
  if (testInfo.project.name.includes('mobile')) {
    test.skip();
  }

  const email = uniqueEmail('e2e-dashboard-modal-a11y');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  const trigger = page.locator('#btn-add-account');
  await trigger.focus();
  await trigger.press('Enter');

  const overlay = page.locator('#add-account-overlay');
  const dialog = page.locator('#add-account-menu');
  await expect(overlay).toBeVisible();
  await expect(dialog).toBeVisible();

  await page.keyboard.press('Tab');
  await page.keyboard.press('Escape');

  await expect(overlay).toBeHidden();
  await expect(trigger).toBeFocused();
});

test('reduced motion mode is applied', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });

  const email = uniqueEmail('e2e-dashboard-motion');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('body.dashboard[data-motion-level="reduced"]')).toBeVisible();
});

test('data source badges are rendered for markets', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-source');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('#marketsDataSource')).toBeVisible();
  await expect(page.locator('#cryptoDataSource')).toBeVisible();
  await expect(page.locator('#fxDataSource')).toBeVisible();

  await expect(page.locator('#cryptoDataSource')).toHaveText(/(Live|Demo|Waiting for data)/);
  await expect(page.locator('#fxDataSource')).toHaveText(/(Live|Demo|Synthetic|Live \+ Synthetic|Waiting for data)/);
});
