'use client';

import React, { useEffect, useState } from 'react';
import { Box, Typography, Button } from '@mui/material';
import dynamic from 'next/dynamic';
import Copyright from '@/components/Copyright';

const Lottie = dynamic(() => import('lottie-react'), { ssr: false });
import animationData from '@/assets/animations/404-3.json';

export default function NotFoundPage() {
  const [year, setYear] = useState(null);

  useEffect(() => {
    setYear(new Date().getFullYear());
  }, []);

  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      height="100vh"
      sx={{
        background: 'linear-gradient(135deg, #f0f4f8, #d9e7ef)',
        textAlign: 'center',
        padding: '20px',
      }}
    >
      <Lottie animationData={animationData} loop style={{ height: '40vh', width: '40vw' }} />
      <Typography variant="h3" sx={{ color: '#00445d', marginBottom: '20px' }}>
        Uh-oh! We couldn’t find that page.
      </Typography>
      <Typography variant="body1" sx={{ color: '#00445d', marginBottom: '40px' }}>
        It seems you’re a bit lost. Let’s help you get back on track!
      </Typography>
      <Button variant="contained" color="primary" href="/" sx={{ padding: '10px 20px' }}>
        Go Back Home
      </Button>
      <Button variant="outlined" color="primary" href="/contact-us" sx={{ marginTop: '20px', padding: '10px 20px' }}>
        Contact Us
      </Button>
      <Box sx={{ marginTop: '40px', width: '100%' }}>
        <Copyright year={year} />
      </Box>
    </Box>
  );
}
