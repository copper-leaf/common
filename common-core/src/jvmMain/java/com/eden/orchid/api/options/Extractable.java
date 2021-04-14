package com.eden.orchid.api.options;

import java.util.Map;

/**
 * Denotes a class that has options and can have data extracted into it. Used to mark an Option field as one which
 * should be recursively extracted.
 */
public interface Extractable {

    void extractOptions(Extractor extractor, Map<String, Object> options);

}
