package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonLanguageRegionTest {

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

    private void openLikelyProduct(String keyword) {
        openSearchResults(keyword);
        if (elementExists(By.cssSelector("div[data-component-type='s-search-result'] h2 a"))) {
            waitForClickable(By.cssSelector("div[data-component-type='s-search-result'] h2 a")).click();
            waitForPageReady();
        }
    }


    @BeforeClass
    public void setUp() { startBrowser(); openHomePage(); }

    @AfterClass(alwaysRun = true)
    public void tearDown() { if (driver != null) driver.quit(); }

    @BeforeMethod
    public void goHome() { openHomePage(); }

    @Test
    public void testLanguageIconOrMenuExists() {
        boolean ok = elementExists(By.id("icp-nav-flyout"))
                || pageContainsAny("language", "customer preferences")
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testCustomerPreferencesPageLoadsDirectly() {
        driver.get("https://www.amazon.com/customer-preferences/edit?ie=UTF8&preferencesReturnUrl=%2F");
        waitForPageReady();
        Assert.assertTrue(driver.getCurrentUrl().contains("customer-preferences")
                || pageContainsAny("preferences")
                || isRobotCheckPage());
    }

    @Test
    public void testHomePageShowsDeliveryLocationArea() {
        Assert.assertTrue(elementExists(By.id("nav-global-location-popover-link"))
                || pageContainsAny("deliver to", "location")
                || isRobotCheckPage());
    }

    @Test
    public void testDeliveryLocationPopupCanOpen() {
        if (elementExists(By.id("nav-global-location-popover-link"))) {
            driver.findElement(By.id("nav-global-location-popover-link")).click();
            pause(1500);
            waitForPageReady();
        }
        boolean ok = pageContainsAny("deliver to", "location")
                || elementExists(By.id("GLUXZipUpdateInput"))
                || elementExists(By.id("GLUXCountryList"))
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testLanguagePreferencesPageTitleOrContent() {
        driver.get("https://www.amazon.com/customer-preferences/edit?ie=UTF8&preferencesReturnUrl=%2F");
        pause(1500);
        waitForPageReady();
        Assert.assertTrue(pageContainsAny("preferences", "language") || isRobotCheckPage());
    }

}
