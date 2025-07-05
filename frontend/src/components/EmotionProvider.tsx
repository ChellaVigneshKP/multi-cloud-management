'use client';
import React, { PropsWithChildren } from 'react';
import { ThemeProvider, CssBaseline, createTheme } from '@mui/material';


const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#00445d' },
    secondary: { main: '#88b5c4' },
    background: {
      default: '#ffffff',
      paper: '#ffffff',
    },
  },
});


export default function EmotionProvider({ children }: Readonly<PropsWithChildren>) {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  );
}