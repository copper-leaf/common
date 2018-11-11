package com.eden.orchid.api.options;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenPair;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.options.annotations.AllOptions;
import com.eden.orchid.api.options.annotations.Archetype;
import com.eden.orchid.api.options.annotations.Archetypes;
import com.eden.orchid.api.options.annotations.Option;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Extractor {

    protected final List<OptionExtractor> extractors;
    private final OptionsValidator validator;

    public Extractor(Collection<OptionExtractor> extractors, OptionsValidator validator) {
        List<OptionExtractor> originalExtractors = new ArrayList<>(extractors);
        Collections.sort(originalExtractors, new Comparator<OptionExtractor>() {
            @Override
            public int compare(OptionExtractor o1, OptionExtractor o2) {
                return o2.getPriority() - o1.getPriority();
            }
        });
        this.extractors = Collections.unmodifiableList(originalExtractors);
        this.validator = validator;
    }

    public final void extractOptions(Object optionsHolder, Map<String, Object> options) {
        if(optionsHolder == null) throw new NullPointerException("optionsHolder cannot be null");

        // setup initial options
        Map<String, Object> initialOptions = (options != null) ? new HashMap<>(options) : new HashMap<String, Object>();
        Map<String, Object> archetypalOptions = loadArchetypalData(optionsHolder, initialOptions);

        Map<String, Object> actualOptions = EdenUtils.merge(archetypalOptions, initialOptions);

        // extract options fields
        EdenPair<Field, Set<Field>> fields = findOptionFields(optionsHolder.getClass());

        if(fields.first != null) {
            setOptionValue(optionsHolder, fields.first, fields.first.getName(), Map.class, actualOptions);
        }

        for (Field field : fields.second) {
            String fieldOptionKey = (!EdenUtils.isEmpty(field.getAnnotation(Option.class).value()))
                    ? field.getAnnotation(Option.class).value()
                    : field.getName();

            setOption(optionsHolder, field, actualOptions, fieldOptionKey);
        }

        if(validator != null) {
            try {
                validator.validate(optionsHolder);
            }
            catch (Exception e) {
                Clog.e("{} did not pass validation", optionsHolder, e);
                throw new IllegalStateException(Clog.format("{} did not pass validation", optionsHolder, e));
            }
        }
    }

    public final Map<String, Object> getOptionsValues(Object optionsHolder) {
        if(optionsHolder == null) throw new NullPointerException("optionsHolder cannot be null");

        // extract options fields
        EdenPair<Field, Set<Field>> fields = findOptionFields(optionsHolder.getClass());

        Map<String, Object> optionsValues = new HashMap<>();

        for (Field field : fields.second) {
            String fieldOptionKey = (!EdenUtils.isEmpty(field.getAnnotation(Option.class).value()))
                    ? field.getAnnotation(Option.class).value()
                    : field.getName();

            optionsValues.put(fieldOptionKey, getOptionValue(optionsHolder, field, fieldOptionKey));
        }

        return optionsValues;
    }

// Find Options
//----------------------------------------------------------------------------------------------------------------------

    protected final EdenPair<Field, Set<Field>> findOptionFields(Class<?> optionsHolderClass) {
        return findOptionFields(optionsHolderClass, true, true);
    }

    protected final EdenPair<Field, Set<Field>> findOptionFields(Class<?> optionsHolderClass, boolean includeOwnOptions, boolean includeInheritedOptions) {
        Field optionsDataField = null;
        Set<Field> fields = new HashSet<>();

        int i = 0;
        while (optionsHolderClass != null) {
            boolean shouldGetOptions = true;
            if(i == 0) {
                if(!includeOwnOptions) {
                    shouldGetOptions = false;
                }
            }
            else {
                if(!includeInheritedOptions) {
                    shouldGetOptions = false;
                }
            }

            if(shouldGetOptions) {
                Field[] declaredFields = optionsHolderClass.getDeclaredFields();
                if (!EdenUtils.isEmpty(declaredFields)) {
                    for (Field field : declaredFields) {
                        if (field.isAnnotationPresent(Option.class)) {
                            fields.add(field);
                        } else if (field.isAnnotationPresent(AllOptions.class) && field.getType().equals(Map.class)) {
                            optionsDataField = field;
                        }
                    }
                }
            }

            optionsHolderClass = optionsHolderClass.getSuperclass();
            i++;
        }

        return new EdenPair<>(optionsDataField, fields);
    }

// Options Archetypes
//----------------------------------------------------------------------------------------------------------------------

    protected List<Archetype> getArchetypes(Class<?> optionsHolderClass) {
        List<Archetype> archetypeAnnotations = new ArrayList<>();

        while (optionsHolderClass != null) {
            Archetypes archetypes = optionsHolderClass.getAnnotation(Archetypes.class);
            if(archetypes != null) {
                Collections.addAll(archetypeAnnotations, archetypes.value());
            }
            else {
                Archetype archetype = optionsHolderClass.getAnnotation(Archetype.class);
                if(archetype != null) {
                    archetypeAnnotations.add(archetype);
                }
            }

            optionsHolderClass = optionsHolderClass.getSuperclass();
        }

        Collections.reverse(archetypeAnnotations);

        return archetypeAnnotations;
    }

    protected final Map<String, Object> loadArchetypalData(Object target, Map<String, Object> actualOptions) {
        Map<String, Object> allAdditionalData = new HashMap<>();

        for(Archetype archetype : getArchetypes(target.getClass())) {
            OptionArchetype archetypeDataProvider = getInstance(archetype.value());

            Map<String, Object> archetypeConfiguration;
            if(actualOptions.containsKey(archetype.key()) && actualOptions.get(archetype.key()) instanceof Map) {
                archetypeConfiguration = (Map<String, Object>) actualOptions.get(archetype.key());
            }
            else {
                archetypeConfiguration = new HashMap<>();
            }

            this.extractOptions(archetypeDataProvider, archetypeConfiguration);
            Map<String, Object> archetypalData = archetypeDataProvider.getOptions(target, archetype.key());

            if(archetypalData != null) {
                allAdditionalData = EdenUtils.merge(allAdditionalData, archetypalData);
            }
        }

        return allAdditionalData;
    }

// Set option values
//----------------------------------------------------------------------------------------------------------------------

    protected final void setOption(Object optionsHolder, Field field, Map<String, Object> options, String key) {
        boolean foundExtractor = false;
        for (OptionExtractor extractor : extractors) {
            if (extractor.acceptsClass(field.getType())) {

                Object sourceObject = null;
                Object resultObject = null;

                if(options.containsKey(key)) {
                    sourceObject = options.get(key);
                    resultObject = extractor.getOption(field, sourceObject, key);
                    if(extractor.isEmptyValue(resultObject)) {
                        resultObject = extractor.getDefaultValue(field);
                    }
                }
                else {
                    resultObject = extractor.getDefaultValue(field);
                }

                setOptionValue(optionsHolder, field, key, field.getType(), resultObject);
                foundExtractor = true;
                break;
            }
        }

        if (!foundExtractor) {
            setOptionValue(optionsHolder, field, key, field.getType(), null);
        }
    }

    protected final void setOptionValue(Object optionsHolder, Field field, String key, Class<?> objectClass, Object value) {
        try {
            String setterMethodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);
            Method method = optionsHolder.getClass().getMethod(setterMethodName, objectClass);
            method.invoke(optionsHolder, value);
            return;
        }
        catch (NoSuchMethodException e) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            field.set(optionsHolder, value);
            return;
        }
        catch (IllegalAccessException e) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Clog.e("Options field {} in class {} is inaccessible. Make sure the field is public or has a bean-style setter method", key, optionsHolder.getClass().getSimpleName());
    }

// Get option values
//----------------------------------------------------------------------------------------------------------------------

    protected final Object getOptionValue(Object optionsHolder, Field field, String key) {
        try {
            String getterMethodName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            Method method = optionsHolder.getClass().getMethod(getterMethodName);
            return method.invoke(optionsHolder);
        }
        catch (NoSuchMethodException e) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // boolean getters have special naming conventions
        if(field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
            try {
                String getterMethodName = "is" + key.substring(0, 1).toUpperCase() + key.substring(1);
                Method method = optionsHolder.getClass().getMethod(getterMethodName);
                return method.invoke(optionsHolder);
            }
            catch (NoSuchMethodException e) {
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String getterMethodName = "has" + key.substring(0, 1).toUpperCase() + key.substring(1);
                Method method = optionsHolder.getClass().getMethod(getterMethodName);
                return method.invoke(optionsHolder);
            }
            catch (NoSuchMethodException e) {
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // also allow for fluent getters
        try {
            String getterMethodName = key;
            Method method = optionsHolder.getClass().getMethod(getterMethodName);
            return method.invoke(optionsHolder);
        }
        catch (NoSuchMethodException e) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Method method = optionsHolder.getClass().getMethod("get", String.class);
            return method.invoke(optionsHolder, key);
        }
        catch (NoSuchMethodException e) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return field.get(optionsHolder);
        }
        catch (IllegalAccessException e) {
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Clog.e("Options field {} in class {} is inaccessible. Make sure the field is public or has a bean-style getter method", key, optionsHolder.getClass().getSimpleName());

        return null;
    }

// Description
//----------------------------------------------------------------------------------------------------------------------

    public String describeOption(Class<?> optionsHolderClass, String optionKey) {
        EdenPair<Field, Set<Field>> fields = findOptionFields(optionsHolderClass, true, true);

        Field optionField = null;
        for (Field field : fields.second) {
            String fieldOptionKey = (!EdenUtils.isEmpty(field.getAnnotation(Option.class).value()))
                    ? field.getAnnotation(Option.class).value()
                    : field.getName();

            if(fieldOptionKey.equals(optionKey)) {
                optionField = field;
                break;
            }
        }

        if(optionField != null) {
            for (OptionExtractor extractor : extractors) {
                if (extractor.acceptsClass(optionField.getType())) {
                    return extractor.describeDefaultValue(optionField);
                }
            }
        }

        return "";
    }

// Utils
//----------------------------------------------------------------------------------------------------------------------

    public <T> T getInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
