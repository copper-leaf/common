package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class FlexibleIterableConverterTest {

    private FlexibleMapConverter mapConverter;
    private FlexibleIterableConverter underTest;

    @BeforeEach
    void setupTest() {
        mapConverter = new FlexibleMapConverter();
        underTest = new FlexibleIterableConverter(mapConverter);
    }

    @Test
    void testResultClass() {
        assertThat(underTest.acceptsClass(Iterable.class), is(equalTo(true)));
    }

    @Test
    void testNull() {
        String source = null;

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(false)));
        assertThat((Iterable<String>) result.second, is(not(nullValue())));
        assertThat(result.second.iterator().hasNext(), is(equalTo(false)));
    }

    @Test
    void testSingleItem() {
        String source = "test";

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<String>) result.second, containsInAnyOrder("test"));
    }

    @Test
    void testArray() {
        String[] source = new String[]{"test"};

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<String>) result.second, containsInAnyOrder("test"));
        assertThat((Iterable<String>) result.second, is(not(sameInstance(source))));
    }

    @Test
    void testSet() {
        Set<String> source = new HashSet<>();
        source.add("test");

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<String>) result.second, containsInAnyOrder("test"));
        assertThat((Iterable<String>) result.second, is(sameInstance(source)));
    }

    @Test
    void testList() {
        List<String> source = new ArrayList<>();
        source.add("test");

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<String>) result.second, containsInAnyOrder("test"));
        assertThat((Iterable<String>) result.second, is(sameInstance(source)));
    }

    @Test
    void testAbstractIterable() {
        Iterable<String> source = new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return Collections.singletonList("test").iterator();
            }
        };

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<String>) result.second, containsInAnyOrder("test"));
        assertThat((Iterable<String>) result.second, is(sameInstance(source)));
    }

    @Test
    void testMapOfStrings() {
        Map<String, String> source = new HashMap<>();
        source.put("key", "test");

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<String>) result.second, containsInAnyOrder("test"));
    }

    @Test
    void testMapOfStrings_typeKey() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("defaultType1", "defaultValue1");
        source.put("defaultType2", "defaultValue2");

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source, "type", "value");

        assertThat(result.first, is(equalTo(true)));

        List<Map<String, String>> maps = new ArrayList<>();
        for (Object map : result.second) {
            maps.add((Map<String, String>) map);
        }

        assertThat(maps.get(0).get("type"), is(equalTo("defaultType1")));
        assertThat(maps.get(0).get("value"), is(equalTo("defaultValue1")));

        assertThat(maps.get(1).get("type"), is(equalTo("defaultType2")));
        assertThat(maps.get(1).get("value"), is(equalTo("defaultValue2")));
    }

    @Test
    void testMapOfMaps() {
        Map<String, Map<String, String>> source = new HashMap<>();
        Map<String, String> object = new HashMap<>();
        object.put("key", "test");
        source.put("otherKey", object);

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source, "newKey", null);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<Map<String, String>>) result.second, is(notNullValue()));
        assertThat(((Iterable<Map<String, String>>) result.second).iterator().hasNext(), is(equalTo(true)));

        Map<String, String> newObject = ((Iterable<Map<String, String>>) result.second).iterator().next();
        assertThat(newObject.get("key"), is(equalTo("test")));
        assertThat(newObject.get("newKey"), is(equalTo("otherKey")));
    }

    @Test
    void testMapOfJsonObjects() {
        Map<String, JSONObject> source = new HashMap<>();
        JSONObject object = new JSONObject();
        object.put("key", "test");
        source.put("otherKey", object);

        EdenPair<Boolean, Iterable> result = underTest.convert(Iterable.class, source, "newKey", null);

        assertThat(result.first, is(equalTo(true)));
        assertThat((Iterable<JSONObject>) result.second, is(notNullValue()));
        assertThat(((Iterable<JSONObject>) result.second).iterator().hasNext(), is(equalTo(true)));

        Map<String, String> newObject = ((Iterable<Map<String, String>>) result.second).iterator().next();
        assertThat(newObject.get("key"), is(equalTo("test")));
        assertThat(newObject.get("newKey"), is(equalTo("otherKey")));
    }

}
