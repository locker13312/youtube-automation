package ua.privatbank.qa.core;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** При падінні тесту зберігає скріншот у screenshots/ і чіпляє його разом з URL і DOM до звіту Allure. */
public class ScreenshotListener implements ITestListener {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Path DIR = Paths.get("screenshots");

    @Override
    public void onTestFailure(ITestResult result) {
        if (!DriverFactory.hasDriver()) {
            return;
        }
        WebDriver driver = DriverFactory.getDriver();

        try {
            Allure.addAttachment("URL на момент падіння", driver.getCurrentUrl());
        } catch (WebDriverException ignored) {
            // сесія могла вже завершитись
        }

        if (driver instanceof TakesScreenshot shooter) {
            try {
                byte[] png = shooter.getScreenshotAs(OutputType.BYTES);
                Allure.addAttachment("Скріншот", "image/png", new ByteArrayInputStream(png), ".png");
                Files.createDirectories(DIR);
                String fileName = result.getMethod().getMethodName() + "_" + LocalDateTime.now().format(STAMP) + ".png";
                Path target = DIR.resolve(fileName);
                Files.write(target, png);
                System.out.println("[screenshot] " + target.toAbsolutePath());
            } catch (IOException | WebDriverException e) {
                System.out.println("[screenshot] не збережено: " + e.getMessage());
            }
        }

        try {
            Allure.addAttachment("DOM сторінки", "text/html",
                    new ByteArrayInputStream(driver.getPageSource().getBytes(StandardCharsets.UTF_8)), ".html");
        } catch (WebDriverException ignored) {
            // теж необов'язково
        }
    }
}
