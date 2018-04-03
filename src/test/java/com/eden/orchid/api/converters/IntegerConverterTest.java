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

public class IntegerConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private IntegerConverter underTest;

    @BeforeEach
    public void setupTest() {
        underTest = new IntegerConverter(new StringConverter(new HashSet<>()));
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.resultClass(), is(equalTo(Integer.class)));
    }

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final Object expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, Integer> result = underTest.convert(sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of(45,              true,  45),
                Arguments.of(true,            false, 0),
                Arguments.of(false,           false, 0),
                Arguments.of("45",            true,  45),
                Arguments.of(new JSONArray(), false, 0),
                Arguments.of(null,            false, 0),
                Arguments.of(45.1,            false, 0),
                Arguments.of(10L,             true,  10),
                Arguments.of(10.0,            false, 0)
        );
    }

}
