package core.utils;

import io.qameta.allure.Step;

import static org.junit.jupiter.api.Assertions.*;

public class Assertions {
    @Step("Проверка равенства кода ответа у запроса actual:{0} и expected:{1}")
    public static void assertStatusCode(int actual, int expected) {
        assertEquals(expected, actual, "Код ответа должен соответствовать ожидаемому.");
    }

    @Step("Проверка равенства значения поля в ответе actual:{0} для expected:{1}")
    public static void assertFieldEquals(String actual, String expected) {
        assertEquals(expected, actual, "Значение поля в ответе должно соответствовать ожидаемому.");
    }
}
