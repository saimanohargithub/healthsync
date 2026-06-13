/* eslint-disable */
import packageJson from '../../package.json';

export const runGeminiDiagnostics = (_apiKey: string, availableModels: string[], selectedModel: string) => {
  console.log('=============================================');
  console.log('[Gemini Diagnostics] SDK Version:', packageJson.dependencies['@google/generative-ai'] || 'Unknown');
  console.log('[Gemini Diagnostics] Available Models (Sample):', availableModels.slice(0, 5).join(', ') + '...');
  console.log('[Gemini Diagnostics] Selected Fallback Model:', selectedModel);
  console.log('=============================================');
};

export const getBestModel = async (apiKey: string): Promise<string> => {
  console.log("[Gemini] API Key Present:", !!(apiKey || import.meta.env.VITE_GEMINI_API_KEY));
  console.log("[Gemini] Active Model:", "gemini-2.5-flash");
  return 'gemini-2.5-flash';
};
