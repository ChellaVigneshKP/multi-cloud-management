'use client';

import React, { useState, useEffect, useContext } from 'react';
import { MailOutline as MailOutlineIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useSearchParams, useRouter } from 'next/navigation';
import AuthLayout from '@/components/layouts/AuthLayout';
import LoadingButton from '@/components/layouts/LoadingButton';
import { AuthContext } from '@/contexts/auth-context';

const VerifyPage = () => {
  const { api } = useContext(AuthContext);
  const [verificationCode, setVerificationCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  const searchParams = useSearchParams();
  const email = searchParams.get('email');
  const router = useRouter();

  useEffect(() => {
    document.title = 'C-Cloud | Verify Email';
  }, []);

  const handleVerify = async (event) => {
    event.preventDefault();

    if (!verificationCode.trim()) {
      setError('Verification code is required.');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');

    try {
      await api.post('/auth/verify', { email, verificationCode });
      setMessage('Verification successful! Redirecting to login...');
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Verification failed. Please try again.';
      setError(errorMessage);

      if (errorMessage === 'User already verified') {
        setTimeout(() => {
          router.push('/login');
        }, 2000);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    setResending(true);
    setError('');
    setMessage('');

    try {
      await api.post('/auth/resend', { email });
      setMessage('Verification code resent. Please check your email.');
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to resend verification code. Please try again.';
      setError(errorMessage);
    } finally {
      setResending(false);
    }
  };

  return (
    <AuthLayout
      avatarIcon={<MailOutlineIcon />}
      title="Verify Your Email"
      description={`A verification code has been sent to ${email || 'your email'}.`}
    >
      <Box component="form" noValidate onSubmit={handleVerify} sx={{ mt: 3, width: '100%', maxWidth: '360px' }}>
        {error && (
          <Typography color="error" variant="body2" align="center" sx={{ mb: 2 }}>
            {error}
          </Typography>
        )}
        {message && (
          <Typography color="success.main" variant="body2" align="center" sx={{ mb: 2 }}>
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
        <LoadingButton
          type="submit"
          fullWidth
          variant="contained"
          sx={{ mt: 3, mb: 2 }}
          loading={loading}
        >
          Verify
        </LoadingButton>
        <LoadingButton
          fullWidth
          variant="outlined"
          sx={{ mt: 2, mb: 2 }}
          onClick={handleResendCode}
          loading={resending}
        >
          {resending ? 'Resending...' : 'Resend Code'}
        </LoadingButton>
        <LoadingButton
          fullWidth
          variant="text"
          sx={{ mt: 2, mb: 2 }}
          onClick={() => router.push('/login')}
        >
          Back to Login
        </LoadingButton>
      </Box>
    </AuthLayout>
  );
};

export default VerifyPage;