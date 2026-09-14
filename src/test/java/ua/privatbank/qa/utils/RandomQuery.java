package ua.privatbank.qa.utils;

import java.util.concurrent.ThreadLocalRandom;

/** Пошуковий запит із 2-4 випадкових цифр. */
public final class RandomQuery {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 4;

    private RandomQuery() {
    }

    /** Рядок, а не число: інакше запити з провідним нулем («07») ніколи не згенеруються. */
    public static String digits() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int length = random.nextInt(MIN_LENGTH, MAX_LENGTH + 1);
        StringBuilder query = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            query.append(random.nextInt(0, 10));
        }
        return query.toString();
    }

    public static boolean isValid(String query) {
        return query != null && query.matches("^\\d{" + MIN_LENGTH + "," + MAX_LENGTH + "}$");
    }
}
