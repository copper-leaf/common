package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;

/**
 * | Input  | Result                | Converter |
 * |--------|-----------------------|-----------|
 * | number | that number as long   |           |
 * | string | parsed number as long | toString  |
 *
 * @since v1.0.0
 */
public final class LongConverter implements TypeConverter<Long> {

    private final StringConverter stringConverter;

    @Inject
    public LongConverter(StringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Long.class) || clazz.equals(long.class);
    }

    @Override
    public EdenPair<Boolean, Long> convert(Class clazz, Object objectToConvert) {
        try {
            return new EdenPair<>(true, Long.parseLong(stringConverter.convert(clazz, objectToConvert).second));
        }
        catch (NumberFormatException e) {
            return new EdenPair<>(false, 0L);
        }
    }

}
