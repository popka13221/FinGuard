import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { PieChart } from '../PieChart';

describe('PieChart', () => {
  it('renders total and percentages', () => {
    render(
      <PieChart
        currency="USD"
        items={[
          { label: 'Food', value: 50, color: '#f00' },
          { label: 'Rent', value: 150, color: '#0f0' },
        ]}
      />,
    );

    const total = screen.getByTestId('pie-total');
    expect(total).toHaveTextContent('USD');
    expect(total.textContent).toMatch(/200/);

    expect(screen.getByTestId('pie-percent-0')).toHaveTextContent('25%');
    expect(screen.getByTestId('pie-percent-1')).toHaveTextContent('75%');
  });

  it('highlights a slice when legend item hovered', () => {
    render(
      <PieChart
        currency="USD"
        items={[
          { label: 'A', value: 40, color: '#4f8bff' },
          { label: 'B', value: 60, color: '#10b981' },
        ]}
      />,
    );

    const legend0 = screen.getByTestId('pie-legend-0');
    const slice0 = screen.getByTestId('pie-slice-0');
    const slice1 = screen.getByTestId('pie-slice-1');

    fireEvent.mouseEnter(legend0);
    expect(slice0.getAttribute('class') || '').toMatch(/pie-slice-active/);
    expect(slice1.getAttribute('class') || '').toMatch(/pie-slice-inactive/);

    fireEvent.mouseLeave(legend0);
    expect(slice0.getAttribute('class') || '').not.toMatch(/pie-slice-inactive/);
    expect(slice1.getAttribute('class') || '').not.toMatch(/pie-slice-inactive/);
  });
});

