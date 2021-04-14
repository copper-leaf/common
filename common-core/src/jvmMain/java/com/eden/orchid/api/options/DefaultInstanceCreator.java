package com.eden.orchid.api.options;

public class DefaultInstanceCreator implements InstanceCreator {

    @Override
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
