package ua.privatbank.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ua.privatbank.qa.core.Config;

import java.util.Arrays;
import java.util.List;

/** База для Page Object: драйвер, очікування, спільні дії. Assert-ів тут немає — вони в тестах. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final WebDriverWait shortWait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Config.timeout());
        this.shortWait = new WebDriverWait(driver, Config.shortTimeout());
    }

    protected WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected WebElement waitClickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Перший локатор із переліку, що дав видимий елемент. YouTube тримає кілька версій верстки
     * одночасно, тому локаторів кілька, але таймаут у них спільний на весь перелік: інакше перший
     * локатор вичерпував би своє очікування до кінця навіть тоді, коли елемент уже є під другим.
     */
    protected WebElement firstVisibleOf(By... locators) {
        try {
            return wait.until(d -> firstDisplayed(d, locators));
        } catch (TimeoutException notFound) {
            throw new NoSuchElementException(
                    "Жоден локатор не дав видимого елемента: " + Arrays.toString(locators) + diagnostics(), notFound);
        }
    }

    protected List<WebElement> firstNonEmptyOf(By... locators) {
        try {
            return wait.until(d -> {
                for (By locator : locators) {
                    try {
                        List<WebElement> found = d.findElements(locator);
                        if (!found.isEmpty()) {
                            return found;
                        }
                    } catch (InvalidSelectorException unsupported) {
                        // локатор не підтримується цим браузером — пробуємо наступний
                    }
                }
                return null;
            });
        } catch (TimeoutException empty) {
            throw new NoSuchElementException(
                    "Жоден локатор не дав елементів: " + Arrays.toString(locators) + diagnostics(), empty);
        }
    }

    /** Знімок без очікувань: перший видимий елемент серед переліку локаторів. */
    private WebElement firstDisplayed(SearchContext context, By[] locators) {
        for (By locator : locators) {
            try {
                for (WebElement element : context.findElements(locator)) {
                    if (element.isDisplayed()) {
                        return element;
                    }
                }
            } catch (InvalidSelectorException | StaleElementReferenceException ignored) {
                // локатор не підтримується або елемент перебудували між пошуком і перевіркою
            }
        }
        return null;
    }

    /**
     * Контекст сторінки у тексті помилки. Коли локатор перестає знаходитися після
     * зміни верстки, з нього одразу видно, чи ми взагалі на тій сторінці, чи впіймали
     * капчу або банер згоди.
     */
    protected String diagnostics() {
        try {
            return "\n  URL: " + driver.getCurrentUrl() + "\n  Title: " + driver.getTitle();
        } catch (WebDriverException e) {
            return "\n  (стан сторінки зчитати не вдалося: " + e.getMessage() + ")";
        }
    }

    protected WebElement firstIn(SearchContext context, By... locators) {
        for (By locator : locators) {
            List<WebElement> found = context.findElements(locator);
            for (WebElement element : found) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
            if (!found.isEmpty()) {
                return found.get(0);
            }
        }
        throw new NoSuchElementException("Усередині елемента не знайдено: " + Arrays.toString(locators));
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", element);
    }

    protected void click(WebElement element) {
        scrollIntoView(element);
        waitClickable(element).click();
    }

    /** Банер накриває будь-яку сторінку. Створюємо на місці: поле в конструкторі дало б рекурсію. */
    protected void dismissPromoBannerIfPresent() {
        new PromoBanner(driver).dismissIfPresent();
    }

    /** Мініплеєр лишається поверх видачі після повернення зі сторінки відео. */
    protected void dismissMiniPlayerIfPresent() {
        new MiniPlayer(driver).dismissIfPresent();
    }

    protected void waitForDocumentReady() {
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    protected boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /**
     * Видимий елемент із точним текстом. Саме видимий: YouTube тримає в розмітці й приховані
     * копії написів. normalize-space прибирає зайві пробіли, часткове входження не проходить.
     */
    protected boolean hasVisibleExactText(SearchContext container, String text) {
        By byExactText = By.xpath(".//*[normalize-space(text())=" + xpathLiteral(text) + "]");
        try {
            return shortWait.until(d -> container.findElements(byExactText).stream().anyMatch(WebElement::isDisplayed));
        } catch (TimeoutException notVisible) {
            return false;
        }
    }

    /** Екранування рядка для XPath — текст може містити апостроф. */
    protected static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        return "concat('" + value.replace("'", "', \"'\", '") + "')";
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
