package extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import core.config.ConfigReader;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import service.MockService;

public class MockServerExtension implements BeforeAllCallback {

    private static final String MOCK_SERVER_KEY = "MOCK_SERVER_GLOBAL";
    private static volatile WireMockServer sharedServer;
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.GLOBAL;

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);

        if (sharedServer == null || !sharedServer.isRunning()) {
            synchronized (MockServerExtension.class) {
                if (sharedServer == null || !sharedServer.isRunning()) {
                    WireMockServer server = new WireMockServer(
                            WireMockConfiguration.options()
                                                 .port(Integer.parseInt(ConfigReader.getProperty("mock.service.port")))
                    );
                    sharedServer = server;
                    MockServerHolder holder = new MockServerHolder(server);
                    store.put(MOCK_SERVER_KEY, holder);
                    MockServiceRegistry.set(new MockService(server));
                }
            }
        }
        if (MockServiceRegistry.get() == null && sharedServer != null) {
            MockServiceRegistry.set(new MockService(sharedServer));
        }
    }

    static class MockServerHolder implements ExtensionContext.Store.CloseableResource, AutoCloseable {
        private final WireMockServer server;

        public MockServerHolder(WireMockServer server) {
            this.server = server;
            this.server.start();
            System.out.println("WireMock Server started on port: " + server.port());
        }

        @Override
        public void close() {
            if (server.isRunning()) {
                server.stop();
                System.out.println("WireMock Server stopped.");
            }
        }

        public WireMockServer getServer() {
            return server;
        }
    }
}