package base;

import annotation.WithMock;
import config.MockType;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.TestInfo;
import service.MockService;

public class MockRegister {

    public static void mockRegisterStub(TestInfo testInfo, MockService mockService){

        testInfo.getTestMethod().ifPresent(method -> {
            WithMock annotation = method.getAnnotation(WithMock.class);
            if (annotation != null) {
                Allure.parameter("Mock Auth ->", annotation.auth().name());
                Allure.parameter("Mock doAction ->", annotation.action().name());

                if (annotation.auth() == MockType.SUCCESS) mockService.stubAuthSuccess();
                if (annotation.auth() == MockType.ERROR) mockService.stubExternalServiceErrorAuthWith500();

                if (annotation.action() == MockType.SUCCESS) mockService.stubDoActionSuccess();
            }
        });
    }
}
