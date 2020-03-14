package com.eden.orchid.api.cli;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FlagsParser {

    private final Set<String> validNames;
    private final Map<String, String> validAliases;
    private final List<String> positionalNames;

    public FlagsParser(
            Set<String> validNames,
            Map<String, String> validAliases,
            List<String> positionalNames) {
        this.validNames = (!EdenUtils.isEmpty(validNames)) ? validNames : new HashSet<String>();
        this.validAliases = (!EdenUtils.isEmpty(validAliases)) ? validAliases : new HashMap<String, String>();
        this.positionalNames = (!EdenUtils.isEmpty(positionalNames)) ? positionalNames : new ArrayList<String>();

        for(Map.Entry<String, String> alias : this.validAliases.entrySet()) {
            if(!validNames.contains(alias.getValue())) {
                throw new IllegalArgumentException("Alias references a flag that is not a valid name: " + alias.getKey() + "->" + alias.getValue());
            }
        }

        for(String positionalArg : this.positionalNames) {
            if(!validNames.contains(positionalArg)) {
                throw new IllegalArgumentException("Positional arg references a flag that is not a valid name: " + positionalArg);
            }
        }
    }

    public static FlagsParserBuilder builder() {
        return new FlagsParserBuilder();
    }

    /**
     * Parse a string into a list of strings, similar to how a shell interpreter would split it. Then parse those args
     * pieces into a ParseResult. Pieces are split on whitespace, and literal strings starting with either single or
     * double quotes may encapsulate any characters until a closing quotation is found. Within those literal strings,
     * a quote may be escaped with a backslash character.
     *
     * @param argString a String to parse as CLI flags
     * @return the result of parsing
     */
    public ParseResult parseArgs(String argString) {
        List<String> argsList = new ArrayList<>();

        Clog.v("Parsing string:");

        String lastString = "";
        char lastQuote = 0;
        char lastChar = 0;

        for(char c : argString.toCharArray()) {
            Clog.v("  char [{}]", c);
            if(lastQuote != 0) {
                Clog.v("    in string");
                if(c == lastQuote) {
                    if(lastChar == '\\') {
                        Clog.v("      endquote is escaped");
                        lastString = lastString.substring(0, lastString.length() - 1);
                        lastString += c;
                    }
                    else {
                        Clog.v("      endquote closes");
                        lastQuote = 0;
                        argsList.add(lastString);
                        lastString = "";
                    }
                }
                else {
                    lastString += c;
                }
            }
            else {
                Clog.v("   not in string");
                if(Character.isWhitespace(c)) {
                    argsList.add(lastString);
                    lastString = "";
                }
                else if(c == '"') {
                    lastQuote = '"';
                }
                else if(c == '\'') {
                    lastQuote = '\'';
                }
                else {
                    lastString += c;
                }
            }

            lastChar = c;
        }

        if(!EdenUtils.isEmpty(lastString)) {
            Clog.v("adding last string: {}", lastString);
            argsList.add(lastString);
        }

        Clog.v("", argsList);

        String[] argsArray = new String[argsList.size()];
        argsList.toArray(argsArray);

        ParseResult result = parseArgs(argsArray);
        result.setOriginalString(argString);
        return result;
    }

    /**
     * Parse the CLI args into maps, according to the rules set when creating the FlagsParser. Given a set of valid
     * flags, a map of aliases to those flags, and a list of the positional flags
     *
     * @param args the CLI flags to parse
     * @return the result of parsing
     */
    public ParseResult parseArgs(String[] args) {
        Clog.v("Parsing with:");
        Clog.v("  valid names: {}", validNames);
        Clog.v("  valid aliases: {}", validAliases);
        Clog.v("  positionalNames: {}", positionalNames);

        Map<String, Object> parsedValidFlags = new HashMap<>();
        Map<String, Object> parsedInvalidFlags = new HashMap<>();
        List<String> positionalArgs = new ArrayList<>();

        // loop over flags, adding them to positional args until we get an arg starting with '--' or '-', at which point
        // we start adding to that key's values
        String currentFlag = null;
        boolean currentFlagIsAlias = false;

        int valuesParsed = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = (args[i] != null) ? args[i] : "";
            Clog.v("arg [{}]", arg);

            if (arg.startsWith("--")) {
                // full flag name
                String flag = arg.replaceFirst("--", "");

                if (flag.contains("=")) {
                    String[] flagParts = flag.split("=");
                    Clog.v("  is double dash equals: {}, {}", flagParts[0], flagParts[1]);
                    addArgValue(parsedValidFlags, parsedInvalidFlags, flagParts[0], flagParts[1], false);
                }
                else {
                    Clog.v("  is double dash: {}", flag);

                    // parse as valid flag name
                    if (valuesParsed == 0 && currentFlag != null) {
                        // the previous flag had no values, mark it as true
                        addArgValue(parsedValidFlags, parsedInvalidFlags, currentFlag, "true", currentFlagIsAlias);
                    }
                    currentFlag = flag;
                    currentFlagIsAlias = false;
                    valuesParsed = 0;
                }
            }
            else if (arg.startsWith("-")) {
                // full flag name
                String flag = arg.replaceFirst("-", "");

                if (flag.contains("=")) {
                    String[] flagParts = flag.split("=");
                    Clog.v("  is single dash equals: {}, {}", flagParts[0], flagParts[1]);
                    addArgValue(parsedValidFlags, parsedInvalidFlags, flagParts[0], flagParts[1], true);
                }
                else {
                    Clog.v("  is single dash: {}", flag);

                    // parse as valid flag name
                    if (valuesParsed == 0 && currentFlag != null) {
                        // the previous flag had no values, mark it as true
                        addArgValue(parsedValidFlags, parsedInvalidFlags, currentFlag, "true", currentFlagIsAlias);
                    }
                    currentFlag = flag;
                    currentFlagIsAlias = true;
                    valuesParsed = 0;
                }
            }
            else {
                Clog.v("  is item");

                if (currentFlag != null) {
                    Clog.v("  is value: {}, {}", currentFlag, arg);

                    // named flag values
                    addArgValue(parsedValidFlags, parsedInvalidFlags, currentFlag, arg, currentFlagIsAlias);
                    valuesParsed++;
                }
                else {
                    Clog.v("  is positional: {}", arg);

                    // positional flag values
                    positionalArgs.add(arg);
                }
            }
        }

        if (valuesParsed == 0 && currentFlag != null) {
            Clog.v("we have an extra value: {}", currentFlag);

            // the final flag had no values, mark it as true
            addArgValue(parsedValidFlags, parsedInvalidFlags, currentFlag, "true", currentFlagIsAlias);
        }

        // add positional args to named args map. Positional args override named args
        if (positionalArgs.size() > 0 && positionalNames.size() > 0) {
            if(positionalNames.size() >= positionalArgs.size()) {
                // parse one arg at a time for each positional arg
                for (int i = 0; i < positionalArgs.size(); i++) {
                    Clog.v("  positional arg: {}", positionalArgs.get(i));
                    addArgValue(parsedValidFlags, parsedInvalidFlags, positionalNames.get(i), positionalArgs.get(i), false);
                }
            }
            else {
                // parse one arg at a time for each positional arg
                for (int i = 0; i < positionalNames.size(); i++) {
                    Clog.v("  positional arg: {}", positionalArgs.get(i));
                    addArgValue(parsedValidFlags, parsedInvalidFlags, positionalNames.get(i), positionalArgs.get(i), false);
                }

                // parse remaining args as last positional arg
                for (int i = positionalNames.size(); i < positionalArgs.size(); i++) {
                    Clog.v("  positional vararg: {}", positionalArgs.get(i));
                    addArgValue(parsedValidFlags, parsedInvalidFlags, positionalNames.get(positionalNames.size() - 1), positionalArgs.get(i), false);
                }
            }
        }

        normalizeFlags(parsedValidFlags);
        normalizeFlags(parsedInvalidFlags);

        return new ParseResult(this, parsedValidFlags, parsedInvalidFlags, args);
    }

    private void addArgValue(
            Map<String, Object> parsedValidFlags,
            Map<String, Object> parsedInvalidFlags,
            String key,
            String value,
            boolean isAlias) {
        if (isAlias) {
            if (validNames != null && validAliases != null && validAliases.containsKey(key)) {
                key = validAliases.get(key);
            }
            else {
                throw new IllegalArgumentException(Clog.format("Unrecognized flag: -{}", key));
            }
        }

        if (validNames != null && validNames.contains(key)) {
            Clog.v("    adding valid arg value: {}, {}", key, value);
            addArgValueToMap(parsedValidFlags, key, value);
        }
        else {
            Clog.v("    adding invalid arg value: {}, {}", key, value);
            addArgValueToMap(parsedInvalidFlags, key, value);
        }
    }

    private void addArgValueToMap(Map<String, Object> map, String key, String value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
        else {
            if (!(map.get(key) instanceof List)) {
                List<Object> listValues = new ArrayList<>();
                listValues.add(map.get(key));
                map.put(key, listValues);
            }

            ((List<Object>) map.get(key)).add(value);
        }
    }

    private void normalizeFlags(Map<String, Object> flags) {
        Iterator<String> it = flags.keySet().iterator();

        while (it.hasNext()) {
            String key = it.next();
            Object value = flags.get(key);

            boolean removeObject = false;
            if (value == null) {
                removeObject = true;
            }
            else if (value instanceof String && EdenUtils.isEmpty((String) value)) {
                removeObject = true;
            }
            else if (value instanceof Collection && EdenUtils.isEmpty((Collection) value)) {
                removeObject = true;
            }

            if (removeObject) {
                it.remove();
            }
        }
    }

    public Set<String> getValidNames() {
        return this.validNames;
    }

    public Map<String, String> getValidAliases() {
        return this.validAliases;
    }

    public List<String> getPositionalNames() {
        return this.positionalNames;
    }

    public static class ParseResult {
        private final FlagsParser source;
        private final Map<String, Object> validFlags;
        private final Map<String, Object> invalidFlags;
        private final String[] originalArgs;
        private String originalString;

        public ParseResult(FlagsParser source, Map<String, Object> validFlags, Map<String, Object> invalidFlags, String[] originalArgs) {
            this.source = source;
            this.validFlags = validFlags;
            this.invalidFlags = invalidFlags;
            this.originalArgs = originalArgs;
        }

        public boolean success() {
            return EdenUtils.isEmpty(invalidFlags);
        }

        public FlagsParser getSource() {
            return this.source;
        }

        public Map<String, Object> getValidFlags() {
            return this.validFlags;
        }

        public Map<String, Object> getInvalidFlags() {
            return this.invalidFlags;
        }

        public String[] getOriginalArgs() {
            return this.originalArgs;
        }

        public String getOriginalString() {
            return this.originalString;
        }

        public void setOriginalString(String originalString) {
            this.originalString = originalString;
        }

        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof ParseResult)) return false;
            final ParseResult other = (ParseResult) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$source = this.getSource();
            final Object other$source = other.getSource();
            if (this$source == null ? other$source != null : !this$source.equals(other$source)) return false;
            final Object this$validFlags = this.getValidFlags();
            final Object other$validFlags = other.getValidFlags();
            if (this$validFlags == null ? other$validFlags != null : !this$validFlags.equals(other$validFlags))
                return false;
            final Object this$invalidFlags = this.getInvalidFlags();
            final Object other$invalidFlags = other.getInvalidFlags();
            if (this$invalidFlags == null ? other$invalidFlags != null : !this$invalidFlags.equals(other$invalidFlags))
                return false;
            if (!java.util.Arrays.deepEquals(this.getOriginalArgs(), other.getOriginalArgs())) return false;
            final Object this$originalString = this.getOriginalString();
            final Object other$originalString = other.getOriginalString();
            if (this$originalString == null ? other$originalString != null : !this$originalString.equals(other$originalString))
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
            final Object $validFlags = this.getValidFlags();
            result = result * PRIME + ($validFlags == null ? 43 : $validFlags.hashCode());
            final Object $invalidFlags = this.getInvalidFlags();
            result = result * PRIME + ($invalidFlags == null ? 43 : $invalidFlags.hashCode());
            result = result * PRIME + java.util.Arrays.deepHashCode(this.getOriginalArgs());
            final Object $originalString = this.getOriginalString();
            result = result * PRIME + ($originalString == null ? 43 : $originalString.hashCode());
            return result;
        }

        public String toString() {
            return "FlagsParser.ParseResult(source=" + this.getSource() + ", validFlags=" + this.getValidFlags() + ", invalidFlags=" + this.getInvalidFlags() + ", originalArgs=" + java.util.Arrays.deepToString(this.getOriginalArgs()) + ", originalString=" + this.getOriginalString() + ")";
        }
    }

    public static class FlagsParserBuilder {
        private ArrayList<String> validNames;
        private ArrayList<String> validAliases$key;
        private ArrayList<String> validAliases$value;
        private List<String> positionalNames;

        FlagsParserBuilder() {
        }

        public FlagsParser.FlagsParserBuilder validName(String validName) {
            if (this.validNames == null) this.validNames = new ArrayList<String>();
            this.validNames.add(validName);
            return this;
        }

        public FlagsParser.FlagsParserBuilder validNames(Collection<? extends String> validNames) {
            if (this.validNames == null) this.validNames = new ArrayList<String>();
            this.validNames.addAll(validNames);
            return this;
        }

        public FlagsParser.FlagsParserBuilder clearValidNames() {
            if (this.validNames != null)
                this.validNames.clear();
            return this;
        }

        public FlagsParser.FlagsParserBuilder validAlias(String validAliasKey, String validAliasValue) {
            if (this.validAliases$key == null) {
                this.validAliases$key = new ArrayList<String>();
                this.validAliases$value = new ArrayList<String>();
            }
            this.validAliases$key.add(validAliasKey);
            this.validAliases$value.add(validAliasValue);
            return this;
        }

        public FlagsParser.FlagsParserBuilder validAliases(Map<? extends String, ? extends String> validAliases) {
            if (this.validAliases$key == null) {
                this.validAliases$key = new ArrayList<String>();
                this.validAliases$value = new ArrayList<String>();
            }
            for (final Map.Entry<? extends String, ? extends String> $lombokEntry : validAliases.entrySet()) {
                this.validAliases$key.add($lombokEntry.getKey());
                this.validAliases$value.add($lombokEntry.getValue());
            }
            return this;
        }

        public FlagsParser.FlagsParserBuilder clearValidAliases() {
            if (this.validAliases$key != null) {
                this.validAliases$key.clear();
                this.validAliases$value.clear();
            }
            return this;
        }

        public FlagsParser.FlagsParserBuilder positionalNames(List<String> positionalNames) {
            this.positionalNames = positionalNames;
            return this;
        }

        public FlagsParser build() {
            Set<String> validNames;
            switch (this.validNames == null ? 0 : this.validNames.size()) {
                case 0:
                    validNames = java.util.Collections.emptySet();
                    break;
                case 1:
                    validNames = java.util.Collections.singleton(this.validNames.get(0));
                    break;
                default:
                    validNames = new java.util.LinkedHashSet<String>(this.validNames.size() < 1073741824 ? 1 + this.validNames.size() + (this.validNames.size() - 3) / 3 : Integer.MAX_VALUE);
                    validNames.addAll(this.validNames);
                    validNames = java.util.Collections.unmodifiableSet(validNames);
            }
            Map<String, String> validAliases;
            switch (this.validAliases$key == null ? 0 : this.validAliases$key.size()) {
                case 0:
                    validAliases = java.util.Collections.emptyMap();
                    break;
                case 1:
                    validAliases = java.util.Collections.singletonMap(this.validAliases$key.get(0), this.validAliases$value.get(0));
                    break;
                default:
                    validAliases = new java.util.LinkedHashMap<String, String>(this.validAliases$key.size() < 1073741824 ? 1 + this.validAliases$key.size() + (this.validAliases$key.size() - 3) / 3 : Integer.MAX_VALUE);
                    for (int $i = 0; $i < this.validAliases$key.size(); $i++)
                        validAliases.put(this.validAliases$key.get($i), (String) this.validAliases$value.get($i));
                    validAliases = java.util.Collections.unmodifiableMap(validAliases);
            }

            return new FlagsParser(validNames, validAliases, positionalNames);
        }

        public String toString() {
            return "FlagsParser.FlagsParserBuilder(validNames=" + this.validNames + ", validAliases$key=" + this.validAliases$key + ", validAliases$value=" + this.validAliases$value + ", positionalNames=" + this.positionalNames + ")";
        }
    }
}
