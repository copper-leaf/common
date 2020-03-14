package com.eden.orchid.api.cli;

import com.eden.common.util.EdenPair;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.options.Extractor;
import com.eden.orchid.api.options.annotations.Option;
import com.eden.orchid.api.options.annotations.Protected;

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

    public static class FlagValue {
        private final Object source;
        private final Class<?> type;
        private final String key;
        private final Object value;
        private final boolean isProtected;

        public FlagValue(Object source, Class<?> type, String key, Object value, boolean isProtected) {
            this.source = source;
            this.type = type;
            this.key = key;
            this.value = value;
            this.isProtected = isProtected;
        }

        public Object getSource() {
            return this.source;
        }

        public Class<?> getType() {
            return this.type;
        }

        public String getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }

        public boolean isProtected() {
            return this.isProtected;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof FlagValue)) return false;
            final FlagValue other = (FlagValue) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$source = this.getSource();
            final Object other$source = other.getSource();
            if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
            final Object this$type = this.getType();
            final Object other$type = other.getType();
            if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
            final Object this$key = this.getKey();
            final Object other$key = other.getKey();
            if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
            final Object this$value = this.getValue();
            final Object other$value = other.getValue();
            if (this$value == null ? other$value != null : !this$value.equals(other$value)) return false;
            if (this.isProtected() != other.isProtected()) return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof FlagValue;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $source = this.getSource();
            result = result * PRIME + ($source == null ? 43 : $source.hashCode());
            final Object $type = this.getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            final Object $key = this.getKey();
            result = result * PRIME + ($key == null ? 43 : $key.hashCode());
            final Object $value = this.getValue();
            result = result * PRIME + ($value == null ? 43 : $value.hashCode());
            result = result * PRIME + (this.isProtected() ? 79 : 97);
            return result;
        }

        public String toString() {
            return "CliFlags.FlagValue(source=" + this.getSource() + ", type=" + this.getType() + ", key=" + this.getKey() + ", value=" + this.getValue() + ", isProtected=" + this.isProtected() + ")";
        }
    }

    public static class ParseResult {
        private final CliFlags source;
        private final FlagsParser.ParseResult parseResult;
        private final List<FlagValue> flagValues;

        public ParseResult(CliFlags source, FlagsParser.ParseResult parseResult, List<FlagValue> flagValues) {
            this.source = source;
            this.parseResult = parseResult;
            this.flagValues = flagValues;
        }

        public boolean success() {
            return parseResult.success();
        }

        public CliFlags getSource() {
            return this.source;
        }

        public FlagsParser.ParseResult getParseResult() {
            return this.parseResult;
        }

        public List<FlagValue> getFlagValues() {
            return this.flagValues;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ParseResult)) return false;
            final ParseResult other = (ParseResult) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$source = this.getSource();
            final Object other$source = other.getSource();
            if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
            final Object this$parseResult = this.getParseResult();
            final Object other$parseResult = other.getParseResult();
            if (this$parseResult == null ? other$parseResult != null : !this$parseResult.equals(other$parseResult))
                return false;
            final Object this$flagValues = this.getFlagValues();
            final Object other$flagValues = other.getFlagValues();
            if (this$flagValues == null ? other$flagValues != null : !this$flagValues.equals(other$flagValues))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof ParseResult;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $source = this.getSource();
            result = result * PRIME + ($source == null ? 43 : $source.hashCode());
            final Object $parseResult = this.getParseResult();
            result = result * PRIME + ($parseResult == null ? 43 : $parseResult.hashCode());
            final Object $flagValues = this.getFlagValues();
            result = result * PRIME + ($flagValues == null ? 43 : $flagValues.hashCode());
            return result;
        }

        public String toString() {
            return "CliFlags.ParseResult(source=" + this.getSource() + ", parseResult=" + this.getParseResult() + ", flagValues=" + this.getFlagValues() + ")";
        }
    }

}
