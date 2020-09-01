package com.eden.orchid.api.options.extractors;

import clog.Clog;
import com.eden.orchid.api.converters.ExtractableConverter;
import com.eden.orchid.api.options.Extractable;
import com.eden.orchid.api.options.OptionExtractor;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * ### Source Types
 *
 * | Item Type  | Coercion |
 * |------------|----------|
 * | JSONObject | direct   |
 * | Map        | new JSONObject from map |
 * | JSONArray  | direct   |
 * | anything[] | new JSONArray from array |
 * | List       | new JSONArray from list |
 *
 *
 * ### Destination Types
 *
 * | Field Type                    | Annotation   | Default Value            |
 * |-------------------------------|--------------|--------------------------|
 * | ? extends OptionsHolder       | none         | null                     |
 * | List[? extends Extractable]   | none         | null                     |
 *
 * ### _Notes_
 *
 * This can deserialize any JSONObject into any class that implements OptionsHolder, and can also handle any generic
 * List of OptionsHolders of the same Class.
 *
 * @since v1.0.0
 */
public final class ExtractableOptionExtractor extends OptionExtractor<Extractable> {

    private final ExtractableConverter converter;

    @Inject
    public ExtractableOptionExtractor(ExtractableConverter converter) {
        super(25);
        this.converter = converter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return Extractable.class.isAssignableFrom(clazz);
    }

    @Override
    public Extractable getOption(Field field, Object sourceObject, String key) {
        return converter.convert(field.getType(), sourceObject).second;
    }

    @Override
    public Extractable getDefaultValue(Field field) {
        Extractable holder = converter.convert(field.getType(), new HashMap<>()).second;

        if(holder == null) {
            Clog.e("Could not create instance of [{}] to extract into class {}", field.getType().getName(), field.getDeclaringClass().getName());
        }

        return holder;
    }

}
