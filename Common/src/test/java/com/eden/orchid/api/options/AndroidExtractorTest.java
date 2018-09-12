package com.eden.orchid.api.options;

import com.eden.orchid.api.options.annotations.BooleanDefault;
import com.eden.orchid.api.options.annotations.DoubleDefault;
import com.eden.orchid.api.options.annotations.FloatDefault;
import com.eden.orchid.api.options.annotations.ImpliedKey;
import com.eden.orchid.api.options.annotations.IntDefault;
import com.eden.orchid.api.options.annotations.LongDefault;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.StringDefault;
import com.eden.orchid.api.options.extractors.EnumOptionExtractorTest;
import com.eden.orchid.api.options.extractors.ExtractableOptionExtractorTest;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AndroidExtractorTest {

    public static class TestOptionsClass  {

        @Option @StringDefault("default string")
        public String stringOption;

        @Option @IntDefault(5)
        public int intOption;

        @Option @LongDefault(5L)
        public long longOption;

        @Option @DoubleDefault(5.5)
        public double doubleOption;

        @Option @FloatDefault(5.5f)
        public float floatOption;

        @Option @BooleanDefault(true)
        public boolean booleanOption;

        @Option @StringDefault("Two")
        public EnumOptionExtractorTest.TestEnumClass enumOption;

        @Option
        public ExtractableOptionExtractorTest.TestExtractableClass extractableOption;

        @Option
        @ImpliedKey("innerStringValue")
        public List<ExtractableOptionExtractorTest.TestExtractableClass> extractableOptionList;

    }

    private Extractor extractor;
    private TestOptionsClass testOptionsClass;

    @BeforeEach
    void setupTest() {
        testOptionsClass = new TestOptionsClass();
        extractor = AndroidExtractor.getInstance();
    }

    @ParameterizedTest
    @MethodSource("getOptionsArguments")
    void testExtractStringOption(final String optionName, final Object sourceValue, final Object expectedExtractedValue) throws Throwable {

        String s = "{" + optionName + ": " + sourceValue + "}";

        final JSONObject options = new JSONObject(s);

        extractor.extractOptions(testOptionsClass, options.toMap());

        Object actualExtractedValue = testOptionsClass.getClass().getField(optionName).get(testOptionsClass);
        assertThat(actualExtractedValue, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getOptionsArguments() {
        return Stream.of(
                Arguments.of(
                        "stringOption",
                        null,
                        "default string"
                ),
                Arguments.of(
                        "stringOption",
                        "'string value'",
                        "string value"
                ),
                Arguments.of(
                        "intOption",
                        null,
                        5
                ),
                Arguments.of(
                        "intOption",
                        10,
                        10
                ),
                Arguments.of(
                        "longOption",
                        null,
                        5L
                ),
                Arguments.of(
                        "longOption",
                        10,
                        10L
                ),
                Arguments.of(
                        "doubleOption",
                        null,
                        5.5
                ),
                Arguments.of(
                        "doubleOption",
                        10.2,
                        10.2
                ),
                Arguments.of(
                        "floatOption",
                        null,
                        5.5f
                ),
                Arguments.of(
                        "floatOption",
                        10.2,
                        10.2f
                ),
                Arguments.of(
                        "booleanOption",
                        null,
                        true
                ),
                Arguments.of(
                        "booleanOption",
                        false,
                        false
                ),
                Arguments.of(
                        "enumOption",
                        "Four",
                        EnumOptionExtractorTest.TestEnumClass.Four
                ),
                Arguments.of(
                        "enumOption",
                        null,
                        EnumOptionExtractorTest.TestEnumClass.Two
                ),
                Arguments.of(
                        "enumOption",
                        EnumOptionExtractorTest.TestEnumClass.Five,
                        EnumOptionExtractorTest.TestEnumClass.Five
                ),
                Arguments.of(
                        "extractableOption",
                        "{\"innerStringValue\": \"asdf\"}",
                        new ExtractableOptionExtractorTest.TestExtractableClass("asdf", 0)
                ),
                Arguments.of(
                        "extractableOption",
                        "{\"innerStringValue\": \"asdf1\", \"innerIntValue\": 1}",
                        new ExtractableOptionExtractorTest.TestExtractableClass("asdf1", 1)
                ),
                Arguments.of(
                        "extractableOption",
                        "{\"innerStringValue\": \"asdf2\", \"innerIntValue\": \"2\"}",
                        new ExtractableOptionExtractorTest.TestExtractableClass("asdf2", 2)
                ),
                Arguments.of(
                        "extractableOptionList",
                        "[{\"innerStringValue\": \"asdf1\", \"innerIntValue\": 1}, {\"innerStringValue\": \"asdf2\", \"innerIntValue\": \"2\"}]",
                        new ArrayList<>(Arrays.asList(
                                new ExtractableOptionExtractorTest.TestExtractableClass("asdf1", 1),
                                new ExtractableOptionExtractorTest.TestExtractableClass("asdf2", 2)
                        ))
                ),
                Arguments.of(
                        "extractableOptionList",
                        "[\"asdf1\", \"asdf2\"]",
                        new ArrayList<>(Arrays.asList(
                                new ExtractableOptionExtractorTest.TestExtractableClass("asdf1", 0),
                                new ExtractableOptionExtractorTest.TestExtractableClass("asdf2", 0)
                        ))
                )
        );
    }

}
