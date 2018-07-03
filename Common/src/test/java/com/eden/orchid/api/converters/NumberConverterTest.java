package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class NumberConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private NumberConverter underTest;

    @BeforeEach
    public void setupTest() {
        StringConverter converter = new StringConverter(new HashSet<>());
        underTest = new NumberConverter(new LongConverter(converter), new DoubleConverter(converter));
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.acceptsClass(Number.class), is(equalTo(true)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final Number expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, Number> result = underTest.convert(sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second.doubleValue(), is(equalTo(expectedExtractedValue.doubleValue())));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of(45,              true,  45.0),
                Arguments.of(true,            false, 0.0),
                Arguments.of(false,           false, 0.0),
                Arguments.of("45",            true,  45.0),
                Arguments.of(new JSONArray(), false, 0.0),
                Arguments.of(null,            false, 0.0),
                Arguments.of(45.1,            true,  45.1),
                Arguments.of(10L,             true,  10.0),
                Arguments.of(10.0,            true,  10.0)
        );
    }

}
