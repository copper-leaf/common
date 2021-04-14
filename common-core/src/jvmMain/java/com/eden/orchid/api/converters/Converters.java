package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;
import java.util.Set;

public class Converters {

    private final Set<TypeConverter> converters;

    @Inject
    public Converters(Set<TypeConverter> converters) {
        this.converters = converters;
    }

    public <T> EdenPair<Boolean, T> convert(Object object, Class<T> targetClass) {
        for(TypeConverter converter : converters) {
            if(converter.acceptsClass(targetClass)) {
                return (EdenPair<Boolean, T>) converter.convert(targetClass, object);
            }
        }

        return new EdenPair<>(false, null);
    }
}
