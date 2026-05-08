package tests;

import annotation.WithMock;
import base.BaseTest;
import config.MockType;
import core.dto.FailRs;
import core.dto.Token;
import core.enums.ActionsEnum;
import extensions.TokenExtensions;
import io.qameta.allure.*;
import org.junit.jupiter.api.*;

import java.util.Map;

import static constants.TestConstants.ERROR_MESSAGE;
import static constants.TestConstants.INTERNAL_SERVER_ERROR;
import static constants.TestConstants.MISSING_KEY_ERROR;
import static constants.TestConstants.OK_MESSAGE;
import static constants.TestConstants.TOKEN_MUST_MATCHES_REGEX;
import static constants.TestConstants.errorMessageTokenNotFound;
import static core.utils.Assertions.assertFieldEquals;
import static core.utils.Assertions.assertStatusCode;
import static core.utils.ParamsRequiredGenerator.generateParamsWithAction;
import static core.utils.ParamsRequiredGenerator.generateParamsWithLogin;
import static core.utils.ParamsRequiredGenerator.generateParamsWithLogout;
import static core.utils.ParamsRequiredGenerator.generateParamsWithTokenAndAction;
import static core.utils.TokenGenerator.generateInValidToken;
import static io.qameta.allure.Allure.step;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Сервис выполнения действий")
@Feature("Управление действиями")
@Owner("AQA")
class AuthLifecycleBaseTest extends BaseTest {

    @Test
    @DisplayName("Сценарий: Полный жизненный цикл действий (LOGIN -> ACTION -> LOGOUT).")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Проверяем, что пользователь может успешно пройти все этапы действий с системой.")
    @WithMock(auth = MockType.SUCCESS, action = MockType.SUCCESS)
    void testFullUserLifecycle(@TokenExtensions Token token) {
        step("Пользователь выполняет LOGIN", () -> {
            var response = serviceSteps.receivedSuccessRs(generateParamsWithLogin(token));
            assertFieldEquals(response.result(), OK_MESSAGE);
        });

        step("Пользователь выполняет ACTION", () -> {
            var response = serviceSteps.receivedSuccessRs(generateParamsWithAction(token));
            assertFieldEquals(response.result(), OK_MESSAGE);
        });

        step("Пользователь выполняет LOGOUT", () -> {
            var response = serviceSteps.receivedSuccessRs(generateParamsWithLogout(token));
            assertFieldEquals(response.result(), OK_MESSAGE);
        });

        step("Повторная попытка выполнить ACTION (негативная проверка)", () -> {
            var response = serviceSteps.receivedFailRs(generateParamsWithAction(token), 403);
            assertAll(
                      () -> assertFieldEquals(response.result(), ERROR_MESSAGE),
                      () -> assertFieldEquals(response.message(), errorMessageTokenNotFound(token.getToken()))
            );
        });
    }

    @Test
    @DisplayName("Сценарий: Выполняются действия LOGIN для успешного вызова внешнего сервиса.")
    @Description("Проверяем, что сервис на действие LOGIN тригерит вызов /auth.")
    @Severity(SeverityLevel.NORMAL)
    @WithMock(auth = MockType.SUCCESS)
    void testSuccessfulLogin(@TokenExtensions Token token) {
        step("Пользователь выполняет LOGIN", () -> {
            var response = serviceSteps.receivedSuccessRs(generateParamsWithLogin(token));
            assertFieldEquals(response.result(), OK_MESSAGE);
        });

        mockService.verifyAuthCalled(token.getToken());
    }

    @Test
    @DisplayName("Сценарий: Выполняются действия LOGIN и ACTION для успешных вызовов внешнего сервиса.")
    @Description("Проверяем, что сервис на действие ACTION тригерит вызов /doAction.")
    @Severity(SeverityLevel.NORMAL)
    @WithMock(auth = MockType.SUCCESS, action = MockType.SUCCESS)
    void testActionAfterLogin(@TokenExtensions Token token) {
        step("Пользователь выполняет LOGIN и ACTION", () -> {
            serviceSteps.receivedSuccessRs(generateParamsWithLogin(token));
            var response = serviceSteps.receivedSuccessRs(generateParamsWithAction(token));
            assertFieldEquals(response.result(), OK_MESSAGE);
        });

        mockService.verifyAuthCalled(token.getToken());
        mockService.verifyDoActionCalled(token.getToken());
    }

    @Test
    @DisplayName("Сценарий: Ошибка внешнего сервиса.")
    @Description("Проверяем, что сервис не делает вызов /auth, при выполнении LOGIN, если внешний сервис не доступен.")
    @Severity(SeverityLevel.NORMAL)
    @WithMock(auth = MockType.ERROR)
    void testLoginFailsWhenExternalServiceIsDown(@TokenExtensions Token token) {
        step("Попытка входа при недоступности внешнего сервиса.", () -> {
            var response = serviceSteps.receivedFailRs(generateParamsWithLogin(token), 500);
            assertAll(
                    () -> assertFieldEquals(response.result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.message(), INTERNAL_SERVER_ERROR)
            );
        });

        mockService.verifyAuthCalled(token.getToken());
    }

    @Test
    @DisplayName("Сценарий: Отправка LOGIN с не валидным token (31 символ).")
    @Description("Проверяем, что сервис вернет ошибку с невалидными данными запроса и не будет вызова внешнего сервиса.")
    @Severity(SeverityLevel.NORMAL)
    void testLoginInvalidTokenLength() {
        String token = generateInValidToken();
        step("Попытка входа при недоступности внешнего сервиса.", () -> {
            var response = serviceSteps.receivedResponse(generateParamsWithTokenAndAction(token, ActionsEnum.LOGIN));
            assertAll(
                    () -> assertStatusCode(response.getStatusCode(), 400),
                    () -> assertFieldEquals(response.as(FailRs.class).result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.as(FailRs.class).message(), TOKEN_MUST_MATCHES_REGEX)
            );
        });

        mockService.verifyAuthNotCalled(token);
    }

    @Test
    @DisplayName("Сценарий: Попытка выполнить действие ACTION без предварительного LOGIN.")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяем, что система должна блокировать попытки выполнения действий без начального действия LOGIN." +
            " Вызов /doAction не происходит.")
    @WithMock(auth = MockType.SUCCESS, action = MockType.SUCCESS)
    void testActionWithoutLoginShouldFail(@TokenExtensions Token token) {
        step("Пользователь выполняет ACTION без LOGIN", () -> {
            var response = serviceSteps.receivedFailRs(generateParamsWithAction(token), 403);
            assertAll(
                    () -> assertFieldEquals(response.result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.message(), errorMessageTokenNotFound(token.getToken()))
            );
        });

        mockService.verifyAuthNotCalled(token.getToken());
        mockService.verifyDoActionNotCalled(token.getToken());
    }

    @Test
    @DisplayName("Сценарий: Попытка LOGIN с неверным X-Api-Key ключом.")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяем, что система должна блокировать попытки выполнения действий с неверным ключом.")
    @WithMock(auth = MockType.SUCCESS, action = MockType.SUCCESS)
    void testInvalidApiKeyCheck(@TokenExtensions Token token) {
        Map<String, String> badHeaders = Map.of("X-Api-Key", "INVALID_KEY_123");
        step("Отправка запроса с поддельным ключом X-Api-Key", () -> {
            var response = serviceSteps.receivedFailResponseWithCustomHeader(generateParamsWithLogin(token), badHeaders);

            assertAll(
                    () -> assertStatusCode(response.getStatusCode(), 401),
                    () -> assertFieldEquals(response.as(FailRs.class).result(), ERROR_MESSAGE),
                    () -> assertFieldEquals(response.as(FailRs.class).message(), MISSING_KEY_ERROR)
            );
        });

        mockService.verifyAuthNotCalled(token.getToken());
        mockService.verifyDoActionNotCalled(token.getToken());
    }
}
