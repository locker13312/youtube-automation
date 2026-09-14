package ua.privatbank.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Модальне вікно з пропозицією увійти, яке YouTube показує гостю після спроби підписатися. */
public class SignInPromptDialog extends BasePage {

    // Гостю вікно приїжджає як ytd-modal-with-title-and-button-renderer. Порядок важливий:
    // у ytd-popup-container раніше лежить схований tp-yt-paper-dialog (display:none) —
    // контейнер уже закритого промо, і саме його чіпляє надто загальний локатор.
    private static final By DIALOG_MODAL =
            By.cssSelector("ytd-popup-container ytd-modal-with-title-and-button-renderer");
    private static final By DIALOG_CONFIRM = By.cssSelector("yt-confirm-dialog-renderer");
    private static final By DIALOG_PAPER_OPENED = By.cssSelector("tp-yt-paper-dialog[opened]");
    private static final By DIALOG_PAPER = By.cssSelector("tp-yt-paper-dialog");

    private static final By[] DIALOG = {DIALOG_MODAL, DIALOG_CONFIRM, DIALOG_PAPER_OPENED, DIALOG_PAPER};

    public SignInPromptDialog(WebDriver driver) {
        super(driver);
    }

    public SignInPromptDialog waitVisible() {
        container();
        return this;
    }

    private WebElement container() {
        return firstVisibleOf(DIALOG);
    }

    /** Текст саме відображається у вікні, а не просто присутній у розмітці. */
    public boolean isTextVisible(String text) {
        return hasVisibleExactText(container(), text);
    }

    public String getDialogText() {
        return container().getText().trim();
    }
}
