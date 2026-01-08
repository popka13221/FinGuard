const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('total balance converts totals to base currency', async ({ page }) => {
  const email = uniqueEmail('e2e-balance');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-account');
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.fill('#newAccountName', 'Euro account');
  await page.selectOption('#newAccountCurrency', 'EUR');
  await page.fill('#newAccountBalance', '100');
  await page.click('#btn-add-account-create');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  await expect(page.locator('#totalBalance')).toHaveText('111.11 USD');

  await page.click('#btn-add-account');
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.fill('#newAccountName', 'USD account');
  await page.selectOption('#newAccountCurrency', 'USD');
  await page.fill('#newAccountBalance', '50');
  await page.click('#btn-add-account-create');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  await expect(page.locator('#totalBalance')).toHaveText('161.11 USD');
});

test('changing base currency converts all amounts', async ({ page }) => {
  const email = uniqueEmail('e2e-base-currency');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-account');
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.fill('#newAccountName', 'Euro account');
  await page.selectOption('#newAccountCurrency', 'EUR');
  await page.fill('#newAccountBalance', '100');
  await page.click('#btn-add-account-create');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  await page.click('#btn-add-account');
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.fill('#newAccountName', 'USD account');
  await page.selectOption('#newAccountCurrency', 'USD');
  await page.fill('#newAccountBalance', '50');
  await page.click('#btn-add-account-create');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  await expect(page.locator('#totalBalance')).toHaveText('161.11 USD');

  await page.click('#btn-base-currency');
  await expect(page.locator('#base-currency-overlay')).toBeVisible();
  await page.selectOption('#baseCurrencySelect', 'EUR');
  await page.click('#btn-base-currency-save');
  await expect(page.locator('#base-currency-overlay')).toBeHidden();

  await expect(page.locator('#btn-base-currency')).toHaveText('Currency: EUR');
  await expect(page.locator('#totalBalance')).toHaveText('145.00 EUR');
});
