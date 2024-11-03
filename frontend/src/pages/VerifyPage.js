import React, { useState, useEffect } from 'react';
import { MailOutline as MailOutlineIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { useLocation, useNavigate } from 'react-router-dom';
import api from '../api';
import AuthLayout from '../components/AuthLayout';
import LoadingButton from '../components/LoadingButton';

const VerifyPage = () => {
  const [verificationCode, setVerificationCode] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  const query = new URLSearchParams(useLocation().search);
  const email = query.get('email');
  const navigate = useNavigate();
  useEffect(() => {
    document.title = 'C-Cloud | Verify Email';
  }, []);
  const handleVerify = (event) => {
    event.preventDefault();

    if (!verificationCode.trim()) {
      setError('Verification code is required.');
      return;
    }

    setLoading(true);
    setError('');
    setMessage('');

    api.post('/auth/verify', { email, verificationCode })
      .then(() => {
        setMessage('Verification successful! Redirecting to login...');
        setError('');
        setLoading(false);
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      })
      .catch(err => {
        setLoading(false);
        const errorMessage = err.response?.data?.message || 'Verification failed. Please try again.';
        setError(errorMessage);

        if (errorMessage === 'User already verified') {
          setTimeout(() => {
            navigate('/login');
          }, 2000);
        }
      });
  };

  const handleResendCode = () => {
    setResending(true);
    setError('');
    setMessage('');

    api.post('/auth/resend', { email })
      .then(() => {
        setMessage('Verification code resent. Please check your email.');
        setResending(false);
      })
      .catch(err => {
        setResending(false);
        const errorMessage = err.response?.data?.message || 'Failed to resend verification code. Please try again.';
        setError(errorMessage);
      });
  };

  return (
    <AuthLayout
      avatarIcon={<MailOutlineIcon />}
      title="Verify Your Email"
      description={`A verification code has been sent to ${email}.`}
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
          onClick={() => navigate('/login')}
          loading={false}
        >
          Back to Login
        </LoadingButton>
      </Box>
    </AuthLayout>
  );
};

export default VerifyPage;
