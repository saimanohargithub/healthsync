import React, { useState } from 'react';
import { 
  Box, 
  Drawer, 
  AppBar, 
  Toolbar, 
  List, 
  Typography, 
  Divider, 
  IconButton, 
  ListItem, 
  ListItemButton, 
  ListItemIcon, 
  ListItemText, 
  Avatar,
  Menu,
  MenuItem
} from '@mui/material';
import { 
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  Restaurant as NutritionIcon,
  SelfImprovement as WellnessIcon,
  Group as CommunityIcon,
  Person as PersonIcon,
  Settings as SettingsIcon,
  ExitToApp as LogoutIcon,
  Timeline as InsightsIcon
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { auth } from '../../backend/firebase';
import { signOut } from 'firebase/auth';
import type { User as FirebaseUser } from 'firebase/auth';
import AIAssistant from './AIAssistant';

const drawerWidth = 280;

interface LayoutProps {
  children: React.ReactNode;
  user: FirebaseUser | null;
}

const Layout: React.FC<LayoutProps> = ({ children, user }) => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const location = useLocation();

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = async () => {
    handleMenuClose();
    await signOut(auth);
    navigate('/login');
  };

  const menuItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, path: '/' },
    { text: 'Nutrition', icon: <NutritionIcon />, path: '/nutrition' },
    { text: 'Wellness', icon: <WellnessIcon />, path: '/wellness' },
    { text: 'Community', icon: <CommunityIcon />, path: '/community' },
    { text: 'Analytics', icon: <InsightsIcon />, path: '/predictive-analysis' },
  ];

  const bottomMenuItems = [
    { text: 'Profile', icon: <PersonIcon />, path: '/profile' },
    { text: 'Settings', icon: <SettingsIcon />, path: '/settings' },
  ];

  const drawer = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Toolbar sx={{ justifyContent: 'center', py: 2 }}>
        <Typography variant="h5" color="primary.main" sx={{ fontWeight: "bold" }}>
          HealthSync
        </Typography>
      </Toolbar>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />
      <List sx={{ flexGrow: 1, px: 2, py: 3 }}>
        {menuItems.map((item) => {
          const active = location.pathname === item.path;
          return (
            <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
              <ListItemButton 
                onClick={() => { navigate(item.path); setMobileOpen(false); }}
                sx={{ 
                  borderRadius: 2, 
                  bgcolor: active ? 'rgba(99, 102, 241, 0.15)' : 'transparent',
                  color: active ? 'primary.main' : 'text.secondary',
                  '&:hover': { bgcolor: 'rgba(99, 102, 241, 0.25)' }
                }}
              >
                <ListItemIcon sx={{ color: active ? 'primary.main' : 'text.secondary', minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText primary={<Typography sx={{ fontWeight: active ? 600 : 400 }}>{item.text}</Typography>} />
              </ListItemButton>
            </ListItem>
          );
        })}
      </List>
      <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />
      <List sx={{ px: 2, py: 2 }}>
        {bottomMenuItems.map((item) => {
           const active = location.pathname === item.path;
           return (
            <ListItem key={item.text} disablePadding sx={{ mb: 1 }}>
              <ListItemButton 
                onClick={() => { navigate(item.path); setMobileOpen(false); }}
                sx={{ 
                  borderRadius: 2, 
                  bgcolor: active ? 'rgba(99, 102, 241, 0.15)' : 'transparent',
                  color: active ? 'primary.main' : 'text.secondary',
                  '&:hover': { bgcolor: 'rgba(99, 102, 241, 0.25)' }
                }}
              >
                <ListItemIcon sx={{ color: active ? 'primary.main' : 'text.secondary', minWidth: 40 }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText primary={<Typography sx={{ fontWeight: active ? 600 : 400 }}>{item.text}</Typography>} />
              </ListItemButton>
            </ListItem>
           );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar
        position="fixed"
        elevation={0}
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
          bgcolor: 'rgba(15, 23, 42, 0.8)',
          backdropFilter: 'blur(10px)',
          borderBottom: '1px solid rgba(255,255,255,0.05)',
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Box sx={{ flexGrow: 1 }} />
          <Typography variant="body2" sx={{ mr: 2, color: 'text.secondary', display: { xs: 'none', sm: 'block' } }}>
            {user?.email}
          </Typography>
          <IconButton onClick={handleMenuOpen} sx={{ p: 0, border: '2px solid', borderColor: 'primary.main' }}>
            <Avatar sx={{ width: 36, height: 36, bgcolor: 'primary.dark' }}>
              {user?.email?.charAt(0).toUpperCase()}
            </Avatar>
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            slotProps={{ paper: {
              sx: { mt: 1.5, minWidth: 150, bgcolor: "background.paper", borderRadius: 2 } }
            }}
          >
            <MenuItem onClick={() => { handleMenuClose(); navigate('/profile'); }}>
              <ListItemIcon><PersonIcon fontSize="small" /></ListItemIcon>
              Profile
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <ListItemIcon><LogoutIcon fontSize="small" color="error" /></ListItemIcon>
              <Typography color="error">Logout</Typography>
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, bgcolor: '#0f172a', borderRight: '1px solid rgba(255,255,255,0.05)' },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth, bgcolor: '#0f172a', borderRight: '1px solid rgba(255,255,255,0.05)' },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, p: 3, maxWidth: 1200, mx: 'auto' }}>
        <Toolbar />
        {children}
      </Box>
      <AIAssistant />
    </Box>
  );
};

export default Layout;
