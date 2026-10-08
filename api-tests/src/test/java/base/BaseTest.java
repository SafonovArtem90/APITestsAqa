package base;

import core.client.NCServiceSteps;
import extensions.AllureEnvironmentExtension;
import extensions.MockServerExtension;
import extensions.MockSetupExtension;
import extensions.TokenResolver;
import extensions.MockServiceRegistry;

import lombok.extern.slf4j.Slf4j;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import service.MockService;

@Slf4j
@ExtendWith({AllureEnvironmentExtension.class,
        TokenResolver.class,
        MockSetupExtension.class,
        MockServerExtension.class})
public class BaseTest {

    protected NCServiceSteps serviceSteps;
    protected MockService mockService;

    @BeforeEach
    @Step("Инициализация окружения")
    void setUp(TestInfo testInfo) {
        MDC.put("testMethod", testInfo.getTestMethod().get().getName());
        this.serviceSteps = new NCServiceSteps();
        this.mockService = MockServiceRegistry.get();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }
}
