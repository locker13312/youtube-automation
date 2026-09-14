package ua.privatbank.qa.core;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/** Налаштування прогону. Системна властивість (-Dkey=value) перекриває config.properties. */
public final class Config {

    private static final Properties PROPS = load();

    private Config() {
    }

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties не знайдено в classpath");
            }
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати config.properties", e);
        }
        return p;
    }

    public static String get(String key) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank() && !"null".equals(fromSystem)) {
            return fromSystem;
        }
        String value = PROPS.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Немає налаштування '" + key + "'");
        }
        return value;
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key).trim());
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key).trim());
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static String browser() {
        return get("browser").trim().toLowerCase();
    }

    public static boolean headless() {
        return getBoolean("headless");
    }

    public static String language() {
        return get("language");
    }

    public static Duration timeout() {
        return Duration.ofSeconds(getInt("timeout.seconds"));
    }

    public static Duration shortTimeout() {
        return Duration.ofSeconds(getInt("timeout.short.seconds"));
    }

    public static int windowWidth() {
        return getInt("window.width");
    }

    public static int windowHeight() {
        return getInt("window.height");
    }
}
