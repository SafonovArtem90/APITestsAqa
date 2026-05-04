package core.utils;

import lombok.extern.slf4j.Slf4j;
import java.util.UUID;
import java.util.regex.Pattern;

import static core.constants.CoreConstants.LENGTH;
import static core.constants.CoreConstants.REGEX;

@Slf4j
public class TokenGenerator {

    public static String generateValidToken() {
        String token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        log.info("TokenGenerator generateValidToken: {}.", token);
        validateTokenFormat(token);
        return token;
    }

    public static String generateInValidToken() {
        String invalidToken = "A".repeat(31);
        log.info("TokenGenerator generateInValidToken: {}.", invalidToken);
        return invalidToken;
    }

    private static void validateTokenFormat(String token) {
        Pattern pattern = Pattern.compile(REGEX);
        if (!pattern.matcher(token).matches() || token.length() != LENGTH) {
            throw new IllegalArgumentException("Generated token does not match REGEX");
        }
    }
}
