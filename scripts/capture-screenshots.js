#!/usr/bin/env node
/* eslint-disable no-console */

const path = require('path');
const fs = require('fs');
const { spawn } = require('child_process');
const { chromium } = require('@playwright/test');

const projectRoot = path.resolve(__dirname, '..');
const outDir = path.join(projectRoot, 'docs', 'screenshots');

const port = String(process.env.SCREENSHOT_PORT || '8085');
const baseURL = String(process.env.SCREENSHOT_BASE_URL || `http://localhost:${port}`).replace(/\/+$/, '');
const healthURL = `${baseURL}/actuator/health`;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function waitForHealth(url, timeoutMs) {
  const started = Date.now();
  while (Date.now() - started < timeoutMs) {
    try {
      const resp = await fetch(url, { method: 'GET' });
      if (resp.ok) return;
    } catch (_) {
      // ignore
    }
    // eslint-disable-next-line no-await-in-loop
    await sleep(500);
  }
  throw new Error(`Server did not become healthy in ${timeoutMs}ms: ${url}`);
}

function killProcessTree(proc) {
  if (!proc || proc.killed) return;
  if (process.platform === 'win32') {
    proc.kill('SIGTERM');
    return;
  }
  try {
    process.kill(-proc.pid, 'SIGTERM');
  } catch (_) {
    proc.kill('SIGTERM');
  }
}

function uniqueEmail(prefix) {
  const id = `${Date.now()}-${Math.random().toString(16).slice(2, 10)}`;
  return `${prefix}-${id}@example.com`;
}

async function seedDemoData(page) {
  const now = new Date().toISOString();

  async function getCsrfHeaders() {
    await page.request.get('/api/auth/csrf');
    const cookies = await page.context().cookies();
    const csrf = (cookies.find((c) => c.name === 'XSRF-TOKEN') || {}).value || '';
    return csrf ? { 'X-XSRF-TOKEN': csrf } : {};
  }

  const createCategory = async (name, type) => {
    const headers = await getCsrfHeaders();
    const res = await page.request.post('/api/categories', {
      headers,
      data: { name, type }
    });
    if (!res.ok()) throw new Error(`Failed to create category ${name}: ${res.status()}`);
    return (await res.json()).id;
  };

  const createAccount = async (name, currency, initialBalance) => {
    const headers = await getCsrfHeaders();
    const res = await page.request.post('/api/accounts', {
      headers,
      data: { name, currency, initialBalance }
    });
    if (!res.ok()) throw new Error(`Failed to create account ${name}: ${res.status()}`);
    return (await res.json()).id;
  };

  const createTransaction = async ({ accountId, categoryId, type, amount, transactionDate, description }) => {
    const headers = await getCsrfHeaders();
    const res = await page.request.post('/api/transactions', {
      headers,
      data: { accountId, categoryId, type, amount, transactionDate, description }
    });
    if (!res.ok()) throw new Error(`Failed to create transaction: ${res.status()}`);
  };

  const food = await createCategory('Food', 'EXPENSE');
  const salary = await createCategory('Salary', 'INCOME');

  const cash = await createAccount('Cash', 'USD', 1200);
  const card = await createAccount('Card', 'EUR', 350);

  await createTransaction({
    accountId: cash,
    categoryId: salary,
    type: 'INCOME',
    amount: 2500,
    transactionDate: now,
    description: 'Salary'
  });
  await createTransaction({
    accountId: cash,
    categoryId: food,
    type: 'EXPENSE',
    amount: 120,
    transactionDate: now,
    description: 'Groceries'
  });
  await createTransaction({
    accountId: card,
    categoryId: food,
    type: 'EXPENSE',
    amount: 35,
    transactionDate: now,
    description: 'Coffee'
  });
}

async function registerAndLogin(page, email) {
  await page.goto('/app/login.html', { waitUntil: 'networkidle' });
  await page.waitForSelector('#tab-register');
  await page.click('#tab-register');

  await page.fill('#regEmail', email);
  await page.fill('#regPassword', 'StrongPass1!');
  await page.fill('#regFullName', 'Demo User');

  await page.waitForSelector('#regCurrency:not([disabled])');
  await page.selectOption('#regCurrency', 'USD');

  await page.click('#btn-register');
  await page.waitForSelector('#regOtpSection', { state: 'visible' });
  await page.fill('#regOtpCode', '654321');
  await page.click('#btn-reg-otp');

  await page.waitForURL(/\/app\/dashboard\.html$/);
  await page.waitForSelector('#userEmail');
}

async function screenshotPage(page, url, outPath, { fullPage = true, waitFor } = {}) {
  await page.goto(url, { waitUntil: 'networkidle' });
  if (waitFor) {
    await page.waitForSelector(waitFor, { state: 'visible', timeout: 30_000 });
  }
  await page.waitForTimeout(800);
  await page.screenshot({ path: outPath, fullPage });
  console.log(`Saved ${path.relative(projectRoot, outPath)}`);
}

async function main() {
  fs.mkdirSync(outDir, { recursive: true });

  console.log(`Starting server (e2e profile) at ${baseURL}...`);
  const server = spawn(
    'mvn',
    [
      '-q',
      '-DskipTests',
      'spring-boot:run',
      '-Dspring-boot.run.profiles=e2e',
      `-Dspring-boot.run.arguments=--server.port=${port}`
    ],
    {
      cwd: projectRoot,
      stdio: ['ignore', 'pipe', 'pipe'],
      detached: process.platform !== 'win32'
    }
  );

  server.stdout.on('data', (chunk) => process.stdout.write(String(chunk)));
  server.stderr.on('data', (chunk) => process.stderr.write(String(chunk)));

  const cleanup = () => killProcessTree(server);
  process.on('SIGINT', () => { cleanup(); process.exit(130); });
  process.on('SIGTERM', () => { cleanup(); process.exit(143); });

  try {
    await waitForHealth(healthURL, 120_000);

    const browser = await chromium.launch();
    const context = await browser.newContext({
      baseURL,
      viewport: { width: 1440, height: 900 }
    });
    const page = await context.newPage();

    // Landing + login (English)
    await context.addInitScript(() => {
      localStorage.setItem('finguard:lang', 'en');
    });
    await screenshotPage(page, `${baseURL}/`, path.join(outDir, 'landing-en.png'), { waitFor: '.hero' });
    await screenshotPage(page, `${baseURL}/app/login.html`, path.join(outDir, 'login-en.png'), { waitFor: '.auth-wrap', fullPage: false });

    // Dashboard (seed data first)
    const email = uniqueEmail('screens');
    await registerAndLogin(page, email);
    await seedDemoData(page);

    await page.evaluate(() => localStorage.setItem('finguard:lang', 'en'));
    await page.reload({ waitUntil: 'networkidle' });
    await page.waitForSelector('#totalBalance');
    await page.waitForTimeout(1200);
    await page.screenshot({ path: path.join(outDir, 'dashboard-en.png'), fullPage: true });
    console.log(`Saved ${path.relative(projectRoot, path.join(outDir, 'dashboard-en.png'))}`);

    await page.evaluate(() => localStorage.setItem('finguard:lang', 'ru'));
    await page.reload({ waitUntil: 'networkidle' });
    await page.waitForSelector('#totalBalance');
    await page.waitForTimeout(1200);
    await page.screenshot({ path: path.join(outDir, 'dashboard-ru.png'), fullPage: true });
    console.log(`Saved ${path.relative(projectRoot, path.join(outDir, 'dashboard-ru.png'))}`);

    // Swagger UI
    await page.evaluate(() => localStorage.setItem('finguard:lang', 'en'));
    await screenshotPage(page, `${baseURL}/swagger-ui/index.html`, path.join(outDir, 'swagger-ui.png'), { waitFor: '#swagger-ui', fullPage: true });

    await context.close();
    await browser.close();
  } finally {
    cleanup();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
