package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;

/**
 * | Input  | Result                 | Converter |
 * |--------|------------------------|-----------|
 * | number | that number as float   |           |
 * | string | parsed number as float | toString  |
 *
 * @since v1.0.0
 */
public final class FloatConverter implements TypeConverter<Float> {

    private final StringConverter stringConverter;

    @Inject
    public FloatConverter(StringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Float.class) || clazz.equals(float.class);
    }

    @Override
    public EdenPair<Boolean, Float> convert(Class clazz, Object objectToConvert) {
        try {
            return new EdenPair<>(true, Float.parseFloat(stringConverter.convert(clazz, objectToConvert).second));
        }
        catch (NumberFormatException e) {
            return new EdenPair<>(false, 0.0f);
        }
    }

}
