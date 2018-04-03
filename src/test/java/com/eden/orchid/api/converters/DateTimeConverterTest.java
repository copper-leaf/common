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

public class DateTimeConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private DateTimeConverter underTest;

    @BeforeEach
    public void setupTest() {
        underTest = new DateTimeConverter(new StringConverter(new HashSet<>()));
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.resultClass(), is(equalTo(LocalDateTime.class)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final LocalDateTime expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, LocalDateTime> result = underTest.convert(sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second.withSecond(0).withNano(0), is(equalTo(expectedExtractedValue.withSecond(0).withNano(0))));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of("2018-01-01T08:30:00", true,  LocalDateTime.of(2018, 1, 1, 8, 30, 0, 0)),
                Arguments.of("2018-01-01T08:30",    true,  LocalDateTime.of(2018, 1, 1, 8, 30, 0, 0)),
                Arguments.of("2018-01-01T08",       false, LocalDateTime.now()),
                Arguments.of("2018-01-01",          true,  LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.now())),
                Arguments.of("2018-01",             false, LocalDateTime.now()),
                Arguments.of("2018",                false, LocalDateTime.now()),
                Arguments.of("",                    false, LocalDateTime.now()),
                Arguments.of(null,                  false, LocalDateTime.now()),

                Arguments.of("08:30:00", true,  LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30, 0))),
                Arguments.of("08:30",    true,  LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30, 0))),
                Arguments.of("08",       false, LocalDateTime.now()),

                Arguments.of(LocalDate.of(2018, 1, 1),               true, LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.now())),
                Arguments.of(LocalTime.of(8, 30, 0),                 true, LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30, 0))),
                Arguments.of(LocalDateTime.of(2018, 1, 1, 8, 30, 0), true, LocalDateTime.of(2018, 1, 1, 8, 30, 0)),

                Arguments.of("now",       true, LocalDateTime.now()),
                Arguments.of("today",     true, LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))),
                Arguments.of("tomorrow",  true, LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0, 0))),
                Arguments.of("yesterday", true, LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0)))
        );
    }

}
