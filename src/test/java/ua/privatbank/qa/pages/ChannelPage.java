package ua.privatbank.qa.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.regex.Pattern;

/** Сторінка каналу. */
public class ChannelPage extends BasePage {

    private static final By HEADER_PAGE = By.cssSelector("yt-page-header-renderer");
    private static final By HEADER_TABBED = By.cssSelector("ytd-c4-tabbed-header-renderer");
    private static final By HEADER_BY_ID = By.cssSelector("#channel-header");
    private static final By HEADER_BROWSE = By.cssSelector("ytd-browse[page-subtype='channels']");

    private static final By[] HEADER = {HEADER_PAGE, HEADER_TABBED, HEADER_BY_ID, HEADER_BROWSE};

    // Кнопка підписки каналу живе у yt-flexible-actions-view-model і має два варіанти
    // обгортки. Прив'язка до хедера каналу обов'язкова: після SPA-переходу з /watch
    // у DOM лишається схована кнопка підписки сторінки відео, і в документі вона раніше.
    private static final By SUBSCRIBE_HEADER_BY_ARIA = By.cssSelector(
            "yt-page-header-renderer yt-flexible-actions-view-model button[aria-label^='Підписатися']");
    private static final By SUBSCRIBE_HEADER_VIEW_MODEL = By.cssSelector(
            "yt-page-header-renderer yt-flexible-actions-view-model yt-subscribe-button-view-model button");
    private static final By SUBSCRIBE_HEADER_BUTTON_VIEW_MODEL = By.cssSelector(
            "yt-page-header-renderer yt-flexible-actions-view-model button-view-model button");

    // Канал відкривається за одним із чотирьох форматів URL: /@handle, /channel/<id>, /c/<name>, /user/<name>.
    private static final Pattern CHANNEL_URL = Pattern.compile("youtube\\.com/(@|channel/|c/|user/)");

    private static final By SUBSCRIBE_CLASSIC = By.cssSelector("ytd-subscribe-button-renderer button");
    private static final By SUBSCRIBE_SHAPE = By.cssSelector("#subscribe-button-shape button");
    private static final By SUBSCRIBE_VIEW_MODEL = By.cssSelector("yt-subscribe-button-view-model button");
    private static final By SUBSCRIBE_BY_ID = By.cssSelector("#subscribe-button button");

    public ChannelPage(WebDriver driver) {
        super(driver);
    }

    public ChannelPage waitLoaded() {
        firstVisibleOf(HEADER);
        return this;
    }


    private WebElement subscribeButton() {
        return firstVisibleOf(SUBSCRIBE_HEADER_BY_ARIA, SUBSCRIBE_HEADER_VIEW_MODEL,
                SUBSCRIBE_HEADER_BUTTON_VIEW_MODEL,
                SUBSCRIBE_CLASSIC, SUBSCRIBE_SHAPE, SUBSCRIBE_VIEW_MODEL, SUBSCRIBE_BY_ID);
    }

    public String getSubscribeButtonText() {
        return subscribeButton().getText().trim();
    }

    public boolean isChannelUrl() {
        return CHANNEL_URL.matcher(getCurrentUrl()).find();
    }

    /** Без урахування регістру: у DOM «Підписатися», верхній регістр дає CSS text-transform. */
    public boolean subscribeButtonHasText(String expected) {
        return getSubscribeButtonText().equalsIgnoreCase(expected.trim());
    }

    @Step("Клік на кнопку підписки")
    public SignInPromptDialog clickSubscribe() {
        // Спершу банер, потім пошук кнопки: його зникнення зсуває верстку і робить елемент stale.
        dismissPromoBannerIfPresent();
        WebElement button = subscribeButton();
        System.out.println("[step] кнопка: '" + button.getText().trim() + "'");
        click(button);
        return new SignInPromptDialog(driver).waitVisible();
    }
}
