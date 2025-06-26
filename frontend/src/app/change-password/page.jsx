'use client';

import React, { useState, useEffect, useRef, useContext } from 'react';
import { LockReset as LockResetIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useSearchParams, useRouter } from 'next/navigation';
import AuthLayout from '@/components/layouts/AuthLayout';
import LoadingButton from '@/components/layouts/LoadingButton';
import { AuthContext } from '@/contexts/auth-context';
import { emailPattern } from '@/utils/validation';

const ChangePasswordPage = () => {
  const { api } = useContext(AuthContext);
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(10);
  const isMounted = useRef(true);

  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    return () => {
      isMounted.current = false;
    };
  }, []);

  useEffect(() => {
    const emailParam = searchParams.get('email') || '';
    setEmail(emailParam);
  }, [searchParams]);

  useEffect(() => {
    if (countdown > 0 && success) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    } else if (countdown === 0 && success) {
      router.push('/login');
    }
  }, [countdown, success, router]);

  const validateForm = () => {
    if (!email.trim()) {
      setError('Email is required.');
      return false;
    }
    if (!emailPattern.test(email)) {
      setError('Please enter a valid email address.');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validateForm()) return;

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await api.post('/auth/take-action', { email });

      if (response.status === 200) {
        setSuccess(response.data.message);
        setCountdown(10);
      } else {
        throw new Error(response.data.message || 'An error occurred. Please try again.');
      }
    } catch (error) {
      const message =
        error.response?.data?.message || error.message || 'Failed to send reset link. Please try again.';
      setError(message);
    } finally {
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