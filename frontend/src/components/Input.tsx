import React from 'react';
import classNames from 'classnames';

type InputProps = React.InputHTMLAttributes<HTMLInputElement> & {
  error?: string;
  label?: string;
};

export const Input: React.FC<InputProps> = ({ error, label, className, ...rest }) => {
  const hasError = Boolean(error);
  const cls = classNames(className, { error: hasError });
  return (
    <div className="field">
      {label && <label>{label}</label>}
      <input className={cls} {...rest} />
      {error && error.trim().length > 0 && <div className="error-text">{error}</div>}
    </div>
  );
};
