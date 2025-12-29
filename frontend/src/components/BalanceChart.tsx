import React, { useMemo } from 'react';
import '../theme.css';
import './charts.css';

type BalanceChartProps = {
  series: number[];
  currency?: string;
  ticks?: number;
};

const WIDTH = 520;
const HEIGHT = 200;
const PAD_LEFT = 110;
const PAD_RIGHT = 16;
const PAD_TOP = 18;
const PAD_BOTTOM = 34;

const formatMoney = (value: number, currency?: string) => {
  const abs = Math.abs(value || 0);
  const sign = (value || 0) < 0 ? '-' : '';
  const amount = abs.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return currency ? `${sign}${amount} ${currency}` : `${sign}${amount}`;
};

const formatPercent = (value: number) => `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`;

export const BalanceChart: React.FC<BalanceChartProps> = ({ series, currency, ticks = 4 }) => {
  const safeSeries = Array.isArray(series) ? series.filter((n) => Number.isFinite(n)) : [];

  const model = useMemo(() => {
    if (safeSeries.length === 0) {
      return null;
    }
    const max = Math.max(...safeSeries);
    const min = Math.min(...safeSeries);
    const span = max - min || 1;

    const points = safeSeries.map((v, idx) => {
      const x = PAD_LEFT + (idx / Math.max(safeSeries.length - 1, 1)) * (WIDTH - PAD_LEFT - PAD_RIGHT);
      const y = HEIGHT - PAD_BOTTOM - ((v - min) / span) * (HEIGHT - PAD_TOP - PAD_BOTTOM);
      return { x, y };
    });

    const line = points.map((p) => `${p.x},${p.y}`).join(' ');
    const area = [`${PAD_LEFT},${HEIGHT - PAD_BOTTOM}`, line, `${WIDTH - PAD_RIGHT},${HEIGHT - PAD_BOTTOM}`].join(' ');

    const delta = safeSeries[safeSeries.length - 1] - safeSeries[0];
    const avg = safeSeries.reduce((a, b) => a + b, 0) / (safeSeries.length || 1);
    const deltaPct = safeSeries[0] !== 0 ? (delta / safeSeries[0]) * 100 : 0;

    const tickCount = Math.max(1, ticks);
    const yLabels = Array.from({ length: tickCount + 1 }, (_, i) => {
      const value = min + (span / tickCount) * i;
      const y = HEIGHT - PAD_BOTTOM - ((value - min) / span) * (HEIGHT - PAD_TOP - PAD_BOTTOM);
      return { value, y };
    });

    return {
      points,
      line,
      area,
      min,
      max,
      avg,
      delta,
      deltaPct,
      yLabels,
    };
  }, [safeSeries, ticks]);

  if (!model) {
    return <div className="muted">Нет данных для графика.</div>;
  }

  return (
    <div className="balance-chart">
      <svg
        viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
        preserveAspectRatio="xMidYMid meet"
        className="balance-chart-svg"
        data-testid="balance-svg"
      >
        <defs>
          <linearGradient id="balanceFill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stopColor="#4f8bff" stopOpacity="0.32" />
            <stop offset="100%" stopColor="#3cc7c4" stopOpacity="0.08" />
          </linearGradient>
          <linearGradient id="balanceStroke" x1="0" x2="1" y1="0" y2="0">
            <stop offset="0%" stopColor="#4f8bff" />
            <stop offset="100%" stopColor="#3cc7c4" />
          </linearGradient>
        </defs>

        {model.yLabels.map((t) => (
          <g key={t.y}>
            <line
              x1={PAD_LEFT}
              x2={WIDTH - PAD_RIGHT}
              y1={t.y}
              y2={t.y}
              className="balance-chart-gridline"
            />
            <text
              x={PAD_LEFT - 10}
              y={t.y + 4}
              textAnchor="end"
              className="balance-chart-axis"
              data-testid="balance-y-label"
            >
              {formatMoney(t.value, currency)}
            </text>
          </g>
        ))}

        <polygon points={model.area} fill="url(#balanceFill)" />
        <polyline
          points={model.line}
          fill="none"
          stroke="url(#balanceStroke)"
          strokeWidth={3.2}
          strokeLinecap="round"
          strokeLinejoin="round"
          data-testid="balance-line"
        />
      </svg>

      <div className="balance-metrics">
        <div className="balance-metric">
          <div className="muted">Мин</div>
          <div data-testid="metric-min">{formatMoney(model.min, currency)}</div>
        </div>
        <div className="balance-metric">
          <div className="muted">Макс</div>
          <div data-testid="metric-max">{formatMoney(model.max, currency)}</div>
        </div>
        <div className="balance-metric">
          <div className="muted">Изменение</div>
          <div data-testid="metric-change">{formatMoney(model.delta, currency)}</div>
        </div>
        <div className="balance-metric">
          <div className="muted">Среднее</div>
          <div data-testid="metric-avg">{formatMoney(model.avg, currency)}</div>
        </div>
        <div className="balance-metric">
          <div className="muted">Тренд %</div>
          <div data-testid="metric-trend">{formatPercent(model.deltaPct)}</div>
        </div>
      </div>
    </div>
  );
};

