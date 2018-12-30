package com.eden.orchid.api.cli;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenUtils;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FlagsParser {

    @Singular
    private final Set<String> validNames;

    @Singular
    private final Map<String, String> validAliases;

    private final List<String> positionalNames;

    @Builder
    public FlagsParser(
            Set<String> validNames,
            Map<String, String> validAliases,
            List<String> positionalNames) {
        this.validNames = validNames;
        this.validAliases = validAliases;
        this.positionalNames = positionalNames;

        for(Map.Entry<String, String> alias : validAliases.entrySet()) {
            if(!validNames.contains(alias.getValue())) {
                throw new IllegalArgumentException("Alias references a flag that is not a valid name: " + alias.getKey() + "->" + alias.getValue());
            }
        }

        for(String positionalArg : positionalNames) {
            if(!validNames.contains(positionalArg)) {
                throw new IllegalArgumentException("Positional arg references a flag that is not a valid name: " + positionalArg);
            }
        }
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
    public ParseResult parseArgsArray(String argString) {
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

        ParseResult result = parseArgsArray(argsArray);
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
    public ParseResult parseArgsArray(String[] args) {
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

        return new ParseResult(parsedValidFlags, parsedInvalidFlags, args);
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

    @Data
    public static class ParseResult {
        private final Map<String, Object> validFlags;
        private final Map<String, Object> invalidFlags;
        private final String[] originalArgs;
        private String originalString;

        public boolean success() {
            return EdenUtils.isEmpty(invalidFlags);
        }
    }

}
