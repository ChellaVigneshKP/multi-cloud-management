import React from 'react';
import { Box, Typography } from '@mui/material';
import Lottie from 'lottie-react'; // Correct import for Lottie component
import loadingAnimation from '../assets/animations/Loading.json'; // Adjust the path as needed

function CustomLoader() {
  return (
    <Box 
      display="flex" 
      flexDirection="column" 
      alignItems="center" 
      justifyContent="center" 
      height="100vh"
      sx={{
        background: 'linear-gradient(135deg, #e6f7f9, #b3e0e6)', // Lighter gradient background
        animation: 'fade-in 1.5s ease-in-out'
      }}
    >
      <Lottie
        animationData={loadingAnimation} // Provide the animation data
        loop
        style={{ height: '60vh', width: '60vw' }} // Dynamic size based on viewport
      />
      <Typography 
        variant="h6" 
        sx={{ 
          color: '#00445d', 
          fontFamily: 'Roboto, sans-serif', 
          letterSpacing: '0.5px',
          marginTop: '-20px', // Move text up further
          animation: 'bounce-text 2s infinite' // Add animation
        }}
      >
        Loading, please wait...
      </Typography>
      
      {/* Keyframe animations */}
      <style>
        {`
          @keyframes fade-in {
            from { opacity: 0; }
            to { opacity: 1; }
          }

          @keyframes bounce-text {
            0%, 20%, 50%, 80%, 100% {
              transform: translateY(0);
            }
            40% {
              transform: translateY(-10px);
            }
            60% {
              transform: translateY(-5px);
            }
          }
        `}
      </style>
    </Box>
  );
}

export default CustomLoader;
