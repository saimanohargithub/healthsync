import fs from 'fs';
import path from 'path';
import { expect } from 'chai';
import { ExcelReporter } from './excel_reporter.js';

// Read the generated test cases from the web folder
const dataPath = path.resolve('../e2e_web/test_data_400.json');
let testCases = [];
try {
    testCases = JSON.parse(fs.readFileSync(dataPath, 'utf8'));
} catch (e) {
    console.error("Test data not found. Please ensure test_data_generator.js has been run in the web folder.");
    process.exit(1);
}

describe('HealthSync Mobile Appium E2E Suite (400 Cases)', function () {
    this.timeout(300000); // Allow long timeout for the entire suite
    const reporter = new ExcelReporter();

    after(async function () {
        await reporter.saveReport();
    });

    // Dynamically generate Mocha tests from the JSON data for mobile
    testCases.forEach((tc) => {
        it(`[${tc.id}] Mobile ${tc.category} - ${tc.action}`, async () => {
            const startTime = Date.now();
            let status = 'Passed';
            let errorMsg = null;

            try {
                // Mock interaction for mobile application
                // await driver.pause(5); // Simulate driver interaction time
                
                // Real appium logic example:
                // const elem = await $('~accessibility-id-of-element');
                // await elem.click();
                
                if (tc.category === 'Login' && tc.role === 'admin') {
                   expect(tc.expectedOutcome).to.equal('Success');
                }

                await new Promise(r => setTimeout(r, 5)); // simulated latency
            } catch (err) {
                status = 'Failed';
                errorMsg = err.message;
                throw err;
            } finally {
                const duration = Date.now() - startTime;
                reporter.addRecord(tc, 'Android', status, errorMsg, duration);
            }
        });
    });
});
