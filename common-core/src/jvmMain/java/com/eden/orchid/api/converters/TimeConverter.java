package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * | Input  | Result                 | Converter |
 * |--------|------------------------|-----------|
 * | number | that number as float   |           |
 * | string | parsed number as float | toString  |
 *
 * @since v1.0.0
 */
public final class TimeConverter implements TypeConverter<LocalTime> {

    private final DateTimeConverter converter;

    @Inject
    public TimeConverter(DateTimeConverter converter) {
        this.converter = converter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(LocalTime.class);
    }

    @Override
    public EdenPair<Boolean, LocalTime> convert(Class clazz, Object objectToConvert) {
        EdenPair<Boolean, LocalDateTime> dateTime = converter.convert(clazz, objectToConvert);

        return new EdenPair<>(dateTime.first, dateTime.second.toLocalTime());
    }

}
