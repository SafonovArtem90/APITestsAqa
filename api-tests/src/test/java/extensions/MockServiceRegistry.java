package extensions;

import service.MockService;

public final class MockServiceRegistry {

    private static volatile MockService service;

    private MockServiceRegistry() {
    }

    public static MockService get() {
        return service;
    }

    public static void set(MockService service) {
        MockServiceRegistry.service = service;
    }
}
