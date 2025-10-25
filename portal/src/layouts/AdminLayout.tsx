import { useMemo } from 'react';
import { Outlet, Link as RouterLink, useLocation } from 'react-router-dom';
import {
  AppBar,
  Box,
  Breadcrumbs,
  CssBaseline,
  Divider,
  Drawer,
  IconButton,
  Link,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import GroupIcon from '@mui/icons-material/Group';
import CategoryIcon from '@mui/icons-material/Category';
import QuizIcon from '@mui/icons-material/Quiz';
import AssessmentIcon from '@mui/icons-material/Assessment';
import SettingsIcon from '@mui/icons-material/Settings';
import DashboardIcon from '@mui/icons-material/Dashboard';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const drawerWidth = 240;

const AdminLayout = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();
  const { user, logout } = useAuth();

  const breadcrumbs = useMemo(() => {
    const parts = location.pathname.replace(/^\/+|\/+$/g, '').split('/');
    const items: { label: string; to?: string }[] = [];
    let pathAcc = '';
    parts.forEach((p, idx) => {
      pathAcc += '/' + p;
      items.push({ label: p || 'admin', to: idx < parts.length - 1 ? pathAcc : undefined });
    });
    return items;
  }, [location.pathname]);

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6">Admin</Typography>
      </Toolbar>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin">
            <ListItemIcon><DashboardIcon /></ListItemIcon>
            <ListItemText primary="Dashboard" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin/clients">
            <ListItemIcon><GroupIcon /></ListItemIcon>
            <ListItemText primary="Clients" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin/catalog">
            <ListItemIcon><CategoryIcon /></ListItemIcon>
            <ListItemText primary="Catalog" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin/segments">
            <ListItemIcon><CategoryIcon /></ListItemIcon>
            <ListItemText primary="Segments" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin/questions">
            <ListItemIcon><QuizIcon /></ListItemIcon>
            <ListItemText primary="Questions" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin/submissions">
            <ListItemIcon><AssessmentIcon /></ListItemIcon>
            <ListItemText primary="Submissions" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton component={RouterLink} to="/admin/settings">
            <ListItemIcon><SettingsIcon /></ListItemIcon>
            <ListItemText primary="Settings" />
          </ListItemButton>
        </ListItem>
      </List>
    </div>
  );

  return (
    <Box sx={{ display: 'flex' }}>
      <CssBaseline />
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          <IconButton color="inherit" edge="start" onClick={() => setMobileOpen(!mobileOpen)} sx={{ mr: 2, display: { sm: 'none' } }}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap sx={{ flexGrow: 1 }}>Admin Console</Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>{user?.email}</Typography>
          <Link color="inherit" component="button" onClick={logout} underline="hover">Logout</Link>
        </Toolbar>
      </AppBar>

      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }} aria-label="folders">
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ display: { xs: 'block', sm: 'none' }, '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth } }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{ display: { xs: 'none', sm: 'block' }, '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth } }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      <Box component="main" sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` } }}>
        <Toolbar />
        <Breadcrumbs sx={{ mb: 2 }}>
          {breadcrumbs.map((b, idx) => b.to ? (
            <Link key={idx} component={RouterLink} to={b.to} color="inherit" underline="hover">{b.label}</Link>
          ) : (
            <Typography key={idx} color="text.primary">{b.label}</Typography>
          ))}
        </Breadcrumbs>
        <Outlet />
      </Box>
    </Box>
  );
};

export default AdminLayout;
