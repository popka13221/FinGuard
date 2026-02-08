const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('wallet analysis strip shows progress and compact wallet intelligence card', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-analysis');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  const hero = page.getByTestId('hero');
  const intelligenceLink = page.getByTestId('wallet-intelligence-link');
  await expect(hero).toBeVisible();
  await expect(intelligenceLink).toBeVisible();
  await expect(intelligenceLink).not.toContainText('USD');

  await page.click('#btn-add-wallet');
  await expect(page.locator('#add-wallet-overlay')).toBeVisible();
  await page.fill('#newWalletLabel', 'MetaMask');
  await page.selectOption('#newWalletNetwork', 'ETH');
  await page.fill('#newWalletAddress', '0x00000000219ab540356cbb839cbe05303d7705fa');
  await page.click('#btn-add-wallet-create');
  await expect(page.locator('#add-wallet-overlay')).toBeHidden();

  await expect(page.locator('#walletsList .wallet-item', { hasText: 'MetaMask' })).toBeVisible();
  await expect(page.locator('#analysisUpdatedAt')).toContainText(/(Updated|Waiting)/);
  const cashflowText = ((await page.locator('#analysisOutflowValue').textContent()) || '').trim();
  expect(cashflowText === '—' || cashflowText.includes('USD')).toBeTruthy();
  const debtText = ((await page.locator('#analysisRecurringValue').textContent()) || '').trim();
  expect(debtText === '—' || debtText.includes('USD')).toBeTruthy();

  await intelligenceLink.click();
  await expect(page.locator('#analysis-detail-overlay')).toBeVisible();
  await expect(page.getByTestId('wallet-intelligence-page')).toBeVisible();
  await expect(page.locator('#analysisDetailWalletName')).toContainText('MetaMask');
  await expect(page.locator('#analysisDetailPortfolio')).toContainText('USD');
  const hasLargeSeries = await page.locator('#analysisDetailSeriesChart svg').count();
  const hasCompactSeries = await page.locator('#analysisDetailSeriesChart .compact-sparkline').count();
  expect(hasLargeSeries + hasCompactSeries).toBeGreaterThan(0);
  await expect(page.locator('#analysisDetailInsightsList')).toBeVisible();
  await expect.poll(async () => ((await page.locator('#analysisDetailInsightsList').textContent()) || '').trim().length, { timeout: 15_000 }).toBeGreaterThan(0);
  await page.click('#btn-analysis-detail-close');
  await expect(page.locator('#analysis-detail-overlay')).toBeHidden();
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
  await expect(page.locator('#getStartedSection')).toBeVisible();
});
