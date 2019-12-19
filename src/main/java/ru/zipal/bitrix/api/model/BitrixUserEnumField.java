package ru.zipal.bitrix.api.model;

import lombok.Data;
import ru.zipal.bitrix.api.common.FieldName;
import ru.zipal.bitrix.api.model.enums.OwnerType;

import java.util.List;

@Data
public class BitrixUserEnumField implements HasId {
    private Long id;
    @FieldName("ENTITY_ID")
    private String entityId;
    @FieldName("FIELD_NAME")
    private String fieldName;
    @FieldName("LIST")
    private List<BitrixEnum> enums;
    private OwnerType ownerType;
}
