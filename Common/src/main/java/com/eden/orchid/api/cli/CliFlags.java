package com.eden.orchid.api.cli;

import com.eden.common.util.EdenPair;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.options.Extractor;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.Protected;
import lombok.Data;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CliFlags {

    private final Extractor extractor;

    @Inject
    public CliFlags(Extractor extractor) {
        this.extractor = extractor;
    }

    public ParseResult parseArgs(
            List<?> flags,
            List<String> positionalArgs,
            String args,
            Map<String, Object> additionalArgs) {
        return parseArgsInternal(flags, positionalArgs, args, additionalArgs);
    }

    public ParseResult parseArgs(
            List<?> flags,
            List<String> positionalArgs,
            String[] args,
            Map<String, Object> additionalArgs) {
        return parseArgsInternal(flags, positionalArgs, args, additionalArgs);
    }

    private ParseResult parseArgsInternal(
            List<?> flags,
            List<String> positionalArgs,
            Object args,
            Map<String, Object> additionalArgs) {
        // find available flags from given objects
        List<Class<?>> flagClasses = new ArrayList<>();
        for (Object flag : flags) {
            flagClasses.add(flag.getClass());
        }
        FlagsParser parser = getFlagsParser(flagClasses, positionalArgs);

        // parse given args against the available flags
        final FlagsParser.ParseResult result;
        if (args instanceof String[]) {
            result = parser.parseArgs((String[]) args);
        }
        else {
            result = parser.parseArgs((String) args);
        }

        // extract results into flag objects, and retrieve options results
        List<FlagValue> flagValues = new ArrayList<>();
        for (Object flag : flags) {
            extractor.extractOptions(flag, EdenUtils.merge(result.getValidFlags(), additionalArgs));
            flagValues.addAll(getFlagValues(flag));
        }

        // return parsing results for introspection
        return new ParseResult(
                this,
                result,
                flagValues
        );
    }


    public FlagsParser getFlagsParser(List<Class<?>> flagClasses, List<String> positionalArgs) {
        FlagsParser.FlagsParserBuilder builder = FlagsParser.builder();
        builder.positionalNames(positionalArgs);

        for (Class<?> flagClass : flagClasses) {
            EdenPair<Field, Set<Field>> flagFields = extractor.findOptionFields(flagClass);

            for (Field field : flagFields.second) {
                if (field.isAnnotationPresent(Option.class)) {
                    String flagKey = (!EdenUtils.isEmpty(field.getAnnotation(Option.class).value()))
                            ? field.getAnnotation(Option.class).value()
                            : field.getName();
                    String[] aliases = (field.isAnnotationPresent(FlagAliases.class))
                            ? field.getAnnotation(FlagAliases.class).value()
                            : null;

                    builder.validName(flagKey);
                    if (!EdenUtils.isEmpty(aliases)) {
                        for (String alias : aliases) {
                            builder.validAlias(alias, flagKey);
                        }
                    }
                }
            }
        }

        return builder.build();
    }

    public List<FlagValue> getFlagValues(Object flagObject) {
        List<FlagValue> values = new ArrayList<>();
        EdenPair<Field, Set<Field>> flagFields = extractor.findOptionFields(flagObject.getClass());

        for (Field field : flagFields.second) {
            if (field.isAnnotationPresent(Option.class)) {
                String flagKey = (!EdenUtils.isEmpty(field.getAnnotation(Option.class).value()))
                        ? field.getAnnotation(Option.class).value()
                        : field.getName();
                boolean isProtected = field.isAnnotationPresent(Protected.class);

                values.add(new FlagValue(
                        flagObject,
                        field.getType(),
                        flagKey,
                        extractor.getOptionValue(flagObject, field, flagKey),
                        isProtected
                ));
            }
        }

        return values;
    }

    @Data
    public static class FlagValue {
        private final Object source;
        private final Class<?> type;
        private final String key;
        private final Object value;
        private final boolean isProtected;
    }

    @Data
    public static class ParseResult {
        private final CliFlags source;
        private final FlagsParser.ParseResult parseResult;
        private final List<FlagValue> flagValues;

        public boolean success() {
            return parseResult.success();
        }
    }

}
