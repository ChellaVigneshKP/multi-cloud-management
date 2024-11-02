// src/pages/ResetPasswordPage.js
import React, { useState, useEffect } from 'react';
import { LockOutlined as LockOutlinedIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../api';
import AuthLayout from '../components/AuthLayout';
import LoadingButton from '../components/LoadingButton';

const ResetPasswordPage = () => {
  const query = new URLSearchParams(useLocation().search);
  const token = query.get('token');
  const navigate = useNavigate();

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

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    const formData = { token, newPassword };
    api.post('/auth/reset-password', formData)
      .then(() => {
        setSuccess('Password has been reset successfully.');
        setError('');
        setNewPassword('');
        setConfirmPassword('');

        setTimeout(() => {
          navigate('/login');
        }, 2000);
      })
      .catch(err => {
        const errorMessage = err.response?.data?.message || 'An error occurred. Please try again.';
        setError(errorMessage);
        setSuccess('');
        setLoading(false);
      });
  };

  return (
    <AuthLayout
      leftImage="images/output.jpg"
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
          <Link href="/login" variant="body2">
            Back to Login
          </Link>
        </Box>
      </Box>
    </AuthLayout>
  );
};

export default ResetPasswordPage;
