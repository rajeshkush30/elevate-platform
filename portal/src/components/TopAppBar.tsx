import { AppBar, Toolbar, Typography, Box, IconButton, Avatar, Menu, MenuItem, Tooltip } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import Logout from '@mui/icons-material/Logout';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AssessmentIcon from '@mui/icons-material/Assessment';
import ListAltIcon from '@mui/icons-material/ListAlt';
import { useState } from 'react';
import { useAuth } from '../context/AuthContext';

const TopAppBar = () => {
  const { user, logout } = useAuth();
  const [anchor, setAnchor] = useState<null | HTMLElement>(null);
  const initials = `${(user?.firstName || 'U')[0]}${(user?.lastName || '').slice(0,1)}`.toUpperCase();

  return (
    <AppBar position="sticky" elevation={0} sx={{ background: 'white', color: 'inherit', borderBottom: '1px solid #eee' }}>
      <Toolbar>
        <DashboardIcon color="primary" sx={{ mr: 1 }} />
        <Typography variant="h6" sx={{ fontWeight: 700 }}>
          Elevate Portal
        </Typography>
        <Box sx={{ flex: 1 }} />
        <Tooltip title={user?.email || ''}>
          <IconButton onClick={(e) => setAnchor(e.currentTarget)} size="small">
            <Avatar sx={{ width: 32, height: 32 }}>{initials}</Avatar>
          </IconButton>
        </Tooltip>
        <Menu anchorEl={anchor} open={!!anchor} onClose={() => setAnchor(null)}>
          <MenuItem onClick={() => { window.location.href = '/dashboard'; setAnchor(null); }}>
            <DashboardIcon fontSize="small" style={{ marginRight: 8 }} /> Dashboard
          </MenuItem>
          <MenuItem onClick={() => { window.location.href = '/my-assessments'; setAnchor(null); }}>
            <ListAltIcon fontSize="small" style={{ marginRight: 8 }} /> My Assessments
          </MenuItem>
          <MenuItem onClick={() => { window.location.href = '/assessment/start'; setAnchor(null); }}>
            <AssessmentIcon fontSize="small" style={{ marginRight: 8 }} /> Start Assessment
          </MenuItem>
          <MenuItem onClick={() => { logout(); setAnchor(null); }}>
            <Logout fontSize="small" style={{ marginRight: 8 }} /> Logout
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
};

export default TopAppBar;
