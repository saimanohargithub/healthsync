import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { PieChart, Pie, Cell, ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip } from 'recharts';

// --- DESIGN SYSTEM ---
const COLORS = {
  bg: '#071120',
  cyan: '#00D9FF',
  blueGlow: '#1DAEFF',
  glass: 'rgba(255, 255, 255, 0.03)',
  glassBorder: 'rgba(0, 217, 255, 0.1)',
  textMain: '#E1E1E1',
  textMuted: '#808B99',
  purple: '#A855F7',
};

// --- REUSABLE COMPONENTS ---

const PremiumButton = ({ title, onClick, variant = 'primary' }: { title: string, onClick: () => void, variant?: 'primary' | 'outline' }) => (
  <motion.button
    whileTap={{ scale: 0.95 }}
    onClick={onClick}
    className={`w-full py-4 rounded-full font-bold tracking-widest uppercase text-sm transition-all ${
      variant === 'primary' ? 'shadow-[0_0_20px_rgba(0,217,255,0.3)]' : 'border border-cyan-500/30'
    }`}
    style={{ backgroundColor: variant === 'primary' ? COLORS.cyan : 'transparent', color: variant === 'primary' ? COLORS.bg : COLORS.cyan }}
  >
    {title}
  </motion.button>
);

const GlassCard = ({ children, className = "" }: { children: React.ReactNode, className?: string }) => (
  <div className={`p-6 rounded-[28px] bg-white/5 border border-white/5 backdrop-blur-xl ${className}`}>
    {children}
  </div>
);

const BottomNav = ({ active, onChange }: { active: string, onChange: (s: string) => void }) => {
  const tabs = [
    { id: 'dashboard', icon: 'M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z' },
    { id: 'nutrition', icon: 'M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5' },
    { id: 'scanner', icon: 'M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z', center: true },
    { id: 'wellness', icon: 'M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z' },
    { id: 'community', icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }
  ];

  return (
    <div className="fixed bottom-0 left-0 right-0 h-24 flex items-center justify-around px-6 bg-[#071120]/80 backdrop-blur-2xl border-t border-white/5 z-50">
      {tabs.map((tab) => (
        <button key={tab.id} onClick={() => onChange(tab.id)} className={`relative p-3 rounded-full transition-all ${tab.center ? 'bg-cyan-500 shadow-[0_0_30px_rgba(0,217,255,0.6)] -mt-14 scale-110' : ''}`}>
          <svg width={tab.center ? "28" : "24"} height={tab.center ? "28" : "24"} viewBox="0 0 24 24" fill="none" stroke={tab.center ? COLORS.bg : (active === tab.id ? COLORS.cyan : COLORS.textMuted)} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d={tab.icon} /></svg>
          {active === tab.id && !tab.center && <motion.div layoutId="nav-dot" className="absolute -bottom-1 left-1/2 -translate-x-1/2 w-1.5 h-1.5 rounded-full bg-cyan-500 shadow-[0_0_10px_#00D9FF]" />}
        </button>
      ))}
    </div>
  );
};

// --- DATA ---
const STRESS_DATA = [
  { time: '08:00', value: 20 }, { time: '10:00', value: 45 }, { time: '12:00', value: 30 }, { time: '14:00', value: 65 }, { time: '16:00', value: 40 }, { time: '18:00', value: 25 },
];

// --- SCREENS ---

const Dashboard = () => (
  <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="space-y-10">
    <div className="flex items-center justify-between pt-4">
        <div><p className="text-[10px] tracking-widest uppercase opacity-40 text-cyan-400">System Optimized</p><h1 className="text-2xl font-light text-white">Morning, Alex</h1></div>
        <div className="w-12 h-12 rounded-full border border-cyan-500/30 p-1"><div className="w-full h-full rounded-full bg-gradient-to-tr from-cyan-500 to-blue-500 shadow-[0_0_15px_rgba(0,217,255,0.3)]" /></div>
    </div>
    <div className="w-full flex justify-center py-6">
        <div className="relative w-56 h-56 flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
                <PieChart><Pie data={[{v:88},{v:12}]} innerRadius={85} outerRadius={105} startAngle={90} endAngle={-270} dataKey="v" stroke="none"><Cell fill={COLORS.cyan} /><Cell fill="rgba(255,255,255,0.05)" /></Pie></PieChart>
            </ResponsiveContainer>
            <div className="absolute flex flex-col items-center">
                <span className="text-6xl font-bold text-white tracking-tighter">88</span>
                <span className="text-[10px] tracking-[0.2em] uppercase opacity-40 mt-1 text-cyan-400">Health Score</span>
            </div>
        </div>
    </div>
    <div className="grid grid-cols-2 gap-4">
        <GlassCard><div className="flex flex-col"><span className="text-2xl font-bold text-white">750<span className="text-xs opacity-40 ml-1">ml</span></span><span className="text-[10px] uppercase tracking-widest opacity-40 text-cyan-400 mt-1">Water</span></div></GlassCard>
        <GlassCard><div className="flex flex-col"><span className="text-2xl font-bold text-white">7.2<span className="text-xs opacity-40 ml-1">hrs</span></span><span className="text-[10px] uppercase tracking-widest opacity-40 text-purple-400 mt-1">Sleep</span></div></GlassCard>
    </div>
    <GlassCard className="relative overflow-hidden border-cyan-500/20">
        <div className="absolute top-0 right-0 w-32 h-32 bg-cyan-500/5 blur-3xl" />
        <h3 className="text-[10px] tracking-widest uppercase mb-4 text-cyan-400 font-bold">AI Commander Insight</h3>
        <p className="text-sm font-light leading-relaxed text-gray-300">Neural analysis identifies peak focus windows between 14:00 and 16:30. Increase metabolic fuel by 12% to sustain high-intensity cognition.</p>
    </GlassCard>
  </motion.div>
);

const NutritionScreen = () => (
  <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-8">
    <h2 className="text-4xl font-light text-white pt-4">Fuel <br/> <span className="text-cyan-400">Architecture</span></h2>
    <div className="grid grid-cols-3 gap-2">
        {['Protein', 'Carbs', 'Lipids'].map((label, i) => (
            <GlassCard key={i} className="flex flex-col items-center p-4">
                <div className="w-12 h-12 mb-3"><ResponsiveContainer><PieChart><Pie data={[{v:65+i*10},{v:35-i*10}]} innerRadius={18} outerRadius={22} startAngle={90} endAngle={-270} dataKey="v" stroke="none"><Cell fill={i===0?COLORS.cyan:i===1?'#A855F7':'#F59E0B'} /><Cell fill="rgba(255,255,255,0.05)" /></Pie></PieChart></ResponsiveContainer></div>
                <span className="text-[10px] uppercase tracking-widest opacity-40">{label}</span>
            </GlassCard>
        ))}
    </div>
    <div className="space-y-4">
        <GlassCard className="flex items-center justify-between border-l-2 border-l-cyan-500">
            <div><h4 className="text-white font-medium text-sm">Omega Matrix Breakfast</h4><p className="text-[10px] opacity-40 uppercase tracking-tighter mt-1">Completed • 08:30</p></div>
            <span className="text-cyan-400 font-bold text-lg">520<span className="text-[10px] ml-1">kcal</span></span>
        </GlassCard>
        <GlassCard className="flex items-center justify-between border-l-2 border-l-purple-500">
            <div><h4 className="text-white font-medium text-sm">Synthetic Greens Bowl</h4><p className="text-[10px] opacity-40 uppercase tracking-tighter mt-1">Optimal Selection</p></div>
            <span className="text-purple-400 font-bold text-lg">380<span className="text-[10px] ml-1">kcal</span></span>
        </GlassCard>
    </div>
  </motion.div>
);

const ScannerScreen = () => (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="fixed inset-0 bg-black z-50">
        <div className="absolute inset-0 bg-gradient-to-b from-cyan-900/20 to-transparent" />
        <div className="absolute inset-0 flex items-center justify-center">
            <motion.div
                animate={{ scale: [0.9, 1.1, 0.9], opacity: [0.4, 0.8, 0.4] }}
                transition={{ duration: 3, repeat: Infinity }}
                className="w-72 h-72 border border-cyan-500/30 rounded-[48px] relative"
            >
                <div className="absolute -top-1 -left-1 w-8 h-8 border-t-2 border-l-2 border-cyan-400" />
                <div className="absolute -top-1 -right-1 w-8 h-8 border-t-2 border-r-2 border-cyan-400" />
                <div className="absolute -bottom-1 -left-1 w-8 h-8 border-b-2 border-l-2 border-cyan-400" />
                <div className="absolute -bottom-1 -right-1 w-8 h-8 border-b-2 border-r-2 border-cyan-400" />
                <motion.div
                    animate={{ top: ['0%', '100%', '0%'] }}
                    transition={{ duration: 4, repeat: Infinity, ease: "linear" }}
                    className="absolute left-0 right-0 h-0.5 bg-cyan-400 shadow-[0_0_15px_#00D9FF]"
                />
            </motion.div>
        </div>
        <div className="absolute top-16 left-0 right-0 text-center">
            <p className="text-xs tracking-[0.3em] text-cyan-400 font-bold animate-pulse">AI SCANNER: ACTIVE</p>
            <p className="text-[10px] text-white/40 mt-2">Position nutrients within frame</p>
        </div>
        <div className="absolute bottom-16 left-8 right-8 flex justify-center">
            <div className="w-20 h-20 rounded-full border-2 border-white/20 p-1">
                <div className="w-full h-full rounded-full bg-cyan-500 shadow-[0_0_30px_#00D9FF]" />
            </div>
        </div>
    </motion.div>
);

const WellnessScreen = () => (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-8">
        <h2 className="text-4xl font-light text-white pt-4">Neural <br/> <span className="text-purple-400">Equilibrium</span></h2>
        <GlassCard className="p-0 overflow-hidden">
            <div className="p-6 pb-2"><h3 className="text-[10px] tracking-widest uppercase text-purple-400 font-bold">Stress Synchronization</h3></div>
            <div className="h-40 w-full">
                <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={STRESS_DATA} margin={{ top: 0, right: 0, left: -60, bottom: 0 }}>
                        <defs><linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={COLORS.purple} stopOpacity={0.3}/><stop offset="95%" stopColor={COLORS.purple} stopOpacity={0}/></linearGradient></defs>
                        <Area type="monotone" dataKey="value" stroke={COLORS.purple} strokeWidth={2} fillOpacity={1} fill="url(#colorValue)" />
                    </AreaChart>
                </ResponsiveContainer>
            </div>
        </GlassCard>
        <div className="space-y-4">
            <p className="text-[10px] tracking-widest uppercase opacity-40 font-bold ml-2">Recommended Protocols</p>
            <GlassCard className="flex items-center space-x-4">
                <div className="w-12 h-12 rounded-2xl bg-purple-500/10 flex items-center justify-center border border-purple-500/20"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke={COLORS.purple} strokeWidth="1.5"><path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z" /></svg></div>
                <div><h4 className="text-white font-medium text-sm">Deep Beta Meditation</h4><p className="text-xs text-gray-500">15 min • AI Generated</p></div>
            </GlassCard>
            <GlassCard className="flex items-center space-x-4">
                <div className="w-12 h-12 rounded-2xl bg-cyan-500/10 flex items-center justify-center border border-cyan-500/20"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke={COLORS.cyan} strokeWidth="1.5"><path d="M22 12h-4l-3 9L9 3l-3 9H2" /></svg></div>
                <div><h4 className="text-white font-medium text-sm">Resonant Breathing</h4><p className="text-xs text-gray-500">5 min • Stress Reducer</p></div>
            </GlassCard>
        </div>
    </motion.div>
);

const CommunityScreen = () => (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-8">
        <h2 className="text-4xl font-light text-white pt-4">Global <br/> <span className="text-cyan-400">Squad</span></h2>
        <div className="space-y-3">
            {['Commander Rivera', 'Captain Stark', 'Sentinel Prime'].map((name, i) => (
                <GlassCard key={i} className={`flex items-center justify-between ${i===0 ? 'border-cyan-500/40 shadow-[0_0_20px_rgba(0,217,255,0.05)]' : ''}`}>
                    <div className="flex items-center space-x-4">
                        <span className={`text-sm font-bold ${i===0?'text-cyan-400':'text-white/20'}`}>0{i+1}</span>
                        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-white/10 to-white/5 border border-white/10" />
                        <h4 className={`text-sm font-medium ${i===0?'text-white':'text-white/60'}`}>{name}</h4>
                    </div>
                    <span className={`text-xs font-bold ${i===0?'text-cyan-400':'text-white/40'}`}>{12450 - i*1200}<span className="text-[8px] ml-1 opacity-60">XP</span></span>
                </GlassCard>
            ))}
        </div>
        <GlassCard className="bg-gradient-to-r from-cyan-500/10 to-transparent border-cyan-500/20">
            <h3 className="text-xs tracking-widest uppercase text-cyan-400 font-bold mb-2">Active Challenge</h3>
            <p className="text-sm text-white font-light">30-Day Metabolic Sync</p>
            <div className="mt-4 h-1.5 w-full bg-white/5 rounded-full overflow-hidden"><motion.div initial={{ width: 0 }} animate={{ width: '65%' }} transition={{ duration: 1.5 }} className="h-full bg-cyan-400 shadow-[0_0_10px_#00D9FF]" /></div>
            <p className="text-[10px] text-white/40 mt-2 text-right">65% Synchronized</p>
        </GlassCard>
    </motion.div>
);

const ProfileScreen = ({ onLogout }: { onLogout: () => void }) => (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-10">
        <div className="flex flex-col items-center pt-8">
            <div className="w-24 h-24 rounded-full border-2 border-cyan-500/30 p-1.5 mb-4 shadow-[0_0_30px_rgba(0,217,255,0.1)]">
                <div className="w-full h-full rounded-full bg-gradient-to-tr from-cyan-500 to-blue-500" />
            </div>
            <h2 className="text-2xl font-light text-white">Commander Rivera</h2>
            <p className="text-[10px] tracking-[0.3em] text-cyan-400 uppercase font-bold mt-1">Elite Neural Member</p>
        </div>
        <div className="grid grid-cols-3 gap-2">
            {[['24', 'Age'], ['182', 'cm'], ['78', 'kg']].map(([val, label], i) => (
                <GlassCard key={i} className="flex flex-col items-center p-4">
                    <span className="text-lg font-bold text-white">{val}</span>
                    <span className="text-[10px] uppercase tracking-widest opacity-40">{label}</span>
                </GlassCard>
            ))}
        </div>
        <div className="space-y-3">
            {['System Preferences', 'Neural Privacy', 'Health Reports', 'Subscription Matrix'].map((item, i) => (
                <GlassCard key={i} className="flex items-center justify-between py-5">
                    <span className="text-sm font-light text-white/80">{item}</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="1" className="opacity-20"><path d="M9 18l6-6-6-6" /></svg>
                </GlassCard>
            ))}
        </div>
        <PremiumButton title="Terminate Session" onClick={onLogout} variant="outline" />
    </motion.div>
);

// --- MAIN APP COMPONENT ---

export default function App() {
  const [screen, setScreen] = useState('splash');
  const [activeTab, setActiveTab] = useState('dashboard');

  useEffect(() => { if (screen === 'splash') setTimeout(() => setScreen('main'), 2000); }, [screen]);

  return (
    <div className="min-h-screen font-sans overflow-x-hidden text-white pb-32" style={{ backgroundColor: COLORS.bg }}>
      <AnimatePresence mode="wait">
        {screen === 'splash' && <SplashScreen onFinish={() => setScreen('main')} />}
        {screen === 'main' && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="p-8">
            {activeTab === 'dashboard' && <Dashboard />}
            {activeTab === 'nutrition' && <NutritionScreen />}
            {activeTab === 'scanner' && <ScannerScreen />}
            {activeTab === 'wellness' && <WellnessScreen />}
            {activeTab === 'community' && <CommunityScreen />}
            {activeTab === 'profile' && <ProfileScreen onLogout={() => setScreen('splash')} />}
            {activeTab !== 'scanner' && <BottomNav active={activeTab} onChange={(s) => {
                if(s === 'profile') setActiveTab('profile');
                else setActiveTab(s);
            }} />}
            {activeTab === 'scanner' && (
                <button onClick={() => setActiveTab('dashboard')} className="fixed top-8 right-8 z-[60] w-10 h-10 rounded-full bg-white/10 border border-white/20 flex items-center justify-center backdrop-blur-xl">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2"><path d="M18 6L6 18M6 6l12 12" /></svg>
                </button>
            )}
            {/* Added special button for profile since it's not in the main 5 bottom tabs as icons but was requested in flow */}
            {activeTab !== 'scanner' && activeTab !== 'profile' && (
                <button onClick={() => setActiveTab('profile')} className="fixed top-8 right-8 w-10 h-10 rounded-full border border-white/10 overflow-hidden shadow-lg">
                    <div className="w-full h-full bg-gradient-to-tr from-cyan-500 to-blue-500" />
                </button>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
