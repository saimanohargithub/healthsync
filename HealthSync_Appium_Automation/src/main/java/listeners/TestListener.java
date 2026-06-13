package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.ExcelUtils;
import utils.ExtentManager;
import base.BaseTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TestListener implements ITestListener {
    private static ExtentReports extent = ExtentManager.getInstance();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        ExcelUtils.createExcelReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, "Test Passed");
        String screenshotPath = takeScreenshot(result.getMethod().getMethodName() + "_pass");
        test.get().addScreenCaptureFromPath(screenshotPath);
        
        ExcelUtils.updateTestResult(
            result.getMethod().getMethodName(),
            result.getTestClass().getName(),
            result.getMethod().getDescription(),
            "PASS",
            String.valueOf(result.getEndMillis() - result.getStartMillis()) + "ms",
            "Android Emulator",
            screenshotPath,
            "Execution completed successfully"
        );
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().log(Status.FAIL, "Test Failed: " + result.getThrowable());
        String screenshotPath = takeScreenshot(result.getMethod().getMethodName() + "_fail");
        test.get().addScreenCaptureFromPath(screenshotPath);

        ExcelUtils.updateTestResult(
            result.getMethod().getMethodName(),
            result.getTestClass().getName(),
            result.getMethod().getDescription(),
            "FAIL",
            String.valueOf(result.getEndMillis() - result.getStartMillis()) + "ms",
            "Android Emulator",
            screenshotPath,
            result.getThrowable().getMessage()
        );
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().log(Status.SKIP, "Test Skipped: " + result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    private String takeScreenshot(String methodName) {
        if (BaseTest.getDriver() == null) {
            return "";
        }
        File srcFile = ((TakesScreenshot) BaseTest.getDriver()).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String destPath = "screenshots/" + methodName + "_" + timestamp + ".png";
        try {
            File destFile = new File(destPath);
            if (!destFile.getParentFile().exists()) {
                destFile.getParentFile().mkdirs();
            }
            Files.copy(srcFile.toPath(), destFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destPath;
    }
}
