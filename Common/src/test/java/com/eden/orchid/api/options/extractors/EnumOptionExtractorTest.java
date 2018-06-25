package com.eden.orchid.api.options.extractors;

import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.StringDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.stream.Stream;

public class EnumOptionExtractorTest extends BaseExtractorTest {

// Test Classes
//----------------------------------------------------------------------------------------------------------------------

    public enum TestEnumClass {
        One, Two, Three, Four, Five
    }

    public static class TestClass1 { @Option @StringDefault("Three") public TestEnumClass testValue; }
    public static class TestClass2 { @Option                         public TestEnumClass testValue; }

// Test Setup
//----------------------------------------------------------------------------------------------------------------------

    @BeforeEach
    public void setupTest() {
        StringConverter stringConverter = new StringConverter(new HashSet<>());

        setupTest(new EnumOptionExtractor(stringConverter), stringConverter);
    }

// Tests
//----------------------------------------------------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("getOptionsArguments")
    public void testExtractOption(
            final Object underTest,
            final Object sourceValue,
            final Object expectedOriginalValue,
            final Object expectedExtractedValue) throws Throwable {
        super.testExtractOption(
                underTest,
                sourceValue,
                expectedOriginalValue,
                expectedExtractedValue
        );
    }
    static Stream<Arguments> getOptionsArguments() {
        return Stream.of(
                Arguments.of(new TestClass1(), "One",            null, TestEnumClass.One),
                Arguments.of(new TestClass1(), null,             null, TestEnumClass.Three),
                Arguments.of(new TestClass1(), "_nullValue",     null, TestEnumClass.Three),

                Arguments.of(new TestClass2(), "One",            null, TestEnumClass.One),
                Arguments.of(new TestClass2(), null,             null, null),
                Arguments.of(new TestClass2(), "_nullValue",     null, null)
        );
    }

}
