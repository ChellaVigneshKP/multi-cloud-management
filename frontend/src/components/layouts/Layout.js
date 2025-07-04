import { useState, useCallback, useContext, useEffect } from 'react';
import {
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
    useMediaQuery,
    getContrastRatio
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
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { AuthContext } from '@/contexts/auth-context';
import CustomLoader from '@/components/CustomLoader';
import PropTypes from 'prop-types';
import Image from 'next/image';
import logo from '@/assets/images/logo/logo.png';
const drawerWidth = 240;
const Layout = ({ children }) => {
    // Hooks
    const [anchorEl, setAnchorEl] = useState(null);
    const { user, isAuthenticated, logout } = useContext(AuthContext);
    const [drawerOpen, setDrawerOpen] = useState(false);
    const [logoutDialogOpen, setLogoutDialogOpen] = useState(false);
    const [hasRedirected, setHasRedirected] = useState(false);
    const router = useRouter();
    const theme = useTheme();
    const isSmallScreen = useMediaQuery((theme) => theme.breakpoints.down('sm'));
    const notifications = [];

    // Functions
    const getColorFromUsername = useCallback((username) => {
        const colors = [
            '#7b1ca3', '#5c6cc3', '#ec6c04', '#048c7c', '#044c3c',
            '#c2175b', '#512da7', '#5d4038', '#78909c', '#0098a6',
            '#846c64', '#689f39', '#0288d0', '#FFC107', '#FF9800',
            '#FF5722', '#795548', '#A335AB', '#346b1d',
        ];
        let hash = 0;
        for (let i = 0; i < username.length; i++) {
            hash = username.charCodeAt(i) + ((hash << 5) - hash);
        }
        const index = Math.abs(hash % colors.length);
        return colors[index];
    }, []);

    // Variables dependent on hooks and functions
    const avatarLetter = user?.username?.charAt(0).toUpperCase() || '';
    const avatarColor = getColorFromUsername(user?.username || '');
    const textColor = getContrastRatio(avatarColor, '#fff') < 4.5 ? '#000' : '#fff';
    const userName = user?.username || '';
    const userEmail = user?.email || '';

    // Conditional returns
    useEffect(() => {
        if (isAuthenticated === false && !hasRedirected) {
            setHasRedirected(true);
            router.push('/login');
        }
    }, [isAuthenticated, router, hasRedirected]);

    if (isAuthenticated === null || (isAuthenticated === false && !hasRedirected)) {
        return <CustomLoader />;
    }

    // Handlers
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
            await logout(); // Use logout from AuthContext
        } catch {
            return;
        } finally {
            setLogoutDialogOpen(false);
        }
    };

    const handleProfileClick = () => {
        handleMenuClose();
        router.push('/profile');
    };

    const handleSettingsClick = () => {
        handleMenuClose();
        router.push('/settings');
    };

    // Component JSX
    return (
        <Box sx={{ display: 'flex' }}>
            {/* AppBar */}
            <header
                style={{
                    position: 'fixed',
                    width: '100%',
                    zIndex: theme.zIndex.drawer + 1,
                    backgroundColor: theme.palette.primary.main,
                    color: theme.palette.primary.contrastText,
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
                        size="large"
                    >
                        <MenuIcon />
                    </IconButton>

                    {/* Logo and Title */}
                    <Box
                        sx={{
                            flexGrow: 1,
                            display: 'flex',
                            alignItems: 'center',
                            fontFamily: `'Comfortaa', sans-serif`,
                        }}
                        aria-label="Multi-Cloud Management Dashboard"
                    >
                        <Image
                            src={logo}
                            alt="Logo"
                            width={35}
                            height={35}
                            style={{ marginRight: 10 }}
                        />
                        <Typography variant="h6" noWrap component="div" sx={{ lineHeight: 1.2 }}>
                            C-Cloud {!isSmallScreen && ' <|> Centralize your Clouds'}
                        </Typography>
                    </Box>


                    {/* Top Navigation Links - Visible only on medium and larger screens */}
                    <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
                        <Button
                            color="inherit"
                            startIcon={<DashboardIcon />}
                            component={Link}
                            href="/dashboard"
                            aria-label="Navigate to Dashboard"
                        >
                            Dashboard
                        </Button>
                        <Button
                            color="inherit"
                            startIcon={<CloudIcon />}
                            component={Link}
                            href="/clouds"
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
                        <Badge badgeContent={notifications?.length || 0} color="error">
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
                        <Avatar sx={{ bgcolor: avatarColor, color: textColor }} aria-label="User Avatar">
                            {avatarLetter}
                        </Avatar>
                    </IconButton>
                </Toolbar>
            </header>

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
                <nav
                    style={{
                        display: 'flex',
                        flexDirection: 'column',
                        height: '100%',
                        overflow: 'auto',
                    }}
                    aria-label="Main Navigation Links"
                >
                    <List>
                        <ListItemButton
                            component={Link}
                            href="/dashboard"
                            onClick={handleDrawerToggle}
                            aria-label="Navigate to Dashboard"
                        >
                            <DashboardIcon sx={{ mr: 2 }} aria-hidden="true" />
                            <ListItemText primary="Dashboard" />
                        </ListItemButton>
                        <ListItemButton
                            component={Link}
                            href="/vms"
                            onClick={handleDrawerToggle}
                            aria-label="Navigate to VMs"
                        >
                            <DesktopWindowsIcon sx={{ mr: 2 }} aria-hidden="true" />
                            <ListItemText primary="VMs" />
                        </ListItemButton>
                        <ListItemButton
                            component={Link}
                            href="/cost-manager"
                            onClick={handleDrawerToggle}
                            aria-label="Navigate to Cost Manager"
                        >
                            <AttachMoneyIcon sx={{ mr: 2 }} aria-hidden="true" />
                            <ListItemText primary="Cost Manager" />
                        </ListItemButton>
                        <ListItemButton
                            component={Link}
                            href="/alerts"
                            onClick={handleDrawerToggle}
                            aria-label="Navigate to Monitoring & Alerts"
                        >
                            <NotificationImportantIcon sx={{ mr: 2 }} aria-hidden="true" />
                            <ListItemText primary="Monitoring & Alerts" />
                        </ListItemButton>
                        {/* Clouds button - Visible only on small screens */}
                        <ListItemButton
                            component={Link}
                            href="/clouds"
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
                                sx={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', fontFamily: `'Comfortaa', sans-serif` }}
                            >
                                <span style={{ marginRight: 4 }}>©</span>
                                {' '}
                                {new Date().getFullYear()}
                                <MuiLink color="primary" href="https://chellavignesh.com/" underline="hover" sx={{ fontWeight: 'bold', ml: 0.5 }}>
                                    chellavignesh.com
                                </MuiLink>
                            </Typography>
                            <Typography variant="caption" color="textSecondary" align="center" sx={{ mt: 0.5, fontFamily: `'Comfortaa', sans-serif` }}>
                                Centralize Your Clouds. <br />Streamline Your Management.
                            </Typography>
                        </Box>
                    </Box>
                </nav>
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
Layout.propTypes = {
    children: PropTypes.node.isRequired,
};
export default Layout;