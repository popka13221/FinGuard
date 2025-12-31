import { defineConfig, devices } from '@playwright/test';

const port = Number(process.env.E2E_PORT || 8085);
const baseURL = `http://127.0.0.1:${port}`;

export default defineConfig({
  testDir: './e2e',
  retries: process.env.CI ? 1 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ['list'],
    ['html', { open: 'never' }],
  ],
  use: {
    baseURL,
    trace: 'on-first-retry',
  },
  webServer: {
    command: `mvn -B -DskipTests spring-boot:run -Dspring-boot.run.profiles=e2e -Dspring-boot.run.arguments=--server.port=${port}`,
    cwd: '..',
    url: `${baseURL}/health`,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});

