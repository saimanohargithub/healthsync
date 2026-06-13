/* eslint-disable */
import React, { useEffect, useState } from 'react';
import { 
  Box, 
  Typography, 
  Card, 
  CardContent, 
  Grid, 
  Button, 
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress
} from '@mui/material';
import { Add as AddIcon, Restaurant as RestaurantIcon } from '@mui/icons-material';
import type { User as FirebaseUser } from 'firebase/auth';
import { saveMealPlan, subscribeToMealPlans } from '../../backend/firestoreService';
import type { MealPlan } from '../../backend/firestoreService';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { getBestModel } from '../../backend/geminiConfig';

interface NutritionProps {
  user: FirebaseUser;
}

const Nutrition: React.FC<NutritionProps> = ({ user }) => {
  const [plans, setPlans] = useState<MealPlan[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [formData, setFormData] = useState<any>({
    id: new Date().toISOString().split('T')[0], // Default to today
    breakfast: '',
    lunch: '',
    dinner: '',
    calories: 0
  });
  const [generating, setGenerating] = useState(false);
  const [aiError, setAiError] = useState('');

  useEffect(() => {
    const unsub = subscribeToMealPlans(user.uid, (data) => {
      setPlans(data.sort((a, b) => b.id.localeCompare(a.id))); // Sort latest first
      setLoading(false);
    });
    return () => unsub();
  }, [user.uid]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleGenerateAI = async () => {
    const apiKey = import.meta.env.VITE_GEMINI_API_KEY;
    if (!apiKey) {
      setAiError('VITE_GEMINI_API_KEY is not configured in .env.local');
      return;
    }
    setGenerating(true);
    setAiError('');
    console.log('[Gemini] Initializing AI Meal Planner...');
    try {
      const genAI = new GoogleGenerativeAI(apiKey);
      const modelName = await getBestModel(apiKey);
      console.log(`[Gemini] Using model: ${modelName}`);
      const model = genAI.getGenerativeModel({ model: modelName });
      const prompt = `Generate a healthy, balanced daily meal plan.
Return ONLY a valid JSON object matching exactly this structure (no markdown, no extra text):
{
  "breakfast": "name of breakfast and brief description",
  "lunch": "name of lunch and brief description",
  "dinner": "name of dinner and brief description",
  "calories": 2000
}`;
      console.log('[Gemini] Sending meal plan request...');
      const result = await model.generateContent(prompt);
      console.log('[Gemini] Meal Plan Response received successfully.');
      const text = result.response.text().replace(/\`\`\`json/g, '').replace(/\`\`\`/g, '').trim();
      const plan = JSON.parse(text);
      
      setFormData({
        ...formData,
        breakfast: plan.breakfast,
        lunch: plan.lunch,
        dinner: plan.dinner,
        calories: plan.calories || 0
      });
    } catch (error: any) {
      console.error('[Gemini] Error generating meal plan:', error);
      
      let errorMsg = 'Unable to process AI response.';
      if (error?.message?.includes('404')) errorMsg = 'Gemini model unavailable.';
      else if (error?.message?.includes('403')) errorMsg = 'Invalid Gemini API Key.';
      else if (error?.message?.includes('429')) errorMsg = 'AI service is temporarily busy. Please try again later.';
      else if (error?.message?.includes('Failed to fetch') || error instanceof TypeError) errorMsg = 'Network connection issue.';
      else if (error instanceof SyntaxError) errorMsg = 'Unable to process AI response.';
      
      setAiError(errorMsg);
    } finally {
      setGenerating(false);
    }
  };

  const handleSave = async () => {
    try {
      if (formData.id) {
        await saveMealPlan(user.uid, formData.id, formData);
        setPlans([{ ...formData } as MealPlan, ...plans.filter(p => p.id !== formData.id)].sort((a, b) => b.id.localeCompare(a.id)));
        setOpen(false);
      }
    } catch (error) {
      console.error("Error saving meal plan", error);
    }
  };

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: "bold" }} color="text.primary">
          Nutrition Tracking
        </Typography>
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<AddIcon />}
          onClick={() => setOpen(true)}
        >
          Add Meal Plan
        </Button>
      </Box>

      <Grid container spacing={3}>
        {plans.length === 0 ? (
          <Grid size={{ xs: 12 }}>
            <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', textAlign: 'center', py: 5 }}>
              <RestaurantIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">No meal plans logged yet.</Typography>
            </Card>
          </Grid>
        ) : (
          plans.map(plan => (
            <Grid size={{ xs: 12, md: 6, lg: 4 }} key={plan.id}>
              <Card sx={{ bgcolor: 'rgba(30, 41, 59, 0.7)', transition: 'transform 0.2s', '&:hover': { transform: 'translateY(-4px)' } }}>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Typography variant="h6" color="primary.light" sx={{ fontWeight: "bold" }}>{plan.id}</Typography>
                    <Typography variant="subtitle2" sx={{ bgcolor: 'rgba(99, 102, 241, 0.2)', px: 1.5, py: 0.5, borderRadius: 2, color: 'primary.main' }}>
                      {plan.calories || 0} kcal
                    </Typography>
                  </Box>
                  <Box sx={{ mb: 1 }}>
                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: "bold" }}>BREAKFAST</Typography>
                    <Typography variant="body2">{typeof plan.breakfast === 'object' ? plan.breakfast?.mealName || 'Not logged' : plan.breakfast || 'Not logged'}</Typography>
                  </Box>
                  <Box sx={{ mb: 1 }}>
                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: "bold" }}>LUNCH</Typography>
                    <Typography variant="body2">{typeof plan.lunch === 'object' ? plan.lunch?.mealName || 'Not logged' : plan.lunch || 'Not logged'}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary" sx={{ fontWeight: "bold" }}>DINNER</Typography>
                    <Typography variant="body2">{typeof plan.dinner === 'object' ? plan.dinner?.mealName || 'Not logged' : plan.dinner || 'Not logged'}</Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      <Dialog open={open} onClose={() => setOpen(false)} slotProps={{ paper: { sx: { bgcolor: 'background.paper', borderRadius: 3 } } }}>
        <DialogTitle sx={{ bgcolor: 'rgba(30, 41, 59, 0.5)' }}>Add Meal Plan</DialogTitle>
        <DialogContent sx={{ mt: 2 }}>
          <Box sx={{ mb: 3 }}>
            <Button 
              variant="outlined" 
              color="secondary" 
              fullWidth 
              onClick={handleGenerateAI}
              disabled={generating}
              startIcon={generating ? <CircularProgress size={20} /> : <RestaurantIcon />}
              sx={{ py: 1.5, borderStyle: 'dashed' }}
            >
              {generating ? 'Generating AI Meal Plan...' : '✨ Auto-Generate with AI'}
            </Button>
            {aiError && (
              <Typography color="error" variant="caption" sx={{ mt: 1, display: 'block', textAlign: 'center' }}>
                {aiError}
              </Typography>
            )}
          </Box>
          <TextField
            margin="dense"
            label="Date (YYYY-MM-DD)"
            type="date"
            name="id"
            fullWidth
            variant="outlined"
            value={formData.id}
            onChange={handleChange}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Breakfast"
            name="breakfast"
            fullWidth
            variant="outlined"
            multiline
            rows={2}
            value={formData.breakfast}
            onChange={handleChange}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Lunch"
            name="lunch"
            fullWidth
            variant="outlined"
            multiline
            rows={2}
            value={formData.lunch}
            onChange={handleChange}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Dinner"
            name="dinner"
            fullWidth
            variant="outlined"
            multiline
            rows={2}
            value={formData.dinner}
            onChange={handleChange}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Total Calories"
            name="calories"
            type="number"
            fullWidth
            variant="outlined"
            value={formData.calories}
            onChange={handleChange}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpen(false)} color="inherit">Cancel</Button>
          <Button onClick={handleSave} variant="contained" color="primary">Save</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Nutrition;
