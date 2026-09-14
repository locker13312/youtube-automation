package ua.privatbank.qa.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Результати пошуку.
 *
 * Позиції рахуються тільки серед карток відео: у видачі є ще реклама, полиці Shorts,
 * канали і плейлисти, і при підрахунку всіх підряд «четверте зверху» щоразу різне.
 */
public class SearchResultsPage extends BasePage {

    // Shorts тепер приходять у видачі тим самим ytd-video-renderer, тільки з посиланням
    // на /shorts/<id>. Сторінка Shorts не має /watch в URL і ламає крок 6, тому позиції
    // рахуємо лише серед карток, чиє посилання веде на /watch.
    private static final By RESULTS_WATCH_ONLY =
            By.cssSelector("ytd-item-section-renderer ytd-video-renderer:has(a#video-title[href*='/watch'])");
    private static final By RESULTS_NEW_LAYOUT_WATCH_ONLY =
            By.cssSelector("yt-lockup-view-model:has(a[href*='/watch'])");

    private static final By RESULTS_PRIMARY = By.cssSelector("ytd-item-section-renderer ytd-video-renderer");
    private static final By RESULTS_FALLBACK = By.cssSelector("ytd-video-renderer");
    private static final By RESULTS_NEW_LAYOUT = By.cssSelector("yt-lockup-view-model");

    /** Порядок важливий: спершу суворі локатори без Shorts, потім старі — як запас. */
    private static final By[] RESULTS = {
            RESULTS_WATCH_ONLY, RESULTS_NEW_LAYOUT_WATCH_ONLY,
            RESULTS_PRIMARY, RESULTS_FALLBACK, RESULTS_NEW_LAYOUT
    };

    private static final By TITLE_CLASSIC = By.cssSelector("a#video-title");
    private static final By TITLE_LINK = By.cssSelector("#video-title-link");
    private static final By TITLE_NEW = By.cssSelector("a.yt-lockup-metadata-view-model__title");
    private static final By TITLE_HEADING = By.cssSelector("h3 a");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public SearchResultsPage waitLoaded() {
        wait.until(ExpectedConditions.urlContains("/results"));
        videoResults();
        return this;
    }

    /** Знімок поточного стану списку, без очікувань — щоб не вкладати wait у wait. */
    private List<WebElement> findResultsNow() {
        for (By locator : RESULTS) {
            try {
                List<WebElement> found = driver.findElements(locator);
                if (!found.isEmpty()) {
                    return found;
                }
            } catch (InvalidSelectorException unsupported) {
                // :has() не підтримують старі версії Firefox — переходимо до запасного локатора
            }
        }
        return List.of();
    }

    private List<WebElement> videoResults() {
        return firstNonEmptyOf(RESULTS);
    }

    /** Видача підвантажується порціями, тому перед зверненням до 4-ї позиції чекаємо потрібну кількість. */
    public SearchResultsPage waitForAtLeastResults(int minimum) {
        wait.withMessage("У видачі не з'явилося щонайменше " + minimum + " відео" + diagnostics())
                .until(d -> findResultsNow().size() >= minimum);
        return this;
    }

    private WebElement resultAt(int position) {
        List<WebElement> results = videoResults();
        if (results.size() < position) {
            throw new IllegalStateException("У видачі " + results.size() + " відео, потрібна позиція " + position);
        }
        return results.get(position - 1);
    }

    private WebElement titleLinkOf(WebElement result) {
        return firstIn(result, TITLE_CLASSIC, TITLE_LINK, TITLE_NEW, TITLE_HEADING);
    }

    public String titleAt(int position) {
        return titleLinkOf(resultAt(position)).getText().trim();
    }

    /** Вибір позиції — це клік по картці. Назад до видачі — {@link VideoPage#backToResults()}. */
    @Step("Відкрити відео з позиції №{position}")
    public VideoPage openVideo(int position) {
        // Мініплеєр і банер перекривають картки. Закриваємо обидва ДО пошуку елемента:
        // їх зникнення зсуває верстку, і знайдений наперед WebElement стає stale.
        dismissMiniPlayerIfPresent();
        dismissPromoBannerIfPresent();
        WebElement title = titleLinkOf(resultAt(position));
        System.out.println("[step] відкриваємо позицію " + position + ": " + title.getText().trim());
        click(title);
        return new VideoPage(driver).waitLoaded();
    }
}
