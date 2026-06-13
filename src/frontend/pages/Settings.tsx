import React, { useContext } from 'react';
import { Box, Typography, Card, CardContent, Switch, List, ListItem, ListItemText, ListItemSecondaryAction, Divider } from '@mui/material';
import type { User as FirebaseUser } from 'firebase/auth';
import { ThemeContext } from '../theme/ThemeContext';

interface SettingsProps {
  user: FirebaseUser;
}

const Settings: React.FC<SettingsProps> = ({ user }) => {
  const { mode, toggleTheme } = useContext(ThemeContext);

  return (
    <Box>
      <Typography variant="h4" sx={{ fontWeight: 'bold', mb: 4 }}>Settings</Typography>
      
      <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.5)', mb: 3 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>Account</Typography>
          <List>
            <ListItem>
              <ListItemText primary="Email" secondary={user.email} />
            </ListItem>
            <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />
            <ListItem>
              <ListItemText primary="User ID" secondary={user.uid} />
            </ListItem>
          </List>
        </CardContent>
      </Card>

      <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.5)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>Preferences</Typography>
          <List>
            <ListItem>
              <ListItemText primary="Dark Mode" secondary="Toggle application theme" />
              <ListItemSecondaryAction>
                <Switch edge="end" checked={mode === 'dark'} onChange={toggleTheme} />
              </ListItemSecondaryAction>
            </ListItem>
            <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />
            <ListItem>
              <ListItemText primary="Push Notifications" secondary="Receive alerts and reminders" />
              <ListItemSecondaryAction>
                <Switch edge="end" defaultChecked />
              </ListItemSecondaryAction>
            </ListItem>
            <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />
            <ListItem>
              <ListItemText primary="Public Profile" secondary="Allow others to see my progress" />
              <ListItemSecondaryAction>
                <Switch edge="end" defaultChecked />
              </ListItemSecondaryAction>
            </ListItem>
          </List>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Settings;
