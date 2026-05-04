package service;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

import static config.MockConstants.APPLICATION_JSON;
import static config.MockConstants.APPLICATION_URL_ENCODED;
import static config.MockConstants.TOKEN_REGEX;
import static config.MockConstants.URL_AUTH;
import static config.MockConstants.URL_DO_ACTION;

@Slf4j
public class MockService {

    private final WireMock wireMock;

    public MockService(WireMock wireMock) {
        this.wireMock = wireMock;
    }

    @Step("MOCK для /auth поднят c возвращаемым статус-кодом 200")
    public void stubAuthSuccess() {
        log.info("Mock Registered for /auth on port 8888");
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo(URL_AUTH))
                                  .withHeader("Content-Type", WireMock.containing(APPLICATION_URL_ENCODED))
                                  .withHeader("Accept", WireMock.containing(APPLICATION_JSON))
                                  .withFormParam("token", WireMock.matching(TOKEN_REGEX))
                                  .willReturn(WireMock.aResponse()
                                                      .withStatus(200)));
    }

    @Step("MOCK для /doAction поднят c возвращаемым статус-кодом 200")
    public void stubDoActionSuccess() {
        log.info("Mock Registered for /doAction on port 8888");
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo(URL_DO_ACTION))
                                  .withHeader("Content-Type", WireMock.containing(APPLICATION_URL_ENCODED))
                                  .withHeader("Accept", WireMock.containing(APPLICATION_JSON))
                                  .withFormParam("token", WireMock.matching(TOKEN_REGEX))
                                  .willReturn(WireMock.aResponse()
                                                      .withStatus(200)));
    }

    @Step("MOCK для {endpoint} поднят c возвращаемым статус-кодом {statusCode}")
    public void stubExternalServiceError(String endpoint, int statusCode) {
        log.info("Mock Registered Error for {} on port 8888 with code {}", endpoint, statusCode);
        wireMock.register(WireMock.post(WireMock.urlPathEqualTo(endpoint))
                                  .withHeader("Content-Type", WireMock.containing(APPLICATION_URL_ENCODED))
                                  .withHeader("Accept", WireMock.containing(APPLICATION_JSON))
                                  .withFormParam("token", WireMock.matching(TOKEN_REGEX))
                                  .willReturn(WireMock.aResponse()
                                                      .withStatus(statusCode)));
    }

    public void stubExternalServiceErrorAuthWith500() {
        log.info("Mock Registered Error for {} on port 8888 with code 500", URL_AUTH);
        stubExternalServiceError(URL_AUTH, 500);
    }

    @Step("Проверка, что вызов /auth произошел 1 раз")
    public void verifyAuthCalled(String token) {
        wireMock.verifyThat(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_AUTH))
                                       .withRequestBody(WireMock.containing("token=" + token)));
    }

    @Step("Проверка, что вызов /doAction произошел 1 раз")
    public void verifyDoActionCalled(String token) {
        wireMock.verifyThat(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_DO_ACTION))
                                       .withRequestBody(WireMock.containing("token=" + token)));
    }

    @Step("Проверка, что вызов /auth не происходил")
    public void verifyAuthNotCalled() {
        wireMock.verifyThat(0, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_AUTH)));
    }

    @Step("Проверка, что вызов /doAction не происходил")
    public void verifyDoActionNotCalled() {
        wireMock.verifyThat(0, WireMock.postRequestedFor(WireMock.urlPathEqualTo(URL_DO_ACTION)));
    }
}
