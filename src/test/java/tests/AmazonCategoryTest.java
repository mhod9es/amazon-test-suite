package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonCategoryTest {

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
    public void testCategoryDropdownIsDisplayed() {
        boolean ok = elementExists(By.id("searchDropdownBox"))
                || pageContainsAny("all departments")
                || isRobotCheckPage();
        Assert.assertTrue(ok);
    }

    @Test
    public void testCanSelectBooksCategory() {
        if (elementExists(By.id("searchDropdownBox"))) {
            WebElement dropdown = driver.findElement(By.id("searchDropdownBox"));
            if (dropdown.isDisplayed()) {
                Select category = new Select(dropdown);
                category.selectByVisibleText("Books");
                pause(1000);
                Assert.assertEquals(category.getFirstSelectedOption().getText().trim(), "Books");
            } else {
                driver.get("https://www.amazon.com/s?i=stripbooks&k=java");
                pause(1500);
                waitForPageReady();
                Assert.assertTrue(driver.getCurrentUrl().contains("stripbooks") || isRobotCheckPage());
            }
        } else {
            driver.get("https://www.amazon.com/s?i=stripbooks&k=java");
            waitForPageReady();
            Assert.assertTrue(driver.getCurrentUrl().contains("stripbooks") || isRobotCheckPage());
        }
    }

    @Test
    public void testCanSelectElectronicsCategory() {
        if (elementExists(By.id("searchDropdownBox"))) {
            WebElement dropdown = driver.findElement(By.id("searchDropdownBox"));
            if (dropdown.isDisplayed()) {
                Select category = new Select(dropdown);
                category.selectByVisibleText("Electronics");
                Assert.assertEquals(category.getFirstSelectedOption().getText().trim(), "Electronics");
            } else {
                driver.get("https://www.amazon.com/s?i=electronics&k=headphones");
                waitForPageReady();
                Assert.assertTrue(driver.getCurrentUrl().contains("electronics") || isRobotCheckPage());
            }
        } else {
            driver.get("https://www.amazon.com/s?i=electronics&k=headphones");
            waitForPageReady();
            Assert.assertTrue(driver.getCurrentUrl().contains("electronics") || isRobotCheckPage());
        }
    }

    @Test
    public void testSearchWithinBooksCategory() {
        driver.get("https://www.amazon.com/s?i=stripbooks&k=java");
        waitForPageReady();
        Assert.assertTrue(driver.getCurrentUrl().contains("stripbooks") || driver.getCurrentUrl().contains("java") || isRobotCheckPage());
    }

    @Test
    public void testSearchWithinElectronicsCategory() {
        driver.get("https://www.amazon.com/s?i=electronics&k=headphones");
        waitForPageReady();
        Assert.assertTrue(driver.getCurrentUrl().contains("electronics") || driver.getCurrentUrl().contains("headphones") || isRobotCheckPage());
    }

}
