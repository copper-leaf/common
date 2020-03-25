package com.eden.orchid.api.options.extractors;

import com.eden.common.util.EdenPair;
import com.eden.orchid.api.converters.ExtractableConverter;
import com.eden.orchid.api.converters.FlexibleMapConverter;
import com.eden.orchid.api.converters.IntegerConverter;
import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.converters.TypeConverter;
import com.eden.orchid.api.options.Extractable;
import com.eden.orchid.api.options.Extractor;
import com.eden.orchid.api.options.OptionExtractor;
import com.eden.orchid.api.options.annotations.ImpliedKey;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.StringDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ExtractableOptionExtractorTest extends BaseExtractorTest {

// Test Classes
//----------------------------------------------------------------------------------------------------------------------

    public static class TestClass {
        @Option
        public TestExtractableClass testValue;
    }

    public static class TestExtractableClass implements Extractable {
        @Option
        public String innerStringValue;

        @Option
        public int innerIntValue;

        public TestExtractableClass() {

        }

        public TestExtractableClass(String innerStringValue, int innerIntValue) {
            this.innerStringValue = innerStringValue;
            this.innerIntValue = innerIntValue;
        }

        @Override
        public void extractOptions(Extractor extractor, Map<String, Object> options) {
            extractor.extractOptions(this, options);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestExtractableClass)) return false;
            TestExtractableClass that = (TestExtractableClass) o;
            return innerIntValue == that.innerIntValue &&
                    Objects.equals(innerStringValue, that.innerStringValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(innerStringValue, innerIntValue);
        }

        @Override
        public String toString() {
            return "TestExtractableClass{" +
                    "innerStringValue='" + innerStringValue + '\'' +
                    ", innerIntValue=" + innerIntValue +
                    '}';
        }
    }

    public static class TestListClass {
        @Option
        @ImpliedKey(typeKey = "innerStringValue")
        @StringDefault({"one", "two"})
        public List<TestExtractableClass> testValues;
    }

// Test Setup
//----------------------------------------------------------------------------------------------------------------------

    @BeforeEach
    public void setupTest() {
        // converters
        StringConverter stringConverter = new StringConverter(new HashSet<>());
        IntegerConverter integerConverter = new IntegerConverter(stringConverter);
        FlexibleMapConverter flexibleMapConverter = new FlexibleMapConverter();
        ExtractableConverter extractableConverter = new ExtractableConverter(() -> extractor, flexibleMapConverter);

        // extractors
        ExtractableOptionExtractor extractableOptionExtractor = new ExtractableOptionExtractor(extractableConverter);
        StringOptionExtractor stringOptionExtractor = new StringOptionExtractor(stringConverter);
        IntOptionExtractor intOptionExtractor = new IntOptionExtractor(integerConverter);

        setupTest(
                new OptionExtractor[]{
                        extractableOptionExtractor,
                        stringOptionExtractor,
                        intOptionExtractor
                },
                new TypeConverter[]{
                        stringConverter,
                        integerConverter,
                        flexibleMapConverter,
                        extractableConverter
                }
        );
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("getOptionsArguments")
    public void testExtractOption(
            final TestClass underTest,
            final Object sourceStringValue,
            final Object sourceIntValue,
            final String expectedExtractedStringValue,
            final int expectedExtractedIntValue
    ) throws Throwable {
        final JSONObject innerOptions = new JSONObject();
        if (sourceStringValue != null) {
            if (sourceStringValue.toString().equals("_nullValue")) {
                innerOptions.put("innerStringValue", (String) null);
            } else {
                innerOptions.put("innerStringValue", sourceStringValue);
            }
        }
        innerOptions.put("innerIntValue", sourceIntValue);

        final JSONObject options = new JSONObject();
        options.put("testValue", innerOptions);

        assertThat(underTest.testValue, is(nullValue()));

        extractor.extractOptions(underTest, options.toMap());

        assertThat(underTest.testValue, is(notNullValue()));
        assertThat(underTest.testValue.innerStringValue, is(equalTo(expectedExtractedStringValue)));
        assertThat(underTest.testValue.innerIntValue, is(equalTo(expectedExtractedIntValue)));
    }

    static Stream<Arguments> getOptionsArguments() throws Throwable {
        return Stream.of(
                Arguments.of(new TestClass(), "asdf", 1, "asdf", 1),
                Arguments.of(new TestClass(), "asdf", "2", "asdf", 2)
        );
    }

    @ParameterizedTest
    @MethodSource("getListOptionsArguments")
    public void testExtractOptionList(
            final Object[] sourceStringValues,
            final Object[] sourceIntValues,
            final String[] expectedExtractedStringValues,
            final int[] expectedExtractedIntValues
    ) throws Throwable {
        final TestListClass underTest = new TestListClass();

        int resultArgLength = expectedExtractedStringValues.length;
        final JSONObject options = new JSONObject();

        if (sourceStringValues != null && sourceIntValues != null) {
            int sourceArgLength = sourceStringValues.length;

            assertThat(sourceStringValues.length, is(equalTo(sourceArgLength)));
            assertThat(sourceIntValues.length, is(equalTo(sourceArgLength)));
            assertThat(expectedExtractedStringValues.length, is(equalTo(resultArgLength)));
            assertThat(expectedExtractedIntValues.length, is(equalTo(resultArgLength)));

            final JSONArray arrayValues = new JSONArray();
            for (int i = 0; i < sourceStringValues.length; i++) {
                final JSONObject innerOptions = new JSONObject();
                if (sourceStringValues[i] != null) {
                    if (sourceStringValues[i].toString().equals("_nullValue")) {
                        innerOptions.put("innerStringValue", (String) null);
                    } else {
                        innerOptions.put("innerStringValue", sourceStringValues[i]);
                    }
                }
                innerOptions.put("innerIntValue", sourceIntValues[i]);
                arrayValues.put(innerOptions);
            }
            options.put("testValues", arrayValues);
        }

        assertThat(underTest.testValues, is(nullValue()));

        extractor.extractOptions(underTest, options.toMap());

        assertThat(underTest.testValues, is(notNullValue()));
        assertThat(underTest.testValues.size(), is(equalTo(resultArgLength)));

        for (int i = 0; i < underTest.testValues.size(); i++) {
            assertThat(underTest.testValues.get(i).innerStringValue, is(equalTo(expectedExtractedStringValues[i])));
            assertThat(underTest.testValues.get(i).innerIntValue, is(equalTo(expectedExtractedIntValues[i])));
        }
    }

    static Stream<Arguments> getListOptionsArguments() throws Throwable {
        return Stream.of(
                Arguments.of(
                        new Object[]{"asdf1", "asdf2"},
                        new Object[]{1, 2},
                        new String[]{"asdf1", "asdf2"},
                        new int[]{1, 2}
                ),
                Arguments.of(
                        null,
                        null,
                        new String[]{"one", "two"},
                        new int[]{0, 0}
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getListOptionsArgumentsJson")
    public void testExtractOptionListFromJson(
            final String sourceJson,
            final List<TestExtractableClass> expectedExtractedValues
    ) throws Throwable {
        final TestListClass underTest = new TestListClass();

        int resultArgLength = expectedExtractedValues.size();
        final JSONObject options = new JSONObject(sourceJson);

        assertThat(underTest.testValues, is(nullValue()));

        extractor.extractOptions(underTest, options.toMap());

        assertThat(underTest.testValues, is(notNullValue()));
        assertThat(underTest.testValues.size(), is(equalTo(resultArgLength)));

        for (int i = 0; i < underTest.testValues.size(); i++) {
            assertThat(underTest.testValues.get(i), is(equalTo(expectedExtractedValues.get(i))));
        }
    }

    static Stream<Arguments> getListOptionsArgumentsJson() throws Throwable {
        return Stream.of(
                Arguments.of(
                        "{" +
                            "\"testValues\": [" +
                                "{\"innerStringValue\": \"asdf1\", \"innerIntValue\": 1}," +
                                "{\"innerStringValue\": \"asdf2\", \"innerIntValue\": 2}," +
                                "{\"innerStringValue\": \"asdf3\"}," +
                                "\"asdf4\"" +
                            "]" +
                        "}",
                        Arrays.asList(
                                new TestExtractableClass("asdf1", 1),
                                new TestExtractableClass("asdf2", 2),
                                new TestExtractableClass("asdf3", 0),
                                new TestExtractableClass("asdf4", 0)
                        )
                ),
                Arguments.of(
                        "{" +
                            "\"testValues\": {" +
                                "\"asdf1\": {\"innerIntValue\": 1}," +
                                "\"asdf2\": {\"innerIntValue\": 2}," +
                                "\"asdf3\": {\"innerIntValue\": 3}" +
                            "}" +
                        "}",
                        Arrays.asList(
                                new TestExtractableClass("asdf1", 1),
                                new TestExtractableClass("asdf2", 2),
                                new TestExtractableClass("asdf3", 3)
                        )
                ),
                Arguments.of(
                        "{" +
                            "\"testValues\": {" +
                                "\"asdf1\": {\"innerIntValue\": 1}" +
                            "}" +
                        "}",
                        Arrays.asList(
                                new TestExtractableClass("asdf1", 1)
                        )
                ),
                Arguments.of(
                        "{" +
                            "\"testValues\": [\"asdf1\"]" +
                        "}",
                        Arrays.asList(
                                new TestExtractableClass("asdf1", 0)
                        )
                ),
                Arguments.of(
                        "{" +
                            "\"testValues\": \"asdf1\"" +
                        "}",
                        Arrays.asList(
                                new TestExtractableClass("asdf1", 0)
                        )
                )
        );
    }

}
