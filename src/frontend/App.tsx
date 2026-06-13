import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { CssBaseline, CircularProgress, Box } from '@mui/material';
import { onAuthStateChanged } from 'firebase/auth';
import type { User as FirebaseUser } from 'firebase/auth';
import { auth } from '../backend/firebase';
import { ThemeContextProvider } from './theme/ThemeContext';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import Nutrition from './pages/Nutrition';
import Wellness from './pages/Wellness';
import Community from './pages/Community';
import Settings from './pages/Settings';
import PredictiveAnalysis from './pages/PredictiveAnalysis';

// Components
import Layout from './components/Layout';

// Protected Route Wrapper
const ProtectedRoute = ({ children, user }: { children: React.ReactNode, user: FirebaseUser | null }) => {
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const App: React.FC = () => {
  const [user, setUser] = useState<FirebaseUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  if (loading) {
    return (
      <ThemeContextProvider>
        <CssBaseline />
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', bgcolor: 'background.default' }}>
          <CircularProgress color="primary" />
        </Box>
      </ThemeContextProvider>
    );
  }

  return (
    <ThemeContextProvider>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/login" element={user ? <Navigate to="/" /> : <Login />} />
          <Route path="/register" element={user ? <Navigate to="/" /> : <Register />} />
          
          <Route path="/" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <Dashboard user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />

          <Route path="/profile" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <Profile user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />

          <Route path="/nutrition" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <Nutrition user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />

          <Route path="/wellness" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <Wellness user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />

          <Route path="/community" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <Community user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />

          <Route path="/predictive-analysis" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <PredictiveAnalysis user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />

          <Route path="/settings" element={
            <ProtectedRoute user={user}>
              {user ? (
                <Layout user={user}>
                  <Settings user={user} />
                </Layout>
              ) : null}
            </ProtectedRoute>
          } />
        </Routes>
      </Router>
    </ThemeContextProvider>
  );
};

export default App;

