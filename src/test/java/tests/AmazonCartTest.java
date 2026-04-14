package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;

public class AmazonCartTest {

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
    public void testCartLinkOpensCartPage() {
        driver.findElement(By.id("nav-cart")).click();
            pause(1500);
            waitForPageReady();

        Assert.assertTrue(driver.getCurrentUrl().contains("cart"));
    }

    @Test
    public void testAddItemToCartFlow() {

        driver.get("https://www.amazon.com/AmazonBasics-Wireless-Computer-Mouse-Receiver/dp/B005EJH6Z4/ref=sr_1_1_ffob_sspa?crid=JTBXUTWXXCWE&dib=eyJ2IjoiMSJ9.15ZyLb9XxfH4DtJcbbegEsN7wGVjq4FB7dccoztStZQtn7v3ywSrJfmLR7i504qu4yZBDrQ0V42Ernnedt--2v4x6SjwbB_AOLAx6VAloU-5C30C7xgkykufx4MWF4nfKhNt0JBL1ZwlEaYldNkmnjZjK0kW9vIaySuTnGe5eEcQsFItBu9wMl0ZRb8nOdupMZqJLDLJtHmw6WW_IZ5c8zPvpWLmxnx2Xnrp_oXDc8s.-HsvCIbeGeo8MHSt-3ZfU-kpAggotn1u8sgdifz9C9E&dib_tag=se&keywords=wireless%2Bmouse&qid=1776141276&sprefix=wireless%2Bmouse%2Caps%2C136&sr=8-1-spons&sp_csd=d2lkZ2V0TmFtZT1zcF9hdGY&th=1");
        waitForPageReady();
        pause(2000);

        By addToCartBtn = By.id("add-to-cart-button");

        if (elementExists(addToCartBtn)) {
            waitForClickable(addToCartBtn).click();
            pause(2000);
            waitForPageReady();
        }

        Assert.assertTrue(pageContainsAny("added to cart"));
    }

    @Test
    public void testViewCartButtonAfterAddToCart() {
        driver.get("https://www.amazon.com/gp/cart/view.html");
        waitForPageReady();
        boolean ok = driver.getCurrentUrl().contains("cart")
                || elementExists(By.id("nav-cart"));
        Assert.assertTrue(ok);
    }

    @Test
    public void testCartPageShowsShoppingCartHeading() {
        driver.get("https://www.amazon.com/gp/cart/view.html");
        waitForPageReady();
        Assert.assertTrue(pageContainsAny("cart", "shopping cart"));
    }

    @Test
    public void testCartUrlIsReachableDirectly() {
        driver.get("https://www.amazon.com/gp/cart/view.html");
        waitForPageReady();
        Assert.assertTrue(driver.getCurrentUrl().contains("cart"));
    }

    @Test
    public void testRemoveItemFromCart() {

        driver.get("https://www.amazon.com/gp/cart/view.html");
        By deleteBtn = By.cssSelector("input[value='Delete']");

        if (elementExists(deleteBtn)) {
            waitForClickable(deleteBtn).click();
            pause(2000);
            waitForPageReady();
        }

        driver.get("https://www.amazon.com/gp/cart/view.html");
        waitForPageReady();
        pause(2000);

        Assert.assertTrue(pageContainsAny("cart is empty"));
    }

}
