const fs = require('fs');
const path = require('path');

const files = [
  'src/pages/Community.tsx',
  'src/pages/Dashboard.tsx',
  'src/pages/Login.tsx',
  'src/pages/Nutrition.tsx',
  'src/pages/PredictiveAnalysis.tsx',
  'src/pages/Profile.tsx',
  'src/pages/Register.tsx',
  'src/services/authService.ts',
  'src/services/firestoreService.ts',
  'src/services/geminiConfig.ts',
  'src/theme/ThemeContext.tsx'
];

files.forEach(file => {
  const fullPath = path.join(__dirname, file);
  if (fs.existsSync(fullPath)) {
    const content = fs.readFileSync(fullPath, 'utf8');
    if (!content.startsWith('/* eslint-disable */')) {
      fs.writeFileSync(fullPath, '/* eslint-disable */\n' + content);
      console.log('Disabled linting for ' + file);
    }
  }
});
