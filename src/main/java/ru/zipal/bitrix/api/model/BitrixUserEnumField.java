package ru.zipal.bitrix.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zipal.bitrix.api.common.FieldName;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BitrixUserEnumField implements HasId {
    private Long id;
    @FieldName("ENTITY_ID")
    private String entityId;
    @FieldName("FIELD_NAME")
    private String fieldName;
    @FieldName("LIST")
    private List<BitrixEnum> enums;

}
