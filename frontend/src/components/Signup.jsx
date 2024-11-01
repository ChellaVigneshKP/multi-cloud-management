import React, { useState } from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import CircularProgress from '@mui/material/CircularProgress';
import api from '../api';

const theme = createTheme({
  palette: {
    primary: { main: '#B45C39' }, // Sunset orange
    secondary: { main: '#231F21' }, // Sunset yellow
  },
});

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

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

export default function SignUpSide() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [touchedEmail, setTouchedEmail] = useState(false);

  const navigate = useNavigate();

  const validateForm = () => {
    if (!firstName.trim() || !lastName.trim() || !username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
      setError('All fields are required.');
      return false;
    }
    if (username.length < 3) {
      setError('Username must be at least 3 characters long.');
      return false;
    }
    if (!emailPattern.test(email)) {
      setError('Invalid email address.');
      return false;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters long.');
      return false;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!validateForm()) return;

    const formData = { firstName, lastName, username, email, password };
    setLoading(true);

    api.post('/auth/signup', formData)
      .then(res => {
        setSuccess('Sign Up successful! Please verify your email.');
        setError('');
        setTimeout(() => {
          navigate(`/verify?email=${encodeURIComponent(email)}`);
        });
      })
      .catch(err => {
        setLoading(false);
        const errorMessage = err.response?.data?.message || 'An error occurred. Please try again.';
        setError(errorMessage);
      });
  };

  return (
    <ThemeProvider theme={theme}>
      <Grid container sx={{ height: '100vh' }}>
        <CssBaseline />
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
            justifyContent: 'space-between', // Footer positioning
            overflowY: 'auto',
            maxHeight: '100vh',
          }}
        >
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              mt: { xs: 10, sm: 6 },
              px: { xs: 2, sm: 4 },
            }}
          >
            <Avatar sx={{ m: 1, bgcolor: 'primary.main', width: 56, height: 56 }}>
              <LockOutlinedIcon />
            </Avatar>
            <Typography component="h1" variant="h5" sx={{ textAlign: 'center', mb: 2 }}>
              Sign Up
            </Typography>
            {loading && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
                <CircularProgress />
              </Box>
            )}
            <Box component="form" noValidate onSubmit={handleSubmit} sx={{ width: '100%' }}>
              {error && (
                <Typography color="error" variant="body2" align="center" sx={{ mb: 2 }}>
                  {error}
                </Typography>
              )}
              {success && (
                <Typography color="success" variant="body2" align="center" sx={{ mb: 2 }}>
                  {success}
                </Typography>
              )}
              <Grid container rowSpacing={2} columnSpacing={2}>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="firstName"
                    label="First Name"
                    name="firstName"
                    autoComplete="given-name"
                    autoFocus
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                  />
                </Grid>
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    margin="normal"
                    required
                    fullWidth
                    id="lastName"
                    label="Last Name"
                    name="lastName"
                    autoComplete="family-name"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                  />
                </Grid>
              </Grid>
              <TextField
                margin="normal"
                required
                fullWidth
                id="username"
                label="Username"
                name="username"
                autoComplete="username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onBlur={() => setTouchedEmail(true)}
                helperText={touchedEmail && !emailPattern.test(email) && "Invalid email address."}
                error={touchedEmail && !emailPattern.test(email)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="confirmPassword"
                label="Confirm Password"
                type="password"
                id="confirmPassword"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                sx={{ mt: 3, mb: 2 }}
                disabled={loading}
              >
                Sign Up
              </Button>
              <Grid container justifyContent="flex-end">
                <Grid size="auto">
                  <Link href="/login" variant="body2">
                    Already have an account? Sign In
                  </Link>
                </Grid>
              </Grid>
            </Box>
          </Box>
          {/* Footer Section */}
          <Box sx={{ py: 2, textAlign: 'center', backgroundColor: 'rgba(255, 255, 255, 0.5)' }}>
            <Copyright />
          </Box>
        </Grid>
      </Grid>
    </ThemeProvider>
  );
}
