package core.constants;

import core.config.ConfigReader;

public class TestConstants {

    public static final String TOKEN_NOT_FOUND = "Token '%s' not found";
    public static final String TOKEN_MUST_MATCHES_REGEX = "token: должно соответствовать \"^[0-9A-F]{32}$\"";
    public static final String ERROR_MESSAGE = "ERROR";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String MISSING_KEY_ERROR = "Missing or invalid API Key";
    public static final String OK_MESSAGE = "OK";
    public static final String REGEX = ConfigReader.getProperty("token.regex");
    public static final int LENGTH = Integer.parseInt(ConfigReader.getProperty("token.length"));

    public static final String X_API_KEY = "X-Api-Key";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";

    public static String errorMessageTokenNotFound(String token) {
        return String.format(TOKEN_NOT_FOUND, token);
    }
}
