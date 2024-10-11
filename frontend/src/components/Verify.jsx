import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import CircularProgress from '@mui/material/CircularProgress';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import axios from 'axios';

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

export default function Verify() {
  const [verificationCode, setVerificationCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false); // Track if the resend request is in progress

  const query = new URLSearchParams(useLocation().search);
  const email = query.get('email');
  const navigate = useNavigate(); // Initialize navigate

  const handleVerify = (event) => {
    event.preventDefault();

    if (!verificationCode.trim()) {
      setError('Verification code is required.');
      return;
    }

    setLoading(true); // Set loading to true when the request starts

    axios.post('http://localhost:6061/auth/verify', { email, verificationCode })
      .then(res => {
        setMessage('Verification successful! Redirecting to login...');
        setError('');
        // Redirect to login page after successful verification
        setTimeout(() => {
          navigate('/login');
        }, 2000); // Delay for user to see the success message
      })
      .catch(err => {
        setLoading(false); // Set loading to false when the request fails
        if (err.response && err.response.data) {
          const errorMessage = err.response.data.message || 'Verification failed. Please try again.';
          setError(errorMessage);

          // Redirect to /login if the error message is "User already verified"
          if (errorMessage === 'User already verified') {
            setTimeout(() => {
              navigate('/login');
            }, 2000); // Delay for user to see the error message
          }
        } else {
          setError('Verification failed. Please try again.');
        }
      });
  };

  const handleResendCode = () => {
    setResending(true); // Set resending to true when the request starts
  
    axios.post('http://localhost:6061/auth/resend', { email }) // Send email in the request body
      .then(() => {
        setMessage('Verification code resent. Please check your email.');
        setError('');
        setResending(false); // Set resending to false when the request finishes
      })
      .catch(err => {
        setResending(false); // Set resending to false when the request fails
        if (err.response && err.response.data) {
          const errorMessage = err.response.data.message || 'Failed to resend verification code. Please try again.';
          setError(errorMessage);
        } else {
          setError('Failed to resend verification code. Please try again.');
        }
      });
  };  

  return (
    <ThemeProvider theme={theme}>
      <Grid container component="main" sx={{ height: '100vh' }}>
        <Grid
          item
          xs={false}
          sm={4}
          md={7}
          sx={{
            backgroundImage: 'url("images/output.jpg")',
            backgroundColor: (t) => t.palette.mode === 'light' ? t.palette.grey[50] : t.palette.grey[900],
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
        <Grid item xs={12} sm={8} md={5} component={Paper} elevation={6} square sx={{ backdropFilter: 'blur(10px)', backgroundColor: 'rgba(255, 255, 255, 0.7)' }}>
          <Box
            sx={{
              my: 8,
              mx: 4,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
          >
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
        </Grid>
      </Grid>
    </ThemeProvider>
  );
}
