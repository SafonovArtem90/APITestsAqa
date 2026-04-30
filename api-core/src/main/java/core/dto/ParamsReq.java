package core.dto;

import core.enums.ActionsEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParamsReq {

    private String token;
    private ActionsEnum actionsEnum;
}
