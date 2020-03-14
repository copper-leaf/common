package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import com.eden.common.util.EdenUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlexibleIterableConverter implements TypeConverter<Iterable> {

    private final FlexibleMapConverter mapConverter;

    @Inject
    public FlexibleIterableConverter(FlexibleMapConverter mapConverter) {
        this.mapConverter = mapConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Iterable.class);
    }

    @Override
    public EdenPair<Boolean, Iterable> convert(Class clazz, Object objectToConvert) {
        return convert(clazz, objectToConvert, null);
    }

    public EdenPair<Boolean, Iterable> convert(Class clazz, Object objectToConvert, String keyName) {
        if(objectToConvert != null) {
            if (objectToConvert instanceof Iterable) {
                return new EdenPair<>(true, (Iterable) objectToConvert);
            }
            else if (objectToConvert.getClass().isArray()) {
                List<Object> list = new ArrayList<>();
                Collections.addAll(list, EdenUtils.box(objectToConvert));
                return new EdenPair<>(true, (Iterable) list);
            }
            else {
                EdenPair<Boolean, Map> potentialMap = mapConverter.convert(clazz, objectToConvert);
                if(potentialMap.first) {
                    Map<String, Object> actualMap = (Map<String, Object>) potentialMap.second;
                    List<Object> list = mapToList(clazz, actualMap, keyName);
                    return new EdenPair<>(true, (Iterable) list);
                }
                else {
                    return new EdenPair<>(true, (Iterable) Collections.singletonList(objectToConvert));
                }
            }
        }

        return new EdenPair<>(false, (Iterable) new ArrayList());
    }

    private List<Object> mapToList(Class clazz, Map<String, Object> map, String keyName) {
        List<Object> list = new ArrayList<>();

        for(String key : map.keySet()) {
            Object item = map.get(key);

            EdenPair<Boolean, Map> potentialMapItem = mapConverter.convert(clazz, item);
            if(potentialMapItem.first) {
                Map<String, Object> mapItem = new HashMap<>((Map<String, Object>) potentialMapItem.second);
                mapItem.put(keyName, key);
                list.add(mapItem);
            }
            else {
                list.add(item);
            }
        }

        return list;
    }

}
