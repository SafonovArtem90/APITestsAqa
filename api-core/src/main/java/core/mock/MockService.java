package core.mock;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static core.constants.TestConstants.APPLICATION_JSON;
import static core.constants.TestConstants.APPLICATION_URL_ENCODED;
import static core.constants.TestConstants.REGEX;
import static core.constants.UrlConstants.URL_AUTH;
import static core.constants.UrlConstants.URL_DO_ACTION;

@Slf4j
public class MockService {

    private final WireMock wireMock;

    public MockService(WireMock wireMock) {
        this.wireMock = wireMock;
    }

    @Step("MOCK для /auth поднят c возвращаемым статус-кодом 200")
    public void stubAuthSuccess() {
        log.info("Mock Registered for /auth on port 8888");
        wireMock.register(post(urlPathEqualTo(URL_AUTH))
                        .withHeader("Content-Type", containing(APPLICATION_URL_ENCODED))
                        .withHeader("Accept", containing(APPLICATION_JSON))
                        .withFormParam("token", matching(REGEX))
                        .willReturn(aResponse()
                                            .withStatus(200)));
    }

    @Step("MOCK для /doAction поднят c возвращаемым статус-кодом 200")
    public void stubDoActionSuccess() {
        log.info("Mock Registered for /doAction on port 8888");
        wireMock.register(post(urlPathEqualTo(URL_DO_ACTION))
                         .withHeader("Content-Type", containing(APPLICATION_URL_ENCODED))
                         .withHeader("Accept", containing(APPLICATION_JSON))
                         .withFormParam("token", matching(REGEX))
                        .willReturn(aResponse()
                                            .withStatus(200)));
    }

    @Step("MOCK для {endpoint} поднят c возвращаемым статус-кодом {statusCode}")
    public void stubExternalServiceError(String endpoint, int statusCode) {
        log.info("Mock Registered Error for {} on port 8888 with code {}", endpoint, statusCode);
        wireMock.register(post(urlPathEqualTo(endpoint))
                        .withHeader("Content-Type", containing(APPLICATION_URL_ENCODED))
                        .withHeader("Accept", containing(APPLICATION_JSON))
                        .withFormParam("token", matching(REGEX))
                        .willReturn(aResponse()
                                            .withStatus(statusCode)));
    }

    public void stubExternalServiceErrorAuthWith500() {
        log.info("Mock Registered Error for {} on port 8888 with code 500", URL_AUTH);
        stubExternalServiceError(URL_AUTH, 500);
    }

    @Step("Проверка, что вызов /auth произошел 1 раз")
    public void verifyAuthCalled(String token) {
        wireMock.verifyThat(1, postRequestedFor(urlPathEqualTo(URL_AUTH))
                       .withRequestBody(containing("token=" + token)));
    }

    @Step("Проверка, что вызов /doAction произошел 1 раз")
    public void verifyDoActionCalled(String token) {
        wireMock.verifyThat(1, postRequestedFor(urlPathEqualTo(URL_DO_ACTION))
                       .withRequestBody(containing("token=" + token)));
    }

    @Step("Проверка, что вызов /auth не происходил")
    public void verifyAuthNotCalled() {
        wireMock.verifyThat(0, postRequestedFor(urlPathEqualTo(URL_AUTH)));
    }

    @Step("Проверка, что вызов /doAction не происходил")
    public void verifyDoActionNotCalled() {
        wireMock.verifyThat(0, postRequestedFor(urlPathEqualTo(URL_DO_ACTION)));
    }
}
