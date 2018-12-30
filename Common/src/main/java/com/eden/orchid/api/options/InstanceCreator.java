package com.eden.orchid.api.options;

public interface InstanceCreator {

    <T> T getInstance(Class<T> clazz);

}
