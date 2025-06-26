'use client';

import React, { useState, useEffect, useContext } from 'react';
import { LockOutlined as LockOutlinedIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import MuiLink from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useRouter, useSearchParams } from 'next/navigation';
import Head from 'next/head';
import AuthLayout from '@/components/layouts/AuthLayout';
import LoadingButton from '@/components/layouts/LoadingButton';
import { AuthContext } from '@/contexts/auth-context';
import Link from 'next/link';

const ResetPasswordPage = () => {
  const searchParams = useSearchParams();
  const token = searchParams.get('token');
  const router = useRouter();
  const { api } = useContext(AuthContext);

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token) {
      setError('Invalid or missing password reset token.');
    }
  }, [token]);

  const validateForm = () => {
    if (!newPassword.trim() || !confirmPassword.trim()) {
      setError('Both password fields are required.');
      return false;
    }
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.');
      return false;
    }
    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters long.');
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

    const formData = { token, newPassword };
    try {
      await api.post('/auth/reset-password', formData);
      setSuccess('Password has been reset successfully.');
      setNewPassword('');
      setConfirmPassword('');

      setTimeout(() => {
        router.push('/login');
      }, 2000);
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || 'An error occurred. Please try again.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Head>
        <title>C-Cloud | Reset Password</title>
      </Head>
      <AuthLayout
        avatarIcon={<LockOutlinedIcon />}
        title="Reset Password"
        description="Enter your new password below."
      >
        <Box component="form" noValidate onSubmit={handleSubmit} sx={{ mt: 3, width: '100%' }}>
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
            name="newPassword"
            label="New Password"
            type="password"
            id="newPassword"
            autoComplete="new-password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
          <TextField
            margin="normal"
            required
            fullWidth
            name="confirmPassword"
            label="Confirm New Password"
            type="password"
            id="confirmPassword"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />
          <LoadingButton
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 3, mb: 2 }}
            loading={loading}
          >
            {loading ? 'Resetting...' : 'Reset Password'}
          </LoadingButton>
          <Box sx={{ display: 'flex', justifyContent: 'center' }}>
            <MuiLink component={Link} href="/login" variant="body2">
              Back to Login
            </MuiLink>
          </Box>
        </Box>
      </AuthLayout>
    </>
  );
};

export default ResetPasswordPage;