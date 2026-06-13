import React, { useState, useRef, useEffect } from 'react';
import { 
  Box, 
  Fab, 
  Dialog, 
  DialogTitle, 
  DialogContent, 
  DialogActions, 
  TextField, 
  IconButton, 
  Typography, 
  Paper,
  CircularProgress
} from '@mui/material';
import { SmartToy as BotIcon, Close as CloseIcon, Send as SendIcon } from '@mui/icons-material';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { getBestModel } from '../../backend/geminiConfig';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

const AIAssistant: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { role: 'assistant', content: 'Hi! I am your HealthSync AI assistant. Ask me anything about fitness, nutrition, or health!' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, open]);

  const handleSend = async () => {
    if (!input.trim() || loading) return;
    
    const userMsg = input.trim();
    setInput('');
    setMessages(prev => [...prev, { role: 'user', content: userMsg }]);
    
    const apiKey = import.meta.env.VITE_GEMINI_API_KEY;
    if (!apiKey) {
      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: 'Error: VITE_GEMINI_API_KEY is not configured in your .env.local file. Please add it to use the AI assistant.' 
      }]);
      return;
    }

    setLoading(true);
    console.log('[Gemini] Initializing API with key (exists: true)');
    try {
      const genAI = new GoogleGenerativeAI(apiKey);
      const modelName = await getBestModel(apiKey);
      console.log(`[Gemini] Using model: ${modelName}`);
      const model = genAI.getGenerativeModel({ model: modelName });
      
      // Basic context for health
      const prompt = `You are a helpful fitness and health assistant named HealthSync AI. 
Keep your answers concise, encouraging, and focused on health, nutrition, and wellness. 
User query: ${userMsg}`;

      console.log('[Gemini] Sending request...');
      const result = await model.generateContent(prompt);
      const response = await result.response;
      console.log(`[Gemini] Response received. Status: ${response ? 'Success' : 'Empty'}`);
      const text = response.text();
      
      setMessages(prev => [...prev, { role: 'assistant', content: text }]);
    } catch (err: unknown) {
      const error = err as Error;
      console.error('[Gemini] Error Status: FAILED');
      console.error('[Gemini] Error Details:', error);
      
      let errorMsg = 'Unable to process AI response.';
      if (error?.message?.includes('404') || error?.message?.includes('not found')) {
        errorMsg = 'Gemini model unavailable.';
      } else if (error?.message?.includes('403') || error?.message?.includes('API key not valid')) {
        errorMsg = 'Invalid Gemini API Key.';
      } else if (error?.message?.includes('429') || error?.message?.includes('quota')) {
        errorMsg = 'AI service is temporarily busy. Please try again later.';
      } else if (error?.message?.includes('Failed to fetch') || error instanceof TypeError) {
        errorMsg = 'Network connection issue.';
      } else if (error instanceof SyntaxError) {
        errorMsg = 'Unable to process AI response.';
      } else if (error?.message) {
        errorMsg = error.message;
      }

      setMessages(prev => [...prev, { 
        role: 'assistant', 
        content: `Sorry, I encountered an error: ${errorMsg}` 
      }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Fab 
        color="secondary" 
        aria-label="ai-assistant"
        onClick={() => setOpen(true)}
        sx={{
          position: 'fixed',
          bottom: 32,
          right: 32,
          zIndex: 1000,
          boxShadow: '0 0 20px rgba(236, 72, 153, 0.5)'
        }}
      >
        <BotIcon />
      </Fab>

      <Dialog 
        open={open} 
        onClose={() => setOpen(false)}
        maxWidth="sm"
        fullWidth
        slotProps={{
          paper: {
            sx: {
              height: '600px',
              bgcolor: 'background.paper',
              borderRadius: 3
            }
          }
        }}
      >
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', bgcolor: 'rgba(30, 41, 59, 0.5)' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <BotIcon color="secondary" />
            <Typography variant="h6" sx={{ fontWeight: 'bold' }}>HealthSync AI</Typography>
          </Box>
          <IconButton onClick={() => setOpen(false)} size="small">
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        
        <DialogContent sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 2, bgcolor: 'rgba(15, 23, 42, 0.3)' }}>
          <Box sx={{ flexGrow: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 2, pt: 1 }}>
            {messages.map((msg, idx) => (
              <Box 
                key={idx} 
                sx={{ 
                  alignSelf: msg.role === 'user' ? 'flex-end' : 'flex-start',
                  maxWidth: '80%'
                }}
              >
                <Paper 
                  elevation={0}
                  sx={{ 
                    p: 2, 
                    bgcolor: msg.role === 'user' ? 'primary.main' : 'rgba(255,255,255,0.1)',
                    color: msg.role === 'user' ? '#fff' : 'text.primary',
                    borderRadius: 2,
                    borderTopRightRadius: msg.role === 'user' ? 4 : 16,
                    borderTopLeftRadius: msg.role === 'assistant' ? 4 : 16,
                  }}
                >
                  <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                    {msg.content}
                  </Typography>
                </Paper>
              </Box>
            ))}
            {loading && (
              <Box sx={{ alignSelf: 'flex-start', p: 2 }}>
                <CircularProgress size={24} color="secondary" />
              </Box>
            )}
            <div ref={messagesEndRef} />
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2, bgcolor: 'rgba(30, 41, 59, 0.5)' }}>
          <TextField
            fullWidth
            placeholder="Ask a health question..."
            variant="outlined"
            size="small"
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyPress={e => {
              if (e.key === 'Enter') handleSend();
            }}
            disabled={loading}
            sx={{ '& .MuiOutlinedInput-root': { borderRadius: 5 } }}
          />
          <IconButton 
            color="primary" 
            onClick={handleSend}
            disabled={!input.trim() || loading}
            sx={{ bgcolor: 'rgba(99, 102, 241, 0.1)', ml: 1 }}
          >
            <SendIcon />
          </IconButton>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default AIAssistant;
