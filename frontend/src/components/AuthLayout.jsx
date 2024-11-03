import React from 'react';
import PropTypes from 'prop-types';
import Avatar from '@mui/material/Avatar';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Typography from '@mui/material/Typography';
import { styled, useTheme, useMediaQuery } from '@mui/material';
import Copyright from './Copyright';

const StyledGrid = styled(Grid)(({ theme }) => ({
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  display: 'block',
  [theme.breakpoints.down('sm')]: {
    display: 'none',
  },
}));

const FormContainer = styled(Paper)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-between', // Space between form and footer
  backdropFilter: 'blur(10px)',
  backgroundColor: 'rgba(255, 255, 255, 0.7)',
  height: '100%',
  overflow: 'hidden', // Prevent overflow to ensure footer stays
  flex: '1 1 auto', // Adjust flex-grow and flex-shrink
}));

const FormBox = styled(Box)(({ theme }) => ({
  margin: theme.spacing(3, 4),
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  flex: '1 1 auto', // Allow it to grow and shrink
  overflowY: 'auto', // Enable scrolling if content exceeds
}));

const FooterBox = styled(Box)(({ theme }) => ({
  padding: theme.spacing(0.5),
  textAlign: 'center',
  backgroundColor: 'rgba(255, 255, 255, 0.5)',
}));

const AuthLayout = ({
  leftImage,
  avatarIcon,
  title,
  description = '', // Default value using destructuring
  children,
}) => {
  const theme = useTheme();
  const isMediumScreen = useMediaQuery(theme.breakpoints.between('sm', 'md'));
  const backgroundImage = leftImage || (isMediumScreen ? '/Background/MediumBackground.png' : '/Background/Background.png');
  return (
    <Grid container sx={{ height: '100vh' }}>
      {/* Left side image */}
      <StyledGrid
        size={{ xs: 0, sm: 4, md: 7 }}
        sx={{
          backgroundImage: `url(${backgroundImage})`,
          backgroundColor:
            theme.palette.mode === 'light'
              ? theme.palette.grey[50]
              : theme.palette.grey[900],
        }}
      />
      {/* Right side form */}
      <Grid
        size={{ xs: 12, sm: 8, md: 5 }}
        component={FormContainer}
        elevation={6}
        square
      >
        <FormBox>
          <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
            {avatarIcon}
          </Avatar>
          <Typography component="h1" variant="h5">
            {title}
          </Typography>
          {description && (
            <Typography variant="body2" align="center" sx={{ mt: 1 }}>
              {description}
            </Typography>
          )}
          {children}
        </FormBox>
        <FooterBox>
          <Copyright />
        </FooterBox>
      </Grid>
    </Grid>
  );
};

AuthLayout.propTypes = {
  leftImage: PropTypes.string.isRequired,
  avatarIcon: PropTypes.element.isRequired,
  title: PropTypes.string.isRequired,
  description: PropTypes.string, // Optional prop
  children: PropTypes.node.isRequired,
};

export default AuthLayout;
