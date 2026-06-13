import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Typography, 
  Card, 
  CardContent, 
  Button, 
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  MenuItem,
  Grid,
  LinearProgress
} from '@mui/material';
import { 
  Add as AddIcon, 
  NightsStay as SleepIcon, 
  Mood as MoodIcon,
  WaterDrop as WaterIcon 
} from '@mui/icons-material';
import type { User as FirebaseUser } from 'firebase/auth';
import { 
  subscribeToSleepLogs, 
  subscribeToMoodLogs,
  subscribeToWaterLogs,
  saveSleepLog, 
  saveMoodLog,
  saveWaterLog
} from '../../backend/firestoreService';
import type { SleepLog, MoodLog } from '../../backend/firestoreService';

interface WellnessProps {
  user: FirebaseUser;
}

const MOODS = ["Happy 😀", "Calm 🙂", "Neutral 😐", "Stressed 😟", "Sad 😢", "Angry 😡"];
const WATER_GOAL = 3000;

const Wellness: React.FC<WellnessProps> = ({ user }) => {
  const [sleepLogs, setSleepLogs] = useState<SleepLog[]>([]);
  const [moodLogs, setMoodLogs] = useState<MoodLog[]>([]);
  const [todayWater, setTodayWater] = useState(0);
  const [loading, setLoading] = useState(true);

  const [openSleep, setOpenSleep] = useState(false);
  const [openMood, setOpenMood] = useState(false);

  const [sleepData, setSleepData] = useState<Partial<SleepLog>>({ id: new Date().toISOString().split('T')[0], hours: 8, score: 85 });
  const [moodData, setMoodData] = useState<Partial<MoodLog>>({ mood: 'Happy 😀', note: '' });

  useEffect(() => {
    const unsubSleep = subscribeToSleepLogs(user.uid, setSleepLogs);
    const unsubMood = subscribeToMoodLogs(user.uid, setMoodLogs);
    const unsubWater = subscribeToWaterLogs(user.uid, (logs) => {
      const todayStr = new Date().toISOString().split('T')[0];
      const todayTotal = logs
        .filter(l => l.date === todayStr)
        .reduce((sum, log) => sum + log.amount, 0);
      setTodayWater(todayTotal);
      setLoading(false); // Can mark loaded here
    });

    return () => {
      unsubSleep();
      unsubMood();
      unsubWater();
    };
  }, [user.uid]);

  const handleSaveSleep = async () => {
    if (sleepData.id && sleepData.hours && sleepData.score) {
      await saveSleepLog(user.uid, sleepData.id, sleepData);
      setOpenSleep(false);
    }
  };

  const handleSaveMood = async () => {
    if (moodData.mood) {
      const log: MoodLog = {
        id: Date.now().toString(),
        mood: moodData.mood,
        note: moodData.note || '',
        timestamp: Date.now()
      };
      await saveMoodLog(user.uid, log);
      setOpenMood(false);
      setMoodData({ mood: 'Happy 😀', note: '' });
    }
  };

  const handleAddWater = async (amount: number) => {
    await saveWaterLog(user.uid, amount);
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: "bold" }} color="text.primary">
          Wellness Tracking
        </Typography>
      </Box>

      <Grid container spacing={4}>
        {/* Hydration Section */}
        <Grid size={{ xs: 12 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h5" sx={{ fontWeight: "bold", display: 'flex', alignItems: 'center', gap: 1 }}>
              <WaterIcon color="info" /> Hydration
            </Typography>
          </Box>
          <Card sx={{ bgcolor: 'rgba(59, 130, 246, 0.1)', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
            <CardContent>
              <Grid container spacing={3}>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Typography variant="h6" sx={{ mb: 1 }}>Daily Goal: {todayWater} / {WATER_GOAL} ml</Typography>
                  <LinearProgress 
                    variant="determinate" 
                    value={Math.min((todayWater / WATER_GOAL) * 100, 100)} 
                    sx={{ height: 12, borderRadius: 6, mb: 1 }} 
                    color="info"
                  />
                  <Typography variant="body2" color="text.secondary">
                    {Math.round((todayWater / WATER_GOAL) * 100)}% completed
                  </Typography>
                </Grid>
                <Grid size={{ xs: 12, md: 6 }}>
                  <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', justifyContent: { xs: 'flex-start', md: 'flex-end' } }}>
                    <Button variant="contained" color="info" onClick={() => handleAddWater(250)}>+ 250ml</Button>
                    <Button variant="contained" color="info" onClick={() => handleAddWater(500)}>+ 500ml</Button>
                    <Button variant="contained" color="info" onClick={() => handleAddWater(750)}>+ 750ml</Button>
                    <Button variant="contained" color="info" onClick={() => handleAddWater(1000)}>+ 1L</Button>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Sleep Section */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h5" sx={{ fontWeight: "bold", display: 'flex', alignItems: 'center', gap: 1 }}>
              <SleepIcon color="primary" /> Sleep Logs
            </Typography>
            <Button variant="outlined" size="small" startIcon={<AddIcon />} onClick={() => setOpenSleep(true)}>
              Log Sleep
            </Button>
          </Box>
          <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', minHeight: 300 }}>
            <CardContent>
              {sleepLogs.length === 0 ? (
                <Typography color="text.secondary" sx={{ textAlign: 'center', mt: 5 }}>No sleep logs yet.</Typography>
              ) : (
                sleepLogs.map(log => (
                  <Box key={log.id} sx={{ mb: 2, p: 2, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>{log.id}</Typography>
                      <Typography variant="subtitle2" color="primary">{log.score} / 100 Score</Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">{log.hours} Hours Slept</Typography>
                  </Box>
                ))
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Mood Section */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h5" sx={{ fontWeight: "bold", display: 'flex', alignItems: 'center', gap: 1 }}>
              <MoodIcon color="secondary" /> Mood History
            </Typography>
            <Button variant="outlined" color="secondary" size="small" startIcon={<AddIcon />} onClick={() => setOpenMood(true)}>
              Log Mood
            </Button>
          </Box>
          <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', minHeight: 300 }}>
            <CardContent>
              {moodLogs.length === 0 ? (
                <Typography color="text.secondary" sx={{ textAlign: 'center', mt: 5 }}>No mood logs yet.</Typography>
              ) : (
                moodLogs.map(log => (
                  <Box key={log.id} sx={{ mb: 2, p: 2, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="h6">{log.mood}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(log.timestamp).toLocaleString()}
                      </Typography>
                    </Box>
                    {log.note && <Typography variant="body2" sx={{ mt: 1 }}>{log.note}</Typography>}
                  </Box>
                ))
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Sleep Dialog */}
      <Dialog open={openSleep} onClose={() => setOpenSleep(false)} slotProps={{ paper: { sx: { bgcolor: 'background.paper', borderRadius: 3 } } }}>
        <DialogTitle sx={{ fontWeight: "bold" }}>Log Sleep</DialogTitle>
        <DialogContent dividers>
          <TextField
            margin="dense" label="Date (YYYY-MM-DD)" type="date" fullWidth variant="outlined"
            value={sleepData.id} onChange={e => setSleepData({ ...sleepData, id: e.target.value })} sx={{ mb: 2 }}
          />
          <TextField
            margin="dense" label="Hours Slept" type="number" fullWidth variant="outlined"
            value={sleepData.hours} onChange={e => setSleepData({ ...sleepData, hours: Number(e.target.value) })} sx={{ mb: 2 }}
          />
          <TextField
            margin="dense" label="Sleep Score (0-100)" type="number" fullWidth variant="outlined"
            value={sleepData.score} onChange={e => setSleepData({ ...sleepData, score: Number(e.target.value) })}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenSleep(false)} color="inherit">Cancel</Button>
          <Button onClick={handleSaveSleep} variant="contained" color="primary">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Mood Dialog */}
      <Dialog open={openMood} onClose={() => setOpenMood(false)} slotProps={{ paper: { sx: { bgcolor: 'background.paper', borderRadius: 3 } } }}>
        <DialogTitle sx={{ fontWeight: "bold" }}>How are you feeling?</DialogTitle>
        <DialogContent dividers sx={{ minWidth: 300 }}>
          <TextField
            select margin="dense" label="Select Mood" fullWidth variant="outlined"
            value={moodData.mood} onChange={e => setMoodData({ ...moodData, mood: e.target.value })} sx={{ mb: 2 }}
          >
            {MOODS.map(option => (
              <MenuItem key={option} value={option}>{option}</MenuItem>
            ))}
          </TextField>
          <TextField
            margin="dense" label="Add a note (Optional)" fullWidth multiline rows={3} variant="outlined"
            value={moodData.note} onChange={e => setMoodData({ ...moodData, note: e.target.value })}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenMood(false)} color="inherit">Cancel</Button>
          <Button onClick={handleSaveMood} variant="contained" color="secondary">Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Wellness;
