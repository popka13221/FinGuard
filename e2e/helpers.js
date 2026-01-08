const crypto = require('crypto');
const { expect } = require('@playwright/test');

function uniqueEmail(prefix = 'user') {
  const id = `${Date.now()}-${crypto.randomBytes(4).toString('hex')}`;
  return `${prefix}-${id}@example.com`;
}

async function registerAndLogin(page, { email, password = 'StrongPass1!', fullName = 'E2E User', baseCurrency = 'USD', fixedCode = '654321' }) {
  await page.goto('/app/login.html');
  await expect(page.locator('.auth-wrap')).toBeVisible();

  await page.click('#tab-register');
  await expect(page.locator('#form-register')).toBeVisible();

  await page.fill('#regEmail', email);
  await page.fill('#regPassword', password);
  await page.fill('#regFullName', fullName);

  const currencySelect = page.locator('#regCurrency');
  await expect(currencySelect).toBeEnabled();
  await currencySelect.selectOption(baseCurrency);

  await page.click('#btn-register');
  await expect(page.locator('#regOtpSection')).toBeVisible();

  await page.fill('#regOtpCode', fixedCode);
  await page.click('#btn-reg-otp');

  await expect(page).toHaveURL(/\/app\/dashboard\.html$/);
  await expect(page.locator('#userEmail')).toContainText(email);
}

module.exports = {
  uniqueEmail,
  registerAndLogin
};
