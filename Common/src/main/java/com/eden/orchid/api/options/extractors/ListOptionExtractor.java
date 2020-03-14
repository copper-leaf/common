package com.eden.orchid.api.options.extractors;

import com.eden.common.util.EdenPair;
import com.eden.orchid.api.converters.Converters;
import com.eden.orchid.api.converters.FlexibleIterableConverter;
import com.eden.orchid.api.converters.FlexibleMapConverter;
import com.eden.orchid.api.options.Extractable;
import com.eden.orchid.api.options.Extractor;
import com.eden.orchid.api.options.OptionExtractor;
import com.eden.orchid.api.options.annotations.BooleanDefault;
import com.eden.orchid.api.options.annotations.DoubleDefault;
import com.eden.orchid.api.options.annotations.FloatDefault;
import com.eden.orchid.api.options.annotations.ImpliedKey;
import com.eden.orchid.api.options.annotations.IntDefault;
import com.eden.orchid.api.options.annotations.LongDefault;
import com.eden.orchid.api.options.annotations.StringDefault;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ### Source Types
 *
 * | Item Type  | Coercion |
 * |------------|----------|
 * | JSONArray  | direct   |
 * | anything[] | new JSONArray from array |
 * | List       | new JSONArray from list |
 *
 *
 * ### Destination Types
 *
 * | Field Type | Annotation  | Default Value |
 * |------------|-------------|---------------|
 * | JSONArray  | none        | null          |
 *
 * @since v1.0.0
 */
public final class ListOptionExtractor extends OptionExtractor<List> {

    private final Provider<Extractor> extractor;
    private final FlexibleIterableConverter iterableConverter;
    private final FlexibleMapConverter mapConverter;
    private final Converters converters;

    @Inject
    public ListOptionExtractor(Provider<Extractor> extractor, FlexibleIterableConverter iterableConverter, FlexibleMapConverter mapConverter, Converters converters) {
        super(2);
        this.extractor = extractor;
        this.iterableConverter = iterableConverter;
        this.mapConverter = mapConverter;
        this.converters = converters;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    public List getOption(Field field, Object sourceObject, String key) {
        List<Object> list = new ArrayList<>();

        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];

        if(Extractable.class.isAssignableFrom(listClass)) {
            String impliedKey = (field.isAnnotationPresent(ImpliedKey.class))
                    ? field.getAnnotation(ImpliedKey.class).value()
                    : null;

            EdenPair<Boolean, Iterable> valueAsIterable = iterableConverter.convert(field.getType(), sourceObject, impliedKey);
            EdenPair<Boolean, Map> valueAsMap = mapConverter.convert(field.getType(), sourceObject, impliedKey);

            if(valueAsIterable.first) {
                for(Object item : valueAsIterable.second) {
                    Extractable holder = (Extractable) extractor.get().getInstanceCreator().getInstance(listClass);
                    EdenPair<Boolean, Map> config = convert(listClass, item, impliedKey);
                    holder.extractOptions(extractor.get(), config.second);
                    list.add(holder);
                }
            }
            else if(valueAsMap.first) {
                for(Map.Entry<String, Object> item : ((Map<String, Object>) valueAsMap.second).entrySet()) {
                    Extractable holder = (Extractable) extractor.get().getInstanceCreator().getInstance(listClass);
                    EdenPair<Boolean, Map> config = convert(listClass, item.getValue(), impliedKey);
                    holder.extractOptions(extractor.get(), config.second);
                    list.add(holder);
                }
            }
        }
        else {
            EdenPair<Boolean, Iterable> valueAsIterable = iterableConverter.convert(field.getType(), sourceObject);

            for(Object item : valueAsIterable.second) {
                EdenPair<Boolean, ?> converted = converters.convert(item, listClass);

                if(converted.first) {
                    list.add(converted.second);
                }
            }
        }

        return list;
    }

    @Override
    public List getDefaultValue(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        Class<?> listClass = (Class<?>) stringListType.getActualTypeArguments()[0];

        if(listClass.equals(Boolean.class)) {
            if(field.isAnnotationPresent(BooleanDefault.class)) {
                List<Boolean> list = new ArrayList<>();
                for(boolean val : field.getAnnotation(BooleanDefault.class).value()) {
                    list.add(val);
                }
                return list;
            }
        }
        else if(listClass.equals(Double.class)) {
            if(field.isAnnotationPresent(DoubleDefault.class)) {
                List<Double> list = new ArrayList<>();
                for(double val : field.getAnnotation(DoubleDefault.class).value()) {
                    list.add(val);
                }
                return list;
            }
        }
        else if(listClass.equals(Float.class)) {
            if(field.isAnnotationPresent(FloatDefault.class)) {
                List<Float> list = new ArrayList<>();
                for(float val : field.getAnnotation(FloatDefault.class).value()) {
                    list.add(val);
                }
                return list;
            }
        }
        else if(listClass.equals(Integer.class)) {
            if(field.isAnnotationPresent(IntDefault.class)) {
                List<Integer> list = new ArrayList<>();
                for(int val : field.getAnnotation(IntDefault.class).value()) {
                    list.add(val);
                }
                return list;
            }
        }
        else if(listClass.equals(Long.class)) {
            if(field.isAnnotationPresent(LongDefault.class)) {
                List<Long> list = new ArrayList<>();
                for(long val : field.getAnnotation(LongDefault.class).value()) {
                    list.add(val);
                }
                return list;
            }
        }
        else if(listClass.equals(String.class)) {
            if(field.isAnnotationPresent(StringDefault.class)) {
                return Arrays.asList(field.getAnnotation(StringDefault.class).value());
            }
        }
        else if(Extractable.class.isAssignableFrom(listClass)) {
            if(field.isAnnotationPresent(ImpliedKey.class) && field.isAnnotationPresent(StringDefault.class)) {
                String impliedKey = field.getAnnotation(ImpliedKey.class).value();
                String[] defaultItems = field.getAnnotation(StringDefault.class).value();

                ArrayList list = new ArrayList();
                for(String defaultItem : defaultItems) {
                    Extractable holder = (Extractable) extractor.get().getInstanceCreator().getInstance(listClass);
                    EdenPair<Boolean, Map> config = convert(listClass, defaultItem, impliedKey);
                    holder.extractOptions(extractor.get(), config.second);
                    list.add(holder);
                }

                return list;
            }
        }

        return new ArrayList();
    }

    @Override
    public String describeDefaultValue(Field field) {
        List<?> value = getDefaultValue(field);

        if(value.size() > 0) {
            StringBuilder defaultValue = new StringBuilder("[");
            for (int i = 0; i < value.size(); i++) {
                if(i == value.size() - 1) {
                    defaultValue.append(converters.convert(value.get(i), String.class).second);
                }
                else {
                    defaultValue.append(converters.convert(value.get(i), String.class).second);
                    defaultValue.append(", ");
                }
            }
            defaultValue.append("]");

            return defaultValue.toString();
        }

        return "empty list";
    }

    private EdenPair<Boolean, Map> convert(Class clazz, Object object, String keyName) {
        if(object != null) {
            Map<String, Object> sourceMap = null;

            if(object instanceof Map || object instanceof JSONObject) {
                if (object instanceof Map) {
                    sourceMap = (Map) object;
                }
                else if(object instanceof JSONObject) {
                    sourceMap = (Map) ((JSONObject) object).toMap();
                }
            }
            else {
                sourceMap = Collections.singletonMap(keyName, object);
            }

            return new EdenPair<>(true, (Map) sourceMap);
        }

        return new EdenPair<>(false, (Map) new HashMap());
    }

}
