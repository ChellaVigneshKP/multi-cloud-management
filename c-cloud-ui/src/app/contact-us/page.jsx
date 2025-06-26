'use client';
import React, { useState } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  Snackbar,
  Alert,
  Grid
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import FacebookIcon from '@mui/icons-material/Facebook';
import TwitterIcon from '@mui/icons-material/Twitter';
import InstagramIcon from '@mui/icons-material/Instagram';
import LinkedInIcon from '@mui/icons-material/LinkedIn';
import HomeIcon from '@mui/icons-material/Home';
import Link from 'next/link';
import Copyright from '@/components/Copyright';
import { keyframes } from '@emotion/react';

export default function ContactUsPage() {
  const [form, setForm] = useState({
    name: '', email: '', subject: '', message: ''
  });
  const socialLinks = [
    {
      name: 'facebook',
      url: 'https://www.facebook.com/profile.php?id=100008159434260',
      icon: FacebookIcon,
      color: '#4267B2',
    },
    {
      name: 'twitter',
      url: 'https://x.com/ChellaVignesh6',
      icon: TwitterIcon,
      color: '#1DA1F2',
    },
    {
      name: 'instagram',
      url: 'https://www.instagram.com/c_v_kp',
      icon: InstagramIcon,
      color: '#E1306C',
    },
    {
      name: 'linkedin',
      url: 'https://www.linkedin.com/in/chella-vignesh-k-p',
      icon: LinkedInIcon,
      color: '#0077B5',
    },
  ];

  const [open, setOpen] = useState(false);
  const [error, setError] = useState(false);
  const [emailError, setEmailError] = useState(false);
  const fadeInUp = keyframes`
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
`;

  const handleChange = e => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    if (name === 'email') {
      const valid = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(value);
      setEmailError(!valid);
    }
  };

  const handleSubmit = e => {
    e.preventDefault();
    if (emailError) return setError(true);
    console.log(form);
    setOpen(true);
    setForm({ name: '', email: '', subject: '', message: '' });
  };

  return (
    <Box sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      flexDirection: 'column',
      position: 'relative',
      p: 3,
      overflowX: 'hidden',
      overflowY: 'hidden',
      background: 'linear-gradient(135deg, #e8f0f2, #c1d5df)',
    }}>
      {/* Background Blur Circles */}
      <Box sx={{
        position: 'absolute', top: '20%', left: '5%',
        width: 300, height: 300, backgroundColor: '#5fb0d3',
        borderRadius: '50%', filter: 'blur(100px)', opacity: 0.5, zIndex: 0
      }} />
      <Box sx={{
        position: 'absolute', bottom: '-20%', right: '-30%',
        width: 300, height: 300, backgroundColor: '#d0e8f2',
        borderRadius: '50%', filter: 'blur(100px)', opacity: 0.5, zIndex: 0
      }} />

      {/* Home Button */}
      <Box sx={{ position: 'absolute', top: 20, left: 20, zIndex: 1 }}>
        <Link href="/" passHref>
          <Button variant="outlined" sx={{
            width: 40, height: 40, minWidth: 0, borderRadius: '50%',
            color: '#00445d', borderColor: '#00445d',
            '&:hover': { backgroundColor: '#e8f0f2' }
          }}>
            <HomeIcon />
          </Button>
        </Link>
      </Box>

      {/* Title */}
      <Typography variant="h3" sx={{ color: '#00445d', fontWeight: 'bold', mb: 1, zIndex: 1 }}>
        Get in Touch
      </Typography>
      <Typography variant="body1" sx={{
        color: '#00445d', mb: 3, textAlign: 'center',
        maxWidth: 500, zIndex: 1
      }}>
        We'd love to hear from you! Please fill out the form below, and we'll get back to you shortly.
      </Typography>

      {/* Form */}
      <Box component="form" onSubmit={handleSubmit} sx={{
        backgroundColor: '#fff', borderRadius: 2, boxShadow: 3,
        p: { xs: 2, sm: 4 }, maxWidth: 600, width: '100%', zIndex: 1, animation: `${fadeInUp} 0.6s ease-out`
      }}>
        <Grid container spacing={2}>
          {[
            { name: 'name', label: 'Name' },
            { name: 'email', label: 'Email', type: 'email', error: emailError },
            { name: 'subject', label: 'Subject' },
          ].map((f) => (
            <Grid key={f.name} size={12}>
              <TextField
                fullWidth variant="outlined"
                name={f.name}
                type={f.type || 'text'}
                label={f.label}
                required
                value={form[f.name]}
                onChange={handleChange}
                error={!!f.error}
                helperText={f.name === 'email' && emailError ? 'Enter a valid email' : ''}
                sx={{ backgroundColor: '#f9f9f9', borderRadius: '4px' }}
              />
            </Grid>
          ))}
          <Grid size={12}>
            <TextField
              fullWidth variant="outlined" required multiline rows={4}
              name="message" label="Message"
              value={form.message}
              onChange={handleChange}
              sx={{ backgroundColor: '#f9f9f9', borderRadius: '4px' }}
            />
          </Grid>
          <Grid size={12} sx={{ textAlign: 'center' }}>
            <Button type="submit" variant="contained" endIcon={<SendIcon />} sx={{
              background: 'linear-gradient(45deg, #00445d, #006880)',
              color: '#fff', py: 1.2, px: 4,
              '&:hover': { background: 'linear-gradient(45deg, #006880, #00445d)' }
            }}>
              Send Message
            </Button>
          </Grid>
        </Grid>
      </Box>

      {/* Snackbars */}
      <Snackbar open={open} autoHideDuration={6000} onClose={() => setOpen(false)}>
        <Alert onClose={() => setOpen(false)} severity="success">Message sent!</Alert>
      </Snackbar>
      <Snackbar open={error} autoHideDuration={6000} onClose={() => setError(false)}>
        <Alert onClose={() => setError(false)} severity="error">Invalid email address.</Alert>
      </Snackbar>

      {/* Footer */}
      <Box sx={{ mt: 'auto', zIndex: 1, pt: 4, width: '100%', px: 2 }}>
        <Box
          display="flex"
          flexDirection={{ xs: 'column', sm: 'column', md: 'row' }}
          justifyContent="center"
          alignItems="center"
          gap={2}
          textAlign="center"
        >
          <Copyright />
          <Box display="flex" justifyContent="center" gap={2}>
            {socialLinks.map(({ name, url, icon: Icon, color }) => (
              <Link key={name} href={url} target="_blank">
                <Icon sx={{
                  fontSize: 24,
                  color,
                  transition: 'transform 0.2s',
                  '&:hover': { transform: 'scale(1.1)' }
                }} />
              </Link>
            ))}
          </Box>
        </Box>
      </Box>
    </Box>
  );
}