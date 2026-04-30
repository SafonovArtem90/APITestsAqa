package core.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("config/config.properties")) {
            PROPERTIES.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении config.properties", e);
        }
    }

    public static String getProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new RuntimeException(String.format("Ключ %s не найден в config.properties", key));
        }
        return value;
    }
}
