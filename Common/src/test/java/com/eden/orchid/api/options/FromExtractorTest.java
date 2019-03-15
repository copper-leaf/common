package com.eden.orchid.api.options;

import com.caseyjbrooks.clog.Clog;
import com.eden.orchid.api.converters.ClogStringConverterHelper;
import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.converters.StringConverterHelper;
import com.eden.orchid.api.options.annotations.Archetype;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.extractors.StringOptionExtractor;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class FromExtractorTest {

    @Archetype(value = FromArchetype.class, key = "from")
    public static class TestOptionsClass {

        @Option
        public String stringOption1;

        @Option
        public String stringOption2;

    }

    public static class FromArchetype implements OptionArchetype {

        @Option
        public String from;

        @Override
        public Map<String, Object> getOptions(Object target, String archetypeKey) {
            Map<String, Object> data = new HashMap<>();

            if(from.equals("configA")) {
                data.put("stringOption1", "A1");
                data.put("stringOption2", "A2");
            }
            else if(from.equals("configB")) {
                data.put("stringOption1", "B1");
                data.put("stringOption2", "B2");
            }
            else if(from.equals("configC")) {
                data.put("stringOption1", "C1");
                data.put("stringOption2", "C2");
            }

            return data;
        }
    }

    private Extractor extractor;

    @BeforeEach
    void setupTest() {
        Set<StringConverterHelper> helpers = new HashSet<>();
        helpers.add(new ClogStringConverterHelper());
        StringConverter stringConverter = new StringConverter(helpers);

        List<OptionExtractor> extractors = new ArrayList<>();
        extractors.add(new StringOptionExtractor(stringConverter));

        extractor = Extractor.builder().extractors(extractors).build();
    }

    @ParameterizedTest
    @MethodSource("getOptionsArgumentsForAllOptionsTest")
    void testExtractStringOptionForAllOptions(final String inputJson, String expectedStringOption1, String expectedStringOption2) throws Throwable {
        Clog.getInstance().setMinPriority(Clog.Priority.VERBOSE);
        TestOptionsClass testOptionsClass = new TestOptionsClass();

        extractor.extractOptions(testOptionsClass, new JSONObject(inputJson).toMap());

        assertThat(testOptionsClass.stringOption1, is(equalTo(expectedStringOption1)));
        assertThat(testOptionsClass.stringOption2, is(equalTo(expectedStringOption2)));
    }

    static Stream<Arguments> getOptionsArgumentsForAllOptionsTest() {
        return Stream.of(
                Arguments.of("{\"from\": \"configA\"}", "A1", "A2"),
                Arguments.of("{\"from\": \"configB\"}", "B1", "B2"),
                Arguments.of("{\"from\": \"configC\"}", "C1", "C2"),
                Arguments.of("{\"stringOption1\": \"D1\", \"stringOption2\": \"D2\"}", "D1", "D2"),
                Arguments.of("{\"from\": \"configA\", \"stringOption1\": \"D1\", \"stringOption2\": \"D2\"}", "D1", "D2")
        );
    }

}
