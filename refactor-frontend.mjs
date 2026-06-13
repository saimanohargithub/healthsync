import fs from 'fs';
import path from 'path';

const rootDir = process.cwd();
const srcDir = path.join(rootDir, 'src');
const frontendDir = path.join(srcDir, 'frontend');

// Create frontend directory
if (!fs.existsSync(frontendDir)) {
  fs.mkdirSync(frontendDir);
}

// Folders to move
const foldersToMove = ['pages', 'components', 'theme'];
for (const folder of foldersToMove) {
  const oldPath = path.join(srcDir, folder);
  const newPath = path.join(frontendDir, folder);
  if (fs.existsSync(oldPath)) {
    fs.renameSync(oldPath, newPath);
    console.log(`Moved ${folder} to frontend/`);
  }
}

// Files to move
const filesToMove = ['App.tsx', 'main.tsx', 'index.css', 'vite-env.d.ts'];
for (const file of filesToMove) {
  const oldPath = path.join(srcDir, file);
  const newPath = path.join(frontendDir, file);
  if (fs.existsSync(oldPath)) {
    fs.renameSync(oldPath, newPath);
    console.log(`Moved ${file} to frontend/`);
  }
}

// Update index.html
const indexHtmlPath = path.join(rootDir, 'index.html');
if (fs.existsSync(indexHtmlPath)) {
  let content = fs.readFileSync(indexHtmlPath, 'utf8');
  if (content.includes('src="/src/main.tsx"')) {
    content = content.replace('src="/src/main.tsx"', 'src="/src/frontend/main.tsx"');
    fs.writeFileSync(indexHtmlPath, content);
    console.log('Updated index.html script src');
  }
}

// Walk and replace imports inside frontend directory
function walkAndReplace(dir, isSubFolder) {
  const files = fs.readdirSync(dir);
  for (const file of files) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      // It's a subfolder like pages, components, theme
      walkAndReplace(fullPath, true);
    } else if (fullPath.endsWith('.ts') || fullPath.endsWith('.tsx')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let changed = false;

      if (isSubFolder) {
        // Files inside frontend/pages, frontend/components, etc.
        // Old import: '../backend/...'
        // New import: '../../backend/...'
        if (content.includes("'../backend/")) {
          content = content.replace(/'\.\.\/backend\//g, "'../../backend/");
          changed = true;
        }
      } else {
        // Files directly in frontend/ (like App.tsx, main.tsx)
        // Old import: './backend/...'
        // New import: '../backend/...'
        if (content.includes("'./backend/")) {
          content = content.replace(/'\.\/backend\//g, "'../backend/");
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

walkAndReplace(frontendDir, false);
console.log('Frontend refactoring complete.');
