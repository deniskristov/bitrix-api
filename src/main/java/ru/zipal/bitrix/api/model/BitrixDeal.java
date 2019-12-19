package ru.zipal.bitrix.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.zipal.bitrix.api.common.FieldName;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BitrixDeal implements HasId {
    private Long id;
    @FieldName("BEGINDATE")
    private Date beginDate;
    @FieldName("CLOSEDATE")
    private Date closeDate;
    @FieldName("TITLE")
    private String title;
    @FieldName("CURRENCY_ID")
    private String currencyId;
    @FieldName("UF_CRM_1568101160")
    private String fio;
    @FieldName("UF_CRM_1568037715")
    private String documentId;
    @FieldName("UF_CRM_1568100533")
    private Integer fineDays;
    @FieldName("UF_CRM_5D53C3D8264DE")
    private String productName;
    // Мобилки, компьютеры и тд
    @FieldName("UF_CRM_5D5CEE2FA17D2")
    private String productType;
    @FieldName("UF_CRM_1570197329")
    private String phoneNumber;
    // Город, где расположено отделение
    @FieldName("UF_CRM_5D53C3D8012F2")
    private String city;
    @FieldName("COMMENTS")
    private String comment;
    @FieldName("CONTACT_ID")
    private Long contactId;
    //Новый договор – ‘PROPOSAL’
    //Просрочен – ‘NEGOTIATION’
    //Проценты вручн. –‘ 1’
    //Выкуплен – ‘WON’
    //Продан – ‘LOSE’
    @FieldName("STAGE_ID")
    private String stageId;
    @FieldName("SOURCE_ID")
    @Builder.Default
    private String sourceId = "PARTNER";

    // Money fields
    @FieldName("UF_CRM_1568037628")
    private String loan;
    // Total to pay today = loan + pct + fine
    @FieldName("OPPORTUNITY")
    private String opportunity;
    @FieldName("UF_CRM_1574242509")
    private String pct;
    @FieldName("UF_CRM_1574242554")
    private String fine;
    @FieldName("UF_CRM_1574683759")
    private Long statusId;


    public enum Currency {
        UAH("UAH");

        private String currency;

        Currency(String currency) {
            this.currency = currency;
        }
    }

    public static class BitrixDealBuilder {
        public BitrixDealBuilder createNewDeal() {
            this.stageId = "PROPOSAL";
            return this;
        }

        public BitrixDealBuilder createFineDeal(int fineDays) {
            this.fineDays = fineDays;
            this.stageId = "NEGOTIATION";
            return this;
        }
    }
}
