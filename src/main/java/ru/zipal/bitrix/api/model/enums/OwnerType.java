package ru.zipal.bitrix.api.model.enums;

import lombok.Getter;
import ru.zipal.bitrix.api.common.BitrixEnum;

@Getter
public enum OwnerType implements BitrixEnum {
    LEAD("1", "lead"),
    DEAL("2", "deal"),
    CONTACT("3", "contact"),
    COMPANY("4", "company");

    private final String id;
    private final String urlPath;

    OwnerType(String id, String urlPath) {
        this.id = id;
        this.urlPath = urlPath;
    }
}
