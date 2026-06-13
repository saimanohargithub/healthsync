/* eslint-disable */
import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Typography, 
  Card, 
  CardContent, 
  Button, 
  TextField,
  CircularProgress,
  Divider,
  List,
  ListItem,
  ListItemAvatar,
  Avatar,
  ListItemText,
  Snackbar,
  Alert
} from '@mui/material';
import { Send as SendIcon, Forum as ForumIcon } from '@mui/icons-material';
import type { User as FirebaseUser } from 'firebase/auth';
import { subscribeToCommunityFeed, postToCommunityFeed } from '../../backend/firestoreService';
import type { CommunityActivity } from '../../backend/firestoreService';

interface CommunityProps {
  user: FirebaseUser;
}

const Community: React.FC<CommunityProps> = ({ user }) => {
  const [feed, setFeed] = useState<CommunityActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [newPost, setNewPost] = useState('');
  const [posting, setPosting] = useState(false);

  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' as 'success' | 'error' });

  useEffect(() => {
    setLoading(true);
    const unsub = subscribeToCommunityFeed((data) => {
      setFeed(data);
      setLoading(false);
    });
    return () => unsub();
  }, [user]);

  const handlePost = async () => {
    if (!newPost.trim()) return;
    setPosting(true);
    try {
      await postToCommunityFeed(user.uid, newPost.trim());
      setNewPost('');
      setSnackbar({ open: true, message: 'Post created successfully!', severity: 'success' });
    } catch (error: any) {
      console.error("Error posting to feed:", error);
      setSnackbar({ open: true, message: error.message || 'Failed to post', severity: 'error' });
    } finally {
      setPosting(false);
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;

  return (
    <Box sx={{ maxWidth: 'md', mx: 'auto' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
        <ForumIcon color="primary" sx={{ fontSize: 40 }} />
        <Typography variant="h4" sx={{ fontWeight: "bold" }} color="text.primary">
          Community Hub
        </Typography>
      </Box>

      <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', mb: 4 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>Share an update</Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="What's on your mind? (e.g., Just finished a 5k run!)"
              value={newPost}
              onChange={(e) => setNewPost(e.target.value)}
              disabled={posting}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handlePost();
                }
              }}
            />
            <Button 
              variant="contained" 
              color="primary" 
              endIcon={posting ? <CircularProgress size={20} color="inherit" /> : <SendIcon />}
              onClick={handlePost}
              disabled={posting || !newPost.trim()}
              sx={{ px: 4 }}
            >
              Post
            </Button>
          </Box>
        </CardContent>
      </Card>

      <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)' }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 3 }}>Recent Activity</Typography>
          {feed.length === 0 ? (
            <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
              No recent activity. Be the first to post!
            </Typography>
          ) : (
            <List>
              {feed.map((item, index) => (
                <React.Fragment key={item.id}>
                  <ListItem alignItems="flex-start" sx={{ px: 2, py: 2 }}>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: 'secondary.main', width: 48, height: 48, fontSize: 20 }}>
                        {item.userName?.charAt(0).toUpperCase() || 'A'}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Typography variant="subtitle1" sx={{ fontWeight: "bold", mb: 0.5 }}>
                          {item.userName || 'Anonymous'}
                        </Typography>
                      }
                      secondary={
                        <React.Fragment>
                          <Typography variant="body1" color="text.primary" sx={{ mb: 1 }}>
                            {item.content || (item as any).activityText}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {item.timestamp ? new Date(item.timestamp).toLocaleString() : 'Just now'}
                          </Typography>
                        </React.Fragment>
                      }
                    />
                  </ListItem>
                  {index < feed.length - 1 && <Divider component="li" sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />}
                </React.Fragment>
              ))}
            </List>
          )}
        </CardContent>
      </Card>

      <Snackbar 
        open={snackbar.open} 
        autoHideDuration={6000} 
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={() => setSnackbar({ ...snackbar, open: false })} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default Community;
