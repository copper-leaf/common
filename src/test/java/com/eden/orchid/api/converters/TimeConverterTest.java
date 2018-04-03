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

public class TimeConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private TimeConverter underTest;

    @BeforeEach
    public void setupTest() {
        DateTimeConverter dateTimeConverter = new DateTimeConverter(new StringConverter(new HashSet<>()));
        underTest = new TimeConverter(dateTimeConverter);
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.resultClass(), is(equalTo(LocalTime.class)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final LocalTime expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, LocalTime> result = underTest.convert(sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second.withSecond(0).withNano(0), is(equalTo(expectedExtractedValue.withSecond(0).withNano(0))));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of("2018-01-01T08:30:00", true,  LocalTime.of(8, 30, 0, 0)),
                Arguments.of("2018-01-01T08:30",    true,  LocalTime.of(8, 30, 0, 0)),
                Arguments.of("2018-01-01T08",       false, LocalTime.now()),
                Arguments.of("2018-01-01",          true,  LocalTime.now()),
                Arguments.of("2018-01",             false, LocalTime.now()),
                Arguments.of("2018",                false, LocalTime.now()),
                Arguments.of("",                    false, LocalTime.now()),
                Arguments.of(null,                  false, LocalTime.now()),

                Arguments.of("08:30:00", true,  LocalTime.of(8, 30, 0)),
                Arguments.of("08:30",    true,  LocalTime.of(8, 30, 0)),
                Arguments.of("08",       false, LocalTime.now()),

                Arguments.of(LocalDate.of(2018, 1, 1),               true, LocalTime.now()),
                Arguments.of(LocalTime.of(8, 30, 0),                 true, LocalTime.of(8, 30, 0)),
                Arguments.of(LocalDateTime.of(2018, 1, 1, 8, 30, 0), true, LocalTime.of(8, 30, 0)),

                Arguments.of("now",       true, LocalTime.now()),
                Arguments.of("today",     true, LocalTime.of(0, 0, 0)),
                Arguments.of("tomorrow",  true, LocalTime.of(0, 0, 0)),
                Arguments.of("yesterday", true, LocalTime.of(0, 0, 0))
        );
    }

}
