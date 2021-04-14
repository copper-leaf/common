package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * | Input  | Result                 | Converter |
 * |--------|------------------------|-----------|
 * | number | that number as float   |           |
 * | string | parsed number as float | toString  |
 *
 * @since v1.0.0
 */
public final class DateTimeConverter implements TypeConverter<LocalDateTime> {

    private final StringConverter stringConverter;

    @Inject
    public DateTimeConverter(StringConverter stringConverter) {
        this.stringConverter = stringConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(LocalDateTime.class);
    }

    @Override
    public EdenPair<Boolean, LocalDateTime> convert(Class clazz, Object objectToConvert) {
        if(objectToConvert instanceof LocalDate) {
            return new EdenPair<>(true, ((LocalDate) objectToConvert).atTime(LocalTime.now()));
        }
        else if(objectToConvert instanceof LocalTime) {
            return new EdenPair<>(true, ((LocalTime) objectToConvert).atDate(LocalDate.now()));
        }
        else if(objectToConvert instanceof LocalDateTime) {
            return new EdenPair<>(true, ((LocalDateTime) objectToConvert));
        }
        else {
            String dateTimeString = stringConverter.convert(clazz, objectToConvert).second;

            if(dateTimeString.equalsIgnoreCase("now")) {
                return new EdenPair<>(true, LocalDateTime.now());
            }
            else if(dateTimeString.equalsIgnoreCase("today")) {
                return new EdenPair<>(true, LocalDate.now().atStartOfDay());
            }
            else if(dateTimeString.equalsIgnoreCase("yesterday")) {
                return new EdenPair<>(true, LocalDate.now().atStartOfDay().minusDays(1));
            }
            else if(dateTimeString.equalsIgnoreCase("tomorrow")) {
                return new EdenPair<>(true, LocalDate.now().atStartOfDay().plusDays(1));
            }

            try {
                return new EdenPair<>(true, LocalDateTime.parse(dateTimeString));
            }
            catch (DateTimeParseException e) { }

            try {
                return new EdenPair<>(true, LocalDate.parse(dateTimeString).atTime(LocalTime.now()));
            }
            catch (DateTimeParseException e) { }

            try {
                return new EdenPair<>(true, LocalTime.parse(dateTimeString).atDate(LocalDate.now()));
            }
            catch (DateTimeParseException e) { }
        }

        return new EdenPair<>(false, LocalDateTime.now());
    }

}
