package com.eden.orchid.api.converters;

import com.caseyjbrooks.clog.Clog;

public class ClogStringConverterHelper implements StringConverterHelper {

    @Override
    public boolean matches(String input) {
        return true;
    }

    public String convert(String input) {
        return Clog.format(input);
    }

}
