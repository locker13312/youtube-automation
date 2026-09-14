package ua.privatbank.qa.tests;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import ua.privatbank.qa.core.Config;
import ua.privatbank.qa.core.DriverFactory;
import ua.privatbank.qa.core.ScreenshotListener;

/** Життєвий цикл драйвера. Нова сесія на кожен тест, щоб падіння одного не тягло решту. */
@Listeners(ScreenshotListener.class)
public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver();
        System.out.println("[setup] " + Config.browser() + ", headless=" + Config.headless()
                + ", " + Config.windowWidth() + "x" + Config.windowHeight());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
        driver = null;
    }
}
