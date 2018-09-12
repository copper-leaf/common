package com.eden.orchid.api.options.extractors;

import com.eden.orchid.api.converters.Converters;
import com.eden.orchid.api.converters.FlexibleIterableConverter;
import com.eden.orchid.api.converters.FlexibleMapConverter;
import com.eden.orchid.api.converters.TypeConverter;
import com.eden.orchid.api.options.Extractor;
import com.eden.orchid.api.options.OptionExtractor;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public abstract class BaseExtractorTest {

    protected Extractor extractor;

    public void setupTest(OptionExtractor extractorUnderTest, TypeConverter... converters) {
        setupTest(new OptionExtractor[]{extractorUnderTest}, converters);
    }

    public void setupTest(OptionExtractor[] extractorsArray, TypeConverter[] convertersArray) {
        FlexibleMapConverter mapConverter = new FlexibleMapConverter();
        FlexibleIterableConverter iterableConverter = new FlexibleIterableConverter(mapConverter);

        List<TypeConverter> convertersList = new ArrayList<>(Arrays.asList(convertersArray));
        convertersList.add(iterableConverter);
        convertersList.add(iterableConverter);
        Converters converters = new Converters(new HashSet<>(convertersList));

        List<OptionExtractor> extractorsList = new ArrayList<>(Arrays.asList(extractorsArray));
        extractorsList.add(new ListOptionExtractor(() -> extractor, iterableConverter, mapConverter, converters));
        extractorsList.add(new StringArrayOptionExtractor(iterableConverter, converters));

        extractor = new Extractor(extractorsList, null);
    }

    public void testExtractOption(
            final Object underTest,
            final Object sourceValue,
            final Object expectedOriginalValue,
            final Object expectedExtractedValue) throws Throwable {

        String optionName = "testValue";

        final JSONObject options = new JSONObject();
        if(sourceValue != null) {
            if(sourceValue.toString().equals("_nullValue")) {
                options.put(optionName, (String) null);
            }
            else {
                options.put(optionName, sourceValue);
            }
        }

        assertThat(underTest.getClass().getField(optionName).get(underTest), is(equalTo(expectedOriginalValue)));
        extractor.extractOptions(underTest, options.toMap());
        assertThat(underTest.getClass().getField(optionName).get(underTest), is(equalTo(expectedExtractedValue)));
    }

    public void testExtractOptionList(
            final Object underTest,
            final Object sourceValue,
            final Object[] expectedExtractedValue) throws Throwable {

        String optionName = "testValue";

        final JSONObject options = new JSONObject();
        if(sourceValue != null) {
            if(sourceValue.toString().equals("_nullValue")) {
                options.put(optionName, (String) null);
            }
            else {
                options.put(optionName, sourceValue);
            }
        }

        assertThat(underTest.getClass().getField(optionName).get(underTest), is(equalTo(null)));
        extractor.extractOptions(underTest, options.toMap());
        assertThat((Iterable<Object>) underTest.getClass().getField(optionName).get(underTest), containsInAnyOrder(expectedExtractedValue));
    }

    public void testExtractOptionArray(
            final Object underTest,
            final Object sourceValue,
            final Object[] expectedExtractedValue) throws Throwable {

        String optionName = "testValue";

        final JSONObject options = new JSONObject();
        if(sourceValue != null) {
            if(sourceValue.toString().equals("_nullValue")) {
                options.put(optionName, (String) null);
            }
            else {
                options.put(optionName, sourceValue);
            }
        }

        assertThat(underTest.getClass().getField(optionName).get(underTest), is(equalTo(null)));
        extractor.extractOptions(underTest, options.toMap());
        assertThat((Object[]) underTest.getClass().getField(optionName).get(underTest), arrayContainingInAnyOrder(expectedExtractedValue));
    }

    public void testOptionDescription(
            final Object underTest,
            final String expectedDescription) throws Throwable {

        String optionName = "testValue";

        String description = extractor.describeOption(underTest.getClass(), optionName);
        assertThat(description, is(equalTo(expectedDescription)));
    }

}
