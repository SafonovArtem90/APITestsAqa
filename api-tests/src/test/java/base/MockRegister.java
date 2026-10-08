package base;

import annotation.WithMock;
import config.MockType;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MockService;

public class MockRegister {
    private static final Logger log = LoggerFactory.getLogger(MockRegister.class);

    /**
     * Регистрирует WireMock-стабы на основании аннотации @WithMock на тестовом методе.
     * MockType.SUCCESS → стаб отвечает 200, MockType.ERROR → стаб отвечает 500.
     */
    public static void mockRegisterStub(ExtensionContext context, MockService mockService, String token){

        context.getTestMethod().ifPresent(method -> {
            WithMock annotation = method.getAnnotation(WithMock.class);
            if (annotation != null && token != null) {
                log.info("Configuring Stubs for Token [{}]: Auth={}, Action={}",
                         token, annotation.auth(), annotation.action());
                // excluded=true: токен генерируется уникальным для каждого теста,
                // параметр виден в отчёте, но не ломает parametersHash (ключ истории Allure)
                Allure.parameter("Configuring Stubs for Token ->", token, true);
                Allure.parameter("Stub Auth ->", annotation.auth());
                Allure.parameter("Stub doAction ->", annotation.action());

                if (annotation.auth() == MockType.SUCCESS) mockService.stubAuthSuccess(token);
                if (annotation.auth() == MockType.ERROR) mockService.stubExternalServiceErrorAuthWith500(token);

                if (annotation.action() == MockType.SUCCESS) mockService.stubDoActionSuccess(token);
                if (annotation.action() == MockType.ERROR) mockService.stubExternalServiceErrorDoActionWith500(token);
            }
        });
    }
}
