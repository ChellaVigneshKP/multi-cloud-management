import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Paper from '@mui/material/Paper';
import Avatar from '@mui/material/Avatar';
import CircularProgress from '@mui/material/CircularProgress';
import MailOutlineIcon from '@mui/icons-material/MailOutline';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import api from '../api';
import Link from '@mui/material/Link';
const theme = createTheme({
  palette: {
    primary: {
      main: '#B45C39',
    },
    secondary: {
      main: '#231F21',
    },
  },
});

function Copyright(props) {
  return (
    <Typography variant="body2" color="text.secondary" align="center" {...props}>
      {'Copyright © '}
      <Link color="inherit" href="https://chellavignesh.com">
        chellavignesh.com
      </Link>{' '}
      {new Date().getFullYear()}
      {'.'}
    </Typography>
  );
}

export default function Verify() {
  const [verificationCode, setVerificationCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  const query = new URLSearchParams(useLocation().search);
  const email = query.get('email');
  const navigate = useNavigate();

  const handleVerify = (event) => {
    event.preventDefault();

    if (!verificationCode.trim()) {
      setError('Verification code is required.');
      return;
    }

    setLoading(true);

    api.post('/auth/verify', { email, verificationCode })
      .then(() => {
        setMessage('Verification successful! Redirecting to login...');
        setError('');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      })
      .catch(err => {
        setLoading(false);
        const errorMessage = err.response?.data?.message || 'Verification failed. Please try again.';
        setError(errorMessage);

        if (errorMessage === 'User already verified') {
          setTimeout(() => {
            navigate('/login');
          }, 2000);
        }
      });
  };

  const handleResendCode = () => {
    setResending(true);

    api.post('/auth/resend', { email })
      .then(() => {
        setMessage('Verification code resent. Please check your email.');
        setError('');
        setResending(false);
      })
      .catch(err => {
        setResending(false);
        const errorMessage = err.response?.data?.message || 'Failed to resend verification code. Please try again.';
        setError(errorMessage);
      });
  };

  return (
    <ThemeProvider theme={theme}>
      <Grid container sx={{ height: '100vh' }}>
        <Grid
          size={{ xs: 0, sm: 4, md: 7 }}
          sx={{
            backgroundImage: 'url("images/output.jpg")',
            backgroundColor: (t) => t.palette.mode === 'light' ? t.palette.grey[50] : t.palette.grey[900],
            backgroundSize: 'cover',
            backgroundPosition: 'center',
            display: { xs: 'none', sm: 'block' },
          }}
        />
        <Grid
          size={{ xs: 12, sm: 8, md: 5 }}
          component={Paper}
          elevation={6}
          square
          sx={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between', // Align footer at bottom
            backdropFilter: 'blur(10px)',
            backgroundColor: 'rgba(255, 255, 255, 0.7)',
          }}
        >
          <Box
            sx={{
              my: 8,
              mx: 4,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
            <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
              <MailOutlineIcon />
            </Avatar>
            <Typography component="h1" variant="h5">
              Verify Your Email
            </Typography>
            <Typography variant="body1" sx={{ mb: 2 }}>
              A verification code has been sent to {email}.
            </Typography>
            {loading && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
                <CircularProgress />
              </Box>
            )}
            {resending && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
                <CircularProgress size={24} />
              </Box>
            )}
            <Box component="form" noValidate onSubmit={handleVerify} sx={{ width: '100%', maxWidth: '360px' }}>
              {error && (
                <Typography color="error" variant="body2" align="center" sx={{ mb: 2 }}>
                  {error}
                </Typography>
              )}
              {message && (
                <Typography color="success" variant="body2" align="center" sx={{ mb: 2 }}>
                  {message}
                </Typography>
              )}
              <TextField
                margin="normal"
                required
                fullWidth
                name="verificationCode"
                label="Verification Code"
                type="text"
                id="verificationCode"
                autoComplete="off"
                value={verificationCode}
                onChange={(e) => setVerificationCode(e.target.value)}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loading}
              >
                Verify
              </Button>
              <Button
                fullWidth
                variant="outlined"
                sx={{ mt: 2, mb: 2 }}
                onClick={handleResendCode}
                disabled={resending}
              >
                {resending ? 'Resending...' : 'Resend Code'}
              </Button>
              <Button
                fullWidth
                variant="text"
                sx={{ mt: 2, mb: 2 }}
                onClick={() => navigate('/login')}
              >
                Back to Login
              </Button>
            </Box>
          </Box>
          {/* Footer Section */}
          <Box sx={{ py: 2, backgroundColor: 'rgba(255, 255, 255, 0.5)', textAlign: 'center' }}>
            <Copyright />
          </Box>
        </Grid>
      </Grid>
    </ThemeProvider>
  );
}