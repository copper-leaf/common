package com.eden.orchid.api.converters;

import com.eden.common.util.EdenPair;
import com.eden.orchid.api.options.Extractable;
import com.eden.orchid.api.options.Extractor;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class ExtractableConverter implements TypeConverter<Extractable> {

    private final Provider<Extractor> extractor;
    private final FlexibleMapConverter mapConverter;

    @Inject
    public ExtractableConverter(Provider<Extractor> extractor, FlexibleMapConverter mapConverter) {
        this.extractor = extractor;
        this.mapConverter = mapConverter;
    }

    @Override
    public boolean acceptsClass(Class clazz) {
        return Extractable.class.isAssignableFrom(clazz);
    }

    @Override
    public EdenPair<Boolean, Extractable> convert(Class clazz, Object o) {
        try {
            Extractable holder = (Extractable) extractor.get().getInstanceCreator().getInstance(clazz);
            EdenPair<Boolean, Map> config = mapConverter.convert(clazz, o);
            holder.extractOptions(extractor.get(), config.second);
            return new EdenPair<>(true, holder);
        }
        catch (Exception e) { }

        return null;
    }

}
