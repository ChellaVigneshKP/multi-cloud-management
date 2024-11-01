import React, { useState, useEffect, useRef } from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import LockResetIcon from '@mui/icons-material/LockReset';
import Typography from '@mui/material/Typography';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CircularProgress from '@mui/material/CircularProgress';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../api'; // Ensure this is correctly set up

const theme = createTheme({
  palette: {
    primary: {
      main: '#B45C39', // Sunset orange
    },
    secondary: {
      main: '#231F21', // Sunset yellow
    },
  },
});

// Helper hook to parse query parameters
function useQuery() {
  return new URLSearchParams(useLocation().search);
}

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

export default function ChangePassword() {
  const query = useQuery();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false); // Loading state
  const [countdown, setCountdown] = useState(10); // Countdown state
  const isMounted = useRef(true); // To track mounted state

  useEffect(() => {
    // Cleanup on unmount
    return () => {
      isMounted.current = false;
    };
  }, []);

  // Extract email from URL query parameters
  useEffect(() => {
    const emailParam = query.get('email') || '';
    setEmail(emailParam);
  }, [query]);

  // Countdown logic for redirection
  useEffect(() => {
    if (countdown > 0 && success) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    } else if (countdown === 0 && success) {
      navigate('/login');
    }
  }, [countdown, success, navigate]);

  const validateForm = () => {
    if (!email.trim()) {
      setError('Email is required.');
      return false;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      setError('Please enter a valid email address.');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true); // Show loading spinner
    setError('');
    setSuccess('');

    try {
      const response = await api.post('/auth/take-action', { email });

      if (response.status === 200) {
        // Success, set the success message and start countdown
        setSuccess(response.data.message);
        setError('');
        setLoading(false);
        setCountdown(10); // Start countdown for redirect
      } else {
        // Handle non-200 status codes
        throw new Error(response.data.message || 'An error occurred. Please try again.');
      }
    } catch (error) {
      console.error('Error:', error);
      setError(error.response?.data?.message || error.message || 'Failed to send reset link. Please try again.');
      setLoading(false); // Re-enable the button if error
    }
  };

  return (
    <ThemeProvider theme={theme}>
      <Grid container component="main" sx={{ height: '100vh' }}>
        <CssBaseline />
        {/* Left side image */}
        <Grid
          item
          xs={false}
          sm={4}
          md={7}
          sx={{
            backgroundImage: 'url("images/output.jpg")',
            backgroundColor: (t) =>
              t.palette.mode === 'light' ? t.palette.grey[50] : t.palette.grey[900],
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
        {/* Right side form */}
        <Grid
          item
          xs={12}
          sm={8}
          md={5}
          component={Paper}
          elevation={6}
          square
          sx={{
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
              <LockResetIcon />
            </Avatar>
            <Typography component="h1" variant="h5">
              Change Password
            </Typography>
            <Typography variant="body2" align="center" sx={{ mt: 1 }}>
              Click the button below to request a password reset link.
            </Typography>
            <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3 }}>
              {error && (
                <Typography variant="body2" align="center" sx={{ mb: 2, color: 'error.main' }}>
                  {error}
                </Typography>
              )}
              {success && (
                <>
                  <Typography variant="body2" align="center" sx={{ mb: 2, color: 'success.main' }}>
                    {success}
                  </Typography>
                  <Typography variant="body2" align="center" sx={{ mb: 2, color: 'success.main' }}>
                    Redirecting to login page in {countdown} seconds...
                  </Typography>
                </>
              )}
              {!success && (
                <TextField
                  margin="normal"
                  required
                  fullWidth
                  id="email"
                  label="Email Address"
                  name="email"
                  autoComplete="email"
                  autoFocus
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  disabled // Make the email field read-only
                />
              )}
              {!success && (
                <Button
                  type="submit"
                  fullWidth
                  variant="contained"
                  sx={{ mt: 3, mb: 2 }}
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <CircularProgress
                        size={24}
                        sx={{
                          color: 'primary.contrastText',
                          position: 'absolute',
                          top: '50%',
                          left: '50%',
                          marginTop: '-12px',
                          marginLeft: '-12px',
                        }}
                        aria-label="Loading"
                      />
                      Requesting...
                    </>
                  ) : (
                    'Request Change Password'
                  )}
                </Button>
              )}
              <Grid container>
                <Grid item xs>
                  <Link href="/login" variant="body2">
                    Back to Login
                  </Link>
                </Grid>
              </Grid>
              <Copyright sx={{ mt: 5 }} />
            </Box>
          </Box>
        </Grid>
      </Grid>
    </ThemeProvider>
  );
}