const { test, expect } = require('@playwright/test');
const { uniqueEmail, registerAndLogin } = require('./helpers');

function overviewFixture(overrides = {}) {
  return {
    asOf: '2026-02-12T15:40:00Z',
    dataFreshness: 'LIVE',
    hero: {
      netWorth: 3210.45,
      baseCurrency: 'USD',
      delta7dPct: 1.23,
      updatedAt: '2026-02-12T15:40:00Z',
      hasMeaningfulData: true
    },
    stats: {
      income30d: 1200.1,
      spend30d: 410.2,
      cashflow30d: 789.9,
      debt: 99.5,
      hasMeaningfulData: true
    },
    getStarted: {
      visible: false,
      connectAccount: false,
      addTransaction: false,
      importHistory: false
    },
    transactionsPreview: [],
    walletsPreview: [],
    upcomingPaymentsPreview: [],
    walletIntelligence: {
      activeWalletId: null,
      status: 'DONE',
      progressPct: 100,
      partialReady: true,
      updatedAt: '2026-02-12T15:40:00Z',
      source: 'LIVE',
      etaSeconds: 0,
      lastSuccessfulStage: 'DONE'
    },
    ...overrides
  };
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    localStorage.setItem('finguard:lang', 'en');
  });
});

test('dashboard empty states expose action CTAs', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-empty');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  const getStarted = page.getByTestId('get-started');
  await expect(getStarted).toBeVisible();
  await expect(getStarted.locator('[data-action="open-add-account"]')).toBeVisible();
  await expect(getStarted.locator('[data-action="open-add-transaction"]')).toBeVisible();
  await expect(getStarted.locator('[data-action="open-import-history"]')).toBeVisible();
  await expect(page.locator('#transactionsList')).toContainText('No transactions yet.');
  await expect(page.locator('#btn-add-transaction')).toBeHidden();
  await expect(page.locator('#transactionsList .inline-cta-primary')).toBeVisible();
  await expect(page.locator('#transactionsList .inline-cta-secondary')).toBeVisible();
  await expect(page.locator('#accountsList .inline-cta-primary')).toBeVisible();
  await expect(page.locator('#walletsList .inline-cta-primary')).toBeVisible();
  await expect(page.locator('#upcomingSection')).toBeHidden();
  await expect(page.locator('#upcomingPaymentsList')).not.toContainText(/Rent|Streaming|Mobile service/);

  await page.locator('#transactionsList .inline-cta-primary').click();
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.click('#btn-add-account-cancel');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  const sectionBox = await getStarted.boundingBox();
  const pageBox = await page.locator('.dashboard-main.page').boundingBox();
  expect(sectionBox).toBeTruthy();
  expect(pageBox).toBeTruthy();
  expect(Math.abs(sectionBox.x - pageBox.x)).toBeLessThanOrEqual(6);
  expect(Math.abs((sectionBox.x + sectionBox.width) - (pageBox.x + pageBox.width))).toBeLessThanOrEqual(6);

  const hasTruncation = await page.evaluate(() => {
    const root = document.querySelector('[data-testid=\"get-started\"]');
    const title = root ? root.querySelector('.section-title') : null;
    const subtitle = root ? root.querySelector('.get-started-subtitle') : null;
    const nodes = [title, subtitle].filter(Boolean);
    return nodes.some((el) => el.scrollWidth > (el.clientWidth + 1));
  });
  expect(hasTruncation).toBeFalsy();
});

test('import history opens dedicated modal and routes to proper action', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-import-history');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  const getStarted = page.getByTestId('get-started');
  await expect(getStarted).toBeVisible();
  await getStarted.locator('[data-action="open-import-history"]').click();

  const importModal = page.getByTestId('import-history-modal');
  await expect(importModal).toBeVisible();
  await expect(importModal).toContainText('Import history');

  await page.click('#btn-import-history-connect');
  await expect(page.locator('#import-history-overlay')).toBeHidden();
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.click('#btn-add-account-cancel');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  await getStarted.locator('[data-action="open-import-history"]').click();
  await expect(importModal).toBeVisible();
  await page.click('#btn-import-history-manual');
  await expect(page.locator('#import-history-overlay')).toBeHidden();
  await expect(page.locator('#add-account-overlay')).toBeVisible();
});

test('import history manual action opens transaction modal when account exists', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-import-history-manual');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await page.click('#btn-add-account');
  await expect(page.locator('#add-account-overlay')).toBeVisible();
  await page.fill('#newAccountName', 'Main card');
  await page.fill('#newAccountBalance', '1200');
  await page.click('#btn-add-account-create');
  await expect(page.locator('#add-account-overlay')).toBeHidden();

  const getStarted = page.getByTestId('get-started');
  await expect(getStarted).toBeVisible();
  await getStarted.locator('[data-action="open-import-history"]').click();
  await expect(page.getByTestId('import-history-modal')).toBeVisible();
  await page.click('#btn-import-history-manual');

  await expect(page.locator('#import-history-overlay')).toBeHidden();
  await expect(page.locator('#add-transaction-overlay')).toBeVisible();
});

test('overview endpoint is authoritative for hero/stats on initial load', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-overview-authoritative');
  let overviewHits = 0;
  let reportSummaryHits = 0;
  page.on('request', (request) => {
    const url = request.url();
    if (url.includes('/api/dashboard/overview')) overviewHits += 1;
    if (url.includes('/api/reports/summary')) reportSummaryHits += 1;
  });

  await registerAndLogin(page, { email, baseCurrency: 'USD' });
  await expect(page.getByTestId('hero')).toBeVisible();
  await expect.poll(() => overviewHits, { timeout: 15000 }).toBeGreaterThan(0);
  expect(reportSummaryHits).toBe(0);
});

test('hero/stats keep last snapshot and show stale marker when overview refresh fails', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-overview-stale');
  let overviewHits = 0;
  const payload = overviewFixture();
  await page.route('**/api/dashboard/overview**', async (route) => {
    overviewHits += 1;
    if (overviewHits === 1) {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(payload)
      });
      return;
    }
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ code: '500000', message: 'boom' })
    });
  });

  await registerAndLogin(page, { email, baseCurrency: 'USD' });
  await expect(page.getByTestId('hero')).toBeVisible();
  await expect.poll(() => overviewHits, { timeout: 15000 }).toBeGreaterThan(0);

  const before = {
    balance: ((await page.locator('#totalBalance').textContent()) || '').trim(),
    change: ((await page.locator('#analysisHeroChange').textContent()) || '').trim(),
    income: ((await page.locator('#analysisIncomeValue').textContent()) || '').trim(),
    spend: ((await page.locator('#analysisExpenseValue').textContent()) || '').trim(),
    cashflow: ((await page.locator('#analysisOutflowValue').textContent()) || '').trim()
  };

  await page.click('#btn-refresh');
  await expect.poll(() => overviewHits, { timeout: 15000 }).toBeGreaterThan(1);

  await expect(page.locator('#totalBalance')).toHaveText(before.balance);
  await expect(page.locator('#analysisHeroChange')).toHaveText(before.change);
  await expect(page.locator('#analysisIncomeValue')).toHaveText(before.income);
  await expect(page.locator('#analysisExpenseValue')).toHaveText(before.spend);
  await expect(page.locator('#analysisOutflowValue')).toHaveText(before.cashflow);
  await expect(page.locator('#analysisUpdatedAt')).toContainText('Data may be stale');
});

test('secondary endpoints do not overwrite hero/stats snapshot values', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-overview-no-overwrite');
  const payload = overviewFixture({
    hero: {
      netWorth: 8888.88,
      baseCurrency: 'USD',
      delta7dPct: -0.75,
      updatedAt: '2026-02-12T16:02:00Z',
      hasMeaningfulData: true
    },
    stats: {
      income30d: 7000,
      spend30d: 1400,
      cashflow30d: 5600,
      debt: 0,
      hasMeaningfulData: true
    }
  });
  let overviewHits = 0;
  let balanceHits = 0;
  let cashFlowHits = 0;
  let transactionsHits = 0;

  await page.route('**/api/dashboard/overview**', async (route) => {
    overviewHits += 1;
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(payload)
    });
  });
  page.on('request', (request) => {
    const url = request.url();
    if (url.includes('/api/accounts/balance')) balanceHits += 1;
    if (url.includes('/api/reports/cash-flow')) cashFlowHits += 1;
    if (url.includes('/api/transactions?')) transactionsHits += 1;
  });

  await registerAndLogin(page, { email, baseCurrency: 'USD' });
  await expect(page.getByTestId('hero')).toBeVisible();
  await expect.poll(() => overviewHits, { timeout: 15000 }).toBeGreaterThan(0);

  const before = {
    balance: ((await page.locator('#totalBalance').textContent()) || '').trim(),
    change: ((await page.locator('#analysisHeroChange').textContent()) || '').trim(),
    income: ((await page.locator('#analysisIncomeValue').textContent()) || '').trim(),
    spend: ((await page.locator('#analysisExpenseValue').textContent()) || '').trim(),
    cashflow: ((await page.locator('#analysisOutflowValue').textContent()) || '').trim()
  };

  await expect.poll(() => balanceHits, { timeout: 15000 }).toBeGreaterThan(0);
  await expect.poll(() => cashFlowHits, { timeout: 15000 }).toBeGreaterThan(0);
  await expect.poll(() => transactionsHits, { timeout: 15000 }).toBeGreaterThan(0);
  await page.waitForTimeout(300);

  await expect(page.locator('#totalBalance')).toHaveText(before.balance);
  await expect(page.locator('#analysisHeroChange')).toHaveText(before.change);
  await expect(page.locator('#analysisIncomeValue')).toHaveText(before.income);
  await expect(page.locator('#analysisExpenseValue')).toHaveText(before.spend);
  await expect(page.locator('#analysisOutflowValue')).toHaveText(before.cashflow);
});

test('empty first-fail overview shows unavailable placeholders without fallback numbers', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-overview-empty-fail');
  await page.route('**/api/dashboard/overview**', async (route) => {
    await route.fulfill({
      status: 500,
      contentType: 'application/json',
      body: JSON.stringify({ code: '500000', message: 'boom' })
    });
  });

  await registerAndLogin(page, { email, baseCurrency: 'USD' });
  await expect(page.getByTestId('hero')).toBeVisible();
  await expect(page.locator('#totalBalance')).toHaveText('—');
  await expect(page.locator('#analysisHeroChange')).toHaveText('—');
  await expect(page.locator('#analysisIncomeValue')).toHaveText('—');
  await expect(page.locator('#analysisExpenseValue')).toHaveText('—');
  await expect(page.locator('#analysisOutflowValue')).toHaveText('—');
  await expect(page.locator('#analysisUpdatedAt')).toContainText('Failed to refresh overview');
});

test('top nav remains visible while scrolling', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-sticky-nav');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  const nav = page.getByTestId('top-nav');
  const hero = page.getByTestId('hero');
  await expect(nav).toBeVisible();
  await expect(hero).toBeVisible();
  const initialBox = await nav.boundingBox();
  const heroBox = await hero.boundingBox();
  expect(initialBox).toBeTruthy();
  expect(heroBox).toBeTruthy();
  const viewport = page.viewportSize();
  expect(viewport).toBeTruthy();
  if (!initialBox || !heroBox || !viewport) {
    throw new Error('Top nav bounds unavailable');
  }
  const initialCenter = initialBox.x + initialBox.width / 2;
  expect(Math.abs(initialCenter - (viewport.width / 2))).toBeLessThanOrEqual(10);
  expect(initialBox.x).toBeGreaterThanOrEqual(0);
  expect(initialBox.x + initialBox.width).toBeLessThanOrEqual(viewport.width + 1);
  expect(heroBox.y).toBeGreaterThanOrEqual(initialBox.y + initialBox.height - 2);
  await expect(page.locator('body.dashboard')).not.toHaveClass(/nav-condensed/);
  await expect(nav).toHaveAttribute('data-nav-state', 'top');

  await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
  await page.waitForTimeout(150);
  const afterBox = await nav.boundingBox();
  expect(afterBox).toBeTruthy();
  if (!afterBox) {
    throw new Error('Top nav bounds unavailable after scroll');
  }

  expect(afterBox.y).toBeLessThanOrEqual(initialBox.y + 2);
  expect(afterBox.y).toBeGreaterThanOrEqual(0);
  const afterCenter = afterBox.x + afterBox.width / 2;
  expect(Math.abs(afterCenter - (viewport.width / 2))).toBeLessThanOrEqual(10);
  await expect(page.locator('body.dashboard')).toHaveClass(/nav-condensed/);
  await expect(nav).toHaveAttribute('data-nav-state', 'compact');
  await expect(nav).toBeVisible();
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

test('markets nav link is hidden until markets page is implemented', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-source');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('.markets-link')).toHaveCount(1);
  await expect(page.locator('.markets-link')).toBeHidden();
  await expect(page.locator('.markets-section')).toHaveCount(0);
});
