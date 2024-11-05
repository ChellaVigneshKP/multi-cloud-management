import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import Lottie from 'lottie-react';
import animationData from '../assets/animations/404-3.json'; // Replace with your branded 404 animation
import Copyright from '../components/Copyright';

function BrandEngaged404Page() {
  return (
    <Box 
      display="flex" 
      flexDirection="column" 
      alignItems="center" 
      justifyContent="center" 
      height="100vh"
      sx={{
        background: 'linear-gradient(135deg, #f0f4f8, #d9e7ef)', // Adjust with your brand colors
        textAlign: 'center',
        padding: '20px',
      }}
    >
      <Lottie 
        animationData={animationData} 
        loop 
        style={{ height: '40vh', width: '40vw' }} // Adjust the size for brand consistency
      />
      <Typography 
        variant="h3" 
        sx={{ 
          color: '#00445d', // Use brand colors
          marginBottom: '20px', 
          fontFamily: 'Roboto, sans-serif' // Use brand font
        }}
      >
        Uh-oh! We couldn’t find that page.
      </Typography>
      <Typography 
        variant="body1" 
        sx={{ 
          color: '#00445d', 
          marginBottom: '40px',
        }}
      >
        It seems you’re a bit lost. Let’s help you get back on track!
      </Typography>
      <Button 
        variant="contained" 
        color="primary" 
        href="/" 
        sx={{ padding: '10px 20px' }}
      >
        Go Back Home
      </Button>
      <Button 
        variant="outlined" 
        color="primary" 
        href="/contact-us" 
        sx={{ marginTop: '20px', padding: '10px 20px' }}
      >
        Contact Us
      </Button>
      <Box sx={{ marginTop: '40px', width: '100%' }}>
        <Copyright />
      </Box>
    </Box>
  );
}

export default BrandEngaged404Page;
