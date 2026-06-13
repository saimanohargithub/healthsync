/* eslint-disable */
import { auth, db } from './firebase';
import { 
  createUserWithEmailAndPassword, 
  signInWithEmailAndPassword, 
  signOut,
  onAuthStateChanged
} from 'firebase/auth';
import type { User as FirebaseUser } from 'firebase/auth';
import { doc, setDoc, getDoc, updateDoc } from 'firebase/firestore';

export interface UserProfile {
  uid: string;
  name: string;
  email: string;
  points: number;
  level: number;
  todaySteps: number;
  challengesDone: number;
  createdAt: number;
  age?: string | number;
  height?: string | number;
  weight?: string | number;
  gender?: string;
  activityLevel?: string;
  goal?: string;
}

export const registerUser = async (email: string, password: string, name: string): Promise<FirebaseUser> => {
    try {
        const userCredential = await createUserWithEmailAndPassword(auth, email, password);
        const user = userCredential.user;
        
        // Create initial user profile
        const userProfile: UserProfile = {
            uid: user.uid,
            email: email,
            name: name,
            level: 1,
            todaySteps: 0,
            age: 0,
            height: 0,
            weight: 0,
            gender: '',
            activityLevel: '',
            goal: '',
            points: 0,
            challengesDone: 0,
            createdAt: Date.now()
        };

        await setDoc(doc(db, 'users', user.uid), userProfile);
        return user;
    } catch (error) {
        console.error("Error during registration:", error);
        throw error;
    }
};

export const loginUser = async (email: string, password: string): Promise<FirebaseUser> => {
    try {
        const userCredential = await signInWithEmailAndPassword(auth, email, password);
        return userCredential.user;
    } catch (error) {
        console.error("Error during login:", error);
        throw error;
    }
};

export const logoutUser = async (): Promise<void> => {
    try {
        await signOut(auth);
    } catch (error) {
        console.error("Error during logout:", error);
        throw error;
    }
};

export const getUserProfile = async (uid: string): Promise<UserProfile | null> => {
    try {
        const docRef = doc(db, 'users', uid);
        const docSnap = await getDoc(docRef);
        if (docSnap.exists()) {
            return docSnap.data() as UserProfile;
        } else {
            console.log("No such document!");
            return null;
        }
    } catch (error) {
        console.error("Error getting user profile:", error);
        throw error;
    }
};

export const updateUserProfile = async (uid: string, data: Partial<UserProfile>): Promise<void> => {
    try {
        const docRef = doc(db, 'users', uid);
        await updateDoc(docRef, data as any);
    } catch (error) {
        console.error("Error updating user profile:", error);
        throw error;
    }
};

// Observer for auth state changes
export const subscribeToAuthChanges = (callback: (user: FirebaseUser | null) => void) => {
    return onAuthStateChanged(auth, callback);
};
