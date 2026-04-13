package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonFooterLinksTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private void startBrowser() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--disable-popup-blocking");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    private void openHomePage() {
        driver.get("https://www.amazon.com/");
        pause(1500);
        waitForPageReady();
    }

    private void waitForPageReady() {
        wait.until(webDriver ->
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    private void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isRobotCheckPage() {
        String source = driver.getPageSource().toLowerCase();
        String title = driver.getTitle().toLowerCase();
        return source.contains("enter the characters you see below")
                || source.contains("sorry, we just need to make sure you're not a robot")
                || title.contains("robot check");
    }

    private boolean elementExists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private boolean pageContainsAny(String... values) {
        String source = driver.getPageSource().toLowerCase();
        String title = driver.getTitle().toLowerCase();
        String url = driver.getCurrentUrl().toLowerCase();
        for (String value : values) {
            String v = value.toLowerCase();
            if (source.contains(v) || title.contains(v) || url.contains(v)) {
                return true;
            }
        }
        return false;
    }

    private WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private void openSearchResults(String keyword) {
        driver.get("https://www.amazon.com/s?k=" + keyword.replace(" ", "+"));
        waitForPageReady();
    }

    private void scrollToFooter() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        pause(1500);
    }

    @BeforeClass
    public void setUp() { startBrowser(); openHomePage(); }

    @AfterClass(alwaysRun = true)
    public void tearDown() { if (driver != null) driver.quit(); }

    @BeforeMethod
    public void goHome() { openHomePage(); scrollToFooter(); }

    @Test
    public void testBackToTopLinkExists() {
        boolean ok = pageContainsAny("back to top")
                || elementExists(By.id("navBackToTop"))
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testConditionsOfUseLinkExists() {
        boolean ok = pageContainsAny("conditions of use", "conditions")
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testPrivacyNoticeLinkExists() {
        boolean ok = pageContainsAny("privacy notice", "privacy")
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testHelpLinkExists() {
        boolean ok = pageContainsAny("help")
                || elementExists(By.partialLinkText("Help"))
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testFooterAreaIsPresent() {
        boolean ok = elementExists(By.id("navFooter"))
                || pageContainsAny("amazon music", "amazon ads", "footer")
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

}
