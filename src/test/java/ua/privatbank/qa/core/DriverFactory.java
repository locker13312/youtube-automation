package ua.privatbank.qa.core;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Створення і зберігання WebDriver. Драйвер у ThreadLocal — під паралельний запуск. */
public final class DriverFactory {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverFactory() {
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("Драйвер не створено");
        }
        return driver;
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static WebDriver createDriver() {
        WebDriver driver = switch (Config.browser()) {
            case "chrome" -> new ChromeDriver(chromeOptions());
            case "firefox" -> new FirefoxDriver(firefoxOptions());
            default -> throw new IllegalArgumentException("Непідтримуваний браузер: " + Config.browser());
        };

        // Стартуємо у вікні фіксованого розміру: розгортання на весь екран — окремий крок сценарію.
        driver.manage().window().setSize(new Dimension(Config.windowWidth(), Config.windowHeight()));

        DRIVER.set(driver);
        AllureEnvironment.write(driver);
        return driver;
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=" + Config.language());

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("intl.accept_languages", Config.language());
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-gpu");
        // Без цього YouTube частіше показує капчу автоматизованому браузеру.
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));

        if (Config.headless()) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=" + Config.windowWidth() + "," + Config.windowHeight());
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.addPreference("intl.accept_languages", Config.language());
        options.addPreference("dom.webnotifications.enabled", false);
        if (Config.headless()) {
            options.addArguments("-headless");
        }
        return options;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (WebDriverException alreadyClosed) {
                // Сесія вже могла завершитись після driver.close() в останньому кроці тесту.
            } finally {
                DRIVER.remove();
            }
        }
    }
}
