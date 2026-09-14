package ua.privatbank.qa.core;

import io.qameta.allure.Step;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;

/** Дії рівня браузера: розмір вікна і вкладки. */
public final class Browser {

    private final WebDriver driver;

    public Browser(WebDriver driver) {
        this.driver = driver;
    }

    public Dimension size() {
        return driver.manage().window().getSize();
    }

    @Step("Розгорнути вікно браузера на весь екран")
    public Dimension maximize() {
        Dimension before = size();
        driver.manage().window().maximize();
        Dimension after = size();
        System.out.println("[step] вікно: " + before.getWidth() + "x" + before.getHeight()
                + " -> " + after.getWidth() + "x" + after.getHeight());
        return after;
    }

    @Step("Закрити поточну вкладку браузера")
    public void closeCurrentTab() {
        driver.close();
        System.out.println("[step] вкладку закрито");
    }

    /** Після закриття останньої вкладки сесія завершується, тому виняток тут — теж ознака успіху. */
    public boolean isCurrentTabClosed() {
        try {
            return driver.getWindowHandles().isEmpty();
        } catch (NoSuchSessionException sessionEnded) {
            return true;
        }
    }
}
