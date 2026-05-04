package base;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import core.client.NCServiceSteps;
import groovy.util.logging.Slf4j;
import io.qameta.allure.Step;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import service.MockService;

import static base.MockRegister.mockRegisterStub;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
public class BaseTest {

    protected NCServiceSteps serviceSteps;
    protected MockService mockService;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension
            .newInstance().options(wireMockConfig().port(8888)).build();


    @BeforeEach
    @Step("Инициализация окружения")
    void setUp(TestInfo testInfo) {
        wireMock.resetAll();
        this.mockService = new MockService(wireMock.getRuntimeInfo().getWireMock());
        this.serviceSteps = new NCServiceSteps();
        mockRegisterStub(testInfo, mockService);
    }
}
