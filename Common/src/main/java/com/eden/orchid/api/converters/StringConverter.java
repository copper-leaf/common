package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;
import java.util.Set;

/**
 * | Input    | Result            | Converter |
 * |----------|-------------------|-----------|
 * | anything | object.toString() |           |
 * | null     | empty string      |           |
 *
 * @since v1.0.0
 */
public final class StringConverter implements TypeConverter<String> {

    private final Set<StringConverterHelper> helpers;

    @Inject
    public StringConverter(Set<StringConverterHelper> helpers) {
        this.helpers = helpers;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(String.class);
    }

    @Override
    public EdenPair<Boolean, String> convert(Class clazz, Object objectToConvert) {
        if(objectToConvert != null) {
            String input = objectToConvert.toString();
            for(StringConverterHelper helper : helpers) {
                if(helper.matches(input)) {
                    input = helper.convert(input);
                }
            }

            return new EdenPair<>(true, input);
        }
        return new EdenPair<>(true, "");
    }

}
