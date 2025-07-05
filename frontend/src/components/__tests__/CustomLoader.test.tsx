import React from 'react';
import { render, screen } from '@testing-library/react';
import CustomLoader from '../CustomLoader';

// Mock the Lottie component to avoid animation runtime overhead
jest.mock('lottie-react', () => () => <div data-testid="lottie-animation" />);

describe('CustomLoader', () => {
  it('renders the loading animation and text', () => {
    render(<CustomLoader />);

    // Check if Lottie is rendered
    const lottie = screen.getByTestId('lottie-animation');
    expect(lottie).toBeInTheDocument();

    // Check if the loading text is rendered
    const text = screen.getByText(/Loading, please wait/i);
    expect(text).toBeInTheDocument();
    expect(text).toHaveTextContent('Loading, please wait...');
  });
});
