package base;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class BaseTest {
    private static ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();

    @BeforeMethod
    public void setUp() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(ConfigReader.getProperty("deviceName"))
                .setPlatformVersion(ConfigReader.getProperty("platformVersion"))
                .setPlatformName(ConfigReader.getProperty("platformName"))
                .setAppPackage(ConfigReader.getProperty("appPackage"))
                .setAppActivity(ConfigReader.getProperty("appActivity"))
                .setApp(System.getProperty("user.dir") + "/" + ConfigReader.getProperty("appPath"))
                .setNoReset(false);

        try {
            AppiumDriver appiumDriver = new AndroidDriver(new URL(ConfigReader.getProperty("appiumServer")), options);
            appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.set(appiumDriver);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static AppiumDriver getDriver() {
        return driver.get();
    }

    @AfterMethod
    public void tearDown() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }
}
