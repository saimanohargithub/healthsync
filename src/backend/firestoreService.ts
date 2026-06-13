/* eslint-disable */
import { db } from './firebase';
import { 
  collection, 
  doc, 
  getDoc,
  getDocs,  
  query, 
  orderBy, 
  limit,
  setDoc,
  onSnapshot,
  serverTimestamp
} from 'firebase/firestore';
import type { Unsubscribe } from 'firebase/firestore';

export interface Challenge {
  id: string;
  name: string;
  currentProgress: number;
  goal: number;
  rewardPoints: number;
  type: string;
  completed: boolean;
  joinedAt: number;
  status: string;
}

export interface CommunityActivity {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  content: string;
  likes: number;
  comments: number;
  timestamp: any; // Firebase Timestamp or number
}

export interface GlobalStats {
  steps: string;
  sleep: string;
}

export interface MealPlan {
  id: string; // date string like 'YYYY-MM-DD'
  breakfast: any;
  lunch: any;
  dinner: any;
  calories: number;
}

export interface SleepLog {
  id: string; // date like 'YYYY-MM-DD'
  hours: number;
  score: number;
}

export interface MoodLog {
  id: string; // timestamp string
  mood: string;
  note: string;
  timestamp: number;
}

export interface WaterLog {
  id: string;
  amount: number; // in ml
  date: string; // YYYY-MM-DD
  timestamp: number;
}

export interface UserProfile {
  name: string;
  email: string;
  points: number;
  level: number;
  todaySteps: number;
  age?: string | number;
  height?: string | number;
  weight?: string | number;
  gender?: string;
  activityLevel?: string;
  goal?: string;
}

export interface PredictiveHealth {
  generatedAt: number;
  wellnessScore: number;
  healthForecast: string;
  sleepForecast: string;
  hydrationForecast: string;
  nutritionForecast: string;
  recommendations: string[];
}

export const subscribeToUserProfile = (uid: string, callback: (profile: UserProfile | null) => void): Unsubscribe => {
  const docRef = doc(db, 'users', uid);
  return onSnapshot(docRef, (docSnap) => {
    if (docSnap.exists()) {
      callback(docSnap.data() as UserProfile);
    } else {
      callback(null);
    }
  }, (error) => {
    console.warn("UserProfile snapshot error (likely auth transition):", error.message);
  });
};

export const getUserActiveChallenges = async (uid: string): Promise<Challenge[]> => {
  const q = query(collection(db, 'users', uid, 'joinedChallenges'));
  const querySnapshot = await getDocs(q);
  return querySnapshot.docs.map(doc => {
    const data = doc.data();
    return {
      id: doc.id,
      name: data.challengeName || data.name || '',
      currentProgress: data.progress || data.currentProgress || 0,
      goal: data.target || data.goal || 0,
      rewardPoints: data.rewardPoints || 0,
      type: data.type || '',
      completed: data.completed || false,
      joinedAt: data.joinedAt || 0,
      status: data.status || 'ACTIVE'
    } as Challenge;
  });
};

export const getCommunityFeed = async (): Promise<CommunityActivity[]> => {
  const q = query(
    collection(db, 'community_feed'),
    orderBy('timestamp', 'desc'),
    limit(20)
  );
  const querySnapshot = await getDocs(q);
  return querySnapshot.docs.map(doc => ({
    id: doc.id,
    ...doc.data()
  } as CommunityActivity));
};

export const subscribeToCommunityFeed = (callback: (feed: CommunityActivity[]) => void): Unsubscribe => {
  const q = query(collection(db, 'community_feed'), orderBy('timestamp', 'desc'), limit(50));
  return onSnapshot(q, (snapshot) => {
    const feed = snapshot.docs.map(doc => {
      const data = doc.data();
      // Handle serverTimestamp which might be null temporarily while pending
      const timestamp = data.timestamp?.toMillis ? data.timestamp.toMillis() : data.timestamp;
      return {
        id: doc.id,
        ...data,
        timestamp
      } as CommunityActivity;
    });
    callback(feed);
  }, (error) => {
    console.warn("CommunityFeed snapshot error:", error.message);
  });
};

export const postToCommunityFeed = async (uid: string, content: string): Promise<void> => {
  // First, get the user's name and email
  let userName = 'Anonymous';
  let userEmail = '';
  try {
    const userDoc = await getDoc(doc(db, 'users', uid));
    if (userDoc.exists()) {
      const userData = userDoc.data();
      userName = userData.name || 'Anonymous';
      userEmail = userData.email || '';
    }
  } catch (err) {
    console.warn("Could not fetch user profile for community post:", err);
  }

  const newPostRef = doc(collection(db, 'community_feed'));
  await setDoc(newPostRef, {
    userId: uid,
    userName,
    userEmail,
    content,
    likes: 0,
    comments: 0,
    timestamp: serverTimestamp()
  });
};

export const getGlobalStats = async (): Promise<GlobalStats> => {
  const docRef = doc(db, 'system', 'global_stats');
  const docSnap = await getDoc(docRef);
  if (docSnap.exists()) {
    const data = docSnap.data();
    return {
      steps: data.totalSteps || '0',
      sleep: data.avgSleep || '0'
    };
  }
  return { steps: '0', sleep: '0' };
};

export const getMealPlans = async (uid: string): Promise<MealPlan[]> => {
  const q = query(collection(db, 'users', uid, 'meal_plans'));
  const querySnapshot = await getDocs(q);
  return querySnapshot.docs.map(doc => ({
    id: doc.id,
    ...doc.data()
  } as MealPlan));
};

export const subscribeToMealPlans = (uid: string, callback: (plans: MealPlan[]) => void): Unsubscribe => {
  const q = query(collection(db, 'users', uid, 'meal_plans'));
  return onSnapshot(q, (snapshot) => {
    const plans = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    } as MealPlan));
    callback(plans);
  }, (error) => {
    console.warn("MealPlans snapshot error:", error.message);
  });
};

export const saveMealPlan = async (uid: string, date: string, data: Partial<MealPlan>): Promise<void> => {
  await setDoc(doc(db, 'users', uid, 'meal_plans', date), data, { merge: true });
};

export const getSleepLogs = async (uid: string): Promise<SleepLog[]> => {
  const q = query(collection(db, 'users', uid, 'sleep_logs'), orderBy('id', 'desc'));
  const querySnapshot = await getDocs(q);
  return querySnapshot.docs.map(doc => ({
    id: doc.id,
    ...doc.data()
  } as SleepLog));
};

export const subscribeToSleepLogs = (uid: string, callback: (logs: SleepLog[]) => void): Unsubscribe => {
  const q = query(collection(db, 'users', uid, 'sleep_logs'), orderBy('id', 'desc'));
  return onSnapshot(q, (snapshot) => {
    const logs = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    } as SleepLog));
    callback(logs);
  }, (error) => {
    console.warn("SleepLogs snapshot error:", error.message);
  });
};

export const saveSleepLog = async (uid: string, date: string, data: Partial<SleepLog>): Promise<void> => {
  await setDoc(doc(db, 'users', uid, 'sleep_logs', date), data, { merge: true });
};

export const getMoodLogs = async (uid: string): Promise<MoodLog[]> => {
  const q = query(collection(db, 'users', uid, 'mood_logs'), orderBy('timestamp', 'desc'));
  const querySnapshot = await getDocs(q);
  return querySnapshot.docs.map(doc => ({
    id: doc.id,
    ...doc.data()
  } as MoodLog));
};

export const subscribeToMoodLogs = (uid: string, callback: (logs: MoodLog[]) => void): Unsubscribe => {
  const q = query(collection(db, 'users', uid, 'mood_logs'), orderBy('timestamp', 'desc'));
  return onSnapshot(q, (snapshot) => {
    const logs = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    } as MoodLog));
    callback(logs);
  }, (error) => {
    console.warn("MoodLogs snapshot error:", error.message);
  });
};

export const saveMoodLog = async (uid: string, data: MoodLog): Promise<void> => {
  await setDoc(doc(db, 'users', uid, 'mood_logs', data.id), data, { merge: true });
};

export const subscribeToWaterLogs = (uid: string, callback: (logs: WaterLog[]) => void): Unsubscribe => {
  const q = query(collection(db, 'users', uid, 'water_logs'), orderBy('timestamp', 'desc'));
  return onSnapshot(q, (snapshot) => {
    const logs = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    } as WaterLog));
    callback(logs);
  }, (error) => {
    console.warn("WaterLogs snapshot error:", error.message);
  });
};

export const saveWaterLog = async (uid: string, amount: number): Promise<void> => {
  const now = new Date();
  const dateStr = now.toISOString().split('T')[0];
  const id = now.getTime().toString();
  
  const data: WaterLog = {
    id,
    amount,
    date: dateStr,
    timestamp: now.getTime()
  };
  
  await setDoc(doc(db, 'users', uid, 'water_logs', id), data, { merge: true });
};

export const subscribeToPredictiveAnalysis = (uid: string, callback: (data: PredictiveHealth | null) => void): Unsubscribe => {
  const docRef = doc(db, 'users', uid, 'predictive_health', 'latest');
  return onSnapshot(docRef, (docSnap) => {
    if (docSnap.exists()) {
      callback(docSnap.data() as PredictiveHealth);
    } else {
      callback(null);
    }
  }, (error) => {
    console.warn("PredictiveHealth snapshot error:", error.message);
  });
};

export const savePredictiveAnalysis = async (uid: string, data: PredictiveHealth): Promise<void> => {
  await setDoc(doc(db, 'users', uid, 'predictive_health', 'latest'), data, { merge: true });
};
