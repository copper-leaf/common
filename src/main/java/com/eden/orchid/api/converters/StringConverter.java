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
 * @orchidApi converters
 */
public final class StringConverter implements TypeConverter<String> {

    private final Set<StringConverterHelper> helpers;

    @Inject
    public StringConverter(Set<StringConverterHelper> helpers) {
        this.helpers = helpers;
    }

    @Override
    public Class<String> resultClass() {
        return String.class;
    }

    @Override
    public EdenPair<Boolean, String> convert(Object object) {
        if(object != null) {
            String input = object.toString();
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
