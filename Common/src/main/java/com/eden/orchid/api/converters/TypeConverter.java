package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

public interface TypeConverter<T> {

    boolean acceptsClass(Class clazz);

    EdenPair<Boolean, T> convert(Class clazz, Object objectToConvert);

}
