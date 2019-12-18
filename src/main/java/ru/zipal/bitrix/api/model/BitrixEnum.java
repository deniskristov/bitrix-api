package ru.zipal.bitrix.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zipal.bitrix.api.common.FieldName;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BitrixEnum {
    private Long id;
    @FieldName("SORT")
    private Integer sort;
    @FieldName("VALUE")
    private String value;
    @FieldName("DEF")
    private String def;
}
