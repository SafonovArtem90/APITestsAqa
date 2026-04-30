package base;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import core.client.NCServiceSteps;
import core.mock.MockService;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@Slf4j
public class BaseTest {

    protected NCServiceSteps serviceSteps;
    protected MockService mockService;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension
            .newInstance().options(wireMockConfig().port(8888)).build();


    public BaseTest() {
        serviceSteps = new NCServiceSteps();
        mockService = new MockService(wireMock.getRuntimeInfo().getWireMock());
        wireMock.resetAll();
    }
}
