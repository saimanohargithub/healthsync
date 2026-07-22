import http from 'k6/http';
import { check, sleep } from 'k6';

// Read Firebase Project ID from the environment
const PROJECT_ID = __ENV.FIREBASE_PROJECT_ID;

if (!PROJECT_ID) {
    throw new Error('Please specify FIREBASE_PROJECT_ID as an environment variable (e.g. k6 run -e FIREBASE_PROJECT_ID=my-project).');
}

// Baseline load test configuration: 100 virtual users for 1 minute
export const options = {
    stages: [
        { duration: '10s', target: 100 }, // Ramp up to 100 users
        { duration: '40s', target: 100 }, // Sustain 100 users
        { duration: '10s', target: 0 },   // Scale down
    ],
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95% of requests should be below 1000ms
        http_req_failed: ['rate<0.01'],    // Error rate should be less than 1%
    }
};

// Base URL for Firestore REST API
const baseUrl = `https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents`;

export default function () {
    // Simulate fetching the community feed, a common read operation
    const communityFeedUrl = `${baseUrl}/community_feed`;

    // Make the HTTP GET request
    const res = http.get(communityFeedUrl);

    // Assert that the response was successful (200 OK means we fetched documents successfully)
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    // Random sleep between 0.5s and 1.5s to simulate real user pacing
    sleep(Math.random() + 0.5); 
}
