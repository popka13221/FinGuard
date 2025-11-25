import React from 'react';
import classNames from 'classnames';

type Option = { value: string; label: string };

type SelectProps = React.SelectHTMLAttributes<HTMLSelectElement> & {
  label?: string;
  error?: string;
  options: Option[];
};

export const Select: React.FC<SelectProps> = ({ label, error, options, className, ...rest }) => {
  const cls = classNames(className, { error: Boolean(error) });
  return (
    <div className="field">
      {label && <label>{label}</label>}
      <select className={cls} {...rest}>
        {options.map((opt) => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
      {error && error.trim().length > 0 && <div className="error-text">{error}</div>}
    </div>
  );
};
