package base;

import annotation.WithMock;
import config.MockType;
import org.junit.jupiter.api.TestInfo;
import service.MockService;

public class MockRegister {

    public static void mockRegisterStub(TestInfo testInfo, MockService mockService){

        testInfo.getTestMethod().ifPresent(method -> {
            WithMock annotation = method.getAnnotation(WithMock.class);
            if (annotation != null) {
                if (annotation.auth() == MockType.SUCCESS) mockService.stubAuthSuccess();
                if (annotation.auth() == MockType.ERROR) mockService.stubExternalServiceErrorAuthWith500();

                if (annotation.action() == MockType.SUCCESS) mockService.stubDoActionSuccess();
            }
        });
    }
}
