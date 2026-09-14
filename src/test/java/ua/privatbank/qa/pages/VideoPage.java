package ua.privatbank.qa.pages;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Сторінка перегляду відео. */
public class VideoPage extends BasePage {

    // Кожен варіант верстки — окрема константа: через кому вийшов би один локатор.
    private static final By PLAYER_BY_ID = By.cssSelector("#movie_player");
    private static final By PLAYER_VIDEO = By.cssSelector("ytd-player video");
    private static final By[] PLAYER = {PLAYER_BY_ID, PLAYER_VIDEO};

    private static final By OWNER_BY_ID = By.cssSelector("#owner");
    private static final By OWNER_RENDERER = By.cssSelector("ytd-video-owner-renderer");
    private static final By[] OWNER_SECTION = {OWNER_BY_ID, OWNER_RENDERER};

    private static final By AVATAR_OWNER = By.cssSelector("#owner #avatar");
    private static final By AVATAR_RENDERER = By.cssSelector("ytd-video-owner-renderer #avatar");
    private static final By AVATAR_LINK = By.cssSelector("a#avatar-link");
    private static final By AVATAR_THUMBNAIL = By.cssSelector("#channel-thumbnail");

    private static final By CHANNEL_NAME_OWNER = By.cssSelector("#owner ytd-channel-name a");
    private static final By CHANNEL_NAME_RENDERER = By.cssSelector("ytd-video-owner-renderer #channel-name a");
    private static final By[] CHANNEL_NAME = {CHANNEL_NAME_OWNER, CHANNEL_NAME_RENDERER};

    public VideoPage(WebDriver driver) {
        super(driver);
    }

    public VideoPage waitLoaded() {
        wait.until(ExpectedConditions.urlContains("/watch"));
        firstVisibleOf(PLAYER);
        firstVisibleOf(OWNER_SECTION);
        return this;
    }

    public String getVideoTitle() {
        return firstVisibleOf(
                By.cssSelector("h1.ytd-watch-metadata yt-formatted-string"),
                By.cssSelector("h1.title yt-formatted-string"),
                By.cssSelector("h1")
        ).getText().trim();
    }

    public String getChannelName() {
        return firstVisibleOf(CHANNEL_NAME).getText().trim();
    }

    /** Аватар рендериться пізніше за плеєр, тому спершу чекаємо блок автора. */
    @Step("Клік на аватар автора відео")
    public ChannelPage clickOwnerAvatar() {
        firstVisibleOf(OWNER_SECTION);
        // Промо-банер унизу вікна накриває блок автора і перехоплює клік по аватару.
        dismissPromoBannerIfPresent();
        WebElement avatar = firstVisibleOf(AVATAR_OWNER, AVATAR_RENDERER, AVATAR_LINK, AVATAR_THUMBNAIL);
        System.out.println("[step] аватар автора: " + safeChannelName());
        click(avatar);
        return new ChannelPage(driver).waitLoaded();
    }

    /** Повернення до видачі: крок 5 відкриває позицію, а далі працюємо знову зі списком. */
    @Step("Повернутися до результатів пошуку")
    public SearchResultsPage backToResults() {
        driver.navigate().back();
        return new SearchResultsPage(driver).waitLoaded();
    }

    private String safeChannelName() {
        try {
            return getChannelName();
        } catch (WebDriverException e) {
            return "(не зчитано)";
        }
    }
}
