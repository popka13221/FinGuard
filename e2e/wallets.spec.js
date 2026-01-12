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

test('wallet label is escaped (no html injection)', async ({ page }) => {
  const email = uniqueEmail('e2e-wallet-xss');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();

  const payload = '<img src=x onerror=alert(1)>';
  await page.fill('#newWalletLabel', payload);
  await page.selectOption('#newWalletNetwork', 'BTC');
  await page.fill('#newWalletAddress', 'BC1QXY2KGDYGJRSQTZQ2N0YRF2493P83KKFJHX0WLH');
  await page.click('#btn-add-wallet-create');

  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const wallet = page.locator('#walletsList .wallet-item', { hasText: payload });
  await expect(wallet).toBeVisible();
  await expect(wallet.locator('img')).toHaveCount(0);
});

test('wallet value updates when base currency changes', async ({ page }) => {
  const email = uniqueEmail('e2e-wallet-base-currency');
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
  await expect(wallet).toContainText('≈ 5,000.00 USD');

  await page.click('#btn-base-currency');
  await expect(page.locator('#base-currency-overlay')).toBeVisible();
  await page.selectOption('#baseCurrencySelect', 'EUR');
  await page.click('#btn-base-currency-save');
  await expect(page.locator('#base-currency-overlay')).toBeHidden();

  await expect(wallet).toContainText('≈ 4,500.00 EUR');
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

test('arbitrum wallet shows total value including tokens', async ({ page }) => {
  const email = uniqueEmail('e2e-wallet-arb');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();

  await page.fill('#newWalletLabel', 'Arbitrum Wallet');
  await page.selectOption('#newWalletNetwork', 'ARBITRUM');
  await page.fill('#newWalletAddress', '0x00000000219ab540356cbb839cbe05303d7705fa');
  await page.click('#btn-add-wallet-create');

  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const wallet = page.locator('#walletsList .wallet-item', { hasText: 'Arbitrum Wallet' });
  await expect(wallet).toBeVisible();
  await expect(wallet).toContainText('Arbitrum');
  await expect(wallet).toContainText('≈ 5,000.00 USD');
});

test('evm total wallet shows combined ETH + Arbitrum value', async ({ page }) => {
  const email = uniqueEmail('e2e-wallet-evm');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();

  await page.fill('#newWalletLabel', 'Total Wallet');
  await page.selectOption('#newWalletNetwork', 'EVM');
  await page.fill('#newWalletAddress', '0x00000000219ab540356cbb839cbe05303d7705fa');
  await page.click('#btn-add-wallet-create');

  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const wallet = page.locator('#walletsList .wallet-item', { hasText: 'Total Wallet' });
  await expect(wallet).toBeVisible();
  await expect(wallet).toContainText('Total');
  await expect(wallet).toContainText('≈ 10,000.00 USD');
});
