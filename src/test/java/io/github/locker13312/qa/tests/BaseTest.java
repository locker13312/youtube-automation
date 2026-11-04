package io.github.locker13312.qa.tests;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import io.github.locker13312.qa.core.Config;
import io.github.locker13312.qa.core.DriverFactory;
import io.github.locker13312.qa.core.ScreenshotListener;

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
