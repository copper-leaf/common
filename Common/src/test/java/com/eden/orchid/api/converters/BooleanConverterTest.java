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

public class BooleanConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private BooleanConverter underTest;

    @BeforeEach
    public void setupTest() {
        StringConverter converter = new StringConverter(new HashSet<>());
        NumberConverter numberConverter = new NumberConverter(new LongConverter(converter), new DoubleConverter(converter));
        underTest = new BooleanConverter(converter, numberConverter);
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.acceptsClass(Boolean.class), is(equalTo(true)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final boolean expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, Boolean> result = underTest.convert(sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of(45,              true,  true),
                Arguments.of(45.1,            true,  true),
                Arguments.of("45",            true,  true),
                Arguments.of("45.1",          true,  true),
                Arguments.of(true,            true,  true),
                Arguments.of(false,           true,  false),
                Arguments.of("true",          true,  true),
                Arguments.of("false",         true,  false),
                Arguments.of(new JSONArray(), false, false),
                Arguments.of(null,            false, false),
                Arguments.of("null",          false, false),
                Arguments.of(10L,             true,  true),
                Arguments.of(10.0,            true,  true),
                Arguments.of(0,               true,  false),
                Arguments.of("0",             true,  false),
                Arguments.of("",              false, false)
        );
    }

}
