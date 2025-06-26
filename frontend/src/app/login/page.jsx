'use client';

import { useState, useEffect, useContext } from 'react';
import {
  LockOutlined as LockOutlinedIcon,
  Visibility,
  VisibilityOff,
} from '@mui/icons-material';
import {
  TextField,
  InputAdornment,
  IconButton,
  FormControlLabel,
  Checkbox,
  Typography,
  Box,
} from '@mui/material';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import FingerprintJS from '@fingerprintjs/fingerprintjs';
import AuthLayout from '@/components/layouts/AuthLayout';
import LoadingButton from '@/components/layouts/LoadingButton';
import { AuthContext } from '@/contexts/auth-context';
import { emailPattern } from '@/utils/validation';
const LoginPage = () => {
  const { login } = useContext(AuthContext);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [visitorId, setVisitorId] = useState(null);
  const router = useRouter();

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
    setLoading(true);

    try {
      await login({ email: email, password, visitorId, remember });
      setSuccess('Login successful!');
      router.push('/dashboard');
    } catch (error) {
      const msg = error?.response?.data?.message;
      if (msg?.includes('Account not verified')) {
        router.push(`/verify?email=${encodeURIComponent(email)}`);
      } else {
        setError(msg || 'An error occurred. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout avatarIcon={<LockOutlinedIcon />} title="Log in">
      <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 1, width: '100%' }}>
        {error && (
          <Typography color="error" variant="body2" align="center" sx={{ mb: 2 }}>
            {error}
          </Typography>
        )}
        {success && (
          <Typography color="success.main" variant="body2" align="center" sx={{ mb: 2 }}>
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
          error={!!email && !emailPattern.test(email)}
          helperText={!!email && !emailPattern.test(email) ? 'Invalid email address.' : ''}
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
                    {showPassword ? <Visibility /> : <VisibilityOff />}
                  </IconButton>
                </InputAdornment>
              ),
            },
          }}
        />
        <FormControlLabel
          control={
            <Checkbox
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
              color="primary"
            />
          }
          label="Remember me"
        />
        <LoadingButton
          type="submit"
          fullWidth
          variant="contained"
          sx={{ mt: 3, mb: 2 }}
          loading={loading}
        >
          Login
        </LoadingButton>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Link href="/forgot-password">
            <Typography variant="body2" sx={{ cursor: 'pointer' }}>
              Forgot password?
            </Typography>
          </Link>
          <Link href="/signup">
            <Typography variant="body2" sx={{ cursor: 'pointer' }}>
              {"Don't have an account? Sign Up"}
            </Typography>
          </Link>
        </Box>
      </Box>
    </AuthLayout>
  );
};

export default LoginPage;