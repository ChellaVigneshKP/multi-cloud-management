import React from 'react';
import Typography from '@mui/material/Typography';
import Link from '@mui/material/Link';
import logo from '../assets/images/logo/logo.png';
const Copyright = (props) => {
  return (
    <Typography variant="body2" color="text.secondary" align="center" {...props}>
      <img
        src={logo}
        alt="Logo"
        style={{
          height: 20,
          verticalAlign: 'middle',
          marginRight: 8,
          transform: 'rotate(1deg)'
        }}
      />
      {'Copyright © '}
      <Link color="inherit" href="https://chellavignesh.com">
        chellavignesh.com
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
};

export default Copyright;
