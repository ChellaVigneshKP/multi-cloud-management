import React, { useState } from 'react';
import { Box, Typography, TextField, Button, Snackbar, Alert } from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import Copyright from '../components/Copyright';
import Grid from '@mui/material/Grid2';
import { Link } from 'react-router-dom'; // Import Link for routing
import FacebookIcon from '@mui/icons-material/Facebook';
import TwitterIcon from '@mui/icons-material/Twitter';
import InstagramIcon from '@mui/icons-material/Instagram';
import LinkedInIcon from '@mui/icons-material/LinkedIn';
import HomeIcon from '@mui/icons-material/Home';
function ContactUsPage() {
  const [formValues, setFormValues] = useState({
    name: '',
    email: '',
    subject: '',
    message: ''
  });
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const [emailError, setEmailError] = useState(false);
  const [errorSnackbar, setErrorSnackbar] = useState(false); 
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormValues({ ...formValues, [name]: value });
    if (name === 'email') {
      validateEmail(value);
    }
  };
  const validateEmail = (email) => {
    // Enhanced email regex pattern with domain and TLD requirement
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    setEmailError(!emailRegex.test(email));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (emailError) {
      setErrorSnackbar(true); // Show error alert if email is invalid
      return; // Prevent submission if email is invalid
    }
    console.log('Form Submitted:', formValues);
    setOpenSnackbar(true);
    setFormValues({ name: '', email: '', subject: '', message: '' });
  };

  const handleCloseSnackbar = () => {
    setOpenSnackbar(false);
  };
  const handleCloseErrorSnackbar = () => {
    setErrorSnackbar(false);
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #e8f0f2, #c1d5df)',
        padding: '20px',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Background Circle Decorations */}
      <Box
        sx={{
          position: 'absolute',
          top: '20%',
          left: '5%',
          width: '300px',
          height: '300px',
          backgroundColor: '#5fb0d3',
          opacity: 0.5,
          borderRadius: '50%',
          filter: 'blur(100px)',
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          bottom: '-20%',
          right: '-30%',
          width: '300px',
          height: '300px',
          backgroundColor: '#d0e8f2',
          opacity: 0.5,
          borderRadius: '50%',
          filter: 'blur(100px)',
        }}
      />
      <Box sx={{ position: 'absolute', top: '20px', left: '20px' }}>
  <Button
    component={Link}
    to="/"
    variant="outlined"
    sx={{
      minWidth: 0, // Keeps the button circular
      width: 40,
      height: 40,
      borderRadius: '50%',
      color: '#00445d',
      borderColor: '#00445d',
      '&:hover': {
        backgroundColor: '#e8f0f2',
        borderColor: '#00445d',
      },
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    }}
  >
    <HomeIcon />
  </Button>
</Box>

      <Typography
        variant="h3"
        sx={{
          color: '#00445d',
          fontFamily: 'Roboto, sans-serif',
          fontWeight: 'bold',
          marginBottom: '20px',
          textAlign: 'center',
          fontSize: { xs: '1.8rem', sm: '2.5rem' } // Responsive font size
        }}
      >
        Get in Touch
      </Typography>
      <Typography
        variant="body1"
        sx={{
          color: '#00445d',
          fontSize: { xs: '1rem', sm: '1.1rem' },
          marginBottom: '30px',
          textAlign: 'center',
          maxWidth: '500px',
        }}
      >
        We’d love to hear from you! Please fill out the form below, and we’ll get back to you shortly.
      </Typography>

      {/* Contact Form */}
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          backgroundColor: 'white',
          borderRadius: '12px',
          boxShadow: '0px 4px 10px rgba(0, 0, 0, 0.1)', // Subtle shadow for depth
          padding: '30px 40px',
          maxWidth: '600px',
          width: '100%',
          animation: 'fadeInUp 0.6s ease-in-out',
          '@media (max-width: 600px)': {
            padding: '20px 20px', // Reduce padding on smaller screens
          },
        }}
      >
        <Grid container spacing={2}>
          <Grid size={12}>
            <TextField
              fullWidth
              label="Name"
              name="name"
              value={formValues.name}
              onChange={handleChange}
              variant="outlined"
              required
              sx={{ backgroundColor: '#f9f9f9', borderRadius: '4px' }}
            />
          </Grid>
          <Grid size={12}>
            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              value={formValues.email}
              onChange={handleChange}
              variant="outlined"
              required
              error={emailError} // Set error if email is invalid
              helperText={emailError ? 'Please enter a valid email address' : ''} // Show error message
              sx={{ backgroundColor: '#f9f9f9', borderRadius: '4px' }}
            />
          </Grid>
          <Grid size={12}>
            <TextField
              fullWidth
              label="Subject"
              name="subject"
              value={formValues.subject}
              onChange={handleChange}
              variant="outlined"
              required
              sx={{ backgroundColor: '#f9f9f9', borderRadius: '4px' }}
            />
          </Grid>
          <Grid size={12}>
            <TextField
              fullWidth
              label="Message"
              name="message"
              multiline
              rows={4}
              value={formValues.message}
              onChange={handleChange}
              variant="outlined"
              required
              sx={{ backgroundColor: '#f9f9f9', borderRadius: '4px' }}
            />
          </Grid>
          <Grid size={12} sx={{ textAlign: 'center' }}>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              endIcon={<SendIcon />}
              sx={{
                padding: '10px 30px',
                background: 'linear-gradient(45deg, #00445d, #006880)',
                color: '#fff',
                '&:hover': {
                  background: 'linear-gradient(45deg, #006880, #00445d)',
                },
                boxShadow: 3,
                transition: 'transform 0.2s',
                '&:active': { transform: 'scale(0.98)' },
              }}
            >
              Send Message
            </Button>
          </Grid>
        </Grid>
      </Box>

      {/* Snackbar for Submission Success */}
      <Snackbar
        open={openSnackbar}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseSnackbar} severity="success" sx={{ width: '100%' }}>
          Your message has been sent!
        </Alert>
      </Snackbar>

      <Snackbar
        open={errorSnackbar}
        autoHideDuration={6000}
        onClose={handleCloseErrorSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseErrorSnackbar} severity="error" sx={{ width: '100%' }}>
          Invalid email address. Please check and try again.
        </Alert>
      </Snackbar>

      <Box sx={{ marginTop: '20px', width: '100%', textAlign: 'center' }}>
        <Box display="flex" alignItems="center" justifyContent="center" gap={2} sx={{ marginBottom: '10px' }}>
          {/* Logo and Copyright */}
          <Box display="flex" alignItems="center" gap={1}>
            <Copyright />
          </Box>
          {/* Social Media Icons */}
          <Box display="flex" alignItems="center" gap={2}>
            <a href="https://facebook.com/yourprofile" target="_blank" rel="noopener noreferrer">
              <FacebookIcon sx={{ color: '#4267B2', fontSize: 30, '&:hover': { color: '#365899' } }} />
            </a>
            <a href="https://twitter.com/yourprofile" target="_blank" rel="noopener noreferrer">
              <TwitterIcon sx={{ color: '#1DA1F2', fontSize: 30, '&:hover': { color: '#0d95e8' } }} />
            </a>
            <a href="https://instagram.com/yourprofile" target="_blank" rel="noopener noreferrer">
              <InstagramIcon sx={{ color: '#E1306C', fontSize: 30, '&:hover': { color: '#bc2a8d' } }} />
            </a>
            <a href="https://linkedin.com/in/yourprofile" target="_blank" rel="noopener noreferrer">
              <LinkedInIcon sx={{ color: '#0077B5', fontSize: 30, '&:hover': { color: '#005582' } }} />
            </a>
          </Box>
        </Box>
      </Box>




      {/* Animation for Form */}
      <style>
        {`
          @keyframes fadeInUp {
            from {
              opacity: 0;
              transform: translateY(20px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }
        `}
      </style>
    </Box>
  );
}

export default ContactUsPage;
