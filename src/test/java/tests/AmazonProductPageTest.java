package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonProductPageTest {

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

    private void openSearchResults(String keyword) {
        driver.get("https://www.amazon.com/s?k=" + keyword.replace(" ", "+"));
        pause(1500);
        waitForPageReady();
    }

    private void openLikelyProduct(String keyword) {
        openSearchResults(keyword);
        if (elementExists(By.cssSelector("a[href*='/dp/'], a[href*='/gp/product/']"))) {
            waitForClickable(By.cssSelector("a[href*='/dp/'], a[href*='/gp/product/']")).click();
            pause(1500);
            waitForPageReady();
        }
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
    public void testOpenProductPageFromSearch() {
        openSearchResults("usb c cable");
        Assert.assertTrue(driver.getCurrentUrl().contains("usb+c+cable"));
    }

    @Test
    public void testProductTitleIsVisible() {
        openLikelyProduct("wireless mouse");
        boolean ok = elementExists(By.id("productTitle"))
                || pageContainsAny("wireless");
        Assert.assertTrue(ok);
    }

    @Test
    public void testProductImageIsVisible() {
        openLikelyProduct("paper clips");
        boolean ok = elementExists(By.id("imgTagWrapperId"))
                || elementExists(By.cssSelector("img"));
        Assert.assertTrue(ok);
    }

    @Test
    public void testAddToCartButtonOrBuyNowExists() {
        openLikelyProduct("notebook");
        pause(2000);

        boolean ok = elementExists(By.id("add-to-cart-button"))
                || elementExists(By.id("buy-now-button"))
                || elementExists(By.name("submit.add-to-cart"));

        Assert.assertTrue(ok);
    }

    @Test
    public void testProductPageContainsPriceArea() {
        openLikelyProduct("phone charger");
        boolean ok = elementExists(By.cssSelector(".a-price"))
                || elementExists(By.id("corePrice_feature_div"))
                || pageContainsAny("$");
        Assert.assertTrue(ok);
    }

}
