package core.filtres;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class CustomAllureRestAssured extends AllureRestAssured {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
                           FilterContext filterContext) {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        String stepUuid = UUID.randomUUID().toString();

        String methodName = requestSpec.getMethod();
        String uri = requestSpec.getURI();
        String stepName = String.format("%s: %s", methodName, uri);

        String action = extractActionFromRequest(requestSpec);
        if (action != null) {
            stepName = String.format("[%s] %s: %s", action, methodName, uri);
        }

        lifecycle.startStep(stepUuid, new StepResult().setName(stepName));

        try {
            long startTime = System.currentTimeMillis();
            Response response = super.filter(requestSpec, responseSpec, filterContext);
            long duration = System.currentTimeMillis() - startTime;
            Allure.parameter("Response Time (ms)", duration);

            String finalName = String.format("%s -> %d %s (%d ms)",
                                             stepName,
                                             response.getStatusCode(),
                                             response.getStatusLine(),
                                             duration);

            lifecycle.updateStep(stepUuid, step -> step.setName(finalName));
            return response;
        } catch (Exception e) {
            String finalStepName = stepName;
            lifecycle.updateStep(stepUuid, step -> {
                step.setName(finalStepName + " -> CONNECTION ERROR");
                step.setStatus(Status.BROKEN);

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                String stackTrace = stringWriter.toString();

                InputStream stream = new ByteArrayInputStream(stackTrace.getBytes(StandardCharsets.UTF_8));
                lifecycle.addAttachment("Stack Trace", "text/plain", "txt", stream);
            });
            throw e;
        } finally {
            lifecycle.stopStep();
        }
    }

    private String extractActionFromRequest(FilterableRequestSpecification requestSpec) {
        try {
            Object bodyObj = requestSpec.getFormParams();
            if (bodyObj != null) {
                String body = bodyObj.toString();
                if (body.contains("action=")) {
                    int start = body.indexOf("action=") + 7;
                    int end = body.indexOf("&", start);
                    if (end == -1) end = body.length();
                    return body.substring(start, end);
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}
