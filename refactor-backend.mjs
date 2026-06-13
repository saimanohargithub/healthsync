import fs from 'fs';
import path from 'path';

const srcDir = path.join(process.cwd(), 'src');
const servicesDir = path.join(srcDir, 'services');
const backendDir = path.join(srcDir, 'backend');
const firebaseFileOld = path.join(srcDir, 'firebase.ts');
const firebaseFileNew = path.join(backendDir, 'firebase.ts');

// 1. Rename services -> backend
if (fs.existsSync(servicesDir)) {
  fs.renameSync(servicesDir, backendDir);
  console.log('Renamed src/services to src/backend');
}

// 2. Move firebase.ts -> backend/firebase.ts
if (fs.existsSync(firebaseFileOld)) {
  if (!fs.existsSync(backendDir)) fs.mkdirSync(backendDir);
  fs.renameSync(firebaseFileOld, firebaseFileNew);
  console.log('Moved src/firebase.ts to src/backend/firebase.ts');
}

// 3. Update all imports in the src directory
function walkAndReplace(dir) {
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      walkAndReplace(fullPath);
    } else if (fullPath.endsWith('.ts') || fullPath.endsWith('.tsx')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let changed = false;

      // Check if this file is inside the backend folder
      const isInsideBackend = fullPath.includes(path.join('src', 'backend'));

      if (isInsideBackend) {
        // Files inside backend importing firebase
        if (content.includes("'../firebase'")) {
           content = content.replace(/'\.\.\/firebase'/g, "'./firebase'");
           changed = true;
        }
      } else {
        // Files outside backend importing from services or firebase
        if (content.includes("'../services/")) {
          content = content.replace(/'\.\.\/services\//g, "'../backend/");
          changed = true;
        }
        if (content.includes("'./services/")) {
          content = content.replace(/'\.\/services\//g, "'./backend/");
          changed = true;
        }
        
        // Root src files importing firebase
        if (content.includes("'./firebase'")) {
          content = content.replace(/'\.\/firebase'/g, "'./backend/firebase'");
          changed = true;
        }
        // Subfolder files importing firebase
        if (content.includes("'../firebase'")) {
          content = content.replace(/'\.\.\/firebase'/g, "'../backend/firebase'");
          changed = true;
        }
      }

      if (changed) {
        fs.writeFileSync(fullPath, content);
        console.log('Updated imports in: ' + fullPath);
      }
    }
  }
}

walkAndReplace(srcDir);
console.log('Refactoring complete.');
