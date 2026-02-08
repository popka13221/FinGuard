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
  await expect(page.locator('#btn-add-transaction')).toBeVisible();

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

test('markets are collapsed to a compact nav link', async ({ page }) => {
  const email = uniqueEmail('e2e-dashboard-source');
  await registerAndLogin(page, { email, baseCurrency: 'USD' });

  await expect(page.locator('.markets-link')).toHaveCount(1);
  await expect(page.locator('.markets-section')).toHaveCount(0);
});
