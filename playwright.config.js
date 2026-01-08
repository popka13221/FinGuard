// @ts-check
const { defineConfig, devices } = require('@playwright/test');

const baseURL = process.env.E2E_BASE_URL || 'http://localhost:8085';
const parsedBaseUrl = new URL(baseURL);
const serverPort = parsedBaseUrl.port || '8085';
const serverUrl = `${baseURL.replace(/\/+$/, '')}/actuator/health`;

module.exports = defineConfig({
  testDir: './e2e',
  timeout: 60_000,
  expect: {
    timeout: 10_000
  },
  retries: process.env.CI ? 1 : 0,
  // Single shared Spring Boot server + in-memory DB => keep E2E deterministic by running serially.
  workers: 1,
  use: {
    baseURL,
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure'
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome']
      }
    }
  ],
  webServer: process.env.E2E_NO_SERVER
    ? undefined
    : {
      command: `mvn -q -DskipTests spring-boot:run -Dspring-boot.run.profiles=e2e -Dspring-boot.run.arguments=--server.port=${serverPort}`,
      url: serverUrl,
      reuseExistingServer: process.env.E2E_REUSE_SERVER === '1' && !process.env.CI,
      timeout: 120_000
    }
});
