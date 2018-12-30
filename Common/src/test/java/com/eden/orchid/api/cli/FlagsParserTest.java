package com.eden.orchid.api.cli;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class FlagsParserTest {

    private FlagsParser underTest;

    @BeforeEach
    void setUp() {
        Set<String> flagNames = new HashSet<>();
        flagNames.add("dest");
        flagNames.add("port");
        flagNames.add("task");
        flagNames.add("baseUrl");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("d", "dest");

        List<String> positionalArgs = Arrays.asList("task");

        underTest = FlagsParser.builder()
                .validNames(flagNames)
                .validAliases(aliases)
                .positionalNames(positionalArgs)
                .build();
    }

    @ParameterizedTest
    @MethodSource("parseCommandLineArgsValues")
    public void parseCommandLineArgsTest(
            String[] input,
            boolean success,
            Map<String, String[]> expectedValidFlags,
            Map<String, String[]> expectedInvalidFlags) {

        FlagsParser.ParseResult output = underTest.parseArgs(input);

        // check whether parsing was successful
        assertThat(output.success(), is(equalTo(success)));

        // check whether the valid flags are correct
        assertThat(output.getValidFlags().keySet(), containsInAnyOrder(expectedValidFlags.keySet().toArray()));
        for(String key : expectedValidFlags.keySet()) {
            assertThat(output.getValidFlags().get(key), is(equalTo(expectedValidFlags.get(key))));
        }

        // check whether the invalid flags are correct
        assertThat(output.getInvalidFlags().keySet(), containsInAnyOrder(expectedInvalidFlags.keySet().toArray()));
        for(String key : expectedValidFlags.keySet()) {
            assertThat(output.getInvalidFlags().get(key), is(equalTo(expectedInvalidFlags.get(key))));
        }
        assertThat(output.getOriginalArgs(), is(equalTo(input)));
    }
    public static Stream<Arguments> parseCommandLineArgsValues() {
        return Stream.of(
                Arguments.of(
                        new String[] {"build"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"clean", "build", "serve"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", Arrays.asList("clean", "build", "serve"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "9000"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--port", "9000"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "9000");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--port", "9000"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("port", "9000");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "9000", "-d", "build/orchid/docs"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                            put("dest", "build/orchid/docs");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "9000", "-d", "build/orchid/docs", "-d", "build/orchid/docs2"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                            put("dest", Arrays.asList("build/orchid/docs", "build/orchid/docs2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "9000", "-d", "build/orchid/docs", "--dest", "build/orchid/docs2"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                            put("dest", Arrays.asList("build/orchid/docs", "build/orchid/docs2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/", "--port"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "true");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "-d", "build/orchid/docs", "--dest", "build/orchid/docs2"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "true");
                            put("dest", Arrays.asList("build/orchid/docs", "build/orchid/docs2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--port"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("port", "true");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--port", "--baseUrl"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("port", "true");
                            put("baseUrl", "true");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--port", ""},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--port", null},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--invalidFlag"},
                        false,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("invalidFlag", "true");
                        }})
                ),
                Arguments.of(
                        new String[] {"--invalidFlag", "value"},
                        false,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("invalidFlag", "value");
                        }})
                ),
                Arguments.of(
                        new String[] {"--invalidFlag", "value1", "value2"},
                        false,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("invalidFlag", Arrays.asList("value1", "value1"));
                        }})
                ),
                Arguments.of(
                        new String[] {"--task=build"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"--task=clean", "--task=build"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", Arrays.asList("clean", "build"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"-d=/build"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("dest", "/build");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        new String[] {"-d=/build1", "-d=/build2"},
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("dest", Arrays.asList("/build1", "/build2"));
                        }}),
                        Collections.emptyMap()
                )

        );
    }

    @ParameterizedTest
    @MethodSource("parseCommandArgsValues")
    public void parseCommandArgsTest(
            String input,
            boolean success,
            Map<String, String[]> expectedValidFlags,
            Map<String, String[]> expectedInvalidFlags) {
        FlagsParser.ParseResult output = underTest.parseArgs(input);

        // check whether parsing was successful
        assertThat(output.success(), is(equalTo(success)));

        // check whether the valid flags are correct
        assertThat(output.getValidFlags().keySet(), containsInAnyOrder(expectedValidFlags.keySet().toArray()));
        for(String key : expectedValidFlags.keySet()) {
            assertThat(output.getValidFlags().get(key), is(equalTo(expectedValidFlags.get(key))));
        }

        // check whether the invalid flags are correct
        assertThat(output.getInvalidFlags().keySet(), containsInAnyOrder(expectedInvalidFlags.keySet().toArray()));
        for(String key : expectedValidFlags.keySet()) {
            assertThat(output.getInvalidFlags().get(key), is(equalTo(expectedInvalidFlags.get(key))));
        }
        assertThat(output.getOriginalString(), is(equalTo(input)));
    }
    public static Stream<Arguments> parseCommandArgsValues() {
        return Stream.of(
                Arguments.of(
                        "build",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "clean build serve",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", Arrays.asList("clean", "build", "serve"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/ --port 9000",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port 9000",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "9000");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "--port 9000",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("port", "9000");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/ --port 9000 -d build/orchid/docs",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                            put("dest", "build/orchid/docs");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/ --port 9000 -d build/orchid/docs -d build/orchid/docs2",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                            put("dest", Arrays.asList("build/orchid/docs", "build/orchid/docs2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/ --port 9000 -d build/orchid/docs --dest build/orchid/docs2",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "9000");
                            put("dest", Arrays.asList("build/orchid/docs", "build/orchid/docs2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/ --port",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "true");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --baseUrl https://orchid.netlify.com/ --port -d build/orchid/docs --dest build/orchid/docs2",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("baseUrl", "https://orchid.netlify.com/");
                            put("port", "true");
                            put("dest", Arrays.asList("build/orchid/docs", "build/orchid/docs2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "--port \"\"",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "--port",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("port", "true");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "--port --baseUrl",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("port", "true");
                            put("baseUrl", "true");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "--invalidFlag",
                        false,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("invalidFlag", "true");
                        }})
                ),
                Arguments.of(
                        "--invalidFlag value",
                        false,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("invalidFlag", "value");
                        }})
                ),
                Arguments.of(
                        "--invalidFlag value1 value2",
                        false,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{

                        }}),
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("invalidFlag", Arrays.asList("value1", "value1"));
                        }})
                ),
                Arguments.of(
                        "--task=build",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "--task=clean --task=build",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", Arrays.asList("clean", "build"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "-d=/build",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("dest", "/build");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "-d=/build1 -d=/build2",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("dest", Arrays.asList("/build1", "/build2"));
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port \"1 2 3\"",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "1 2 3");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port \"1 '2' 3\"",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "1 '2' 3");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port '1 2 3'",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "1 2 3");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port '1 \"2\" 3'",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "1 \"2\" 3");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port '1 \\'2\\' 3'",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "1 '2' 3");
                        }}),
                        Collections.emptyMap()
                ),
                Arguments.of(
                        "build --port \"1 \\\"2\\\" 3\"",
                        true,
                        Collections.unmodifiableMap(new HashMap<String, Object>() {{
                            put("task", "build");
                            put("port", "1 \"2\" 3");
                        }}),
                        Collections.emptyMap()
                )
        );
    }

    @Test
    public void testIncorrectAliasSetupThrows() {
        Set<String> flagNames = new HashSet<>();
        flagNames.add("dest");
        flagNames.add("port");
        flagNames.add("task");
        flagNames.add("baseUrl");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("d", "dest");
        aliases.put("t", "tasks");

        List<String> positionalArgs = Arrays.asList("task");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FlagsParser.builder()
                    .validNames(flagNames)
                    .validAliases(aliases)
                    .positionalNames(positionalArgs)
                    .build();
        });
    }

    @Test
    public void testIncorrectPositionalArgSetupThrows() {
        Set<String> flagNames = new HashSet<>();
        flagNames.add("dest");
        flagNames.add("port");
        flagNames.add("task");
        flagNames.add("baseUrl");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("d", "dest");

        List<String> positionalArgs = Arrays.asList("task", "tasks");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            FlagsParser.builder()
                    .validNames(flagNames)
                    .validAliases(aliases)
                    .positionalNames(positionalArgs)
                    .build();
        });
    }

    @Test
    public void testInvalidAliasThrows() {
        Set<String> flagNames = new HashSet<>();
        flagNames.add("dest");
        flagNames.add("port");
        flagNames.add("task");
        flagNames.add("baseUrl");

        Map<String, String> aliases = new HashMap<>();
        aliases.put("d", "dest");

        List<String> positionalArgs = Arrays.asList("task");

        FlagsParser parser = FlagsParser.builder()
                .validNames(flagNames)
                .validAliases(aliases)
                .positionalNames(positionalArgs)
                .build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            parser.parseArgs(new String[]{"-t"});
        });
    }

}
