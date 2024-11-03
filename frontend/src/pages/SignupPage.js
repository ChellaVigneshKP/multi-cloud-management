import React, { useState,useEffect } from 'react';
import { LockOutlined as LockOutlinedIcon } from '@mui/icons-material';
import TextField from '@mui/material/TextField';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import { useNavigate } from 'react-router-dom';
import api from '../api';
import AuthLayout from '../components/AuthLayout';
import LoadingButton from '../components/LoadingButton';

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const SignupPage = () => {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [touchedEmail, setTouchedEmail] = useState(false);

  const navigate = useNavigate();
  useEffect(() => {
    document.title = 'C-Cloud | Signup';
  }, []);
  const validateForm = () => {
    if (
      !firstName.trim() ||
      !lastName.trim() ||
      !username.trim() ||
      !email.trim() ||
      !password.trim() ||
      !confirmPassword.trim()
    ) {
      setError('All fields are required.');
      return false;
    }
    if (username.length < 3) {
      setError('Username must be at least 3 characters long.');
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
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return false;
    }
    setError('');
    return true;
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!validateForm()) return;

    const formData = { firstName, lastName, username, email, password };
    setLoading(true);
    setError('');
    setSuccess('');

    api
      .post('/auth/signup', formData)
      .then((res) => {
        setSuccess('Sign Up successful! Please verify your email.');
        setError('');
        setLoading(false);
        setTimeout(() => {
          navigate(`/verify?email=${encodeURIComponent(email)}`);
        }, 2000);
      })
      .catch((err) => {
        setLoading(false);
        const errorMessage =
          err.response?.data?.message || 'An error occurred. Please try again.';
        setError(errorMessage);
      });
  };

  return (
    <AuthLayout
      avatarIcon={<LockOutlinedIcon />}
      title="Sign Up"
      description="" // Empty description
    >
      <Box
        component="form"
        noValidate
        onSubmit={handleSubmit}
        sx={{ mt: 3, width: '100%' }}
      >
        {error && (
          <Typography
            color="error"
            variant="body2"
            align="center"
            sx={{ mb: 2 }}
          >
            {error}
          </Typography>
        )}
        {success && (
          <Typography
            color="success.main"
            variant="body2"
            align="center"
            sx={{ mb: 2 }}
          >
            {success}
          </Typography>
        )}
        <Grid container rowSpacing={2} columnSpacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="firstName"
              label="First Name"
              name="firstName"
              autoComplete="given-name"
              autoFocus
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="lastName"
              label="Last Name"
              name="lastName"
              autoComplete="family-name"
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
            />
          </Grid>
        </Grid>
        <TextField
          margin="normal"
          required
          fullWidth
          id="username"
          label="Username"
          name="username"
          autoComplete="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          id="email"
          label="Email Address"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          onBlur={() => setTouchedEmail(true)}
          helperText={
            touchedEmail && !emailPattern.test(email) && 'Invalid email address.'
          }
          error={touchedEmail && !emailPattern.test(email)}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="password"
          label="Password"
          type="password"
          id="password"
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <TextField
          margin="normal"
          required
          fullWidth
          name="confirmPassword"
          label="Confirm Password"
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
          Sign Up
        </LoadingButton>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Link href="/login" variant="body2">
            Already have an account? Sign In
          </Link>
        </Box>
      </Box>
    </AuthLayout>
  );
};

export default SignupPage;