package ua.privatbank.qa.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.openqa.selenium.Dimension;
import org.testng.annotations.Test;
import ua.privatbank.qa.core.Browser;
import ua.privatbank.qa.pages.ChannelPage;
import ua.privatbank.qa.pages.SearchResultsPage;
import ua.privatbank.qa.pages.SignInPromptDialog;
import ua.privatbank.qa.pages.VideoPage;
import ua.privatbank.qa.pages.YouTubeHomePage;
import ua.privatbank.qa.utils.RandomQuery;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Сценарій із ТЗ, кроки 1-10. */
@Epic("YouTube")
@Feature("Пошук і підписка")
public class YouTubeSearchAndSubscribeTest extends BaseTest {

    private static final String EXPECTED_TAB_TITLE = "YouTube";
    private static final String SUBSCRIBE_BUTTON_TEXT = "ПІДПИСАТИСЯ";
    private static final String SIGN_IN_TEXT = "Увійти";

    private static final int POSITION_TO_SELECT = 2;
    private static final int POSITION_TO_OPEN = 4;

    @Test(description = "Гість шукає відео за випадковим числовим запитом і намагається підписатися на канал автора")
    @Story("Неавторизований користувач не може підписатися без входу")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Пошук за випадковим числовим запитом, відкриття четвертого відео у видачі, "
            + "перехід на канал автора і спроба підписатися. Очікується вікно з пропозицією увійти.")
    public void guestSearchesAndTriesToSubscribe() {

        Browser browser = new Browser(driver);

        // 1. Перехід на https://www.youtube.com/
        YouTubeHomePage home = new YouTubeHomePage(driver).open();

        // 2. Вкладка браузера називається "YouTube"
        // Allure.step, бо перевірка не йде через метод сторінки з @Step — без обгортки
        // цього кроку у звіті не видно. Асерт лишається в тесті.
        Allure.step("Вкладка браузера називається \"" + EXPECTED_TAB_TITLE + "\"", () ->
                assertEquals(home.getPageTitle(), EXPECTED_TAB_TITLE, "Назва вкладки не збігається"));

        // 3. Запит із 2-4 випадкових цифр
        String query = RandomQuery.digits();
        assertTrue(RandomQuery.isValid(query), "Запит не відповідає формату 2-4 цифри: '" + query + "'");

        home.typeSearchQuery(query);
        assertEquals(home.getSearchQueryValue(), query, "У рядку пошуку не той текст, що вводили");

        // 4. Розгорнути браузер на весь екран
        Dimension beforeMaximize = browser.size();
        Dimension afterMaximize = browser.maximize();
        assertTrue(afterMaximize.getWidth() >= beforeMaximize.getWidth()
                        && afterMaximize.getHeight() >= beforeMaximize.getHeight(),
                "Вікно не побільшало: було " + beforeMaximize + ", стало " + afterMaximize);

        SearchResultsPage results = home.submitSearch()
                .waitLoaded()
                .waitForAtLeastResults(POSITION_TO_OPEN);

        // 5. Вибрати другу позицію зверху у списку результатів.
        // Вибір — це клік: він відкриває відео, і далі сценарій повертається до тієї ж видачі.
        String secondTitle = results.titleAt(POSITION_TO_SELECT);
        VideoPage second = results.openVideo(POSITION_TO_SELECT);
        assertTrue(second.getCurrentUrl().contains("/watch"),
                "Друга позиція не відкрилася. URL: " + second.getCurrentUrl());
        assertFalse(second.getVideoTitle().isBlank(),
                "Сторінка другого відео без заголовка (у видачі було: " + secondTitle + ")");

        results = second.backToResults().waitForAtLeastResults(POSITION_TO_OPEN);

        // 6. Запустити четверте зверху відео
        String fourthTitle = results.titleAt(POSITION_TO_OPEN);
        VideoPage video = results.openVideo(POSITION_TO_OPEN);
        assertTrue(video.getCurrentUrl().contains("/watch"),
                "Не відкрилася сторінка перегляду. URL: " + video.getCurrentUrl());
        assertFalse(video.getVideoTitle().isBlank(),
                "Сторінка відео без заголовка (очікували: " + fourthTitle + ")");

        // 7. Клік на аватар відправника відео
        ChannelPage channel = video.clickOwnerAvatar();
        assertTrue(channel.isChannelUrl(),
                "Не відкрився канал автора. URL: " + channel.getCurrentUrl());

        // 8. Клік на кнопку з текстом "ПІДПИСАТИСЯ"
        assertTrue(channel.subscribeButtonHasText(SUBSCRIBE_BUTTON_TEXT),
                "Текст кнопки не збігається. Очікували '" + SUBSCRIBE_BUTTON_TEXT
                        + "', фактично '" + channel.getSubscribeButtonText() + "'");

        SignInPromptDialog dialog = channel.clickSubscribe();

        // 9. У контейнері, що з'явився, є текст "Увійти" (точний збіг)
        Allure.step("У вікні видно текст рівно \"" + SIGN_IN_TEXT + "\"", () ->
                assertTrue(dialog.isTextVisible(SIGN_IN_TEXT),
                        "У вікні не видно елемента з текстом рівно '" + SIGN_IN_TEXT
                                + "'. Текст вікна: \"" + dialog.getDialogText() + "\""));

        // 10. Закрити вкладку браузера
        browser.closeCurrentTab();
        assertTrue(browser.isCurrentTabClosed(), "Вкладка залишилася відкритою");
    }
}
