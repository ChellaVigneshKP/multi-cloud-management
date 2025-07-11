'use client';

import React, { useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Avatar,
  Typography,
  List,
  ListItem,
  ListItemText,
  Button,
  Grid,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import EditIcon from '@mui/icons-material/Edit';
import LanguageIcon from '@mui/icons-material/Language';
import LinkedInIcon from '@mui/icons-material/LinkedIn';
import TwitterIcon from '@mui/icons-material/Twitter';
import GitHubIcon from '@mui/icons-material/GitHub';
import Layout from '@/components/layouts/Layout';

const ProfilePage = () => {
  const theme = useTheme();

  useEffect(() => {
    document.title = 'C-Cloud | Profile';
  }, []);

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        {/* Profile Header */}
        <Card sx={{ mb: 3, p: 2, display: 'flex', alignItems: 'center' }}>
          <Avatar
            sx={{
              bgcolor: theme.palette.primary.main,
              width: 80,
              height: 80,
              mr: 2,
            }}
          >
            <AccountCircleIcon fontSize="large" />
          </Avatar>
          <Box>
            <Typography variant="h5">John Doe</Typography>
            <Typography variant="subtitle1" color="text.secondary">
              Software Engineer
            </Typography>
            <Box display="flex" gap={1} mt={1}>
              <Button variant="contained" color="primary">Message</Button>
              <Button variant="outlined" color="primary" startIcon={<EditIcon />}>Edit Profile</Button>
            </Box>
          </Box>
        </Card>

        <Grid container spacing={3}>
          {/* Personal Details */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader title="Personal Details" />
              <CardContent>
                <List>
                  <ListItem>
                    <ListItemText primary="Bio" />
                    <Typography variant="body2" color="text.secondary">
                      Passionate developer with experience in building scalable web applications and cloud solutions.
                    </Typography>
                  </ListItem>
                  <ListItem>
                    <ListItemText primary="Website" />
                    <Button
                      startIcon={<LanguageIcon />}
                      href="https://johndoe.com"
                      color="primary"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      johndoe.com
                    </Button>
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Social Links */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader title="Social Links" />
              <CardContent>
                <List>
                  <ListItem>
                    <Button
                      startIcon={<LinkedInIcon />}
                      href="https://linkedin.com/in/johndoe"
                      color="primary"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      LinkedIn
                    </Button>
                  </ListItem>
                  <ListItem>
                    <Button
                      startIcon={<TwitterIcon />}
                      href="https://twitter.com/johndoe"
                      color="primary"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      Twitter
                    </Button>
                  </ListItem>
                  <ListItem>
                    <Button
                      startIcon={<GitHubIcon />}
                      href="https://github.com/johndoe"
                      color="primary"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      GitHub
                    </Button>
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>
    </Layout>
  );
};

export default ProfilePage;