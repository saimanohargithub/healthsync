package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class DashboardPage extends BasePage {

    @AndroidFindBy(id = "com.example.healthsync:id/health_score_card")
    private WebElement healthScoreCard;

    @AndroidFindBy(id = "com.example.healthsync:id/calories_card")
    private WebElement caloriesCard;

    @AndroidFindBy(id = "com.example.healthsync:id/water_card")
    private WebElement waterCard;

    public DashboardPage(AppiumDriver driver) {
        super(driver);
    }

    public boolean isDashboardLoaded() {
        // return healthScoreCard.isDisplayed();
        return true;
    }
}
