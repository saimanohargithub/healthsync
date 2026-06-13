/* eslint-disable */
import React, { useEffect, useState } from 'react';
import { 
  Box, Typography, Card, CardContent, Grid, Button, CircularProgress, 
  Alert 
} from '@mui/material';
import { 
  Timeline as TimelineIcon, AutoGraph as AutoGraphIcon, 
  WaterDrop as WaterDropIcon, Hotel as HotelIcon, 
  Restaurant as RestaurantIcon, Warning as WarningIcon 
} from '@mui/icons-material';
import type { User as FirebaseUser } from 'firebase/auth';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { getBestModel } from '../../backend/geminiConfig';
import { 
  subscribeToPredictiveAnalysis, savePredictiveAnalysis
} from '../../backend/firestoreService';
import type { PredictiveHealth } from '../../backend/firestoreService';
import { db } from '../../backend/firebase';
import { collection, getDocs, query, orderBy, limit } from 'firebase/firestore';

interface PredictiveAnalysisProps {
  user: FirebaseUser;
}

const PredictiveAnalysis: React.FC<PredictiveAnalysisProps> = ({ user }) => {
  const [data, setData] = useState<PredictiveHealth | null>(null);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const unsub = subscribeToPredictiveAnalysis(user.uid, (healthData) => {
      setData(healthData);
      setLoading(false);
    });
    return () => unsub();
  }, [user.uid]);

  const generateForecast = async () => {
    const apiKey = import.meta.env.VITE_GEMINI_API_KEY;
    if (!apiKey) {
      setError('VITE_GEMINI_API_KEY is not configured.');
      return;
    }

    setGenerating(true);
    setError('');

    try {
      // 1. Gather context data
      const sleepSnap = await getDocs(query(collection(db, 'users', user.uid, 'sleep_logs'), orderBy('id', 'desc'), limit(7)));
      const moodSnap = await getDocs(query(collection(db, 'users', user.uid, 'mood_logs'), orderBy('timestamp', 'desc'), limit(7)));
      
      const sleepData = sleepSnap.docs.map(d => d.data());
      const moodData = moodSnap.docs.map(d => d.data());

      const contextData = {
        recentSleep: sleepData,
        recentMoods: moodData,
        // In a full implementation, we'd add water_logs, meals, etc.
      };

      // 2. Query Gemini
      const genAI = new GoogleGenerativeAI(apiKey);
      const modelName = await getBestModel(apiKey);
      const model = genAI.getGenerativeModel({ model: modelName });
      
      const prompt = `You are an advanced Predictive Health AI. Analyze the following recent health data for a user:
${JSON.stringify(contextData)}

Based on this limited data (if empty, assume baseline average health), predict their upcoming health trends. 
Return ONLY a valid JSON object matching exactly this structure (no markdown, no extra text):
{
  "wellnessScore": 85,
  "healthForecast": "Overall health is trending positively based on consistent habits.",
  "sleepForecast": "Sleep quality expected to improve if evening routine is maintained.",
  "hydrationForecast": "High risk of dehydration today based on previous patterns.",
  "nutritionForecast": "Calorie intake is balanced.",
  "recommendations": ["Drink 2L of water today", "Maintain 8 hours of sleep"]
}`;

      const result = await model.generateContent(prompt);
      const text = result.response.text().replace(/\`\`\`json/g, '').replace(/\`\`\`/g, '').trim();
      const plan = JSON.parse(text);

      const predictiveHealth: PredictiveHealth = {
        generatedAt: Date.now(),
        wellnessScore: plan.wellnessScore || 70,
        healthForecast: plan.healthForecast || 'No forecast available.',
        sleepForecast: plan.sleepForecast || 'No forecast available.',
        hydrationForecast: plan.hydrationForecast || 'No forecast available.',
        nutritionForecast: plan.nutritionForecast || 'No forecast available.',
        recommendations: plan.recommendations || ['Keep up the good work!']
      };

      await savePredictiveAnalysis(user.uid, predictiveHealth);

    } catch (error: any) {
      console.error('Failed to generate forecast:', error);
      let errorMsg = 'Unable to process AI response.';
      if (error?.message?.includes('404')) errorMsg = 'Gemini model unavailable.';
      else if (error?.message?.includes('403')) errorMsg = 'Invalid Gemini API Key.';
      else if (error?.message?.includes('429')) errorMsg = 'AI service is temporarily busy. Please try again later.';
      else if (error?.message?.includes('Failed to fetch') || error instanceof TypeError) errorMsg = 'Network connection issue.';
      else if (error instanceof SyntaxError) errorMsg = 'Unable to process AI response.';
      else errorMsg = error.message || errorMsg;
      setError(errorMsg);
    } finally {
      setGenerating(false);
    }
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: "bold" }} color="text.primary">
          Predictive Health Analysis
        </Typography>
        <Button 
          variant="contained" 
          color="secondary" 
          startIcon={generating ? <CircularProgress size={20} color="inherit" /> : <AutoGraphIcon />}
          onClick={generateForecast}
          disabled={generating}
        >
          {generating ? 'Analyzing Data...' : 'Generate New Forecast'}
        </Button>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      {!data ? (
        <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', textAlign: 'center', py: 8 }}>
          <TimelineIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">No predictions generated yet.</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            Click the button above to analyze your recent health logs and generate an AI forecast.
          </Typography>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {/* Main Forecast */}
          <Grid size={{ xs: 12, md: 8 }}>
            <Card sx={{ height: '100%', bgcolor: 'rgba(30, 41, 59, 0.7)', border: '1px solid rgba(99, 102, 241, 0.3)' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <AutoGraphIcon sx={{ color: 'primary.main', mr: 1, fontSize: 32 }} />
                  <Typography variant="h5" color="text.primary" sx={{ fontWeight: 'bold' }}>
                    Overall Health Forecast
                  </Typography>
                </Box>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 3, fontSize: '1.1rem', lineHeight: 1.6 }}>
                  {data.healthForecast}
                </Typography>

                <Grid container spacing={2}>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Box sx={{ p: 2, bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <HotelIcon sx={{ color: '#8b5cf6', mr: 1 }} />
                        <Typography variant="subtitle2" color="text.primary">Sleep Forecast</Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">{data.sleepForecast}</Typography>
                    </Box>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Box sx={{ p: 2, bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <WaterDropIcon sx={{ color: '#0ea5e9', mr: 1 }} />
                        <Typography variant="subtitle2" color="text.primary">Hydration Risk</Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">{data.hydrationForecast}</Typography>
                    </Box>
                  </Grid>
                  <Grid size={{ xs: 12, sm: 4 }}>
                    <Box sx={{ p: 2, bgcolor: 'rgba(0,0,0,0.2)', borderRadius: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <RestaurantIcon sx={{ color: '#f59e0b', mr: 1 }} />
                        <Typography variant="subtitle2" color="text.primary">Nutrition Trend</Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">{data.nutritionForecast}</Typography>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Score & Recommendations */}
          <Grid size={{ xs: 12, md: 4 }}>
            <Grid container spacing={3}>
              <Grid size={{ xs: 12 }}>
                <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', textAlign: 'center' }}>
                  <CardContent>
                    <Typography variant="subtitle1" color="text.secondary" sx={{ mb: 1 }}>Predicted Wellness Score</Typography>
                    <Box sx={{ position: 'relative', display: 'inline-flex' }}>
                      <CircularProgress variant="determinate" value={100} size={120} sx={{ color: 'rgba(255,255,255,0.1)' }} />
                      <CircularProgress variant="determinate" value={data.wellnessScore} size={120} color="secondary" sx={{ position: 'absolute', left: 0 }} />
                      <Box sx={{ position: 'absolute', top: 0, left: 0, bottom: 0, right: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Typography variant="h3" color="text.primary" sx={{ fontWeight: 'bold' }}>{data.wellnessScore}</Typography>
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
              
              <Grid size={{ xs: 12 }}>
                <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)' }}>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                      <WarningIcon sx={{ color: '#f43f5e', mr: 1 }} />
                      <Typography variant="h6" color="text.primary" sx={{ fontWeight: 'bold' }}>
                        Actionable Insights
                      </Typography>
                    </Box>
                    <Box component="ul" sx={{ pl: 2, m: 0, color: 'text.secondary' }}>
                      {data.recommendations.map((rec, i) => (
                        <Typography component="li" variant="body2" key={i} sx={{ mb: 1 }}>{rec}</Typography>
                      ))}
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default PredictiveAnalysis;
