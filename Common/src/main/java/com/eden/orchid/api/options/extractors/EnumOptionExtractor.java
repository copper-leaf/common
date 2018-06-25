package com.eden.orchid.api.options.extractors;

import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.options.OptionExtractor;
import com.eden.orchid.api.options.annotations.StringDefault;

import javax.inject.Inject;
import java.lang.reflect.Field;

public class EnumOptionExtractor extends OptionExtractor<Object> {

    private final StringConverter converter;

    @Inject
    public EnumOptionExtractor(StringConverter converter) {
        super(15);
        this.converter = converter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.isEnum();
    }

    @Override
    public Object getOption(Field field, Object sourceObject, String key) {
        String converted = converter.convert(sourceObject).second;
        return getEnumValue(field.getType(), converted);
    }

    @Override
    public boolean isEmptyValue(Object value) {
        return value == null;
    }

    @Override
    public Object getDefaultValue(Field field) {
        if(field.isAnnotationPresent(StringDefault.class)) {
            String[] defaultValue = field.getAnnotation(StringDefault.class).value();
            if(defaultValue.length > 0) {
                return getEnumValue(field.getType(), defaultValue[0]);
            }
        }
        return null;
    }

    private Object getEnumValue(Class<?> enumClass, String name) {
        try {
            return enumClass.getMethod("valueOf", String.class).invoke(null, name);
        }
        catch (Exception e) { }
        return null;
    }

    @Override
    public String describeDefaultValue(Field field) {
        Object value = getDefaultValue(field);

        if(value != null) {
            return value.toString();
        }
        else {
            return "null";
        }
    }
}