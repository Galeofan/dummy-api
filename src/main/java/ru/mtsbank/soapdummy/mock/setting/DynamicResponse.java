package ru.mtsbank.soapdummy.mock.setting;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.ConstructorProperties;

/**
 * Class described the dynamic field in mock response
 */
@Value
@Getter
@EqualsAndHashCode
@ToString
public class DynamicResponse {

    /**
     * Field placeholder for putting the field
     */
    @Nonnull
    String placeHolder;
    /**
     * Type of field
     */
    @Nonnull
    DynamicType type;
    /**
     * Fields length
     */
    @Nonnull
    Integer fieldLength;

    @ConstructorProperties({
            "placeHolder",
            "type",
            "fieldLength"
    })
    public DynamicResponse(@Nonnull String placeHolder, @Nonnull DynamicType type, @Nullable Integer fieldLength) {
        this.placeHolder = placeHolder;
        this.type = type;
        this.fieldLength = fieldLength != null && fieldLength != 0 ? fieldLength : maxDefaultLengthByType(type);
    }

    /**
     * Max default length for field by specific type
     * @param type type
     * @return max default length
     */
    private static int maxDefaultLengthByType(DynamicType type) {
        switch (type) {
            case INTEGER:
                return 10;
            case BIG_INTEGER:
                return 19;
            case UUID_SEPARATED:
            case STRING:
            case UUID:
            default:
                return 1;
        }
    }
}
