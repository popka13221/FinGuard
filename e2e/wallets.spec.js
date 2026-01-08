const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('add and remove crypto wallet (watch-only)', async ({ page }) => {
  const email = uniqueEmail('e2e-wallet');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('#walletsList')).toBeVisible();

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();

  await page.fill('#newWalletLabel', 'Ledger');
  await page.selectOption('#newWalletNetwork', 'BTC');
  await page.fill('#newWalletAddress', 'BC1QXY2KGDYGJRSQTZQ2N0YRF2493P83KKFJHX0WLH');
  await page.click('#btn-add-wallet-create');

  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const wallet = page.locator('#walletsList .wallet-item', { hasText: 'Ledger' });
  await expect(wallet).toBeVisible();
  await expect(wallet).toContainText('BTC');
  await expect(wallet).toContainText('≈');
  await expect(wallet).toContainText('USD');

  await wallet.locator('button.wallet-remove').click();

  await expect(page.locator('#walletsList')).toContainText('No wallets added yet.');
});

