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

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();
  await page.fill('#newWalletLabel', 'Ledger');
  await page.selectOption('#newWalletNetwork', 'BTC');
  await page.fill('#newWalletAddress', 'BC1QXY2KGDYGJRSQTZQ2N0YRF2493P83KKFJHX0WLH');
  await page.click('#btn-add-wallet-create');
  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const walletAgain = page.locator('#walletsList .wallet-item', { hasText: 'Ledger' });
  await expect(walletAgain).toBeVisible();
});

test('eth wallet shows total value including tokens', async ({ page }) => {
  const email = uniqueEmail('e2e-wallet-eth');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();

  await page.fill('#newWalletLabel', 'MetaMask');
  await page.selectOption('#newWalletNetwork', 'ETH');
  await page.fill('#newWalletAddress', '0x00000000219ab540356cbb839cbe05303d7705fa');
  await page.click('#btn-add-wallet-create');

  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const wallet = page.locator('#walletsList .wallet-item', { hasText: 'MetaMask' });
  await expect(wallet).toBeVisible();
  await expect(wallet).toContainText('ETH');
  await expect(wallet).toContainText('≈ 5,000.00 USD');
 });
