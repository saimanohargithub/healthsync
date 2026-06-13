/* eslint-disable */
import React, { useEffect, useState } from 'react';
import { 
  Grid, 
  Card, 
  CardContent, 
  Typography, 
  Box, 
  CircularProgress,
  LinearProgress,
  Avatar,
  Divider,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Button
} from '@mui/material';
import { 
  Restaurant as CaloriesIcon,
  WaterDrop as WaterIcon,
  Bedtime as SleepIcon,
  SentimentSatisfiedAlt as MoodIcon,
  Timeline as TimelineIcon,
  ArrowForward as ArrowForwardIcon 
} from '@mui/icons-material';
import { 
  subscribeToUserProfile,
  subscribeToSleepLogs,
  subscribeToMoodLogs,
  subscribeToWaterLogs,
  subscribeToMealPlans,
  getUserActiveChallenges,
  getCommunityFeed,
  subscribeToPredictiveAnalysis
} from '../../backend/firestoreService';
import type { 
  Challenge, 
  CommunityActivity, 
  UserProfile,
  SleepLog,
  MoodLog,
  PredictiveHealth
} from '../../backend/firestoreService';
import type { User as FirebaseUser } from 'firebase/auth';
import { useNavigate } from 'react-router-dom';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { getBestModel } from '../../backend/geminiConfig';

interface DashboardProps {
  user: FirebaseUser;
}

const Dashboard: React.FC<DashboardProps> = ({ user }) => {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [challenges, setChallenges] = useState<Challenge[]>([]);
  const [feed, setFeed] = useState<CommunityActivity[]>([]);
  
  // Real-time data
  const [todayCalories, setTodayCalories] = useState(0);
  const [todayWater, setTodayWater] = useState(0);
  const [latestSleep, setLatestSleep] = useState<SleepLog | null>(null);
  const [latestMood, setLatestMood] = useState<MoodLog | null>(null);
  const [predictiveData, setPredictiveData] = useState<PredictiveHealth | null>(null);
  
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [insights, setInsights] = useState('');
  const [loadingInsights, setLoadingInsights] = useState(false);
  const [insightError, setInsightError] = useState('');

  useEffect(() => {
    // Real-time subscriptions
    const unsubProfile = subscribeToUserProfile(user.uid, setProfile);
    getUserActiveChallenges(user.uid).then(setChallenges).catch(console.error);
    getCommunityFeed().then(setFeed).catch(console.error);
    
    const todayStr = new Date().toISOString().split('T')[0];

    const unsubMeals = subscribeToMealPlans(user.uid, (plans) => {
      const todayPlan = plans.find(p => p.id === todayStr);
      setTodayCalories(todayPlan?.calories || 0);
    });

    const unsubWater = subscribeToWaterLogs(user.uid, (logs) => {
      const todayLogs = logs.filter(l => l.date === todayStr);
      const totalWater = todayLogs.reduce((acc, curr) => acc + curr.amount, 0);
      setTodayWater(totalWater);
    });

    const unsubSleep = subscribeToSleepLogs(user.uid, (logs) => {
      if (logs.length > 0) setLatestSleep(logs[0]);
    });

    const unsubMood = subscribeToMoodLogs(user.uid, (logs) => {
      if (logs.length > 0) setLatestMood(logs[0]);
    });

    const unsubPredictive = subscribeToPredictiveAnalysis(user.uid, (data) => {
      setPredictiveData(data);
    });

    setLoading(false);

    return () => {
      unsubProfile();
      unsubMeals();
      unsubWater();
      unsubSleep();
      unsubMood();
      unsubPredictive();
    };
  }, [user]);

  const handleGetInsights = async () => {
    const apiKey = import.meta.env.VITE_GEMINI_API_KEY;
    if (!apiKey) {
      setInsightError('VITE_GEMINI_API_KEY is not configured in .env.local');
      return;
    }
    setLoadingInsights(true);
    setInsightError('');
    console.log('[Gemini] Initializing AI Health Insights...');
    try {
      const genAI = new GoogleGenerativeAI(apiKey);
      const modelName = await getBestModel(apiKey);
      console.log(`[Gemini] Using model: ${modelName}`);
      const model = genAI.getGenerativeModel({ model: modelName });
      const prompt = `You are HealthSync AI, a fitness analyst.
Analyze the following user data and provide a short, motivating, personalized health insight (2-3 sentences).
User: ${profile?.name || 'User'}
Calories Today: ${todayCalories}
Water Today: ${todayWater}ml
Last Sleep: ${latestSleep?.hours || 0} hours
Current Mood: ${latestMood?.mood || 'Unknown'}`;
      
      const result = await model.generateContent(prompt);
      setInsights(result.response.text());
    } catch (error: any) {
      console.error('[Gemini] Error generating insights:', error);
      let errorMsg = 'Unable to process AI response.';
      if (error?.message?.includes('404')) errorMsg = 'Gemini model unavailable.';
      else if (error?.message?.includes('403')) errorMsg = 'Invalid Gemini API Key.';
      else if (error?.message?.includes('429')) errorMsg = 'AI service is temporarily busy. Please try again later.';
      else if (error?.message?.includes('Failed to fetch') || error instanceof TypeError) errorMsg = 'Network connection issue.';
      else if (error instanceof SyntaxError) errorMsg = 'Unable to process AI response.';
      setInsightError(errorMsg);
    } finally {
      setLoadingInsights(false);
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: "bold" }} color="text.primary">
        Welcome back, {profile?.name || user.email?.split('@')[0]}!
      </Typography>
      <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 4 }}>
        Here is your real-time fitness overview for today.
      </Typography>

      <Grid container spacing={3}>
        {/* Top Stat Cards */}
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card elevation={0} sx={{ bgcolor: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: '#ef4444', mr: 2 }}><CaloriesIcon /></Avatar>
              <Box>
                <Typography variant="body2" color="text.secondary">Calories</Typography>
                <Typography variant="h5" sx={{ fontWeight: "bold" }}>{todayCalories} kcal</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card elevation={0} sx={{ bgcolor: 'rgba(59, 130, 246, 0.1)', border: '1px solid rgba(59, 130, 246, 0.2)' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: '#3b82f6', mr: 2 }}><WaterIcon /></Avatar>
              <Box>
                <Typography variant="body2" color="text.secondary">Water Intake</Typography>
                <Typography variant="h5" sx={{ fontWeight: "bold" }}>{todayWater} ml</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card elevation={0} sx={{ bgcolor: 'rgba(139, 92, 246, 0.1)', border: '1px solid rgba(139, 92, 246, 0.2)' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: '#8b5cf6', mr: 2 }}><SleepIcon /></Avatar>
              <Box>
                <Typography variant="body2" color="text.secondary">Latest Sleep</Typography>
                <Typography variant="h5" sx={{ fontWeight: "bold" }}>{latestSleep?.hours || '0'}h</Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card elevation={0} sx={{ bgcolor: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
            <CardContent sx={{ display: 'flex', alignItems: 'center' }}>
              <Avatar sx={{ bgcolor: '#10b981', mr: 2 }}><MoodIcon /></Avatar>
              <Box>
                <Typography variant="body2" color="text.secondary">Latest Mood</Typography>
                <Typography variant="h5" sx={{ fontWeight: "bold", textTransform: 'capitalize' }}>
                  {latestMood?.mood || 'None'}
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* AI Insights Card */}
        <Grid size={{ xs: 12 }}>
          <Card sx={{ bgcolor: 'rgba(99, 102, 241, 0.05)', border: '1px solid rgba(99, 102, 241, 0.2)' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: "bold", display: 'flex', alignItems: 'center', gap: 1 }}>
                  <span role="img" aria-label="sparkles">✨</span> AI Health Insights
                </Typography>
                <Button 
                  variant="contained" 
                  color="secondary" 
                  size="small" 
                  onClick={handleGetInsights}
                  disabled={loadingInsights}
                >
                  {loadingInsights ? 'Analyzing...' : 'Generate Insight'}
                </Button>
              </Box>
              {insightError && <Typography color="error" variant="body2">{insightError}</Typography>}
              {insights && !insightError && (
                <Typography variant="body1" sx={{ fontStyle: 'italic', color: 'primary.light' }}>
                  "{insights}"
                </Typography>
              )}
              {!insights && !insightError && !loadingInsights && (
                <Typography variant="body2" color="text.secondary">
                  Click the button to get personalized AI analysis based on your real-time fitness activity today.
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Predictive Health Analysis Card */}
        {predictiveData && (
          <Grid size={{ xs: 12 }}>
            <Card sx={{ bgcolor: 'rgba(244, 63, 94, 0.05)', border: '1px solid rgba(244, 63, 94, 0.2)' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                  <Typography variant="h6" sx={{ fontWeight: "bold", display: 'flex', alignItems: 'center', gap: 1 }}>
                    <TimelineIcon sx={{ color: '#f43f5e' }} /> Predictive Health Analysis
                  </Typography>
                  <Button 
                    variant="text" 
                    color="secondary" 
                    endIcon={<ArrowForwardIcon />}
                    onClick={() => navigate('/predictive-analysis')}
                  >
                    View Full Report
                  </Button>
                </Box>
                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 8 }}>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                      {predictiveData.healthForecast}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                      {predictiveData.recommendations.slice(0, 2).map((rec, i) => (
                        <Typography key={i} variant="caption" sx={{ bgcolor: 'rgba(255,255,255,0.1)', px: 1.5, py: 0.5, borderRadius: 1, color: 'text.primary' }}>
                          💡 {rec}
                        </Typography>
                      ))}
                    </Box>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }} sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Box sx={{ position: 'relative', display: 'inline-flex' }}>
                      <CircularProgress variant="determinate" value={100} size={80} sx={{ color: 'rgba(255,255,255,0.1)' }} />
                      <CircularProgress variant="determinate" value={predictiveData.wellnessScore} size={80} sx={{ position: 'absolute', left: 0, color: '#f43f5e' }} />
                      <Box sx={{ position: 'absolute', top: 0, left: 0, bottom: 0, right: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column' }}>
                        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>{predictiveData.wellnessScore}</Typography>
                      </Box>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        )}

        {/* Active Challenges */}
        <Grid size={{ xs: 12, md: 7 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: "bold", mb: 2 }}>Active Challenges</Typography>
              <Divider sx={{ mb: 2, borderColor: 'rgba(255,255,255,0.05)' }} />
              {challenges.length === 0 ? (
                <Typography color="text.secondary">You have no active challenges right now.</Typography>
              ) : (
                challenges.map(challenge => {
                  const progressVal = Math.min(100, Math.round(((challenge.currentProgress || 0) / challenge.goal) * 100));
                  return (
                    <Box key={challenge.id} sx={{ mb: 3 }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body1" sx={{ fontWeight: 500 }}>{challenge.name || 'Unnamed Challenge'}</Typography>
                        <Typography variant="body2" color="primary.main">{progressVal}%</Typography>
                      </Box>
                      <LinearProgress 
                        variant="determinate" 
                        value={progressVal} 
                        sx={{ height: 8, borderRadius: 4, bgcolor: 'rgba(255,255,255,0.1)' }} 
                      />
                      <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                        {challenge.currentProgress || 0} / {challenge.goal}
                      </Typography>
                    </Box>
                  );
                })
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Community Feed */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: "bold", mb: 2 }}>Recent Community Activity</Typography>
              <Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />
              <List>
                {feed.length === 0 ? (
                  <Typography color="text.secondary" sx={{ mt: 2 }}>No recent activity.</Typography>
                ) : (
                  feed.map((item, index) => (
                    <React.Fragment key={item.id}>
                      <ListItem alignItems="flex-start" sx={{ px: 0 }}>
                        <ListItemAvatar>
                          <Avatar sx={{ bgcolor: 'secondary.main', width: 32, height: 32, fontSize: 14 }}>
                            {item.userName?.charAt(0).toUpperCase()}
                          </Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={<Typography variant="body2" sx={{ fontWeight: "bold" }}>{item.userName}</Typography>}
                          secondary={<Typography variant="body2" color="text.secondary">{item.content || (item as any).activityText}</Typography>}
                        />
                      </ListItem>
                      {index < feed.length - 1 && <Divider component="li" sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />}
                    </React.Fragment>
                  ))
                )}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
