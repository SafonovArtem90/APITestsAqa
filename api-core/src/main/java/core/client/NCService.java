package core.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class NCService {

    protected static Response performPostMethod(String path, RequestSpecification reqSpec,
                                                ResponseSpecification resSpec, Map<String, String> params) {
        return given(reqSpec)
                .formParams(params)
                .post(path)
                .then()
                .spec(resSpec)
                .extract().response();
    }
}
