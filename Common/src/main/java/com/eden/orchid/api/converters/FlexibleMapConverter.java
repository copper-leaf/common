package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlexibleMapConverter implements TypeConverter<Map> {

    @Inject
    public FlexibleMapConverter() {
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return clazz.equals(Map.class);
    }

    @Override
    public EdenPair<Boolean, Map> convert(Class clazz, Object objectToConvert) {
        return convert(clazz, objectToConvert, null);
    }

    public EdenPair<Boolean, Map> convert(Class clazz, Object object, String keyName) {
        if(object != null) {
            if (object instanceof Map) {
                return new EdenPair<>(true, (Map) object);
            }
            else if(object instanceof JSONObject) {
                return new EdenPair<>(true, (Map) ((JSONObject) object).toMap());
            }
            else {
                return new EdenPair<>(false, (Map) Collections.singletonMap(null, object));
            }
        }

        return new EdenPair<>(false, (Map) new HashMap());
    }

}
