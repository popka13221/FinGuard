const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('wallet analysis strip shows progress and instant value cards', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-analysis');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('#walletAnalysisPanel')).toBeVisible();
  await expect(page.locator('#analysisEmptyCta')).toBeVisible();

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();
  await page.fill('#newWalletLabel', 'MetaMask');
  await page.selectOption('#newWalletNetwork', 'ETH');
  await page.fill('#newWalletAddress', '0x00000000219ab540356cbb839cbe05303d7705fa');
  await page.click('#btn-add-wallet-create');
  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  await expect(page.locator('#walletsList .wallet-item', { hasText: 'MetaMask' })).toBeVisible();
  await expect(page.locator('#analysisEmptyCta')).toBeHidden();

  await expect.poll(async () => {
    return (await page.locator('#analysisProgressText').textContent())?.trim();
  }, { timeout: 20_000 }).toBe('100%');

  await expect(page.locator('#analysisDataSource')).toHaveText(/(Live|Live \+ Synthetic)/);
  await expect(page.locator('#analysisPortfolioValue')).toContainText('USD');
  await expect(page.locator('#analysisGrowthValue')).toContainText('%');
  await expect(page.locator('#analysisGrowthSpark .spark-svg')).toBeVisible();
  await expect(page.locator('#analysisRecurringMeta')).toContainText('%');
  await expect(page.locator('#analysisOutflowSource')).toHaveText(/(Live|Synthetic|Live \+ Synthetic)/);
  await expect(page.locator('#analysisRecurringSource')).toHaveText(/(Live|Synthetic|Live \+ Synthetic)/);

  await page.click('#analysisQuickCard');
  await expect(page.locator('#analysisQuickPanel')).toBeVisible();
  await expect(page.locator('#analysisQuickPortfolio')).toContainText('USD');
  await expect(page.locator('#analysisQuickWallets')).toHaveText('1');
  await expect(page.locator('#analysisQuickTransactions')).toHaveText('0');
});

test('wallet analysis strip resets after wallet deletion', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-analysis-reset');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();
  await page.fill('#newWalletLabel', 'Reset Wallet');
  await page.selectOption('#newWalletNetwork', 'BTC');
  await page.fill('#newWalletAddress', 'BC1QXY2KGDYGJRSQTZQ2N0YRF2493P83KKFJHX0WLH');
  await page.click('#btn-add-wallet-create');
  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  const wallet = page.locator('#walletsList .wallet-item', { hasText: 'Reset Wallet' });
  await expect(wallet).toBeVisible();

  await wallet.locator('button.wallet-remove').click();
  await expect(page.locator('#walletsList')).toContainText('No wallets added yet.');
  await expect(page.locator('#analysisEmptyCta')).toBeVisible();
  await expect(page.locator('#analysisProgressText')).toHaveText('0%');
  await expect(page.locator('#analysisBannerTitle')).toHaveText('Connect a wallet to unlock analysis');
});
