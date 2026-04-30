package core.client;

import core.dto.FailRs;
import core.dto.ParamsReq;
import core.dto.SuccessRs;
import io.restassured.response.Response;

import java.util.Map;

import static core.client.NCServiceSpecification.baseSpecReq;
import static core.client.NCServiceSpecification.baseSpecReqWithHeader;
import static core.client.NCServiceSpecification.baseSpecRes;
import static core.client.NCServiceSpecification.baseSpecResWithStatusCode;
import static core.constants.UrlConstants.URL_ENDPOINT;

public class NCServiceManager extends NCService {

    private static final String TOKEN_NAME = "token";
    private static final String ACTION_NAME = "action";

    public static SuccessRs receivedSuccessRsFromEndpoint(ParamsReq paramsReq) {
        Map<String, String> formParams = Map.of(TOKEN_NAME, paramsReq.getToken(), ACTION_NAME, paramsReq.getActionsEnum().getAction());
        return performPostMethod(URL_ENDPOINT, baseSpecReq(), baseSpecResWithStatusCode(200), formParams).as(SuccessRs.class);
    }

    public static FailRs receivedFailRsFromEndpoint(ParamsReq paramsReq, int statusCode) {
        Map<String, String> formParams = Map.of(TOKEN_NAME, paramsReq.getToken(), ACTION_NAME, paramsReq.getActionsEnum().getAction());
        return performPostMethod(URL_ENDPOINT, baseSpecReq(), baseSpecResWithStatusCode(statusCode), formParams).as(FailRs.class);
    }

    public static Response receivedFailRsWithCustomHeaderFromEndpoint(ParamsReq paramsReq, Map<String, String> customHeaders) {
        Map<String, String> formParams = Map.of(TOKEN_NAME, paramsReq.getToken(), ACTION_NAME, paramsReq.getActionsEnum().getAction());
        return performPostMethod(URL_ENDPOINT, baseSpecReqWithHeader(customHeaders), baseSpecRes(), formParams);
    }

    public static Response receivedResponseFromEndpoint(ParamsReq paramsReq) {
        Map<String, String> formParams = Map.of(TOKEN_NAME, paramsReq.getToken(), ACTION_NAME, paramsReq.getActionsEnum().getAction());
        return performPostMethod(URL_ENDPOINT, baseSpecReq(), baseSpecRes(), formParams);
    }

    public static Response receivedResponseFromEndpointWithAllParams(Map<String, String> formParams, Map<String, String> customHeaders,
                                                         int statusCode) {
        return performPostMethod(URL_ENDPOINT, baseSpecReqWithHeader(customHeaders),
                                 baseSpecResWithStatusCode(statusCode), formParams);
    }
}
