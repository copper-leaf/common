package com.eden.orchid.api.cli;

import com.eden.orchid.api.options.DefaultExtractor;
import com.eden.orchid.api.options.annotations.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class CliFlagsTest {

    private CliFlags underTest;

    @BeforeEach
    void setUp() {
        underTest = new CliFlags(DefaultExtractor.getInstance());
    }

    @Test
    void testParsingOptionsFlags() {
        FlagsParser parser = underTest.getFlagsParser(Arrays.asList(TestCliOptions1.class), null);

        assertThat(parser.getValidNames(), containsInAnyOrder("task", "baseUrl"));
        assertThat(parser.getValidAliases(), hasEntry("t", "task"));
    }

    @Test
    void testParsingOptionsFlagsArrayValues() {
        TestCliOptions1 cliOptions1 = new TestCliOptions1();
        TestCliOptions2 cliOptions2 = new TestCliOptions2();
        CliFlags.ParseResult results = underTest.parseArgs(
                Arrays.asList(cliOptions1, cliOptions2),
                Arrays.asList("task"),
                new String[]{"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "9000"},
                null
        );

        assertThat(results.success(), is(true));
        assertThat(results.getParseResult().getSource().getValidNames(), containsInAnyOrder("task", "baseUrl", "port"));
        assertThat(results.getParseResult().getSource().getValidAliases(), hasEntry("t", "task"));

        assertThat(cliOptions1.task, is(equalTo("build")));
        assertThat(cliOptions1.baseUrl, is(equalTo("https://orchid.netlify.com/")));
        assertThat(cliOptions2.port, is(equalTo(9000)));

        CliFlags.FlagValue taskValue = results
                .getFlagValues()
                .stream()
                .filter(it -> it.getKey().equals("task"))
                .findFirst()
                .orElse(null);
        assertThat(taskValue, is(notNullValue()));
        assertThat(taskValue.getType(), is(equalTo(String.class)));
        assertThat(taskValue.getValue(), is(equalTo("build")));

        CliFlags.FlagValue baseUrlValue = results
                .getFlagValues()
                .stream()
                .filter(it -> it.getKey().equals("baseUrl"))
                .findFirst()
                .orElse(null);
        assertThat(baseUrlValue, is(notNullValue()));
        assertThat(baseUrlValue.getType(), is(equalTo(String.class)));
        assertThat(baseUrlValue.getValue(), is(equalTo("https://orchid.netlify.com/")));

        CliFlags.FlagValue portValue = results
                .getFlagValues()
                .stream()
                .filter(it -> it.getKey().equals("port"))
                .findFirst()
                .orElse(null);
        assertThat(portValue, is(notNullValue()));
        assertThat(portValue.getType(), is(equalTo(int.class)));
        assertThat(portValue.getValue(), is(equalTo(9000)));
    }

    @Test
    void testParsingOptionsFlagsStringValues() {
        TestCliOptions1 cliOptions1 = new TestCliOptions1();
        TestCliOptions2 cliOptions2 = new TestCliOptions2();
        CliFlags.ParseResult results = underTest.parseArgs(
                Arrays.asList(cliOptions1, cliOptions2),
                Arrays.asList("task"),
                "build --baseUrl https://orchid.netlify.com/ --port 9000",
                null
        );

        assertThat(results.success(), is(true));
        assertThat(results.getParseResult().getSource().getValidNames(), containsInAnyOrder("task", "baseUrl", "port"));
        assertThat(results.getParseResult().getSource().getValidAliases(), hasEntry("t", "task"));

        assertThat(cliOptions1.task, is(equalTo("build")));
        assertThat(cliOptions1.baseUrl, is(equalTo("https://orchid.netlify.com/")));
        assertThat(cliOptions2.port, is(equalTo(9000)));

        CliFlags.FlagValue taskValue = results
                .getFlagValues()
                .stream()
                .filter(it -> it.getKey().equals("task"))
                .findFirst()
                .orElse(null);
        assertThat(taskValue, is(notNullValue()));
        assertThat(taskValue.getType(), is(equalTo(String.class)));
        assertThat(taskValue.getValue(), is(equalTo("build")));

        CliFlags.FlagValue baseUrlValue = results
                .getFlagValues()
                .stream()
                .filter(it -> it.getKey().equals("baseUrl"))
                .findFirst()
                .orElse(null);
        assertThat(baseUrlValue, is(notNullValue()));
        assertThat(baseUrlValue.getType(), is(equalTo(String.class)));
        assertThat(baseUrlValue.getValue(), is(equalTo("https://orchid.netlify.com/")));

        CliFlags.FlagValue portValue = results
                .getFlagValues()
                .stream()
                .filter(it -> it.getKey().equals("port"))
                .findFirst()
                .orElse(null);
        assertThat(portValue, is(notNullValue()));
        assertThat(portValue.getType(), is(equalTo(int.class)));
        assertThat(portValue.getValue(), is(equalTo(9000)));
    }

    @Test
    void testAdditionalArgsOverridesStringArrayArgs() {
        TestCliOptions1 cliOptions1 = new TestCliOptions1();
        TestCliOptions2 cliOptions2 = new TestCliOptions2();
        CliFlags.ParseResult results = underTest.parseArgs(
                Arrays.asList(cliOptions1, cliOptions2),
                Arrays.asList("task"),
                new String[]{"build", "--baseUrl", "https://orchid.netlify.com/", "--port", "9000"},
                new HashMap<String, Object>() {{
                    put("task", "build2");
                }}
        );

        assertThat(results.getParseResult().getSource().getValidNames(), containsInAnyOrder("task", "baseUrl", "port"));
        assertThat(results.getParseResult().getSource().getValidAliases(), hasEntry("t", "task"));

        assertThat(cliOptions1.task, is(equalTo("build2")));
        assertThat(cliOptions1.baseUrl, is(equalTo("https://orchid.netlify.com/")));
        assertThat(cliOptions2.port, is(equalTo(9000)));
    }

    @Test
    void testAdditionalArgsOverridesStringArgs() {
        TestCliOptions1 cliOptions1 = new TestCliOptions1();
        TestCliOptions2 cliOptions2 = new TestCliOptions2();
        CliFlags.ParseResult results = underTest.parseArgs(
                Arrays.asList(cliOptions1, cliOptions2),
                Arrays.asList("task"),
                "build --baseUrl https://orchid.netlify.com/ --port 9000",
                new HashMap<String, Object>() {{
                    put("task", "build2");
                }}
        );

        assertThat(results.getParseResult().getSource().getValidNames(), containsInAnyOrder("task", "baseUrl", "port"));
        assertThat(results.getParseResult().getSource().getValidAliases(), hasEntry("t", "task"));

        assertThat(cliOptions1.task, is(equalTo("build2")));
        assertThat(cliOptions1.baseUrl, is(equalTo("https://orchid.netlify.com/")));
        assertThat(cliOptions2.port, is(equalTo(9000)));
    }

    public static class TestCliOptions1 {

        @Option
        @FlagAliases("t")
        public String task;

        @Option
        public String baseUrl;

    }

    public static class TestCliOptions2 {

        @Option
        public int port;

    }

}
