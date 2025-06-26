'use client';

import React, { useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  List,
  ListItem,
  ListItemText,
  Switch,
  FormControlLabel,
  Grid,
} from '@mui/material';
import Layout from '@/components/layouts/Layout';

const SettingsPage = () => {
  useEffect(() => {
    document.title = 'C-Cloud | Settings';
  }, []);

  return (
    <Layout>
      <Box sx={{ p: 3 }}>
        <Grid container spacing={3}>
          {/* Account Settings */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader title="Account Settings" />
              <CardContent>
                <List>
                  <ListItem>
                    <ListItemText primary="Email Notifications" />
                    <FormControlLabel control={<Switch defaultChecked />} label="Enabled" />
                  </ListItem>
                  <ListItem>
                    <ListItemText primary="Two-Factor Authentication" />
                    <FormControlLabel control={<Switch />} label="Disabled" />
                  </ListItem>
                  <ListItem>
                    <ListItemText primary="Account Privacy" />
                    <FormControlLabel control={<Switch />} label="Public" />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Notification Settings */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader title="Notification Settings" />
              <CardContent>
                <List>
                  <ListItem>
                    <ListItemText primary="Push Notifications" />
                    <FormControlLabel control={<Switch defaultChecked />} label="Enabled" />
                  </ListItem>
                  <ListItem>
                    <ListItemText primary="SMS Notifications" />
                    <FormControlLabel control={<Switch />} label="Disabled" />
                  </ListItem>
                  <ListItem>
                    <ListItemText primary="Email Reports" />
                    <FormControlLabel control={<Switch defaultChecked />} label="Enabled" />
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

export default SettingsPage;