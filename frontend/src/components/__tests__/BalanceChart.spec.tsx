import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BalanceChart } from '../BalanceChart';

describe('BalanceChart', () => {
  it('renders a polyline without points and shows metrics', () => {
    const { container } = render(<BalanceChart series={[100, 200, 150]} currency="USD" />);

    expect(screen.getByTestId('balance-line').tagName.toLowerCase()).toBe('polyline');
    expect(container.querySelectorAll('circle')).toHaveLength(0);

    const yLabels = screen.getAllByTestId('balance-y-label');
    expect(yLabels).toHaveLength(5);
    const ys = yLabels.map((el) => Number(el.getAttribute('y')));
    expect(new Set(ys).size).toBe(ys.length);

    expect(screen.getByTestId('metric-min')).toHaveTextContent('100');
    expect(screen.getByTestId('metric-max')).toHaveTextContent('200');
    expect(screen.getByTestId('metric-avg')).toHaveTextContent('150');
    expect(screen.getByTestId('metric-change')).toHaveTextContent('50');
    expect(screen.getByTestId('metric-trend')).toHaveTextContent('+50.0%');
  });
});

