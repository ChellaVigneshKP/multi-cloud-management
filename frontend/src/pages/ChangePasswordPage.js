// src/pages/ChangePasswordPage.js
import React, { useState, useEffect, useRef } from 'react';
import { LockReset as LockResetIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../api';
import AuthLayout from '../components/AuthLayout';
import LoadingButton from '../components/LoadingButton';

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
};

const ChangePasswordPage = () => {
  const query = useQuery();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(10);
  const isMounted = useRef(true);

  useEffect(() => {
    return () => {
      isMounted.current = false;
    };
  }, []);
  

  useEffect(() => {
    const emailParam = query.get('email') || '';
    setEmail(emailParam);
  }, [query]);

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

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await api.post('/auth/take-action', { email });

      if (response.status === 200) {
        setSuccess(response.data.message);
        setError('');
        setLoading(false);
        setCountdown(10);
      } else {
        throw new Error(response.data.message || 'An error occurred. Please try again.');
      }
    } catch (error) {
      console.error('Error:', error);
      setError(error.response?.data?.message || error.message || 'Failed to send reset link. Please try again.');
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      avatarIcon={<LockResetIcon />}
      title="Change Password"
      description="Click the button below to request a password reset link."
    >
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
            disabled={loading}
          />
        )}
        {!success && (
          <LoadingButton
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            loading={loading}
          >
            Request Change Password
          </LoadingButton>
        )}
        {!success && (
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <Link href="/login" variant="body2">
              Back to Login
            </Link>
          </Box>
        )}
      </Box>
    </AuthLayout>
  );
};

export default ChangePasswordPage;
