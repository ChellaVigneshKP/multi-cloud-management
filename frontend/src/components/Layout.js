import React, { useState, useEffect ,useCallback } from 'react';
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
  useMediaQuery
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

import api,{clearAccessToken} from '../api';

const drawerWidth = 240;

const Layout = ({ children }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [logoutDialogOpen, setLogoutDialogOpen] = useState(false);
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const [avatarLetter, setAvatarLetter] = useState('');
  const [avatarColor, setAvatarColor] = useState('');
  const theme = useTheme();
  const navigate = useNavigate();
  const getColorFromUsername = useCallback((username) => {
    const colors = [
      '#F44336', '#E91E63', '#9C27B0', '#673AB7', '#3F51B5',
      '#2196F3', '#03A9F4', '#00BCD4', '#009688', '#4CAF50',
      '#8BC34A', '#CDDC39', '#FFEB3B', '#FFC107', '#FF9800',
      '#FF5722', '#795548', '#A335AB', '#607D8B'
    ];
    let hash = 0;
    for (let i = 0; i < username.length; i++) {
      hash = username.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash % colors.length);
    return colors[index];
  }, []);
  useEffect(() => {
    const fetchUserData = async () => {
      try {
        // No need to get the apiToken from cookies
        const response = await api.get('/auth/userinfo');
        const { username, email } = response.data;
        setUserName(username);
        setUserEmail(email);
        setAvatarLetter(username.charAt(0).toUpperCase());
        setAvatarColor(getColorFromUsername(username));
      } catch (error) {
        console.error('Error fetching user data:', error);
        navigate('/login');
      }
    };
  
    fetchUserData();
  }, [navigate, getColorFromUsername]);  

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
  const handleLogoutConfirm = async () => {
    try {
      await api.post('/auth/logout', {}, { withCredentials: true }); // Ensure refresh token is included
  
      // Clear access token and redirect to login
      clearAccessToken();
      localStorage.clear();
      navigate('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      setLogoutDialogOpen(false);
    }
  };  

  const handleProfileClick = () => {
    handleMenuClose();
    navigate('/profile'); // This will navigate to the profile page
  };

  const handleSettingsClick = () => {
    handleMenuClose();
    navigate('/settings'); // This will navigate to the settings page
  };
  const isSmallScreen = useMediaQuery((theme) => theme.breakpoints.down('sm'));

  return (
    <Box sx={{ display: 'flex' }}>
      {/* AppBar */}
      <AppBar
        position="fixed"
        color="primary"
        sx={{
          zIndex: theme.zIndex.drawer + 1,
        }}
        role="banner"
      >
        <Toolbar>
          {/* Menu Button */}
          <IconButton
            edge="start"
            color="inherit"
            aria-label="open drawer"
            onClick={handleDrawerToggle}
            sx={{ mr: 2 }}
            size="large"
          >
            <MenuIcon />
          </IconButton>

          {/* Logo and Title */}
          <Typography
            variant="h6"
            noWrap
            component="div"
            sx={{ flexGrow: 1 ,fontFamily: `'Comfortaa', sans-serif` }}
            aria-label="Multi-Cloud Management Dashboard"
          >
            <img
              src="/logo/Logo.png"
              alt="Logo"
              style={{ height: 40, verticalAlign: 'middle', marginRight: 10 }}
            />
            {isSmallScreen ? 'C-Cloud' : 'C-Cloud <|> Centralize your Clouds'}
          </Typography>

          {/* Top Navigation Links - Visible only on medium and larger screens */}
          <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
            <Button
              color="inherit"
              startIcon={<DashboardIcon />}
              component={Link}
              to="/dashboard"
              aria-label="Navigate to Dashboard"
            >
              Dashboard
            </Button>
            <Button
              color="inherit"
              startIcon={<CloudIcon />}
              component={Link}
              to="/clouds"
              aria-label="Navigate to Clouds"
            >
              Clouds
            </Button>
          </Box>

          {/* Notification and Account Icons */}
          <IconButton
            color="inherit"
            sx={{ ml: 1 }}
            aria-label="View Notifications"
            size="large"
          >
            <Badge badgeContent={4} color="error">
              <NotificationsIcon />
            </Badge>
          </IconButton>
          <IconButton
            edge="end"
            color="inherit"
            onClick={handleMenuClick}
            sx={{ ml: 1 }}
            aria-label="Open Account Menu"
            size="large"
          >
            <Avatar sx={{ bgcolor: avatarColor }} aria-label="User Avatar">
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
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
          },
        }}
        aria-label="Side Navigation"
      >
        <Toolbar />
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            height: '100%',
            overflow: 'auto',
          }}
          role="navigation"
          aria-label="Main Navigation Links"
        >
          <List>
            <ListItemButton
              component={Link}
              to="/dashboard"
              onClick={handleDrawerToggle}
              aria-label="Navigate to Dashboard"
            >
              <DashboardIcon sx={{ mr: 2 }} aria-hidden="true" />
              <ListItemText primary="Dashboard" />
            </ListItemButton>
            <ListItemButton
              component={Link}
              to="/vms"
              onClick={handleDrawerToggle}
              aria-label="Navigate to VMs"
            >
              <DesktopWindowsIcon sx={{ mr: 2 }} aria-hidden="true" />
              <ListItemText primary="VMs" />
            </ListItemButton>
            <ListItemButton
              component={Link}
              to="/cost-manager"
              onClick={handleDrawerToggle}
              aria-label="Navigate to Cost Manager"
            >
              <AttachMoneyIcon sx={{ mr: 2 }} aria-hidden="true" />
              <ListItemText primary="Cost Manager" />
            </ListItemButton>
            <ListItemButton
              component={Link}
              to="/alerts"
              onClick={handleDrawerToggle}
              aria-label="Navigate to Monitoring & Alerts"
            >
              <NotificationImportantIcon sx={{ mr: 2 }} aria-hidden="true" />
              <ListItemText primary="Monitoring & Alerts" />
            </ListItemButton>
            {/* Clouds button - Visible only on small screens */}
            <ListItemButton
              component={Link}
              to="/clouds"
              onClick={handleDrawerToggle}
              sx={{ display: { xs: 'flex', md: 'none' } }}
              aria-label="Navigate to Clouds"
            >
              <CloudIcon sx={{ mr: 2 }} aria-hidden="true" />
              <ListItemText primary="Clouds" />
            </ListItemButton>
          </List>
          {/* Spacer to push content to the bottom */}
          <Box sx={{ flexGrow: 1 }} />
          {/* Footer */}
          <Box sx={{ p: 2, backgroundColor: theme.palette.grey[100] }}>
            <Box display="flex" flexDirection="column" alignItems="center">
              <Typography
                variant="body2"
                color="textSecondary"
                align="center"
                sx={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center' }}
              >
                <span style={{ marginRight: 4 }}>©</span>
                {' '}
                {new Date().getFullYear()}
                <MuiLink color="primary" href="https://chellavignesh.com/" underline="hover" sx={{ fontWeight: 'bold', ml: 0.5 }}>
                  chellavignesh.com
                </MuiLink>
              </Typography>
              <Typography variant="caption" color="textSecondary" align="center" sx={{ mt: 0.5 }}>
              Centralize Your Clouds. <br/>Streamline Your Management.
              </Typography>
            </Box>
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
        aria-label="Account Menu"
      >
        {/* Enhanced Profile Menu */}
        <Box sx={{ p: 2, display: 'flex', alignItems: 'center' }}>
          <Avatar sx={{ bgcolor: avatarColor, mr: 2 }} aria-hidden="true">
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
        <MenuItem onClick={handleProfileClick} aria-label="Navigate to Profile">
          <PersonIcon sx={{ mr: 1 }} aria-hidden="true" />
          Profile
        </MenuItem>
        <MenuItem onClick={handleSettingsClick} aria-label="Navigate to Settings">
          <SettingsIcon sx={{ mr: 1 }} aria-hidden="true" />
          Settings
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogoutClick} aria-label="Logout">
          <LogoutIcon sx={{ mr: 1 }} aria-hidden="true" />
          Logout
        </MenuItem>
      </Menu>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          mt: 8, // Adjusted for AppBar height
        }}
        aria-label="Main Content Area"
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
          <Button onClick={handleLogoutDialogClose} color="primary" aria-label="Cancel Logout">
            Cancel
          </Button>
          <Button onClick={handleLogoutConfirm} color="secondary" autoFocus aria-label="Confirm Logout">
            Logout
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Layout;
