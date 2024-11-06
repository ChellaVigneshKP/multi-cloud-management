import React, { useState, useEffect, useContext } from 'react';
import { LockOutlined as LockOutlinedIcon, Visibility, VisibilityOff } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useNavigate } from 'react-router-dom';
import FingerprintJS from '@fingerprintjs/fingerprintjs';
import AuthLayout from '../components/AuthLayout';
import LoadingButton from '../components/LoadingButton';
import { AuthContext } from '../components/AuthContext';
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const LoginPage = () => {
  const { login } = useContext(AuthContext);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [visitorId, setVisitorId] = useState(null);
  const navigate = useNavigate();
  
  useEffect(() => {
    document.title = 'C-Cloud | Login';
  }, []);

  useEffect(() => {
    const getVisitorId = async () => {
      const fp = await FingerprintJS.load();
      const result = await fp.get();
      setVisitorId(result.visitorId);
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

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validateForm()) return;

    setSuccess('');
    setError('');

    try {
      // Use AuthContext login and handle success and error within AuthContext
      await login({ email, password, visitorId, remember });
      setSuccess('Login successful!');
      navigate('/dashboard'); // Redirect to dashboard on successful login
    } catch (error) {
      if (error.response?.data?.message?.includes('Account not verified')) {
        navigate(`/verify?email=${encodeURIComponent(email)}`);
      } else if (error.response?.data?.message) {
        setError(error.response.data.message);
      } else {
        setError('An error occurred. Please try again.');
      }
    }
  };
  return (
    <AuthLayout avatarIcon={<LockOutlinedIcon />} title="Log in" description="">
      <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
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
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
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
        <LoadingButton type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} loading={false}>
          Login
        </LoadingButton>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Link href="/forgot-password" variant="body2">Forgot password?</Link>
          <Link href="/signup" variant="body2">{"Don't have an account? Sign Up"}</Link>
        </Box>
      </Box>
    </AuthLayout>
  );
};

export default LoginPage;
