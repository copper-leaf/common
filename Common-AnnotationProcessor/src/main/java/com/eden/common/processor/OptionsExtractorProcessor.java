package com.eden.common.processor;

import com.caseyjbrooks.clog.Clog;
import com.eden.common.util.EdenUtils;
import com.eden.orchid.api.options.annotations.Option;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.json.JSONObject;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
public class OptionsExtractorProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Map<String, ProcessorHelper> classesWithOptions = getAnnotatedClasses(roundEnvironment);

        for (ProcessorHelper processorHelper : classesWithOptions.values()) {
            generateHelperClass(processorHelper);
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Option.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

// Helper Methods
//----------------------------------------------------------------------------------------------------------------------

    private Map<String, ProcessorHelper> getAnnotatedClasses(RoundEnvironment roundEnvironment) {
        Map<String, ProcessorHelper> classesWithOptions = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Option.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be only be applied to fields.");
            }

            VariableElement optionField = (VariableElement) element;
            TypeElement enclosingClass = (TypeElement) optionField.getEnclosingElement();
            String name = enclosingClass.getQualifiedName().toString();

            if(!classesWithOptions.containsKey(name)) {
                classesWithOptions.put(name, new ProcessorHelper(enclosingClass));
            }
            classesWithOptions.get(name).addField(optionField);
        }

        return classesWithOptions;
    }

    private void generateHelperClass(ProcessorHelper processorHelper) {
        MethodSpec.Builder extractOptions = MethodSpec.methodBuilder("extractOptions")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get(processorHelper.sourcePackageName, processorHelper.sourceClassName), "optionHolder")
                .addParameter(JSONObject.class, "options");

        for(VariableElement el : processorHelper.optionsFields) {
            extractOptions = setOption(extractOptions, processorHelper, el);
        }

        TypeSpec extractorHelper = TypeSpec.classBuilder(processorHelper.generatedClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(extractOptions.build())
                .build();

        JavaFile javaFile = JavaFile.builder(processorHelper.generatedPackageName, extractorHelper)
                .build();

        Clog.v("\n-----\n{}-----\n", javaFile.toString());
        try {
            javaFile.writeTo(filer);
        }
        catch (IOException e) {
            Clog.v("Something went wrong writing Java file", e);
        }
    }

    private MethodSpec.Builder setOption(MethodSpec.Builder builder, ProcessorHelper processorHelper, VariableElement el) {
        String optionKey = !EdenUtils.isEmpty(el.getAnnotation(Option.class).value())
                ? el.getAnnotation(Option.class).value()
                : el.getSimpleName().toString();

        String fieldName = el.getSimpleName().toString();
        String setterName = getSetterMethod(optionKey, processorHelper, el);
        TypeName optionType = ClassName.get(el.asType());
        String hash = Integer.toHexString(fieldName.hashCode());

        builder = builder.addComment("Get and convert source option value");
        builder = builder.addStatement("$T $L_$L = ($T) options.get($S)", optionType, optionKey, hash, optionType, optionKey);

        if(setterName != null) {
            builder = builder.addComment("Set $L with setter '$L'", optionKey, setterName);
            builder = builder.addStatement("optionHolder.$L($L_$L)", setterName, optionKey, hash);
        }
        else {
            builder = builder.addComment("Set $L directly to field '$L'", optionKey, fieldName);
            builder = builder.addStatement("optionHolder.$L = $L_$L", fieldName, optionKey, hash);
        }

        builder.addCode("\n");

        return builder;
    }

    private String getSetterMethod(String optionKey, ProcessorHelper processorHelper, VariableElement el) {
        String setterName;

        setterName = "set" + optionKey.substring(0, 1).toUpperCase() + optionKey.substring(1);
        if(hasMatchingSetter(setterName, processorHelper, el)) {
            return setterName;
        }

        setterName = "set" + el.getSimpleName().toString().substring(0, 1).toUpperCase() + el.getSimpleName().toString().substring(1);
        if(hasMatchingSetter(setterName, processorHelper, el)) {
            return setterName;
        }

        return null;
    }

    private boolean hasMatchingSetter(String setterName, ProcessorHelper processorHelper, VariableElement el) {
        for(Element enclosedElement : processorHelper.enclosingClass.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) enclosedElement;
                if(method.getSimpleName().toString().equals(setterName)) {
                    if(method.getParameters().size() == 1) {
                        VariableElement parameter = method.getParameters().get(0);

                        if(types.isSameType(el.asType(), parameter.asType())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

// Helper Classes
//----------------------------------------------------------------------------------------------------------------------

    private class ProcessorHelper {

        private final String sourcePackageName;
        private final String sourceClassName;

        private final String generatedPackageName;
        private final String generatedClassName;

        private final TypeElement enclosingClass;
        private final Set<VariableElement> optionsFields;

        private ProcessorHelper(TypeElement enclosingClass) {
            this.enclosingClass = enclosingClass;
            this.optionsFields = new HashSet<>();

            this.sourcePackageName = elements.getPackageOf(enclosingClass).getQualifiedName().toString();
            this.sourceClassName = enclosingClass.getSimpleName().toString();

            this.generatedPackageName = this.sourcePackageName;
            this.generatedClassName = this.sourceClassName + "ExtractorHelper";
        }

        private void addField(VariableElement optionField) {
            optionsFields.add(optionField);
        }
    }

}