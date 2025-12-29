import React, { useMemo, useState } from 'react';
import '../theme.css';
import './charts.css';

export type PieChartItem = {
  label: string;
  value: number;
  color?: string;
};

type PieChartProps = {
  items: PieChartItem[];
  currency?: string;
  title?: string;
};

const DEFAULT_COLORS = ['#4f8bff', '#10b981', '#f97316', '#3cc7c4', '#9aa0aa'];

const formatMoney = (value: number, currency?: string) => {
  const amount = (value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  return currency ? `${amount} ${currency}` : amount;
};

export const PieChart: React.FC<PieChartProps> = ({ items, currency }) => {
  const safeItems = Array.isArray(items)
    ? items.filter((i) => i && typeof i.label === 'string' && Number.isFinite(i.value) && i.value >= 0)
    : [];
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  const model = useMemo(() => {
    const total = safeItems.reduce((acc, item) => acc + item.value, 0);
    const size = 200;
    const radius = 78;
    const center = size / 2;

    let offset = 0;
    const slices = safeItems.map((item, idx) => {
      const value = item.value;
      const pct = total > 0 ? value / total : 0;
      const startAngle = total > 0 ? (offset / total) * Math.PI * 2 : 0;
      const endAngle = startAngle + pct * Math.PI * 2;
      offset += value;

      const start = {
        x: center + radius * Math.cos(startAngle - Math.PI / 2),
        y: center + radius * Math.sin(startAngle - Math.PI / 2),
      };
      const end = {
        x: center + radius * Math.cos(endAngle - Math.PI / 2),
        y: center + radius * Math.sin(endAngle - Math.PI / 2),
      };
      const largeArc = endAngle - startAngle > Math.PI ? 1 : 0;
      const d = `M ${start.x} ${start.y} A ${radius} ${radius} 0 ${largeArc} 1 ${end.x} ${end.y}`;

      const color = item.color || DEFAULT_COLORS[idx % DEFAULT_COLORS.length];
      const percent = total > 0 ? Math.round((value / total) * 100) : 0;

      return { d, color, percent, value, label: item.label };
    });

    return { total, size, center, slices };
  }, [safeItems]);

  const activate = (idx: number) => setActiveIndex(idx);
  const reset = () => setActiveIndex(null);

  if (safeItems.length === 0) {
    return <div className="muted">Нет данных.</div>;
  }

  return (
    <div className="pie-chart-layout" data-testid="pie-layout">
      <svg viewBox={`0 0 ${model.size} ${model.size}`} className="pie-chart-svg" aria-label="Структура расходов">
        {model.slices.map((slice, idx) => (
          <path
            key={slice.label + idx}
            d={slice.d}
            fill="none"
            stroke={slice.color}
            strokeWidth={16}
            strokeLinecap="butt"
            data-testid={`pie-slice-${idx}`}
            className={[
              'pie-slice',
              activeIndex === null ? '' : activeIndex === idx ? 'pie-slice-active' : 'pie-slice-inactive',
            ].join(' ')}
            onMouseEnter={() => activate(idx)}
            onMouseLeave={reset}
          />
        ))}
        <text x={model.center} y={model.center - 6} textAnchor="middle" fontWeight={900} data-testid="pie-total">
          {formatMoney(model.total, currency)}
        </text>
        <text x={model.center} y={model.center + 14} textAnchor="middle" className="muted">
          Всего
        </text>
      </svg>

      <div className="pie-legend" aria-label="Легенда">
        {model.slices.map((slice, idx) => (
          <button
            key={slice.label + idx}
            type="button"
            className={['pie-legend-item', activeIndex === idx ? 'active' : ''].join(' ')}
            onMouseEnter={() => activate(idx)}
            onMouseLeave={reset}
            onFocus={() => activate(idx)}
            onBlur={reset}
            data-testid={`pie-legend-${idx}`}
            aria-label={slice.label}
          >
            <span className="pie-legend-dot" style={{ background: slice.color }} aria-hidden="true" />
            <span>{slice.label}</span>
            <span data-testid={`pie-percent-${idx}`}>{slice.percent}%</span>
          </button>
        ))}
      </div>
    </div>
  );
};

