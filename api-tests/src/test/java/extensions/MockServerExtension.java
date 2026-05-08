package extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import core.config.ConfigReader;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MockServerExtension implements BeforeAllCallback {

    private static final String MOCK_SERVER_KEY = "MOCK_SERVER_GLOBAL";
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.GLOBAL;

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        MockServerHolder holder = store.get(MOCK_SERVER_KEY, MockServerHolder.class);

        if (holder == null) {
            synchronized (MockServerExtension.class) {
                holder = store.get(MOCK_SERVER_KEY, MockServerHolder.class);
                if (holder == null) {
                    WireMockServer server = new WireMockServer(
                            WireMockConfiguration.options()
                                                 .port(Integer.parseInt(ConfigReader.getProperty("mock.service.port")))
                    );

                    holder = new MockServerHolder(server);
                    store.put(MOCK_SERVER_KEY, holder);
                }
            }
        }
    }

    static class MockServerHolder implements ExtensionContext.Store.CloseableResource {
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