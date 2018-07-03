package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * | Input  | Result                 | Converter |
 * |--------|------------------------|-----------|
 * | number | that number as float   |           |
 * | string | parsed number as float | toString  |
 *
 * @since v1.0.0
 * @orchidApi converters
 */
public final class DateConverter implements TypeConverter<LocalDate> {

    private final DateTimeConverter converter;

    @Inject
    public DateConverter(DateTimeConverter converter) {
        this.converter = converter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(LocalDate.class);
    }

    @Override
    public EdenPair<Boolean, LocalDate> convert(Object object) {
        EdenPair<Boolean, LocalDateTime> dateTime = converter.convert(object);

        return new EdenPair<>(dateTime.first, dateTime.second.toLocalDate());
    }

}
