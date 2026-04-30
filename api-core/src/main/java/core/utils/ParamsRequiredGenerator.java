package core.utils;

import core.dto.ParamsReq;
import core.dto.Token;
import core.enums.ActionsEnum;

public class ParamsRequiredGenerator {

    public static ParamsReq generateParamsWithLogin(Token token) {
        return ParamsReq.builder()
                        .token(token.getToken())
                        .actionsEnum(ActionsEnum.LOGIN)
                        .build();
    }

    public static ParamsReq generateParamsWithAction(Token token) {
        return ParamsReq.builder()
                        .token(token.getToken())
                        .actionsEnum(ActionsEnum.ACTION)
                        .build();
    }

    public static ParamsReq generateParamsWithLogout(Token token) {
        return ParamsReq.builder()
                        .token(token.getToken())
                        .actionsEnum(ActionsEnum.LOGOUT)
                        .build();
    }

    public static ParamsReq generateParamsWithTokenAndAction(String token, ActionsEnum action) {
        return ParamsReq.builder()
                        .token(token)
                        .actionsEnum(action)
                        .build();
    }
}
