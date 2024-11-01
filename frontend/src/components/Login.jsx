import React, { useState, useEffect } from 'react';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Typography from '@mui/material/Typography';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { useNavigate } from 'react-router-dom';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import FingerprintJS from '@fingerprintjs/fingerprintjs';
import api from '../api';
import { setAccessToken } from '../api';

const theme = createTheme({
  palette: {
    primary: { main: '#B45C39' },
    secondary: { main: '#231F21' },
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

export default function SignInSide() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false); 
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [visitorId, setVisitorId] = useState(null); 
  const navigate = useNavigate();

  useEffect(() => {
    const getVisitorId = async () => {
      const fp = await FingerprintJS.load();
      const result = await fp.get();
      setVisitorId(result.visitorId);
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

    const formData = { email, password, visitorId, remember };
    api.post('/auth/login', formData)
      .then(res => {
        setAccessToken(res.data.jwtToken);
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
          size={{ xs: 0, sm: 4, md: 7 }}
          sx={{
            display: { xs: 'none', sm: 'block' },
            backgroundImage: 'url("images/output.jpg")',
            backgroundColor: (t) => t.palette.grey[50],
            backgroundSize: 'cover',
            backgroundPosition: 'center',
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
            justifyContent: 'space-between', // Space between the form and footer
            backdropFilter: 'blur(10px)',
            backgroundColor: 'rgba(255, 255, 255, 0.7)',
          }}
        >
          <Box sx={{ px: 4, pt: 8, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
              <LockOutlinedIcon />
            </Avatar>
            <Typography component="h1" variant="h5">
              Log in
            </Typography>
            <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
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
                type={showPassword ? 'text' : 'password'}
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={() => setShowPassword(!showPassword)}
                          edge="end"
                        >
                          {showPassword ? <VisibilityOff /> : <Visibility />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
              />
              <FormControlLabel
                control={<Checkbox checked={remember} onChange={(e) => setRemember(e.target.checked)} color="primary" />}
                label="Remember me"
              />
              <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>
                Login
              </Button>
              <Grid container justifyContent="space-between">
                <Grid>
                  <Link href="/forgot-password" variant="body2">Forgot password?</Link>
                </Grid>
                <Grid>
                  <Link href="/signup" variant="body2">{"Don't have an account? Sign Up"}</Link>
                </Grid>
              </Grid>
            </Box>
          </Box>
          {/* Footer Section */}
          <Box sx={{ py: 2, px: 4, backgroundColor: 'rgba(255, 255, 255, 0.5)' }}>
            <Copyright />
          </Box>
        </Grid>
      </Grid>
    </ThemeProvider>
  );
}