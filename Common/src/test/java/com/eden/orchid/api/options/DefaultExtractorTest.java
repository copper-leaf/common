package com.eden.orchid.api.options;

import com.caseyjbrooks.clog.Clog;
import com.eden.orchid.api.options.annotations.DoubleDefault;
import com.eden.orchid.api.options.annotations.IntDefault;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.StringDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DefaultExtractorTest {

    public static class TestOptionsClass  {

        @Option @StringDefault("default string")
        public String stringOption;

        @Option @IntDefault(5)
        public int intOption;

        @Option @DoubleDefault(5.5)
        public double doubleOption;

    }

    private Extractor extractor;
    private TestOptionsClass testOptionsClass;

    @BeforeAll
    static void setupAll() {
        Clog.getInstance().setMinPriority(Clog.Priority.FATAL);
    }

    @BeforeEach
    void setupTest() {
        testOptionsClass = new TestOptionsClass();
        extractor = DefaultExtractor.getInstance();
    }

    @ParameterizedTest
    @MethodSource("getOptionsArguments")
    void testExtractStringOption(final String optionName, final Object sourceValue, final Object expectedExtractedValue) throws Throwable {

        String s = "{" + optionName + ": " + sourceValue + "}";

        final JSONObject options = new JSONObject(s);

        extractor.extractOptions(testOptionsClass, options);

        Object actualExtractedValue = testOptionsClass.getClass().getField(optionName).get(testOptionsClass);
        assertThat(actualExtractedValue, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getOptionsArguments() {
        return Stream.of(
                Arguments.of("stringOption",       null,             "default string"),
                Arguments.of("stringOption",       "'string value'", "string value"),
                Arguments.of("intOption",          null,             5),
                Arguments.of("intOption",          10,               10),
                Arguments.of("doubleOption",       null,             5.5),
                Arguments.of("doubleOption",       10.2,             10.2)
        );
    }

}
