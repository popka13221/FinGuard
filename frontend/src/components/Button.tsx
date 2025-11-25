import React from 'react';
import classNames from 'classnames';

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'ghost';
};

export const Button: React.FC<ButtonProps> = ({ variant = 'primary', className, children, ...rest }) => {
  const cls = classNames(className, {
    secondary: variant === 'secondary',
    ghost: variant === 'ghost',
  });
  return (
    <button className={cls} {...rest}>
      {children}
    </button>
  );
};
