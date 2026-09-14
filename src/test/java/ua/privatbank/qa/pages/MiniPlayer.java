package ua.privatbank.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Мініплеєр YouTube (ytd-miniplayer).
 *
 * Після переходу «назад» зі сторінки перегляду YouTube не зупиняє відтворення, а згортає
 * відео у вікно в правому нижньому куті поверх видачі. Воно накриває нижні картки і
 * перехоплює клік по них, тому крок 6 (запуск четвертого відео) міг не дійти до /watch.
 *
 * Кнопка закриття проявляється лише під курсором: без наведення вона є в DOM, але
 * має нульовий розмір. Тому спершу наведення, потім справжній клік по кнопці —
 * як і з промо-банером, JS-клік приховав би саму проблему перекриття.
 */
public class MiniPlayer extends BasePage {

    // Ознака розгорнутого мініплеєра — клас на хості. Атрибут active лишився від старішої
    // верстки і на поточній не проставляється, тому він запасний, а не основний.
    private static final By PLAYER_VISIBLE = By.cssSelector("ytd-miniplayer.ytdMiniplayerComponentVisible");
    private static final By PLAYER_ACTIVE = By.cssSelector("ytd-miniplayer[active]");
    private static final By PLAYER_HOST = By.cssSelector("ytd-miniplayer");

    private static final By[] PLAYER = {PLAYER_VISIBLE, PLAYER_ACTIVE, PLAYER_HOST};

    private static final By CLOSE_PLAYER_CONTROL = By.cssSelector("button.ytp-miniplayer-close-button");
    private static final By CLOSE_BY_ID = By.cssSelector("ytd-miniplayer #close-button button");
    private static final By CLOSE_BY_ARIA = By.cssSelector("ytd-miniplayer button[aria-label^='Закрити']");

    private static final By[] CLOSE = {CLOSE_PLAYER_CONTROL, CLOSE_BY_ID, CLOSE_BY_ARIA};

    public MiniPlayer(WebDriver driver) {
        super(driver);
    }

    /** Мініплеєр з'являється не завжди, тому крок необов'язковий і не валить тест. */
    public void dismissIfPresent() {
        try {
            WebElement player = firstVisible(PLAYER);
            if (player == null) {
                return;
            }
            new Actions(driver).moveToElement(player).perform();

            WebElement close = shortWait.until(d -> firstVisible(CLOSE));
            close.click();
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(PLAYER_HOST));
            System.out.println("[step] мініплеєр закрито");
        } catch (TimeoutException | ElementNotInteractableException | StaleElementReferenceException playerVanished) {
            // мініплеєр зник сам або не встиг показати кнопку — сценарію це не заважає
        }
    }

    /** Без очікувань: наявність перевіряємо миттєво, бо частіше за все мініплеєра немає. */
    private WebElement firstVisible(By[] locators) {
        for (By locator : locators) {
            List<WebElement> found = driver.findElements(locator);
            for (WebElement element : found) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
        }
        return null;
    }
}
