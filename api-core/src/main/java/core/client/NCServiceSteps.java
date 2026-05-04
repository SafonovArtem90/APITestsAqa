package core.client;

import core.dto.FailRs;
import core.dto.ParamsReq;
import core.dto.SuccessRs;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

public class NCServiceSteps {

    @Step("Отправка успешного запроса /endpoint с параметрами: {paramsReq}")
    public SuccessRs receivedSuccessRs(ParamsReq paramsReq) {
        return NCServiceManager.receivedSuccessRsFromEndpoint(paramsReq);
    }

    @Step("Отправка не успешного запроса /endpoint с параметрами:{paramsReq} и c заголовками:{customHeaders}")
    public Response receivedFailResponseWithCustomHeader(ParamsReq paramsReq, Map<String, String> customHeaders) {
        return NCServiceManager.receivedFailResponseWithCustomHeaderFromEndpoint(paramsReq, customHeaders);
    }

    @Step("Отправка запроса /endpoint с параметрами:{paramsReq}")
    public Response receivedResponse(ParamsReq paramsReq) {
        return NCServiceManager.receivedResponseFromEndpoint(paramsReq);
    }

    @Step("Отправка не успешного запроса /endpoint с параметрами:{paramsReq} и статус-кодом:{statusCode}")
    public FailRs receivedFailRs(ParamsReq paramsReq, int statusCode) {
        return NCServiceManager.receivedFailRsFromEndpoint(paramsReq, statusCode);
    }

    @Step("Отправка запроса /endpoint с параметрами:{paramsReq} и c заголовками:{customHeaders} и статус-кодом:{statusCode}")
    public Response receivedResponseWithCustomData(Map<String, String> paramsReq, Map<String, String> customHeaders, int statusCode) {
        return NCServiceManager.receivedResponseFromEndpointWithAllParams(paramsReq, customHeaders, statusCode);
    }
}
