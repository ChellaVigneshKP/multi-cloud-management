import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, IconButton, Menu, MenuItem, Button, Drawer, List, ListItemButton, ListItemText, Box, useTheme, useMediaQuery, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';
import { Menu as MenuIcon, AccountCircle, Notifications as NotificationsIcon, Settings as SettingsIcon, ExitToApp as LogoutIcon, Person as PersonIcon, Dashboard as DashboardIcon, Cloud as CloudIcon, DesktopWindows as DesktopWindowsIcon, AttachMoney as AttachMoneyIcon, NotificationImportant as NotificationImportantIcon } from '@mui/icons-material';
import { Link , useNavigate} from 'react-router-dom';
import Cookies from 'js-cookie';

const Layout = ({ children }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [logoutDialogOpen, setLogoutDialogOpen] = useState(false);
  const theme = useTheme();
  const isSmallScreen = useMediaQuery(theme.breakpoints.down('sm'));
  const navigate = useNavigate();
  const handleMenuClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  const handleDrawerClose = () => {
    setDrawerOpen(false);
  };
  const handleLogoutClick = () => {
    setLogoutDialogOpen(true);
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
      <AppBar
        position="fixed"
        sx={{
          width: { sm: drawerOpen ? `calc(100% - 240px)` : '100%' },
          ml: { sm: drawerOpen ? '240px' : '0' },
          transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.leavingScreen,
          }),
        }}
      >
        <Toolbar>
          <IconButton
            edge="start"
            color="inherit"
            aria-label="menu"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { xs: 'block', sm: 'block' } }}
          >
            <MenuIcon />
          </IconButton>

          <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1 }}>
            <Typography
              variant="h6"
              component="div"
              sx={{ flexGrow: 1, fontSize: { xs: '1rem', sm: '1.5rem' } }}
            >
              Multi-Cloud Management
            </Typography>

            <Box sx={{ display: { xs: 'none', sm: 'flex' }, ml: 2 }}>
              <Button color="inherit" startIcon={<DashboardIcon />} component={Link} to="/dashboard">Dashboard</Button>
              <Button color="inherit" startIcon={<CloudIcon />} component={Link} to="/clouds">Clouds</Button>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <IconButton color="inherit" sx={{ ml: 2 }}>
              <NotificationsIcon />
            </IconButton>
            <IconButton
              edge="end"
              color="inherit"
              onClick={handleMenuClick}
              sx={{ ml: 2 }}
            >
              <AccountCircle />
            </IconButton>
          </Box>
        </Toolbar>
      </AppBar>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleMenuClose}>
          <PersonIcon sx={{ mr: 1 }} />
          My Account
        </MenuItem>
        <MenuItem onClick={handleMenuClose}>
          <SettingsIcon sx={{ mr: 1 }} />
          Settings
        </MenuItem>
        <MenuItem onClick={handleLogoutClick}>
          <LogoutIcon sx={{ mr: 1 }} />
          Logout
        </MenuItem>
      </Menu>
      <Drawer
        anchor="left"
        open={drawerOpen}
        onClose={handleDrawerClose}
        variant={isSmallScreen ? 'temporary' : 'persistent'}
        sx={{
          '& .MuiDrawer-paper': {
            width: 240,
            height: '100%',
            top: 0,
            position: isSmallScreen ? 'absolute' : 'fixed',
            transition: theme.transitions.create('transform', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.leavingScreen,
            }),
            transform: isSmallScreen ? 'translateX(0)' : 'translateX(0)',
          },
        }}
      >
        <Box
          role="presentation"
          onClick={handleDrawerClose}
          onKeyDown={handleDrawerClose}
        >
          <List>
            <ListItemButton component={Link} to="/dashboard">
              <DashboardIcon sx={{ mr: 1 }} />
              <ListItemText primary="Dashboard" />
            </ListItemButton>
            <ListItemButton component={Link} to="/vms">
              <DesktopWindowsIcon sx={{ mr: 1 }} />
              <ListItemText primary="VMs" />
            </ListItemButton>
            <ListItemButton component={Link} to="/cost-manager">
              <AttachMoneyIcon sx={{ mr: 1 }} />
              <ListItemText primary="Cost Manager" />
            </ListItemButton>
            <ListItemButton component={Link} to="/alerts">
              <NotificationImportantIcon sx={{ mr: 1 }} />
              <ListItemText primary="Monitoring and Alerts" />
            </ListItemButton>
          </List>
        </Box>
      </Drawer>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          mt: 8,
          ml: { sm: drawerOpen ? '240px' : '0' },
          transition: theme.transitions.create(['margin', 'width'], {
            easing: theme.transitions.easing.easeOut,
            duration: theme.transitions.duration.enteringScreen,
          }),
        }}
      >
        {children}
      </Box>
      <Dialog
        open={logoutDialogOpen}
        onClose={handleLogoutDialogClose}
        aria-labelledby="logout-dialog-title"
        aria-describedby="logout-dialog-description"
      >
        <DialogTitle id="logout-dialog-title">
          {"Confirm Logout"}
        </DialogTitle>
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
