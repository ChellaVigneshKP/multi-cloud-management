'use client';

import React, { useEffect, useState } from 'react';
import { Box, Typography, Link } from '@mui/material';
import Image from 'next/image';

const Copyright = (props: React.ComponentProps<typeof Typography>) => {
  const [year, setYear] = useState<number | null>(null);

  useEffect(() => {
    setYear(new Date().getFullYear());
  }, []);

  return (
    <Box display="flex" justifyContent="center" alignItems="center" {...props}>
      <Image
        src="/assets/images/logo/logo.png"
        alt="Logo"
        width={30}
        height={30}
        style={{
          marginRight: 8,
          transform: 'rotate(1deg)',
        }}
      />
      <Typography variant="body2" color="text.secondary" align="center">
        {'Copyright © '}
        <Link color="inherit" href="https://chellavignesh.com">
          chellavignesh.com
        </Link>{' '}
        {year ?? ''}
        {'.'}
      </Typography>
    </Box>
  );
};

export default Copyright;