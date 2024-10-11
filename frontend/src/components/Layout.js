import React, { useState, useEffect } from 'react';
import {
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Menu,
  MenuItem,
  Button,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Box,
  useTheme,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  Avatar,
  Badge,
  Link as MuiLink,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Notifications as NotificationsIcon,
  Settings as SettingsIcon,
  ExitToApp as LogoutIcon,
  Person as PersonIcon,
  Dashboard as DashboardIcon,
  Cloud as CloudIcon,
  DesktopWindows as DesktopWindowsIcon,
  AttachMoney as AttachMoneyIcon,
  NotificationImportant as NotificationImportantIcon,
} from '@mui/icons-material';
import { Link, useNavigate } from 'react-router-dom';
import Cookies from 'js-cookie';
import axios from 'axios'; // Import axios to make API requests

const drawerWidth = 240;

const Layout = ({ children }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [logoutDialogOpen, setLogoutDialogOpen] = useState(false);
  const [userName, setUserName] = useState(''); // State to store user's name
  const [userEmail, setUserEmail] = useState(''); // State to store user's email
  const [avatarLetter, setAvatarLetter] = useState(''); // State to store the first letter for the avatar
  const theme = useTheme();
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch user data when the component mounts
    const fetchUserData = async () => {
      try {
        const apiToken = Cookies.get('apiToken');
        if (!apiToken) {
          console.error('API Token is missing');
          return;
        }
        const response = await axios.get('http://localhost:6061/auth/userinfo', {
          headers: {
            Authorization: `Bearer ${apiToken}`,
          },
        });
        const { username, email } = response.data;
        setUserName(username);
        setUserEmail(email);
        setAvatarLetter(username.charAt(0).toUpperCase());
      } catch (error) {
        console.error('Error fetching user data:', error);
      }
    };

    fetchUserData();
  }, []);

  const handleMenuClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  const handleLogoutClick = () => {
    setLogoutDialogOpen(true);
    handleMenuClose();
  };

  const handleLogoutDialogClose = () => {
    setLogoutDialogOpen(false);
  };

  const handleLogoutConfirm = () => {
    Cookies.remove('apiToken');
    localStorage.clear();
    setLogoutDialogOpen(false);
    navigate('/login');
  };

  return (
    <Box sx={{ display: 'flex' }}>
      {/* AppBar */}
      <AppBar
        position="fixed"
        color="primary"
        sx={{
          zIndex: theme.zIndex.drawer + 1,
        }}
      >
        <Toolbar>
          {/* Menu Button */}
          <IconButton
            edge="start"
            color="inherit"
            aria-label="open drawer"
            onClick={handleDrawerToggle}
            sx={{ mr: 2 }}
          >
            <MenuIcon />
          </IconButton>

          {/* Logo and Title */}
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            <img
              src="/images/logo.png"
              alt="Logo"
              style={{ height: 30, verticalAlign: 'middle', marginRight: 10 }}
            />
            Multi-Cloud Management
          </Typography>

          {/* Top Navigation Links */}
          <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
            <Button
              color="inherit"
              startIcon={<DashboardIcon />}
              component={Link}
              to="/dashboard"
            >
              Dashboard
            </Button>
            <Button
              color="inherit"
              startIcon={<CloudIcon />}
              component={Link}
              to="/clouds"
            >
              Clouds
            </Button>
          </Box>

          {/* Notification and Account Icons */}
          <IconButton color="inherit" sx={{ ml: 1 }}>
            <Badge badgeContent={4} color="error">
              <NotificationsIcon />
            </Badge>
          </IconButton>
          <IconButton
            edge="end"
            color="inherit"
            onClick={handleMenuClick}
            sx={{ ml: 1 }}
          >
            <Avatar sx={{ bgcolor: theme.palette.secondary.main }}>
              {avatarLetter}
            </Avatar>
          </IconButton>
        </Toolbar>
      </AppBar>

      {/* Drawer */}
      <Drawer
        variant="temporary"
        open={drawerOpen}
        onClose={handleDrawerToggle}
        sx={{
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
          },
        }}
      >
        <Toolbar />
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            height: '100%',
            overflow: 'auto',
          }}
        >
          <List>
            <ListItemButton
              component={Link}
              to="/dashboard"
              onClick={handleDrawerToggle}
            >
              <DashboardIcon sx={{ mr: 2 }} />
              <ListItemText primary="Dashboard" />
            </ListItemButton>
            <ListItemButton
              component={Link}
              to="/vms"
              onClick={handleDrawerToggle}
            >
              <DesktopWindowsIcon sx={{ mr: 2 }} />
              <ListItemText primary="VMs" />
            </ListItemButton>
            <ListItemButton
              component={Link}
              to="/cost-manager"
              onClick={handleDrawerToggle}
            >
              <AttachMoneyIcon sx={{ mr: 2 }} />
              <ListItemText primary="Cost Manager" />
            </ListItemButton>
            <ListItemButton
              component={Link}
              to="/alerts"
              onClick={handleDrawerToggle}
            >
              <NotificationImportantIcon sx={{ mr: 2 }} />
              <ListItemText primary="Monitoring & Alerts" />
            </ListItemButton>
          </List>
          {/* Spacer to push content to the bottom */}
          <Box sx={{ flexGrow: 1 }} />
          {/* Copyright Message */}
          <Box sx={{ p: 2 }}>
            <Typography variant="body2" color="textSecondary" align="center">
              {'Copyright © '}
              <MuiLink color="inherit" href="https://chellavignesh.com/">
                chellavignesh.com
              </MuiLink>{' '}
              {new Date().getFullYear()}.
            </Typography>
          </Box>
        </Box>
      </Drawer>

      {/* Account Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        {/* Enhanced Profile Menu */}
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
          <Avatar sx={{ bgcolor: theme.palette.secondary.main, mr: 2 }}>
            {avatarLetter}
          </Avatar>
          <Box>
            <Typography variant="subtitle1">{userName}</Typography>
            <Typography variant="body2" color="textSecondary">
              {userEmail}
            </Typography>
          </Box>
        </Box>
        <Divider />
        <MenuItem onClick={handleMenuClose}>
          <PersonIcon sx={{ mr: 1 }} />
          Profile
        </MenuItem>
        <MenuItem onClick={handleMenuClose}>
          <SettingsIcon sx={{ mr: 1 }} />
          Settings
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogoutClick}>
          <LogoutIcon sx={{ mr: 1 }} />
          Logout
        </MenuItem>
      </Menu>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          mt: 8,
        }}
      >
        {children}
      </Box>

      {/* Logout Confirmation Dialog */}
      <Dialog
        open={logoutDialogOpen}
        onClose={handleLogoutDialogClose}
        aria-labelledby="logout-dialog-title"
        aria-describedby="logout-dialog-description"
      >
        <DialogTitle id="logout-dialog-title">{'Confirm Logout'}</DialogTitle>
        <DialogContent>
          <DialogContentText id="logout-dialog-description">
            Are you sure you want to logout?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleLogoutDialogClose} color="primary">
            Cancel
          </Button>
          <Button onClick={handleLogoutConfirm} color="secondary" autoFocus>
            Logout
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Layout;
