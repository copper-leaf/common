package com.eden.orchid.api.converters;

import clog.Clog;

import static clog.dsl.UtilsKt.format;

public class ClogStringConverterHelper implements StringConverterHelper {

    @Override
    public boolean matches(String input) {
        return true;
    }

    public String convert(String input) {
        return format(Clog.INSTANCE, input);
    }

}
