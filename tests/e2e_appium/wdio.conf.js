export const config = {
    runner: 'local',
    port: 4723, // Default Appium port
    specs: [
        './mobile_e2e_test.js'
    ],
    maxInstances: 1,
    capabilities: [{
        platformName: 'Android',
        'appium:deviceName': 'Android Emulator',
        'appium:automationName': 'UiAutomator2',
        // Update this path to point to your compiled .apk file
        'appium:app': 'path/to/your/app.apk', 
        'appium:appWaitActivity': '*'
    }],
    logLevel: 'info',
    bail: 0,
    waitforTimeout: 10000,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,
    framework: 'mocha',
    reporters: ['spec'],
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000
    },
}
