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
