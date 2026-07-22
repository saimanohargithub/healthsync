import fs from 'fs';
import path from 'path';

// Procedurally generate 400 test cases
function generateTestCases() {
    const testCases = [];
    let idCounter = 1;

    // Categories
    const categories = ['Login', 'Navigation', 'FormValidation', 'Dashboard', 'Community'];
    const roles = ['admin', 'manager', 'user', 'guest'];

    for (let i = 0; i < 400; i++) {
        const category = categories[i % categories.length];
        const role = roles[i % roles.length];
        
        let action = '';
        let expectedOutcome = '';

        switch(category) {
            case 'Login':
                action = `Attempt login with ${role} credentials`;
                expectedOutcome = role === 'guest' ? 'Fail' : 'Success';
                break;
            case 'Navigation':
                action = `Navigate to ${role} restricted area`;
                expectedOutcome = role === 'guest' ? 'Redirect' : 'Success';
                break;
            case 'FormValidation':
                action = `Submit form with boundary value ${i * 10}`;
                expectedOutcome = 'Validation Check';
                break;
            case 'Dashboard':
                action = `Load dashboard widgets for ${role}`;
                expectedOutcome = 'Widgets Loaded';
                break;
            case 'Community':
                action = `View community feed post #${i}`;
                expectedOutcome = 'Post Visible';
                break;
        }

        testCases.push({
            id: `TC-${idCounter.toString().padStart(4, '0')}`,
            category,
            role,
            action,
            expectedOutcome,
            testData: {
                inputValue: `TestInput_${i}`,
                boundaryCheck: i * 10
            }
        });
        idCounter++;
    }

    return testCases;
}

const cases = generateTestCases();
const outputPath = path.join(process.cwd(), 'tests', 'e2e_web', 'test_data_400.json');

// Ensure directory exists
fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, JSON.stringify(cases, null, 2));

console.log(`Successfully generated ${cases.length} test cases at ${outputPath}`);
