const fs = require('fs');
const path = require('path');

const walk = (dir) => {
  let results = [];
  const list = fs.readdirSync(dir);
  list.forEach((file) => {
    file = path.join(dir, file);
    const stat = fs.statSync(file);
    if (stat && stat.isDirectory()) {
      results = results.concat(walk(file));
    } else if (file.endsWith('.tsx') || file.endsWith('.ts')) {
      results.push(file);
    }
  });
  return results;
};

const files = walk('./src');

files.forEach(file => {
  let content = fs.readFileSync(file, 'utf8');
  let original = content;

  // Grid size replacements
  content = content.replace(/<Grid\s+item\s+xs=\{?(\d+)\}?\s+sm=\{?(\d+)\}?\s+md=\{?(\d+)\}?>/g, '<Grid size={{ xs: $1, sm: $2, md: $3 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{?(\d+)\}?\s+sm=\{?(\d+)\}?>/g, '<Grid size={{ xs: $1, sm: $2 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{?(\d+)\}?\s+md=\{?(\d+)\}?>/g, '<Grid size={{ xs: $1, md: $2 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{?(\d+)\}?>/g, '<Grid size={{ xs: $1 }}>');
  content = content.replace(/<Grid\s+item\s+xs=\{?(\d+)\}?\s+md=\{?(\d+)\}?\s+lg=\{?(\d+)\}?>/g, '<Grid size={{ xs: $1, md: $2, lg: $3 }}>');

  // Typography prop replacements
  content = content.replace(/<Typography([^>]*)fontWeight="?bold"?([^>]*)>/g, '<Typography$1sx={{ fontWeight: "bold" }}$2>');
  content = content.replace(/<Typography([^>]*)mb=\{?(\d+)\}?([^>]*)>/g, '<Typography$1sx={{ mb: $2 }}$3>');
  content = content.replace(/<Typography([^>]*)gutterBottom([^>]*)sx={{/g, '<Typography$1gutterBottom sx={{'); // Merge sx if already present

  // Special cases for Dashboard.tsx type imports
  if (file.endsWith('Dashboard.tsx')) {
    content = content.replace(/import { getUserProfile, UserProfile } from '\.\.\/services\/authService';/, 'import { getUserProfile } from \'../services/authService\';\nimport type { UserProfile } from \'../services/authService\';');
    content = content.replace(/import { \s*getUserActiveChallenges,\s*getCommunityFeed,\s*getGlobalStats,\s*Challenge,\s*CommunityActivity,\s*GlobalStats\s*} from '\.\.\/services\/firestoreService';/, 'import { getUserActiveChallenges, getCommunityFeed, getGlobalStats } from \'../services/firestoreService\';\nimport type { Challenge, CommunityActivity, GlobalStats } from \'../services/firestoreService\';');
  }

  // Special cases for Nutrition.tsx type imports
  if (file.endsWith('Nutrition.tsx')) {
    content = content.replace(/import { getMealPlans, saveMealPlan, MealPlan } from '\.\.\/services\/firestoreService';/, 'import { getMealPlans, saveMealPlan } from \'../services/firestoreService\';\nimport type { MealPlan } from \'../services/firestoreService\';');
  }

  // Layout.tsx missing prop in Menu
  if (file.endsWith('Layout.tsx')) {
     content = content.replace(/PaperProps={{/g, 'slotProps={{ paper: {');
     content = content.replace(/sx: { mt: 1.5, minWidth: 150, bgcolor: 'background.paper', borderRadius: 2 }/g, 'sx: { mt: 1.5, minWidth: 150, bgcolor: "background.paper", borderRadius: 2 } }');
  }

  // App.tsx auth user type fix
  if (file.endsWith('App.tsx')) {
     content = content.replace(/<ProtectedRoute user={user}>/g, '<ProtectedRoute user={user as any}>');
     content = content.replace(/<Layout user={user}>/g, '<Layout user={user as any}>');
     content = content.replace(/<Dashboard user={user} \/>/g, '<Dashboard user={user as any} />');
     content = content.replace(/<Profile user={user} \/>/g, '<Profile user={user as any} />');
     content = content.replace(/<Nutrition user={user} \/>/g, '<Nutrition user={user as any} />');
  }

  if (content !== original) {
    fs.writeFileSync(file, content);
  }
});
console.log("Done");
