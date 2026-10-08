package extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import core.config.ConfigReader;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import service.MockService;

public class MockServerExtension implements BeforeAllCallback {

    private static volatile WireMockServer sharedServer;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (sharedServer == null || !sharedServer.isRunning()) {
            synchronized (MockServerExtension.class) {
                if (sharedServer == null || !sharedServer.isRunning()) {
                    WireMockServer server = new WireMockServer(
                            WireMockConfiguration.options()
                                                 .port(Integer.parseInt(ConfigReader.getProperty("mock.service.port")))
                    );
                    server.start();
                    sharedServer = server;
                    MockServiceRegistry.set(new MockService(server));

                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        if (sharedServer != null && sharedServer.isRunning()) {
                            sharedServer.stop();
                            System.out.println("WireMock Server stopped.");
                        }
                    }));
                }
            }
        }
        if (MockServiceRegistry.get() == null && sharedServer != null) {
            MockServiceRegistry.set(new MockService(sharedServer));
        }
    }
}