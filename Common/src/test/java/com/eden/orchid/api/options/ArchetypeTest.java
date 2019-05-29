package com.eden.orchid.api.options;

import com.eden.orchid.api.converters.ClogStringConverterHelper;
import com.eden.orchid.api.converters.StringConverter;
import com.eden.orchid.api.options.annotations.Archetype;
import com.eden.orchid.api.options.extractors.StringOptionExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ArchetypeTest {

    public static class TestArchetype implements OptionArchetype {
        @Override
        public Map<String, Object> getOptions(Object target, String archetypeKey) {
            return null;
        }
    }
    public static class TestArchetype1 implements OptionArchetype {
        @Override
        public Map<String, Object> getOptions(Object target, String archetypeKey) {
            return null;
        }
    }
    public static class TestArchetype2 implements OptionArchetype {
        @Override
        public Map<String, Object> getOptions(Object target, String archetypeKey) {
            return null;
        }
    }
    public static class TestArchetype3 implements OptionArchetype {
        @Override
        public Map<String, Object> getOptions(Object target, String archetypeKey) {
            return null;
        }
    }

    private Extractor extractor;

    @BeforeEach
    void setupTest() {
        extractor = Extractor.builder().extractors(
                Collections.singleton(
                        new StringOptionExtractor(
                                new StringConverter(
                                        Collections.singleton(
                                                new ClogStringConverterHelper()
                                        )
                                )
                        )
                )
        ).build();
    }

    @Archetype(key = "3", value = TestArchetype.class)
    @Archetype(key = "2", value = TestArchetype.class)
    @Archetype(key = "1", value = TestArchetype.class)
    public static class Test3SameArchetypes { }
    @Test
    void test3SameArchetypes() {
        List<Archetype> archetypes = extractor.getArchetypes(Test3SameArchetypes.class);

        assertThat(archetypes.get(0).key(), is(equalTo("1")));
        assertThat(archetypes.get(0).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(1).key(), is(equalTo("2")));
        assertThat(archetypes.get(1).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(2).key(), is(equalTo("3")));
        assertThat(archetypes.get(2).value(), is(equalTo(TestArchetype.class)));
    }

    @Archetype(key = "3", value = TestArchetype3.class)
    @Archetype(key = "2", value = TestArchetype2.class)
    @Archetype(key = "1", value = TestArchetype1.class)
    public static class Test3DifferentArchetypes { }
    @Test
    void test3DifferentArchetypes() {
        List<Archetype> archetypes = extractor.getArchetypes(Test3DifferentArchetypes.class);

        assertThat(archetypes.get(0).key(), is(equalTo("1")));
        assertThat(archetypes.get(0).value(), is(equalTo(TestArchetype1.class)));

        assertThat(archetypes.get(1).key(), is(equalTo("2")));
        assertThat(archetypes.get(1).value(), is(equalTo(TestArchetype2.class)));

        assertThat(archetypes.get(2).key(), is(equalTo("3")));
        assertThat(archetypes.get(2).value(), is(equalTo(TestArchetype3.class)));
    }

    @Archetype(key = "3", value = TestArchetype.class, order = 3)
    @Archetype(key = "2", value = TestArchetype.class, order = 1)
    @Archetype(key = "1", value = TestArchetype.class, order = 2)
    public static class TestArchetypesInDifferentOrder { }
    @Test
    void testArchetypesInDifferentOrder() {
        List<Archetype> archetypes = extractor.getArchetypes(TestArchetypesInDifferentOrder.class);

        assertThat(archetypes.get(0).key(), is(equalTo("2")));
        assertThat(archetypes.get(0).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(1).key(), is(equalTo("1")));
        assertThat(archetypes.get(1).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(2).key(), is(equalTo("3")));
        assertThat(archetypes.get(2).value(), is(equalTo(TestArchetype.class)));
    }

    @Archetype(key = "3", value = TestArchetype.class)
    @Archetype(key = "2", value = TestArchetype.class, order = 1)
    @Archetype(key = "1", value = TestArchetype.class)
    public static class TestArchetypesInDifferentOrderOnlyOneOrderGiven { }
    @Test
    void testArchetypesInDifferentOrderOnlyOneOrderGiven() {
        List<Archetype> archetypes = extractor.getArchetypes(TestArchetypesInDifferentOrderOnlyOneOrderGiven.class);

        assertThat(archetypes.get(0).key(), is(equalTo("1")));
        assertThat(archetypes.get(0).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(1).key(), is(equalTo("3")));
        assertThat(archetypes.get(1).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(2).key(), is(equalTo("2")));
        assertThat(archetypes.get(2).value(), is(equalTo(TestArchetype.class)));
    }


    @Archetype(key = "6", value = TestArchetype.class)
    @Archetype(key = "5", value = TestArchetype.class)
    @Archetype(key = "4", value = TestArchetype.class)
    public static class TestArchetypesFromParentClass extends TestArchetypesInDifferentOrderOnlyOneOrderGiven { }
    @Test
    void testArchetypesFromParentClass() {
        List<Archetype> archetypes = extractor.getArchetypes(TestArchetypesFromParentClass.class);

        assertThat(archetypes.get(0).key(), is(equalTo("1")));
        assertThat(archetypes.get(0).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(1).key(), is(equalTo("3")));
        assertThat(archetypes.get(1).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(2).key(), is(equalTo("4")));
        assertThat(archetypes.get(2).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(3).key(), is(equalTo("5")));
        assertThat(archetypes.get(3).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(4).key(), is(equalTo("6")));
        assertThat(archetypes.get(4).value(), is(equalTo(TestArchetype.class)));

        assertThat(archetypes.get(5).key(), is(equalTo("2")));
        assertThat(archetypes.get(5).value(), is(equalTo(TestArchetype.class)));
    }

}
