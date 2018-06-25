package com.eden.orchid.api.options;

import com.eden.orchid.api.converters.BooleanConverter;
import com.eden.orchid.api.converters.ClogStringConverterHelper;
import com.eden.orchid.api.converters.Converters;
import com.eden.orchid.api.converters.DateConverter;
import com.eden.orchid.api.converters.DateTimeConverter;
import com.eden.orchid.api.converters.DoubleConverter;
import com.eden.orchid.api.converters.FlexibleIterableConverter;
import com.eden.orchid.api.converters.FlexibleMapConverter;
import com.eden.orchid.api.converters.FloatConverter;
import com.eden.orchid.api.converters.IntegerConverter;
import com.eden.orchid.api.converters.LongConverter;
import com.eden.orchid.api.converters.NumberConverter;
import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.converters.StringConverterHelper;
import com.eden.orchid.api.converters.TimeConverter;
import com.eden.orchid.api.converters.TypeConverter;
import com.eden.orchid.api.options.extractors.AnyOptionExtractor;
import com.eden.orchid.api.options.extractors.ArrayOptionExtractor;
import com.eden.orchid.api.options.extractors.BooleanOptionExtractor;
import com.eden.orchid.api.options.extractors.DateOptionExtractor;
import com.eden.orchid.api.options.extractors.DateTimeOptionExtractor;
import com.eden.orchid.api.options.extractors.DoubleOptionExtractor;
import com.eden.orchid.api.options.extractors.EnumOptionExtractor;
import com.eden.orchid.api.options.extractors.FloatOptionExtractor;
import com.eden.orchid.api.options.extractors.IntOptionExtractor;
import com.eden.orchid.api.options.extractors.JSONArrayOptionExtractor;
import com.eden.orchid.api.options.extractors.JSONObjectOptionExtractor;
import com.eden.orchid.api.options.extractors.ListOptionExtractor;
import com.eden.orchid.api.options.extractors.LongOptionExtractor;
import com.eden.orchid.api.options.extractors.StringOptionExtractor;
import com.eden.orchid.api.options.extractors.TimeOptionExtractor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class DefaultExtractor extends Extractor {

    private static DefaultExtractor instance;

    public static DefaultExtractor getInstance() {
        if(instance == null) {
            instance = new DefaultExtractor();
        }
        return instance;
    }

    private DefaultExtractor() {
        super(getExtractors(), null);
    }

    public static Collection<OptionExtractor> getExtractors() {
        // String converter Helpers
        Set<StringConverterHelper> stringConverterHelpers = new HashSet<>();
        stringConverterHelpers.add(new ClogStringConverterHelper());

        // TypeConverters
        StringConverter stringConverter                     = new StringConverter(stringConverterHelpers);
        LongConverter longConverter                         = new LongConverter(stringConverter);
        IntegerConverter integerConverter                   = new IntegerConverter(stringConverter);
        DoubleConverter doubleConverter                     = new DoubleConverter(stringConverter);
        FloatConverter floatConverter                       = new FloatConverter(stringConverter);
        NumberConverter numberConverter                     = new NumberConverter(longConverter, doubleConverter);
        BooleanConverter booleanConverter                   = new BooleanConverter(stringConverter, numberConverter);
        DateTimeConverter dateTimeConverter                 = new DateTimeConverter(stringConverter);
        DateConverter dateConverter                         = new DateConverter(dateTimeConverter);
        TimeConverter timeConverter                         = new TimeConverter(dateTimeConverter);
        FlexibleMapConverter flexibleMapConverter           = new FlexibleMapConverter();
        FlexibleIterableConverter flexibleIterableConverter = new FlexibleIterableConverter(flexibleMapConverter);

        Set<TypeConverter> typeConverters = new HashSet<>();
        typeConverters.add(stringConverter);
        typeConverters.add(longConverter);
        typeConverters.add(integerConverter);
        typeConverters.add(doubleConverter);
        typeConverters.add(floatConverter);
        typeConverters.add(numberConverter);
        typeConverters.add(booleanConverter);
        typeConverters.add(dateTimeConverter);
        typeConverters.add(dateConverter);
        typeConverters.add(timeConverter);
        typeConverters.add(flexibleMapConverter);
        typeConverters.add(flexibleIterableConverter);

        Converters converters = new Converters(typeConverters);

        // OptionExtractor
        Set<OptionExtractor> extractors = new HashSet<>();
        extractors.add(new AnyOptionExtractor());
        extractors.add(new ArrayOptionExtractor(flexibleIterableConverter, converters));
        extractors.add(new BooleanOptionExtractor(booleanConverter));
        extractors.add(new DateOptionExtractor(dateTimeConverter));
        extractors.add(new DateTimeOptionExtractor(dateTimeConverter));
        extractors.add(new DoubleOptionExtractor(doubleConverter));
        extractors.add(new FloatOptionExtractor(floatConverter));
        extractors.add(new IntOptionExtractor(integerConverter));
        extractors.add(new JSONArrayOptionExtractor(flexibleIterableConverter));
        extractors.add(new JSONObjectOptionExtractor(flexibleMapConverter));
        extractors.add(new ListOptionExtractor(flexibleIterableConverter, converters));
        extractors.add(new LongOptionExtractor(longConverter));
        extractors.add(new StringOptionExtractor(stringConverter));
        extractors.add(new TimeOptionExtractor(timeConverter));
        extractors.add(new EnumOptionExtractor(stringConverter));

        return extractors;
    }
}
