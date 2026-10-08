package tests;

import annotation.WithMock;
import base.BaseTest;
import config.MockType;
import core.dto.FailRs;
import core.dto.Token;
import extensions.TokenExtensions;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static constants.TestConstants.ERROR_MESSAGE;
import static constants.TestConstants.INTERNAL_SERVER_ERROR;
import static constants.TestConstants.MISSING_KEY_ERROR;
import static constants.TestConstants.errorMessageTokenNotFound;
import static core.utils.Assertions.assertFieldEquals;
import static core.utils.Assertions.assertMessageContains;
import static core.utils.Assertions.assertStatusCode;
import static core.utils.ParamsRequiredGenerator.generateParamsWithAction;
import static core.utils.ParamsRequiredGenerator.generateParamsWithLogin;
import static core.utils.ParamsRequiredGenerator.generateParamsWithLogout;
import static core.utils.ParamsRequiredGenerator.generateParamsWithTokenAndAction;
import static core.enums.ActionsEnum.LOGIN;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Сервис выполнения действий")
@Feature("Негативные сценарии")
@Owner("AQA")
class NegativeAndEdgeCasesTest extends BaseTest {

    @Test
    @DisplayName("Сценарий: Запрос без заголовка X-Api-Key отклоняется.")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяем, что запрос без заголовка X-Api-Key возвращает 401 и не триггерит вызов внешнего сервиса.")
    void testMissingApiKeyHeader() {
        String token = "AABBCCDDEEFF00112233445566778899";
        Map<String, String> formParams = Map.of("token", token, "action", "LOGIN");
        step("Отправка запроса без заголовка X-Api-Key", () -> {
            var response = serviceSteps.receivedResponseWithCustomData(formParams, Map.of(), 401);
            assertAll(
                    () -> assertFieldEquals(response.as(FailRs.class).result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.as(FailRs.class).message(), MISSING_KEY_ERROR)
            );
        });
        mockService.verifyAuthNotCalled(token);
    }

    @Test
    @DisplayName("Сценарий: Токен в нижнем регистре отклоняется.")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяем, что токен в нижнем регистре не проходит валидацию и внешний сервис не вызывается.")
    void testLoginWithLowercaseTokenFails() {
        String lowercaseToken = "a".repeat(32);
        step("Попытка входа с токеном в нижнем регистре", () -> {
            var response = serviceSteps.receivedResponse(generateParamsWithTokenAndAction(lowercaseToken, LOGIN));
            assertAll(
                    () -> assertStatusCode(response.getStatusCode(), 400),
                    () -> assertFieldEquals(response.as(FailRs.class).result(), ERROR_MESSAGE),
                    () -> assertMessageContains(response.as(FailRs.class).message(), "^[0-9A-F]{32}$")
            );
        });
        mockService.verifyAuthNotCalled(lowercaseToken);
    }

    @Test
    @DisplayName("Сценарий: Токен длиной 33 символа отклоняется.")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяем, что токен длиннее 32 символов возвращает 400 и внешний сервис не вызывается.")
    void testLoginWithTooLongTokenFails() {
        String longToken = "A".repeat(33);
        step("Попытка входа с токеном из 33 символов", () -> {
            var response = serviceSteps.receivedResponse(generateParamsWithTokenAndAction(longToken, LOGIN));
            assertAll(
                    () -> assertStatusCode(response.getStatusCode(), 400),
                    () -> assertFieldEquals(response.as(FailRs.class).result(), ERROR_MESSAGE),
                    () -> assertMessageContains(response.as(FailRs.class).message(), "^[0-9A-F]{32}$")
            );
        });
        mockService.verifyAuthNotCalled(longToken);
    }

    @Test
    @DisplayName("Сценарий: ACTION после LOGOUT отклоняется.")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяем, что после LOGOUT токен удаляется и попытка ACTION возвращает 403 без вызова /doAction.")
    @WithMock(auth = MockType.SUCCESS)
    void testActionAfterLogoutFails(@TokenExtensions Token token) {
        step("Пользователь выполняет LOGIN и LOGOUT", () -> {
            var loginRs = serviceSteps.receivedSuccessRs(generateParamsWithLogin(token));
            assertFieldEquals(loginRs.result(), "OK");
            var logoutRs = serviceSteps.receivedSuccessRs(generateParamsWithLogout(token));
            assertFieldEquals(logoutRs.result(), "OK");
        });

        step("Попытка выполнить ACTION после LOGOUT", () -> {
            var response = serviceSteps.receivedFailRs(generateParamsWithAction(token), 403);
            assertAll(
                    () -> assertFieldEquals(response.result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.message(), errorMessageTokenNotFound(token.getToken()))
            );
        });

        mockService.verifyAuthCalled(token.getToken());
        mockService.verifyDoActionNotCalled(token.getToken());
    }

    @Test
    @DisplayName("Сценарий: Ошибка внешнего сервиса /doAction.")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяем, что при ошибке внешнего сервиса /doAction действие ACTION возвращает ошибку 500.")
    @WithMock(auth = MockType.SUCCESS, action = MockType.ERROR)
    void testActionFailsWhenDoActionReturnsError(@TokenExtensions Token token) {
        step("Пользователь выполняет LOGIN", () -> {
            var response = serviceSteps.receivedSuccessRs(generateParamsWithLogin(token));
            assertFieldEquals(response.result(), "OK");
        });

        step("Действие ACTION при ошибке внешнего сервиса /doAction", () -> {
            var response = serviceSteps.receivedFailRs(generateParamsWithAction(token), 500);
            assertAll(
                    () -> assertFieldEquals(response.result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.message(), INTERNAL_SERVER_ERROR)
            );
        });

        mockService.verifyAuthCalled(token.getToken());
        mockService.verifyDoActionCalled(token.getToken());
    }
}
