package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;

/**
 * | Input          | Result | Converter       |
 * |----------------|--------|-----------------|
 * | null           | false  |                 |
 * | true, "true"   | true   |                 |
 * | false, "false" | true   |                 |
 * | number 0       | false  | NumberConverter |
 * | number non-0   | true   | NumberConverter |
 *
 * @since v1.0.0
 * @orchidApi converters
 */
public final class BooleanConverter implements TypeConverter<Boolean> {

    private final StringConverter stringConverter;
    private final NumberConverter numberConverter;

    @Inject
    public BooleanConverter(StringConverter stringConverter, NumberConverter numberConverter) {
        this.stringConverter = stringConverter;
        this.numberConverter = numberConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Boolean.class) || clazz.equals(boolean.class);
    }

    @Override
    public EdenPair<Boolean, Boolean> convert(Class clazz, Object objectToConvert) {
        if (objectToConvert == null) {
            return new EdenPair<>(false, false);
        }

        if (objectToConvert instanceof Boolean) {
            return new EdenPair<>(true, (Boolean) objectToConvert);
        }
        if (objectToConvert instanceof String) {
            String s = stringConverter.convert(clazz, objectToConvert).second;
            if (s.equalsIgnoreCase("true")) {
                return new EdenPair<>(true, true);
            }
            else if (s.equalsIgnoreCase("false")) {
                return new EdenPair<>(true, false);
            }
        }

        EdenPair<Boolean, Number> numberValue = numberConverter.convert(clazz, objectToConvert);
        if (numberValue.first) {
            if (numberValue.second.doubleValue() == 0) {
                return new EdenPair<>(true, false);
            }
            else {
                return new EdenPair<>(true, true);
            }
        }

        return new EdenPair<>(false, false);
    }

}
