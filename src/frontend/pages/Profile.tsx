/* eslint-disable */
import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Card, 
  CardContent, 
  Typography, 
  TextField, 
  Button, 
  Grid, 
  Avatar, 
  CircularProgress,
  Snackbar,
  Alert
} from '@mui/material';
import { Person as PersonIcon, Edit as EditIcon, Save as SaveIcon } from '@mui/icons-material';
import type { User as FirebaseUser } from 'firebase/auth';
import { updateUserProfile } from '../../backend/authService';
import { subscribeToUserProfile } from '../../backend/firestoreService';
import type { UserProfile } from '../../backend/firestoreService';

interface ProfileProps {
  user: FirebaseUser;
}

const Profile: React.FC<ProfileProps> = ({ user }) => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState<Partial<UserProfile>>({});
  const [toast, setToast] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  useEffect(() => {
    const unsub = subscribeToUserProfile(user.uid, (data) => {
      if (data) {
        setProfile(data);
        if (!isEditing) setFormData(data); // Only update form if not editing
      }
      setLoading(false);
    });
    return () => unsub();
  }, [user.uid, isEditing]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSave = async () => {
    setSaving(true);
    try {
      await updateUserProfile(user.uid, formData);
      setProfile({ ...profile, ...formData } as UserProfile);
      setIsEditing(false);
      setToast({ open: true, message: 'Profile updated successfully!', severity: 'success' });
    } catch (error) {
      setToast({ open: true, message: 'Failed to update profile', severity: 'error' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
  }

  return (
    <Box sx={{ maxWidth: 'md', mx: 'auto' }}>
      <Typography variant="h4" sx={{ fontWeight: "bold" }} gutterBottom color="text.primary">
        Your Profile
      </Typography>

      <Card sx={{ mt: 3, p: 2, bgcolor: 'rgba(30, 41, 59, 0.7)' }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
            <Avatar sx={{ width: 80, height: 80, bgcolor: 'primary.main', mr: 3 }}>
              {profile?.name?.charAt(0).toUpperCase() || <PersonIcon fontSize="large" />}
            </Avatar>
            <Box sx={{ flexGrow: 1 }}>
              <Typography variant="h5" sx={{ fontWeight: "bold" }}>{profile?.name}</Typography>
              <Typography color="text.secondary">{profile?.email}</Typography>
            </Box>
            <Button 
              variant={isEditing ? "outlined" : "contained"} 
              color="primary"
              startIcon={isEditing ? null : <EditIcon />}
              onClick={() => isEditing ? setIsEditing(false) : setIsEditing(true)}
            >
              {isEditing ? 'Cancel' : 'Edit Profile'}
            </Button>
          </Box>

          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Full Name"
                name="name"
                value={formData.name || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Age"
                name="age"
                type="number"
                value={formData.age || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Height (cm)"
                name="height"
                type="number"
                value={formData.height || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Weight (kg)"
                name="weight"
                type="number"
                value={formData.weight || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Gender"
                name="gender"
                value={formData.gender || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Activity Level"
                name="activityLevel"
                value={formData.activityLevel || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                fullWidth
                label="Primary Goal"
                name="goal"
                value={formData.goal || ''}
                onChange={handleChange}
                disabled={!isEditing}
                variant="outlined"
                multiline
                rows={2}
              />
            </Grid>
          </Grid>

          {isEditing && (
            <Box sx={{ mt: 4, display: 'flex', justifyContent: 'flex-end' }}>
              <Button 
                variant="contained" 
                color="secondary" 
                startIcon={<SaveIcon />}
                onClick={handleSave}
                disabled={saving}
              >
                {saving ? <CircularProgress size={24} color="inherit" /> : 'Save Changes'}
              </Button>
            </Box>
          )}
        </CardContent>
      </Card>

      <Snackbar 
        open={toast.open} 
        autoHideDuration={6000} 
        onClose={() => setToast({ ...toast, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity={toast.severity} sx={{ width: '100%' }}>
          {toast.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default Profile;
