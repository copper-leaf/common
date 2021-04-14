package com.eden.orchid.api.options;

import java.util.Map;

public interface OptionArchetype {

    Map<String, Object> getOptions(Object target, String archetypeKey);

}
