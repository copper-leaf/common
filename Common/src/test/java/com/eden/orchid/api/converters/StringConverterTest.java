package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class StringConverterTest {

// Setup
//----------------------------------------------------------------------------------------------------------------------

    private StringConverter underTest;

    private StringConverterHelper helper1;
    private StringConverterHelper helper2;

    @BeforeEach
    public void setupTest() {
        Set<StringConverterHelper> helpers = new HashSet<>();

        helper1 = spy(new ClogStringConverterHelper());
        helpers.add(helper1);

        helper2 = spy(new ClogStringConverterHelper());
        helpers.add(helper2);

        underTest = new StringConverter(helpers);
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("getTestInputsArguments")
    public void testInputs(
            final Object sourceValue,
            final Object expectedSuccessful,
            final Object expectedExtractedValue) throws Throwable {
        EdenPair<Boolean, String> result = underTest.convert(String.class, sourceValue);
        assertThat(result.first, is(equalTo(expectedSuccessful)));
        assertThat(result.second, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getTestInputsArguments() {
        return Stream.of(
                Arguments.of(45,              true, "45"),
                Arguments.of(true,            true, "true"),
                Arguments.of(false,           true, "false"),
                Arguments.of("45",            true, "45"),
                Arguments.of("#",             true, "#"),
                Arguments.of(new JSONArray(), true, "[]"),
                Arguments.of(null,            true, "")
        );
    }

    @Test
    public void testHelpersCalled() throws Throwable {
        EdenPair<Boolean, String> result = underTest.convert(String.class, "input");
        assertThat(result.first, is(equalTo(true)));
        assertThat(result.second, is(equalTo("input")));

        verify(helper1, times(1)).convert(any());
        verify(helper1, times(1)).matches(any());

        verify(helper2, times(1)).convert(any());
        verify(helper2, times(1)).matches(any());
    }

    @Test
    public void testResultClass() throws Throwable {
        assertThat(underTest.acceptsClass(String.class), is(equalTo(true)));
    }

}
