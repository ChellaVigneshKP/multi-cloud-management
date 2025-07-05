import React from 'react';
import { render, screen } from '@testing-library/react';
import EmotionProvider from '../EmotionProvider';
import { Button } from '@mui/material';

describe('EmotionProvider', () => {
  it('renders children inside ThemeProvider', () => {
    render(
      <EmotionProvider>
        <Button variant="contained" color="primary">
          Test Button
        </Button>
      </EmotionProvider>
    );

    const button = screen.getByRole('button', { name: /test button/i });
    expect(button).toBeInTheDocument();

  });
});