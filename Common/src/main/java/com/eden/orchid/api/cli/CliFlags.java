package com.eden.orchid.api.cli;

import com.eden.common.util.EdenPair;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.options.Extractor;
import com.eden.orchid.api.options.annotations.Option;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CliFlags {

    private final Extractor extractor;

    @Inject
    public CliFlags(Extractor extractor) {
        this.extractor = extractor;
    }

    public FlagsParser.ParseResult parseArgs(List<Object> flags, List<String> positionalArgs, String args) {
        // find available flags from given objects
        List<Class<?>> flagClasses = new ArrayList<>();
        for (int i = 0; i < flags.size(); i++) {
            flagClasses.add(flags.get(i).getClass());
        }
        FlagsParser parser = getFlagsParser(flagClasses, positionalArgs);

        // parse given args against the available flags
        FlagsParser.ParseResult result = parser.parseArgs(args);
        for(Object flag : flags) {
            extractor.extractOptions(flag, result.getValidFlags());
        }

        // return parsing results for introspection
        return result;
    }

    public FlagsParser.ParseResult parseArgs(List<Object> flags, List<String> positionalArgs, String[] args) {
        // find available flags from given objects
        List<Class<?>> flagClasses = new ArrayList<>();
        for (int i = 0; i < flags.size(); i++) {
            flagClasses.add(flags.get(i).getClass());
        }
        FlagsParser parser = getFlagsParser(flagClasses, positionalArgs);

        // parse given args against the available flags
        FlagsParser.ParseResult result = parser.parseArgs(args);
        for(Object flag : flags) {
            extractor.extractOptions(flag, result.getValidFlags());
        }

        // return parsing results for introspection
        return result;
    }

    public FlagsParser getFlagsParser(List<Class<?>> flagClasses, List<String> positionalArgs) {
        FlagsParser.FlagsParserBuilder builder = FlagsParser.builder();
        builder.positionalNames(positionalArgs);

        for(Class<?> flagClass : flagClasses) {
            EdenPair<Field, Set<Field>> flagFields = extractor.findOptionFields(flagClass);

            for(Field field : flagFields.second) {
                if(field.isAnnotationPresent(Option.class)) {
                    String flagKey = (!EdenUtils.isEmpty(field.getAnnotation(Option.class).value()))
                            ? field.getAnnotation(Option.class).value()
                            : field.getName();
                    String[] aliases = (field.isAnnotationPresent(FlagAliases.class))
                            ? field.getAnnotation(FlagAliases.class).value()
                            : null;

                    builder.validName(flagKey);
                    if(!EdenUtils.isEmpty(aliases)) {
                        for(String alias : aliases) {
                            builder.validAlias(alias, flagKey);
                        }
                    }
                }
            }
        }

        return builder.build();
    }

}
