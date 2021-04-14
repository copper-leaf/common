package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;

/**
 * | Input  | Result               | Converter |
 * |--------|----------------------|-----------|
 * | number | that number as int   |           |
 * | string | parsed number as int | toString  |
 *
 * @since v1.0.0
 */
public final class IntegerConverter implements TypeConverter<Integer> {

    private final StringConverter stringConverter;

    @Inject
    public IntegerConverter(StringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Integer.class) || clazz.equals(int.class);
    }

    @Override
    public EdenPair<Boolean, Integer> convert(Class clazz, Object objectToConvert) {
        try {
            return new EdenPair<>(true, Integer.parseInt(stringConverter.convert(clazz, objectToConvert).second));
        }
        catch (NumberFormatException e) {
            return new EdenPair<>(false, 0);
        }
    }

}
