package core.constants;

import core.config.ConfigReader;

public final class CoreConstants {

    private CoreConstants() {
    }

    public static final String REGEX = ConfigReader.getProperty("token.regex");
    public static final int LENGTH = Integer.parseInt(ConfigReader.getProperty("token.length"));

    public static final String X_API_KEY = "X-Api-Key";
}
