package com.eden.orchid.api.cli;

import com.eden.orchid.api.options.DefaultExtractor;
import com.eden.orchid.api.options.Extractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cli {

    public static <T> T parseArgsInto(T optionHolder, String[] args) {
        return parseArgsInto(optionHolder, args, null);
    }

    public static <T> T parseArgsInto(T optionHolder, String[] args, List<String> positionalArgs) {
        return parseArgsInto(DefaultExtractor.getInstance(), optionHolder, args, positionalArgs);
    }

    public static <T> T parseArgsInto(Extractor extractor, T optionHolder, String[] args, List<String> positionalArgs) {
        CliFlags cliFlags = new CliFlags(extractor);
        FlagsParser parser = cliFlags.getFlagsParser(new ArrayList<Class<?>>(Collections.singleton(optionHolder.getClass())), positionalArgs);
        FlagsParser.ParseResult result = parser.parseArgs(args);
        DefaultExtractor.getInstance().extractOptions(optionHolder, result.getValidFlags());

        return optionHolder;
    }

}
