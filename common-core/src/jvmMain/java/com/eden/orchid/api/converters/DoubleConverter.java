package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;

/**
 * | Input  | Result                  | Converter |
 * |--------|-------------------------|-----------|
 * | number | that number as double   |           |
 * | string | parsed number as double | toString  |
 *
 * @since v1.0.0
 */
public final class DoubleConverter implements TypeConverter<Double> {

    private final StringConverter stringConverter;

    @Inject
    public DoubleConverter(StringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Double.class) || clazz.equals(double.class);
    }

    @Override
    public EdenPair<Boolean, Double> convert(Class clazz, Object objectToConvert) {
        try {
            return new EdenPair<>(true, Double.parseDouble(stringConverter.convert(clazz, objectToConvert).second));
        }
        catch (NumberFormatException e) {
            return new EdenPair<>(false, 0.0);
        }
    }

}
