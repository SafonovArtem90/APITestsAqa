package core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionsEnum {

    LOGIN("LOGIN"),
    ACTION("ACTION"),
    LOGOUT("LOGOUT");

    private final String action;
}
