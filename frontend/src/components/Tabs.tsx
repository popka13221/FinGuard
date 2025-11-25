import React from 'react';
import classNames from 'classnames';

type TabOption = {
  id: string;
  label: string;
};

type TabsProps = {
  value: string;
  options: TabOption[];
  onChange: (id: string) => void;
};

export const Tabs: React.FC<TabsProps> = ({ value, options, onChange }) => {
  return (
    <div className="tabs">
      {options.map((opt) => (
        <button
          key={opt.id}
          className={classNames('tab-btn', { active: opt.id === value })}
          onClick={() => onChange(opt.id)}
          type="button"
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
};
