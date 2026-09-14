package ua.privatbank.qa.core;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Блок ENVIRONMENT у звіті Allure.
 *
 * Пишеться з живого драйвера, а не з файлу в ресурсах: версія браузера і ОС
 * різні на машині розробника й на раннері CI, а захардкожений файл показував би
 * у звіті те, чого в прогоні не було.
 */
final class AllureEnvironment {

    private AllureEnvironment() {
    }

    static void write(WebDriver driver) {
        Properties env = new Properties();
        env.setProperty("Base.URL", Config.baseUrl());
        env.setProperty("Browser", browser(driver));
        env.setProperty("Browser.Headless", String.valueOf(Config.headless()));
        env.setProperty("Browser.Language", Config.language());
        env.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        env.setProperty("Java", System.getProperty("java.version"));

        Path file = Paths.get(System.getProperty("allure.results.directory", "target/allure-results"))
                .resolve("environment.properties");
        try {
            Files.createDirectories(file.getParent());
            try (Writer out = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                // Без коментаря: Properties.store екранує в ньому кирилицю, а користі з нього нуль.
                env.store(out, null);
            }
        } catch (IOException e) {
            System.out.println("[allure] environment.properties не записано: " + e.getMessage());
        }
    }

    private static String browser(WebDriver driver) {
        if (driver instanceof HasCapabilities hasCapabilities) {
            return hasCapabilities.getCapabilities().getBrowserName()
                    + " " + hasCapabilities.getCapabilities().getBrowserVersion();
        }
        return Config.browser();
    }
}
