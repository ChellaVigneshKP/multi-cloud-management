'use client';

import React from 'react';
import { Box, Typography } from '@mui/material';
import Lottie from 'lottie-react';
import loadingAnimation from '@/assets/animations/Loading.json';

const CustomLoader = () => {
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      height="100vh"
      sx={{
        background: 'linear-gradient(135deg, #e6f7f9, #b3e0e6)',
        animation: 'fade-in 1.5s ease-in-out',
      }}
    >
      <Lottie
        animationData={loadingAnimation}
        loop
        style={{ height: '60vh', width: '60vw' }}
      />
      <Typography
        variant="h6"
        sx={{
          color: '#00445d',
          fontFamily: 'Roboto, sans-serif',
          letterSpacing: '0.5px',
          marginTop: '-20px',
          animation: 'bounce-text 2s infinite',
        }}
      >
        Loading, please wait...
      </Typography>
    </Box>
  );
};

export default CustomLoader;