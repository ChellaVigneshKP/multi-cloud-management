import React from 'react';
import { render, screen } from '@testing-library/react';
import Copyright from '../Copyright';
import '@testing-library/jest-dom';

describe('Copyright component', () => {
  it('renders logo, text, and current year', () => {
    render(<Copyright />);

    // Logo
    const logo = screen.getByAltText('Logo');
    expect(logo).toBeInTheDocument();

    // Link
    const link = screen.getByRole('link', { name: /chellavignesh\.com/i });
    expect(link).toHaveAttribute('href', 'https://chellavignesh.com');

    // Year
    const currentYear = new Date().getFullYear().toString();
    expect(screen.getByText(new RegExp(currentYear))).toBeInTheDocument();
  });
});