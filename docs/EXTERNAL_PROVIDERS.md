## External providers (FX + Crypto)

FinGuard uses third-party APIs for:
- FX rates (`GET /api/fx/rates`)
- Crypto market rates (`GET /api/crypto/rates`)
- Watch-only crypto wallet balances/portfolio (BTC/ETH/Arbitrum)

### HTTP timeouts

- `app.http.connect-timeout-ms` (default `2000`)
- `app.http.read-timeout-ms` (default `5000`)

### Provider base URLs

- FX rates: `app.fx.provider-base-url` (default `https://open.er-api.com/v6/latest`)
- Crypto rates: `app.crypto.provider-base-url` (default `https://api.coingecko.com/api/v3`)

- BTC balance: `app.crypto.wallet.btc.provider-base-url` (default `https://blockstream.info/api`)
- ETH balance: `app.crypto.wallet.eth.provider-base-url` (default `https://api.blockcypher.com/v1/eth/main`)
- ETH RPC (optional cross-check): `app.crypto.wallet.eth.rpc-base-url` (default `https://cloudflare-eth.com`)
- Arbitrum RPC: `app.crypto.wallet.arbitrum.provider-base-url` (default `https://arb1.arbitrum.io/rpc`)

- ETH portfolio (tokens): `app.crypto.wallet.eth.portfolio.provider-base-url` (default `https://api.ethplorer.io`)
- ETH portfolio API key: `app.crypto.wallet.eth.portfolio.api-key` (default `freekey`)
  - In `prod` profile `freekey` is rejected at startup (set a real key via `config/secrets.*`).
- ETH portfolio max tokens scanned: `app.crypto.wallet.eth.portfolio.max-tokens-scanned` (default `200`)

### Optional ETH balance cross-check (anti-tamper)

- `app.crypto.wallet.eth.cross-check.enabled` (default `false`)
- `app.crypto.wallet.eth.cross-check.abs-threshold-eth` (default `0.01`)

- Arbitrum portfolio token list (Blockscout): `app.crypto.wallet.arbitrum.portfolio.blockscout-base-url` (default `https://arbitrum.blockscout.com`)
- Arbitrum portfolio prices (DefiLlama): `app.crypto.wallet.arbitrum.portfolio.prices-base-url` (default `https://coins.llama.fi`)

### Provider guard (retries, circuit breaker, budgets)

Retries + circuit breaker are applied to outbound HTTP calls.

Retry:
- `app.external.providers.retry.max-attempts` (default `2`)
- `app.external.providers.retry.initial-backoff-ms` (default `150`)
- `app.external.providers.retry.max-backoff-ms` (default `1500`)

Circuit breaker:
- `app.external.providers.circuit.failure-threshold` (default `5`)
- `app.external.providers.circuit.open-ms` (default `30000`)

Global budget storage:
- `app.external.providers.budget.max-entries` (default `2000`)

Per-provider budgets:
- `app.external.providers.budget.fx.limit`, `app.external.providers.budget.fx.window-ms`
- `app.external.providers.budget.coingecko.limit`, `app.external.providers.budget.coingecko.window-ms`
- `app.external.providers.budget.blockstream.limit`, `app.external.providers.budget.blockstream.window-ms`
- `app.external.providers.budget.blockcypher.limit`, `app.external.providers.budget.blockcypher.window-ms`
- `app.external.providers.budget.arbitrum-rpc.limit`, `app.external.providers.budget.arbitrum-rpc.window-ms`
- `app.external.providers.budget.ethplorer.limit`, `app.external.providers.budget.ethplorer.window-ms`
- `app.external.providers.budget.blockscout.limit`, `app.external.providers.budget.blockscout.window-ms`
- `app.external.providers.budget.llama.limit`, `app.external.providers.budget.llama.window-ms`
