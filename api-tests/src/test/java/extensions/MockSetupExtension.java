package extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import core.dto.Token;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MockService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static base.MockRegister.mockRegisterStub;

public class MockSetupExtension implements InvocationInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MockSetupExtension.class);

    @Override
    public void interceptTestMethod(InvocationInterceptor.Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {

        String testName = extensionContext.getDisplayName();
        String threadName = Thread.currentThread().getName();
        log.info("\n>>> STARTING TEST [{}] in thread [{}]", testName, threadName);

        ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
        MockServerExtension.MockServerHolder holder =
                store.get("MOCK_SERVER_GLOBAL", MockServerExtension.MockServerHolder.class);

        String tokenValue = findTokenArgument(invocationContext.getArguments());
        Object testInstance = extensionContext.getRequiredTestInstance();

        WireMockServer wireMockServer = holder.getServer();
        MockService mockService = getOrCreateMockService(testInstance, wireMockServer);

        try {
            mockRegisterStub(extensionContext, mockService, tokenValue);
            invocation.proceed();
            log.info("\n<<< FINISHED TEST [{}] successfully", testName);
        } catch (Throwable e) {
            log.error("\n<<< FAILED TEST [{}] with error: {}", testName, e.getMessage());
            throw e;
        } finally {
            if (tokenValue != null) {
                mockService.cleanupStubs(tokenValue);
            }
        }
    }

    private MockService getOrCreateMockService(Object instance, WireMockServer server) {
        Field field = findField(instance.getClass(), "mockService");
        if (field != null) {
            field.setAccessible(true);
            try {
                MockService service = (MockService) field.get(instance);
                if (service == null) {
                    service = new MockService(server); // Передаем сервер
                    field.set(instance, service);
                }
                return service;
            } catch (IllegalAccessException e) { /* ignore */ }
        }
        return new MockService(server);
    }

    private String findTokenArgument(List<Object> arguments) {
        return arguments.stream()
                        .filter(arg -> arg instanceof Token)
                        .map(arg -> ((Token) arg).getToken())
                        .findFirst()
                        .orElse(null);
    }

    private Field findField(Class<?> clazz, String name) {
        try { return clazz.getDeclaredField(name); }
        catch (NoSuchFieldException e) {
            return clazz.getSuperclass() != null ? findField(clazz.getSuperclass(), name) : null;
        }
    }
}
