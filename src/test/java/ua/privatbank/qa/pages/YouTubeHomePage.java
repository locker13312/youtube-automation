package ua.privatbank.qa.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ua.privatbank.qa.core.Config;

/** Головна сторінка YouTube. */
public class YouTubeHomePage extends BasePage {

    private static final By SEARCH_INPUT_NEW = By.cssSelector("input.ytSearchboxComponentInput");
    private static final By SEARCH_INPUT_CLASSIC = By.cssSelector("input#search");
    private static final By SEARCH_INPUT_BY_NAME = By.cssSelector("input[name='search_query']");

    private static final By CONSENT_BUTTONS = By.xpath(
            "//button[.//span[contains(., 'Прийняти все') or contains(., 'Accept all') or contains(., 'Принять все')]]"
                    + " | //button[@aria-label='Прийняти все' or @aria-label='Accept all']"
                    + " | //form[contains(@action,'consent')]//button");

    public YouTubeHomePage(WebDriver driver) {
        super(driver);
    }

    @Step("Відкрити головну сторінку YouTube")
    public YouTubeHomePage open() {
        driver.get(Config.baseUrl());
        waitForDocumentReady();
        dismissConsentIfPresent();
        waitVisible(By.tagName("body"));
        return this;
    }

    /** Банер згоди перекриває сторінку, але з'являється не завжди — крок необов'язковий. */
    public YouTubeHomePage dismissConsentIfPresent() {
        try {
            shortWait.until(ExpectedConditions.elementToBeClickable(CONSENT_BUTTONS)).click();
            waitForDocumentReady();
        } catch (TimeoutException e) {
            // банера немає
        }
        return this;
    }

    private WebElement searchInput() {
        return firstVisibleOf(SEARCH_INPUT_NEW, SEARCH_INPUT_CLASSIC, SEARCH_INPUT_BY_NAME);
    }

    /** Введення і відправка розділені: між ними за сценарієм стоїть розгортання вікна. */
    @Step("Ввести пошуковий запит: {query}")
    public YouTubeHomePage typeSearchQuery(String query) {
        WebElement input = searchInput();
        input.click();
        input.clear();
        input.sendKeys(query);
        wait.until(d -> query.equals(searchInput().getAttribute("value")));
        System.out.println("[step] запит: " + query);
        return this;
    }

    public String getSearchQueryValue() {
        return searchInput().getAttribute("value");
    }

    @Step("Відправити пошуковий запит")
    public SearchResultsPage submitSearch() {
        searchInput().sendKeys(Keys.ENTER);
        return new SearchResultsPage(driver);
    }
}
