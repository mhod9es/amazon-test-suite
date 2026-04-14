package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonNavigationMenuTest {

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

    private void clickElement(By locator) {
        WebElement element = waitForClickable(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        pause(800);
        element.click();
        pause(2000);
        waitForPageReady();
    }

    @BeforeClass
    public void setUp() {
        startBrowser();
        openHomePage();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeMethod
    public void goHome() {
        openHomePage();
    }

    @Test
    public void testHamburgerMenuIsVisible() {
        Assert.assertTrue(
                elementExists(By.id("nav-hamburger-menu")),
                "Hamburger menu is not visible.");
    }

    @Test
    public void testHamburgerMenuOpens() {
        clickElement(By.id("nav-hamburger-menu"));

        Assert.assertTrue(elementExists(By.id("hmenu-content")),
                "Hamburger menu did not open.");
    }

    @Test
    public void testTodaysDealsLinkWorks() {
        clickElement(By.linkText("Today's Deals"));

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("deals"),
                "Today's Deals link did not open the correct page.");
    }

    @Test
    public void testReturnsAndOrdersLinkWorks() {
        clickElement(By.id("nav-orders"));

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("yourorders"),
                "Returns & Orders link did not open the correct page.");
    }

    @Test
    public void testAccountLinkWorks() {
        clickElement(By.id("nav-link-accountList"));

        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("ap/signin"),
                "Account link did not open the correct page.");
    }
}
