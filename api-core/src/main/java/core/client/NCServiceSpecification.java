package core.client;

import core.config.ConfigReader;
import core.filtres.CustomAllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static core.constants.TestConstants.X_API_KEY;

public class NCServiceSpecification {

    private static final String BASE_URL = ConfigReader.getProperty("service.base.url");
    private static final String API_KEY = ConfigReader.getProperty("service.api.key");

    public static RequestSpecification baseSpecReqWithHeader(Map<String, String> customHeaders) {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType("application/x-www-form-urlencoded")
                .setAccept("application/json")
                .addHeaders(customHeaders)
                .log(LogDetail.ALL)
                .addFilter(new CustomAllureRestAssured())
                .build();
    }

    public static RequestSpecification baseSpecReq() {
        return baseSpecReqWithHeader(Map.of(X_API_KEY, API_KEY));
    }

    public static ResponseSpecification baseSpecRes() {
        return new ResponseSpecBuilder()
                .expectContentType((ContentType.JSON))
                .log(LogDetail.ALL)
                .build();
    }

    public static ResponseSpecification baseSpecResWithStatusCode(int code) {
        return new ResponseSpecBuilder()
                .expectContentType((ContentType.JSON))
                .expectStatusCode(code)
                .log(LogDetail.ALL)
                .build();
    }
}
