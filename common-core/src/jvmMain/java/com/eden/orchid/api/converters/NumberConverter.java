package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;

/**
 * | Input          | Result                | Converter       |
 * |----------------|-----------------------|-----------------|
 * | decimal number | that number as double | DoubleConverter |
 * | integer number | that number as long   | LongConverter   |
 *
 * @since v1.0.0
 */
public final class NumberConverter implements TypeConverter<Number> {

    private final LongConverter longConverter;
    private final DoubleConverter doubleConverter;

    @Inject
    public NumberConverter(LongConverter longConverter, DoubleConverter doubleConverter) {
        this.longConverter = longConverter;
        this.doubleConverter = doubleConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Number.class);
    }

    @Override
    public EdenPair<Boolean, Number> convert(Class clazz, Object objectToConvert) {
        if(objectToConvert != null) {
            EdenPair<Boolean, Long> longValue = longConverter.convert(clazz, objectToConvert);
            if(longValue.first) {
                return new EdenPair<>(true, (Number) longValue.second);
            }
            EdenPair<Boolean, Double> doubleValue = doubleConverter.convert(clazz, objectToConvert);
            if(doubleValue.first) {
                return new EdenPair<>(true, (Number) doubleValue.second);
            }
        }

        return new EdenPair<>(false, (Number) 0);
    }

}
