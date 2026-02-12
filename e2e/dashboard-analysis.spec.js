const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('wallet analysis strip shows progress and compact wallet intelligence card', async ({ page }, testInfo) => {
  const email = uniqueEmail('e2e-dashboard-analysis');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });
  const startUrl = page.url();
  let requested1y = false;
  page.on('request', (request) => {
    if (request.url().includes('/analysis/series') && request.url().includes('window=1y')) {
      requested1y = true;
    }
  });

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
  await expect(page.locator('body.dashboard.analysis-drawer-open')).toBeVisible();
  await expect(page.locator('body.dashboard')).toHaveCSS('overflow', 'hidden');
  await expect(page.locator('#analysisDetailWalletName')).toContainText('MetaMask');
  await expect(page.locator('#analysisDetailPortfolio')).toContainText('USD');
  await expect(page.locator('#analysisDetailSeriesChart')).not.toContainText(/synthetic|demo/i);
  await expect(page.url()).toBe(startUrl);
  const detailHeader = page.getByTestId('wallet-intelligence-header');
  const detailBody = page.getByTestId('wallet-intelligence-body');
  await expect(detailHeader).toBeVisible();
  await expect(detailBody).toBeVisible();
  const headerBeforeScroll = await detailHeader.boundingBox();
  await detailBody.evaluate((el) => { el.scrollTop = 280; });
  await page.waitForTimeout(120);
  const headerAfterScroll = await detailHeader.boundingBox();
  if (!headerBeforeScroll || !headerAfterScroll) {
    throw new Error('Wallet intelligence header bounds are unavailable');
  }
  expect(Math.abs(headerBeforeScroll.y - headerAfterScroll.y)).toBeLessThanOrEqual(3);
  const hasLargeSeries = await page.locator('#analysisDetailSeriesChart svg').count();
  const hasCompactSeries = await page.locator('#analysisDetailSeriesChart .compact-sparkline').count();
  if ((hasLargeSeries + hasCompactSeries) === 0) {
    await expect(page.locator('#analysisDetailSeriesChart')).toContainText(/No data|Нет данных/i);
  } else {
    expect(hasLargeSeries + hasCompactSeries).toBeGreaterThan(0);
  }
  await page.locator('#analysisDetailWindowTabs button[data-window="1y"]').click();
  await expect.poll(() => requested1y, { timeout: 15000 }).toBeTruthy();
  await expect(page.locator('#analysisDetailInsightsList')).toBeVisible();
  await expect.poll(async () => ((await page.locator('#analysisDetailInsightsList').textContent()) || '').trim().length, { timeout: 15_000 }).toBeGreaterThan(0);

  const overlay = page.getByTestId('wallet-intelligence-overlay');
  const menu = page.getByTestId('wallet-intelligence-page');
  const overlayBox = await overlay.boundingBox();
  const menuBox = await menu.boundingBox();
  if (!overlayBox || !menuBox) {
    throw new Error('Wallet intelligence modal bounds are unavailable');
  }
  if (testInfo.project.name.includes('mobile')) {
    expect(menuBox.width).toBeGreaterThan((overlayBox.width * 0.94));
    expect(Math.abs((menuBox.y + menuBox.height) - (overlayBox.y + overlayBox.height))).toBeLessThanOrEqual(6);
  } else {
    expect(menuBox.width).toBeGreaterThan(overlayBox.width * 0.45);
    expect(menuBox.width).toBeLessThan(overlayBox.width * 0.9);
    const leftInset = menuBox.x - overlayBox.x;
    const rightInset = (overlayBox.x + overlayBox.width) - (menuBox.x + menuBox.width);
    expect(Math.abs(leftInset - rightInset)).toBeLessThanOrEqual(24);
  }

  await page.click('#btn-analysis-detail-close');
  await expect(page.locator('#analysis-detail-overlay')).toBeHidden();
  await expect(page.locator('body.dashboard.analysis-drawer-open')).toBeHidden();
  await expect(page.url()).toBe(startUrl);
  if (!testInfo.project.name.includes('mobile')) {
    await expect(intelligenceLink).toBeFocused();
  }

  await intelligenceLink.click();
  await expect(page.locator('#analysis-detail-overlay')).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(page.locator('#analysis-detail-overlay')).toBeHidden();
  await expect(page.url()).toBe(startUrl);
  if (!testInfo.project.name.includes('mobile')) {
    await expect(intelligenceLink).toBeFocused();
  }

  await intelligenceLink.click();
  await expect(page.locator('#analysis-detail-overlay')).toBeVisible();
  await page.getByTestId('wallet-intelligence-overlay').click({ position: { x: 10, y: 10 } });
  await expect(page.locator('#analysis-detail-overlay')).toBeHidden();
  await expect(page.url()).toBe(startUrl);
  if (!testInfo.project.name.includes('mobile')) {
    await expect(intelligenceLink).toBeFocused();
  }
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
