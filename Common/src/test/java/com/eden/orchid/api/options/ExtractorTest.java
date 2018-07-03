package com.eden.orchid.api.options;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.converters.ClogStringConverterHelper;
import com.eden.orchid.api.converters.IntegerConverter;
import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.converters.StringConverterHelper;
import com.eden.orchid.api.options.annotations.AllOptions;
import com.eden.orchid.api.options.annotations.Archetype;
import com.eden.orchid.api.options.annotations.Archetypes;
import com.eden.orchid.api.options.annotations.IntDefault;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.StringDefault;
import com.eden.orchid.api.options.extractors.IntOptionExtractor;
import com.eden.orchid.api.options.extractors.StringOptionExtractor;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ExtractorTest {

    public static class AllOptionsTestClass {

        @AllOptions
        public Map<String, Object> allOptions;

    }

    public static class ParentTestOptionsClass {

        @Option
        @StringDefault("default string")
        public String parentStringOption;

        @Option
        @IntDefault(5)
        public int parentIntOption;

    }

    public static class TestOptionsClass extends ParentTestOptionsClass {

        @Option
        @StringDefault("default string")
        public String stringOption;

        @Option
        @IntDefault(5)
        public int intOption;

        @Option("beanSetter")
        private String beanSetterValue;

        public void setBeanSetter(String value) {
            this.beanSetterValue = "setter value";
        }

        public String getBeanSetter() {
            return this.beanSetterValue;
        }

    }

    private Extractor extractor;
    private TestOptionsClass testOptionsClass;

    @BeforeEach
    void setupTest() {
        testOptionsClass = new TestOptionsClass();

        Set<StringConverterHelper> helpers = new HashSet<>();
        helpers.add(new ClogStringConverterHelper());
        StringConverter stringConverter = new StringConverter(helpers);

        Set<OptionExtractor> extractors = new HashSet<>();

        extractors.add(new StringOptionExtractor(stringConverter));
        extractors.add(new IntOptionExtractor(new IntegerConverter(stringConverter)));

        extractor = new Extractor(extractors, null);
    }

    @ParameterizedTest
    @MethodSource("getOptionsArguments")
    void testExtractStringOption(
            final String optionName,
            final boolean getterIsMethod,
            final Object sourceValue,
            final Object expectedOriginalValue,
            final Object expectedExtractedValue) throws Throwable {

        String s = "{" + optionName + ": " + sourceValue + "}";

        final Map<String, Object> options = new JSONObject(s).toMap();

        Object actualOriginalValue = (getterIsMethod)
                ? testOptionsClass.getClass().getMethod("get" + optionName.substring(0, 1).toUpperCase() + optionName.substring(1)).invoke(testOptionsClass)
                : testOptionsClass.getClass().getField(optionName).get(testOptionsClass);
        assertThat(actualOriginalValue, is(equalTo(expectedOriginalValue)));

        extractor.extractOptions(testOptionsClass, options);

        Object actualExtractedValue = (getterIsMethod)
                ? testOptionsClass.getClass().getMethod("get" + optionName.substring(0, 1).toUpperCase() + optionName.substring(1)).invoke(testOptionsClass)
                : testOptionsClass.getClass().getField(optionName).get(testOptionsClass);
        assertThat(actualExtractedValue, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getOptionsArguments() {
        return Stream.of(
                Arguments.of("stringOption", false, null, null, "default string"),
                Arguments.of("stringOption", false, "'string value'", null, "string value"),
                Arguments.of("intOption", false, null, 0, 5),
                Arguments.of("intOption", false, 10, 0, 10),
                Arguments.of("parentStringOption", false, null, null, "default string"),
                Arguments.of("parentStringOption", false, "'string value'", null, "string value"),
                Arguments.of("parentIntOption", false, null, 0, 5),
                Arguments.of("parentIntOption", false, 10, 0, 10),
                Arguments.of("beanSetter", true, "passed value", null, "setter value")
        );
    }

    @ParameterizedTest
    @MethodSource("getOptionsArgumentsForAllOptionsTest")
    void testExtractStringOptionForAllOptions(final String inputJson) throws Throwable {
        Clog.getInstance().setMinPriority(Clog.Priority.VERBOSE);
        AllOptionsTestClass testOptionsClass = new AllOptionsTestClass();

        extractor.extractOptions(testOptionsClass, new JSONObject(inputJson).toMap());

        assertThat(new JSONObject(testOptionsClass.allOptions).similar(new JSONObject(inputJson)), is(equalTo(true)));
    }

    static Stream<Arguments> getOptionsArgumentsForAllOptionsTest() {
        return Stream.of(
                Arguments.of("{\"stringOption\": \"one\"}"),
                Arguments.of("{\"intOption\": 1}")
        );
    }

// Archetypes Test
//----------------------------------------------------------------------------------------------------------------------

    private static Map<String, Object> splitStringToMap(String input) {
        Map<String, Object> testValue = new HashMap<>();
        if(!EdenUtils.isEmpty(input)) {
            String[] pairs = input.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":");
                testValue.put(kv[0], kv[1]);
            }
        }

        return testValue;
    }

    public static class TestArchetype implements OptionArchetype {
        @Override
        public Map<String, Object> getOptions(Object target, String archetypeKey) {
            return splitStringToMap(archetypeKey);
        }
    }

    @Archetype(value = TestArchetype.class, key = "val1:1")
    public static class SingleAnnotationClass {
        @Option public int val1;
    }

    @Archetype(value = TestArchetype.class, key = "val1:1")
    @Archetype(value = TestArchetype.class, key = "val2:2")
    public static class RepeatableAnnotationClass {
        @Option public int val1;
        @Option public int val2;
    }

    @Archetypes({
            @Archetype(value = TestArchetype.class, key = "val1:1"),
            @Archetype(value = TestArchetype.class, key = "val2:2")
    })
    public static class ContainerAnnotationClass {
        @Option public int val1;
        @Option public int val2;
    }

    @Archetypes({
            @Archetype(value = TestArchetype.class, key = "val1:2"),
            @Archetype(value = TestArchetype.class, key = "val1:1")
    })
    public static class EarlierAnnotationIsPreferredClass {
        @Option public int val1;
    }

    public static class ParentArchetypesOnlyClass extends SingleAnnotationClass {

    }

    @Archetype(value = TestArchetype.class, key = "val1:2")
    public static class ParentArchetypesOverrideClass extends SingleAnnotationClass {

    }

    @ParameterizedTest
    @MethodSource("getArchetypesArguments")
    void testArchetypes(
            final Object target,
            final String optionName,
            final Object expectedExtractedValue,
            final String options) throws Throwable {
        extractor.extractOptions(target, splitStringToMap(options));

        Object actualExtractedValue = target.getClass().getField(optionName).get(target);
        assertThat(actualExtractedValue, is(equalTo(expectedExtractedValue)));
    }

    static Stream<Arguments> getArchetypesArguments() {
        return Stream.of(
                Arguments.of(new SingleAnnotationClass(),             "val1", 1, ""),
                Arguments.of(new RepeatableAnnotationClass(),         "val1", 1, ""),
                Arguments.of(new RepeatableAnnotationClass(),         "val2", 2, ""),
                Arguments.of(new ContainerAnnotationClass(),          "val1", 1, ""),
                Arguments.of(new ContainerAnnotationClass(),          "val2", 2, ""),
                Arguments.of(new EarlierAnnotationIsPreferredClass(), "val1", 2, ""),
                Arguments.of(new ParentArchetypesOnlyClass(),         "val1", 1, ""),
                Arguments.of(new ParentArchetypesOverrideClass(),     "val1", 2, ""),

                Arguments.of(new SingleAnnotationClass(),             "val1", 3, "val1:3"),
                Arguments.of(new RepeatableAnnotationClass(),         "val1", 3, "val1:3"),
                Arguments.of(new RepeatableAnnotationClass(),         "val2", 3, "val2:3"),
                Arguments.of(new ContainerAnnotationClass(),          "val1", 3, "val1:3"),
                Arguments.of(new ContainerAnnotationClass(),          "val2", 3, "val2:3"),
                Arguments.of(new EarlierAnnotationIsPreferredClass(), "val1", 3, "val1:3"),
                Arguments.of(new ParentArchetypesOnlyClass(),         "val1", 3, "val1:3"),
                Arguments.of(new ParentArchetypesOverrideClass(),     "val1", 3, "val1:3")
        );
    }

}
