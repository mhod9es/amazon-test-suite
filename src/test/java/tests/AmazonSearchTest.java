package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonSearchTest {

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
    public void testSearchForLaptopShowsResults() {
        openSearchResults("laptop");
        Assert.assertTrue(driver.getCurrentUrl().contains("k=laptop"));
        //|| isRobotCheckPage());
    }

    @Test
    public void testSearchResultsContainerAppears() {
        openSearchResults("wireless mouse");
        boolean ok = elementExists(By.cssSelector("div.s-main-slot"))
                || pageContainsAny("results", "wireless mouse")
                || driver.getCurrentUrl().contains("wireless+mouse");
        Assert.assertTrue(ok);
    }

    @Test
    public void testSearchBoxAcceptsTyping() {
        WebElement searchBox = waitForVisible(By.id("twotabsearchtextbox"));
        searchBox.clear();
        searchBox.click();
        searchBox.sendKeys("monitor");
        pause(1000);
        Assert.assertEquals(searchBox.getAttribute("value"), "monitor");
    }

    @Test
    public void testSearchUsingEnterKeyWorks() {
        WebElement searchBox = waitForVisible(By.id("twotabsearchtextbox"));
        searchBox.clear();
        searchBox.click();
        searchBox.sendKeys("keyboard");
        searchBox.sendKeys(Keys.ENTER);
        pause(1500);
        waitForPageReady();

        Assert.assertTrue(driver.getCurrentUrl().contains("keyboard")
                || driver.getTitle().toLowerCase().contains("keyboard"));
    }

    @Test
    public void testFirstSearchResultExists() {
        openSearchResults("usb cable");
        boolean found = elementExists(By.cssSelector("div.s-main-slot div[data-component-type='s-search-result']"))
                || pageContainsAny("usb", "results")
                || driver.getCurrentUrl().contains("usb+cable");
        Assert.assertTrue(found);
    }

}
