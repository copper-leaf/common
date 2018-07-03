package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class LongConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private LongConverter underTest;

    @BeforeEach
    public void setupTest() {
        underTest = new LongConverter(new StringConverter(new HashSet<>()));
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.acceptsClass(Long.class), is(equalTo(true)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final Object expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, Long> result = underTest.convert(sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of(45,              true,  45L),
                Arguments.of(true,            false, 0L),
                Arguments.of(false,           false, 0L),
                Arguments.of("45",            true,  45L),
                Arguments.of(new JSONArray(), false, 0L),
                Arguments.of(null,            false, 0L),
                Arguments.of(45.1,            false, 0L),
                Arguments.of(10L,             true,  10L),
                Arguments.of(10.0,            false, 0L)
        );
    }

}
