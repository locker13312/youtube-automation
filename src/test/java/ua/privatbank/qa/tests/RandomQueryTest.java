package ua.privatbank.qa.tests;

import org.testng.annotations.Test;
import ua.privatbank.qa.utils.RandomQuery;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertTrue;

/** Перевірка генератора тестових даних, без браузера. */
public class RandomQueryTest {

    private static final int RUNS = 1000;

    @Test(description = "Запит завжди складається з 2-4 цифр")
    public void generatesTwoToFourDigits() {
        for (int i = 0; i < RUNS; i++) {
            String query = RandomQuery.digits();
            assertTrue(RandomQuery.isValid(query), "Невалідний запит на ітерації " + i + ": '" + query + "'");
        }
    }

    @Test(description = "Запит змінюється між запусками")
    public void generatesDifferentValues() {
        Set<String> unique = new HashSet<>();
        for (int i = 0; i < RUNS; i++) {
            unique.add(RandomQuery.digits());
        }
        // Простір значень 10^2 + 10^3 + 10^4 = 11 100.
        assertTrue(unique.size() > 100, "Замало різних значень: " + unique.size() + " за " + RUNS + " спроб");
    }
}
