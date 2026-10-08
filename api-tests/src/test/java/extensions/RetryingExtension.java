package extensions;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class RetryingExtension implements InvocationInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RetryingExtension.class);

    private int maxAttempts() {
        String prop = System.getProperty("test.retry.count");
        if (prop != null) {
            try {
                return Math.max(1, Integer.parseInt(prop) + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        String env = System.getenv("TEST_RETRY_COUNT");
        if (env != null) {
            try {
                return Math.max(1, Integer.parseInt(env) + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        return 2;
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        int maxAttempts = maxAttempts();
        Throwable last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                int current = attempt;
                Allure.step("Попытка " + current + " из " + maxAttempts + " выполнения теста", () -> {
                    if (current == 1) {
                        invocation.proceed();
                    } else {
                        invokeDirectly(invocationContext, extensionContext);
                    }
                });
                return;
            } catch (Throwable t) {
                last = t;
                log.warn("Test attempt {}/{} failed: {}", attempt, maxAttempts, t.getMessage());
                try (ByteArrayInputStream stream =
                             new ByteArrayInputStream(stackTrace(t).getBytes(StandardCharsets.UTF_8))) {
                    Allure.getLifecycle().addAttachment("Retry " + attempt + " failure", "text/plain", "txt", stream);
                }
                if (attempt < maxAttempts) {
                    log.info("Retrying test, next attempt {}/{}", attempt + 1, maxAttempts);
                }
            }
        }
        throw last;
    }

    /**
     * Повторно запускает тестовый метод (для ретраев) напрямую через рефлексию,
     * поскольку Invocation.proceed() в Interceptor можно вызвать только один раз.
     */
    private static Void invokeDirectly(ReflectiveInvocationContext<Method> invocationContext,
                                       ExtensionContext extensionContext) throws Throwable {
        Method executableMethod = extensionContext.getTestMethod()
                .orElseThrow(() -> new IllegalStateException("Test method not available"));
        executableMethod.setAccessible(true);
        Object instance = extensionContext.getRequiredTestInstance();
        Object[] args = invocationContext.getArguments().toArray();
        try {
            executableMethod.invoke(instance, args);
            return null;
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static String stackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
