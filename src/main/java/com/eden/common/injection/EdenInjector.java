package com.eden.common.injection;

import com.caseyjbrooks.clog.Clog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EdenInjector {

    private List<AnnotationDefinition> annotationDefinitionList;

    public EdenInjector() {
        this.annotationDefinitionList = new ArrayList<>();
    }

    public void addAnnotation(AnnotationDefinition annotationDefinition) {
        this.annotationDefinitionList.add(annotationDefinition);
    }

    public void processAnnotations(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            for (AnnotationDefinition definition : annotationDefinitionList) {
                if (field.isAnnotationPresent(definition.annotationClass)) {
                    if (definition.fieldTypeClass.isAssignableFrom(field.getType())) {
                        definition.annotationHandler.handle(field.getAnnotation(definition.annotationClass), object, field);
                    }
                    else {
                        Clog.e("@#{$1} annotation must be used on a field of type #{$2} or one of its subclasses.",
                                new Object[]{
                                        definition.annotationClass.getSimpleName(),
                                        definition.fieldTypeClass.getSimpleName()
                                }
                        );
                    }
                }
            }
        }
    }

// Interfaces
//----------------------------------------------------------------------------------------------------------------------

    public interface AnnotationHandler {
        void handle(Annotation annotation, Object targetObject, Field annotatedField);
    }

    public static class AnnotationDefinition {
        Class<? extends Annotation> annotationClass;
        Class<?> fieldTypeClass;
        AnnotationHandler annotationHandler;

        public AnnotationDefinition(Class<? extends Annotation> annotationClass, Class<?> fieldTypeClass, AnnotationHandler annotationHandler) {
            this.annotationClass = annotationClass;
            this.fieldTypeClass = fieldTypeClass;
            this.annotationHandler = annotationHandler;
        }
    }
}
