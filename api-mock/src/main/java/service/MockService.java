package service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static config.MockConstants.APPLICATION_JSON;
import static config.MockConstants.APPLICATION_URL_ENCODED;
import static config.MockConstants.TOKEN_REGEX;
import static config.MockConstants.URL_AUTH;
import static config.MockConstants.URL_DO_ACTION;

@Slf4j
public class MockService {

    private final WireMockServer wireMockServer;
    private final Map<String, StubMapping> createdStubs = new ConcurrentHashMap<>();

    public MockService(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
    }

    private static String stubKey(String endpoint, String token) {
        return endpoint + "|" + token;
    }

    @Step("MOCK для /auth поднят c возвращаемым статус-кодом 200")
    public void stubAuthSuccess(String token) {
        log.info("Mock Registered for /auth on port {}", wireMockServer.port());
        StubMapping stub = wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo(URL_AUTH))
                                  .withHeader("Content-Type", containing(APPLICATION_URL_ENCODED))
                                  .withHeader("Accept", containing(APPLICATION_JSON))
                                  .withFormParam("token", WireMock.matching(TOKEN_REGEX))
                                  .withRequestBody(containing("token=" + token))
                                  .willReturn(WireMock.aResponse()
                                                      .withStatus(200)));
        createdStubs.put(stubKey(URL_AUTH, token), stub);
    }

    @Step("MOCK для /doAction поднят c возвращаемым статус-кодом 200")
    public void stubDoActionSuccess(String token) {
        log.info("Mock Registered for /doAction on port {}", wireMockServer.port());
        StubMapping stub = wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo(URL_DO_ACTION))
                                  .withHeader("Content-Type", containing(APPLICATION_URL_ENCODED))
                                  .withHeader("Accept", containing(APPLICATION_JSON))
                                  .withFormParam("token", WireMock.matching(TOKEN_REGEX))
                                  .withRequestBody(containing("token=" + token))
                                  .willReturn(WireMock.aResponse()
                                                      .withStatus(200)));
        createdStubs.put(stubKey(URL_DO_ACTION, token), stub);
    }

    @Step("MOCK для {endpoint} поднят c возвращаемым статус-кодом {statusCode}")
    public void stubExternalServiceError(String endpoint, int statusCode, String token) {
        log.info("Mock Registered Error for {} on port {} with code {}", endpoint, wireMockServer.port(), statusCode);
        StubMapping stub = wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo(endpoint))
                                  .withHeader("Content-Type", containing(APPLICATION_URL_ENCODED))
                                  .withHeader("Accept", containing(APPLICATION_JSON))
                                  .withFormParam("token", WireMock.matching(TOKEN_REGEX))
                                  .withRequestBody(containing("token=" + token))
                                  .willReturn(WireMock.aResponse()
                                                      .withStatus(statusCode)));
        createdStubs.put(stubKey(endpoint, token), stub);
    }

    public void stubExternalServiceErrorAuthWith500(String token) {
        stubExternalServiceError(URL_AUTH, 500, token);
    }

    public void stubExternalServiceErrorDoActionWith500(String token) {
        stubExternalServiceError(URL_DO_ACTION, 500, token);
    }

    @Step("Проверка, что вызов /auth произошел 1 раз")
    public void verifyAuthCalled(String token) {
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_AUTH))
                                       .withRequestBody(containing("token=" + token)));
    }

    @Step("Проверка, что вызов /doAction произошел 1 раз")
    public void verifyDoActionCalled(String token) {
        wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_DO_ACTION))
                                       .withRequestBody(containing("token=" + token)));
    }

    @Step("Проверка, что вызов /auth не происходил")
    public void verifyAuthNotCalled(String token) {
        wireMockServer.verify(0, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_AUTH))
                                       .withRequestBody(containing("token=" + token)));
    }

    @Step("Проверка, что вызов /doAction не происходил")
    public void verifyDoActionNotCalled(String token) {
        wireMockServer.verify(0, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_DO_ACTION))
                                       .withRequestBody(containing("token=" + token)));
    }

    @Step("Очищаются Mock для текущего теста")
    public void cleanupStubs(String token) {
        createdStubs.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            boolean match = key.endsWith("|" + token);
            if (match && entry.getValue() != null) {
                wireMockServer.removeStub(entry.getValue());
            }
            return match;
        });
    }
}
