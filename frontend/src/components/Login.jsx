import React, { useState, useEffect } from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import Cookies from 'js-cookie';
import FingerprintJS from '@fingerprintjs/fingerprintjs'; // Import FingerprintJS
import api from '../api';

const theme = createTheme({
  palette: {
    primary: { main: '#B45C39' },
    secondary: { main: '#231F21' },
  },
});

// Basic email regex pattern
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

export default function SignInSide() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [visitorId, setVisitorId] = useState(null); // State for visitor ID
  const navigate = useNavigate();

  // Initialize FingerprintJS and retrieve the visitor ID
  useEffect(() => {
    const getVisitorId = async () => {
      const fp = await FingerprintJS.load();
      const result = await fp.get();
      setVisitorId(result.visitorId); // Store visitor ID in state
      console.log('Visitor ID:', result.visitorId);
    };
    getVisitorId();
  }, []);

  const validateForm = () => {
    if (!email.trim() || !password.trim()) {
      setError('Email and Password are required.');
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
    setError('');
    return true;
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!validateForm()) return;

    const formData = { email, password, visitorId }; // Include visitor ID
    api.post('/auth/login', formData)
      .then(res => {
        Cookies.set('apiToken', res.data.jwtToken, { expires: 1, secure: false });
        Cookies.set('refreshToken', res.data.refreshToken, { expires: 7, secure: false });
        setSuccess('Login successful!');
        setEmail('');
        setPassword('');
        setRemember(false);
        navigate('/dashboard');
      })
      .catch(err => {
        if (err.response?.data?.message?.includes('Account not verified')) {
          navigate(`/verify?email=${encodeURIComponent(email)}`);
        } else if (err.response?.data?.message) {
          setError(err.response.data.message);
        } else {
          setError('An error occurred. Please try again.');
        }
      });
  };

  return (
    <ThemeProvider theme={theme}>
      <Grid container component="main" sx={{ height: '100vh' }}>
        <CssBaseline />
        <Grid
          item
          xs={false}
          sm={4}
          md={7}
          sx={{
            backgroundImage: 'url("images/output.jpg")',
            backgroundColor: (t) => t.palette.grey[50],
            backgroundSize: 'cover',
            backgroundPosition: 'center',
          }}
        />
        <Grid item xs={12} sm={8} md={5} component={Paper} elevation={6} square sx={{ backdropFilter: 'blur(10px)', backgroundColor: 'rgba(255, 255, 255, 0.7)' }}>
          <Box sx={{ my: 8, mx: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
              <LockOutlinedIcon />
            </Avatar>
            <Typography component="h1" variant="h5">Log in</Typography>
            <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1 }}>
              {error && <Typography color="error" variant="body2" align="center" sx={{ mb: 2 }}>{error}</Typography>}
              {success && <Typography color="success" variant="body2" align="center" sx={{ mb: 2 }}>{success}</Typography>}
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
                error={!emailPattern.test(email)}
                helperText={!emailPattern.test(email) && "Invalid email address."}
              />
              <TextField
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <FormControlLabel
                control={<Checkbox checked={remember} onChange={(e) => setRemember(e.target.checked)} color="primary" />}
                label="Remember me"
              />
              <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>Login</Button>
              <Grid container>
                <Grid item xs>
                  <Link href="/forgot-password" variant="body2">Forgot password?</Link>
                </Grid>
                <Grid item>
                  <Link href="/signup" variant="body2">{"Don't have an account? Sign Up"}</Link>
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
