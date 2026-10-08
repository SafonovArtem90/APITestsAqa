package extensions;

import core.dto.Token;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MockService;

import java.lang.reflect.Method;
import java.util.List;

import static base.MockRegister.mockRegisterStub;

/**
 * Перехватывает выполнение тестового метода, чтобы:
 * 1) до теста зарегистрировать мок-заглушки на WireMock (через аннотацию @WithMock и mockRegisterStub),
 * 2) после теста удалить зарегистрированные стабы (cleanupStubs).
 */
public class MockSetupExtension implements InvocationInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MockSetupExtension.class);

    @Override
    public void interceptTestMethod(InvocationInterceptor.Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {

        String testName = extensionContext.getDisplayName();
        String threadName = Thread.currentThread().getName();
        log.info("\n>>> STARTING TEST [{}] in thread [{}]", testName, threadName);

        String tokenValue = findTokenArgument(invocationContext.getArguments());
        MockService mockService = MockServiceRegistry.get();

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

    private String findTokenArgument(List<Object> arguments) {
        // Токен для стабов берём из аргумента-параметра теста типа Token (@TokenExtensions Token token).
        return arguments.stream()
                        .filter(arg -> arg instanceof Token)
                        .map(arg -> ((Token) arg).getToken())
                        .findFirst()
                        .orElse(null);
    }
}
