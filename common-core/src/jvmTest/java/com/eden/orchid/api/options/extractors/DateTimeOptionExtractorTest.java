package com.eden.orchid.api.options.extractors;

import com.eden.orchid.api.converters.DateTimeConverter;
import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.options.annotations.Option;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
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

public class DateTimeOptionExtractorTest extends BaseExtractorTest {

// Test Classes
//----------------------------------------------------------------------------------------------------------------------

    public static class TestClass { @Option public LocalDateTime testValue; }

// Test Setup
//----------------------------------------------------------------------------------------------------------------------

    @BeforeEach
    public void setupTest() {
        StringConverter stringConverter = new StringConverter(new HashSet<>());
        DateTimeConverter dateTimeConverter = new DateTimeConverter(stringConverter);

        setupTest(new DateTimeOptionExtractor(dateTimeConverter), stringConverter, dateTimeConverter);
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("getOptionsArguments")
    public void testExtractOption(
            final Object underTest,
            final Object sourceValue,
            final Object expectedOriginalValue,
            final LocalDateTime expectedExtractedValue) throws Throwable {
        String optionName = "testValue";

        final JSONObject options = new JSONObject();
        if(sourceValue != null) {
            if(sourceValue.toString().equals("_nullValue")) {
                options.put(optionName, (String) null);
            }
            else {
                options.put(optionName, sourceValue);
            }
        }

        LocalDateTime dateTime = (LocalDateTime) underTest.getClass().getField(optionName).get(underTest);

        assertThat(dateTime, is(equalTo(expectedOriginalValue)));
        extractor.extractOptions(underTest, options.toMap());

        dateTime = ((LocalDateTime) underTest.getClass().getField(optionName).get(underTest)).withNano(0).withSecond(0);
        assertThat(dateTime, is(equalTo(expectedExtractedValue.withNano(0).withSecond(0))));
    }

    static Stream<Arguments> getOptionsArguments() throws Throwable {
        return Stream.of(
                Arguments.of(new TestClass(), "2018-01-01",                           null, LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.now())),
                Arguments.of(new TestClass(), "2018-01-01T08:30:00",                  null, LocalDateTime.of(2018, 1, 1, 8, 30, 0)),
                Arguments.of(new TestClass(), LocalDate.of(2018, 1, 1),               null, LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.now())),
                Arguments.of(new TestClass(), LocalDateTime.of(2018, 1, 1, 8, 30, 0), null, LocalDateTime.of(2018, 1, 1, 8, 30, 0)),
                Arguments.of(new TestClass(), "now",                                  null, LocalDateTime.now()),
                Arguments.of(new TestClass(), null,                                   null, LocalDateTime.now()),
                Arguments.of(new TestClass(), "_nullValue",                           null, LocalDateTime.now()),

                Arguments.of(new TestClass(), "now",                                  null, LocalDateTime.now()),
                Arguments.of(new TestClass(), "today",                                null, LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))),
                Arguments.of(new TestClass(), "tomorrow",                             null, LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(0, 0, 0))),
                Arguments.of(new TestClass(), "yesterday",                            null, LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0)))
        );
    }

    @ParameterizedTest
    @MethodSource("getOptionsDescriptionArguments")
    public void testOptionsDescription(
            final Object underTest,
            final String expectedDescription) throws Throwable {
        super.testOptionDescription(
                underTest,
                expectedDescription
        );
    }

    static Stream<Arguments> getOptionsDescriptionArguments() {
        return Stream.of(
                Arguments.of(new TestClass(), "now (yyyy-mm-dd HH:MM:SS)")
        );
    }

}
