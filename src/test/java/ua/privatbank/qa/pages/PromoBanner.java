package ua.privatbank.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Промо-банер YouTube Premium (yt-mealbar-promo-renderer).
 *
 * Вилітає в довільний момент сесії внизу вікна і при висоті вікна близько 700px
 * накриває те, що там у цей момент є: блок автора на сторінці відео або кнопку
 * підписки на сторінці каналу. Клік по перекритому елементу падає з
 * ElementClickInterceptedException, тому банер закривається справжнім кліком по
 * його ж кнопці «Ні, дякую» — окремим компонентом, щоб локатор жив в одному місці.
 */
public class PromoBanner extends BasePage {

    private static final By DISMISS_MEALBAR = By.cssSelector("yt-mealbar-promo-renderer #dismiss-button button");
    private static final By DISMISS_POPUP = By.cssSelector("ytd-popup-container #dismiss-button button");

    private static final By[] DISMISS = {DISMISS_MEALBAR, DISMISS_POPUP};

    public PromoBanner(WebDriver driver) {
        super(driver);
    }

    /** Банер з'являється не завжди, тому крок необов'язковий і без очікувань наперед. */
    public void dismissIfPresent() {
        for (By locator : DISMISS) {
            if (!isPresent(locator)) {
                continue;
            }
            try {
                shortWait.until(ExpectedConditions.elementToBeClickable(locator)).click();
                System.out.println("[step] промо-банер закрито");
                return;
            } catch (TimeoutException | ElementNotInteractableException bannerVanished) {
                // банер зник сам, поки ми до нього йшли
            }
        }
    }
}
