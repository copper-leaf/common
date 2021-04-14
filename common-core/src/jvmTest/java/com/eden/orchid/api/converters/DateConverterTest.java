package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DateConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private DateConverter underTest;

    @BeforeEach
    public void setupTest() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter(new StringConverter(new HashSet<>()));
        underTest = new DateConverter(dateTimeConverter);
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.acceptsClass(LocalDate.class), is(equalTo(true)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final LocalDate expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, LocalDate> result = underTest.convert(LocalDate.class, sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of("2018-01-01T08:30:00", true,  LocalDate.of(2018, 1, 1)),
                Arguments.of("2018-01-01T08:30",    true,  LocalDate.of(2018, 1, 1)),
                Arguments.of("2018-01-01T08",       false, LocalDate.now()),
                Arguments.of("2018-01-01",          true,  LocalDate.of(2018, 1, 1)),
                Arguments.of("2018-01",             false, LocalDate.now()),
                Arguments.of("2018",                false, LocalDate.now()),
                Arguments.of("",                    false, LocalDate.now()),
                Arguments.of(null,                  false, LocalDate.now()),

                Arguments.of("08:30:00", true,  LocalDate.now()),
                Arguments.of("08:30",    true,  LocalDate.now()),
                Arguments.of("08",       false, LocalDate.now()),

                Arguments.of(LocalDate.of(2018, 1, 1),               true, LocalDate.of(2018, 1, 1)),
                Arguments.of(LocalTime.of(8, 30, 0),                 true, LocalDate.now()),
                Arguments.of(LocalDateTime.of(2018, 1, 1, 8, 30, 0), true, LocalDate.of(2018, 1, 1)),

                Arguments.of("now",       true, LocalDate.now()),
                Arguments.of("today",     true, LocalDate.now()),
                Arguments.of("tomorrow",  true, LocalDate.now().plusDays(1)),
                Arguments.of("yesterday", true, LocalDate.now().minusDays(1))
        );
    }

}
