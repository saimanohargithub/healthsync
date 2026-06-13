package pages;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

public class AuthenticationPage extends BasePage {

    @AndroidFindBy(id = "com.example.healthsync:id/email_edit_text")
    private WebElement emailInput;

    @AndroidFindBy(id = "com.example.healthsync:id/password_edit_text")
    private WebElement passwordInput;

    @AndroidFindBy(id = "com.example.healthsync:id/login_button")
    private WebElement loginButton;

    @AndroidFindBy(id = "com.example.healthsync:id/register_link")
    private WebElement registerLink;

    public AuthenticationPage(AppiumDriver driver) {
        super(driver);
    }

    public void login(String email, String password) {
        // emailInput.sendKeys(email);
        // passwordInput.sendKeys(password);
        // loginButton.click();
    }

    public void clickRegister() {
        // registerLink.click();
    }
}
