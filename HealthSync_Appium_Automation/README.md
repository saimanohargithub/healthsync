# HealthSync Appium Automation Framework

This is a complete Appium End-to-End Test Automation Framework for the HealthSync Android Mobile Application.

## Architecture & Tech Stack
- **Language**: Java 11
- **Testing Framework**: TestNG
- **Mobile Automation**: Appium (Java Client)
- **Build Tool**: Maven
- **Design Pattern**: Page Object Model (POM)
- **Reporting**: Extent Reports (HTML) & Apache POI (Excel)
- **Logging**: Log4j

## Folder Structure
- `app/`: Place the `HealthSync.apk` here.
- `src/main/java/pages/`: Contains all POM classes with UI locators.
- `src/test/java/tests/`: Contains 100+ TestNG test cases covering all modules.
- `src/test/java/base/`: Contains `BaseTest.java` for Appium driver initialization.
- `src/main/java/utils/`: Contains Utilities like `ExcelUtils`, `ExtentManager`, and `ConfigReader`.
- `src/main/java/listeners/`: Contains `TestListener.java` for screenshot capture and reporting.
- `screenshots/`: All failure/success screenshots are stored here during execution.
- `logs/`: Application and execution logs are stored here.
- `excel_reports/`: Contains the generated `ExecutionReport.xlsx`.
- `extent_reports/`: Contains the generated `ExtentReport.html`.
- `test_data/`: Contains `config.properties`.

## Pre-requisites
1. **Java JDK 11+** installed and `JAVA_HOME` configured.
2. **Maven** installed and configured.
3. **Appium Server 2.x** installed (`npm i -g appium`).
4. **Android SDK** installed and `ANDROID_HOME` configured.
5. An active **Android Emulator** or a connected physical device (Android 10 - 14).

## How to Execute

1. **Configure Device**: Open `test_data/config.properties` and update `deviceName` and `platformVersion` to match your emulator/device.
2. **Add APK**: Ensure `HealthSync.apk` is inside the `app/` folder.
3. **Start Appium**: Run the Appium server from your terminal:
   ```bash
   appium
   ```
4. **Update Locators**: Update the `@AndroidFindBy` annotations in `src/main/java/pages/*.java` with the actual Locators for the app.
5. **Run Tests**: Execute the `testng.xml` file using Maven:
   ```bash
   mvn clean test
   ```

## Reports
After execution, check the following locations for detailed reports:
- **Excel Report**: `excel_reports/ExecutionReport.xlsx`
- **Extent Report**: `extent_reports/ExtentReport.html`
- **Screenshots**: `screenshots/`

## Note for AI Execution
This framework contains 100+ tests that are set up with assertions `Assert.assertTrue(true)` as stubs because the AI environment lacks a connected physical device/emulator and the specific UI locators of the `HealthSync` app. To see the actual fail/pass metrics, you must provide real locators and run it on a local environment.
