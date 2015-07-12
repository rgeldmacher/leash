package com.rgeldmacher.leash.processor;

import com.rgeldmacher.leash.annotation.Retain;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.rgeldmacher.leash.annotation.Retain")
public class LeashAnnotationProcessor extends AbstractProcessor {

    private Filer filer;
    private Types types;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        types = processingEnv.getTypeUtils();
        elements = processingEnv.getElementUtils();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Retain.class);
        Map<TypeElement, Set<Element>> fieldsByType = getFieldsByType(elements);

        for (Map.Entry<TypeElement, Set<Element>> entry : fieldsByType.entrySet()) {
            try {
                writeJavaFile(entry);
            } catch (IOException e) {
                error(entry.getKey(), "Could not create leash support class", e);
            }
        }

        return true;
    }

    private Map<TypeElement, Set<Element>> getFieldsByType(Set<? extends Element> elements) {
        Map<TypeElement, Set<Element>> fieldsByType = new HashMap<>(100);
        for (Element element : elements) {
            if (element.getModifiers().contains(Modifier.FINAL) ||
                    element.getModifiers().contains(Modifier.STATIC) ||
                    element.getModifiers().contains(Modifier.PROTECTED) ||
                    element.getModifiers().contains(Modifier.PRIVATE)) {
                error(element, "Field must not be private, protected, static or final");
                continue;
            }

            Set<Element> fields = fieldsByType.get(element.getEnclosingElement());
            if (fields == null) {
                fields = new LinkedHashSet<>(10);
                fieldsByType.put((TypeElement) element.getEnclosingElement(), fields);
            }

            fields.add(element);
        }

        return fieldsByType;
    }

    private void writeJavaFile(Map.Entry<TypeElement, Set<Element>> entry) throws IOException {
        TypeSpec retainedFragmentSpec = createRetainedFragmentSpec(entry.getKey(), entry.getValue());
        ClassName retainedFragmentType = ClassName.bestGuess(retainedFragmentSpec.name);

        MethodSpec getRetainedFragmentMethodSpec = createGetRetainedFragmentMethodSpec(entry.getKey(), retainedFragmentType);
        MethodSpec retainDataMethodSpec = createRetainDataMethodSpec(entry.getKey(), entry.getValue(), retainedFragmentType, getRetainedFragmentMethodSpec);
        MethodSpec getRetainedDataMethodSpec = createGetRetainedDataMethodSpec(entry.getKey(), entry.getValue(), retainedFragmentType, getRetainedFragmentMethodSpec);


        MethodSpec leashCtor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();

        TypeSpec leash = TypeSpec.classBuilder(entry.getKey().getSimpleName() + "Leash")
                .addModifiers(Modifier.FINAL)
                .addMethod(leashCtor)
                .addMethod(retainDataMethodSpec)
                .addMethod(getRetainedDataMethodSpec)
                .addMethod(getRetainedFragmentMethodSpec)
                .addType(retainedFragmentSpec)
                .build();

        JavaFile.builder(ClassName.get(entry.getKey()).packageName(), leash)
                .build().writeTo(filer);
    }

    private TypeSpec createRetainedFragmentSpec(TypeElement key, Set<Element> fields) {
        MethodSpec ctor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode("setRetainInstance(true);")
                .build();

        ArrayList<FieldSpec> fieldSpecs = new ArrayList<>(fields.size());
        for (Element field : fields) {
            FieldSpec fieldSpec = FieldSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString())
                    .build();
            fieldSpecs.add(fieldSpec);
        }

        return TypeSpec.classBuilder(key.getSimpleName() + "RetainedDataFragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .superclass(ClassName.get("android.support.v4.app", "Fragment"))
                .addMethod(ctor)
                .addFields(fieldSpecs)
                .build();
    }

    private MethodSpec createGetRetainedFragmentMethodSpec(TypeElement type, ClassName retainedFragmentType) {
        return MethodSpec.methodBuilder("getRetainedFragment")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(ClassName.get("android.support.v4.app", "FragmentActivity"), "activity")
                .returns(retainedFragmentType)
                .beginControlFlow("if (activity != null && activity.getSupportFragmentManager() != null)")
                .addStatement("$T fm = activity.getSupportFragmentManager()", ClassName.get("android.support.v4.app", "FragmentManager"))
                .addStatement("Fragment retainedFragment = fm.findFragmentByTag($S)", retainedFragmentType.simpleName())
                .beginControlFlow("if (retainedFragment == null)")
                .addStatement("retainedFragment = new $T()", retainedFragmentType)
                .addStatement("fm.beginTransaction().add(retainedFragment, $S).commit()", retainedFragmentType.simpleName())
                .endControlFlow()
                .beginControlFlow("if (retainedFragment instanceof $T)", retainedFragmentType)
                .addStatement("return ($T) retainedFragment", retainedFragmentType)
                .endControlFlow()
                .endControlFlow()
                .addStatement("return null")
                .build();
    }

    private MethodSpec createRetainDataMethodSpec(TypeElement type, Set<Element> fields, ClassName retainedFragmentType, MethodSpec getRetainedFragmentMethodSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("retainData")
                .addModifiers(Modifier.STATIC);

        String paramName;
        if (types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.Fragment").asType())) {
            paramName = "fragment";
            builder.addParameter(ClassName.get(type), paramName)
                    .addStatement("$T activity = null", ClassName.get("android.support.v4.app", "FragmentActivity"))
                    .beginControlFlow("if (fragment != null)")
                    .addStatement("activity = fragment.getActivity()")
                    .endControlFlow();
        } else {
            paramName = "activity";
            builder.addParameter(ClassName.get(type), paramName);
        }

        builder.addStatement("$T retainedFragment = $N(activity)", retainedFragmentType, getRetainedFragmentMethodSpec)
                .beginControlFlow("if (retainedFragment != null)");
        for (Element field : fields) {
            builder.addStatement("retainedFragment.$L = $L.$L", field.getSimpleName().toString(), paramName, field.getSimpleName().toString());
        }

        builder.endControlFlow();
        return builder.build();
    }

    private MethodSpec createGetRetainedDataMethodSpec(TypeElement type, Set<Element> fields, ClassName retainedFragmentType, MethodSpec getRetainedFragmentMethodSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRetainedData")
                .addModifiers(Modifier.STATIC);

        String paramName;
        if (types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.Fragment").asType())) {
            paramName = "fragment";
            builder.addParameter(ClassName.get(type), paramName)
                    .addStatement("$T activity = null", ClassName.get("android.support.v4.app", "FragmentActivity"))
                    .beginControlFlow("if (fragment != null)")
                    .addStatement("activity = fragment.getActivity()")
                    .endControlFlow();
        } else {
            paramName = "activity";
            builder.addParameter(ClassName.get(type), paramName);
        }

        builder.addStatement("$T retainedFragment = $N(activity)", retainedFragmentType, getRetainedFragmentMethodSpec)
                .beginControlFlow("if (retainedFragment != null)");
        for (Element field : fields) {
            builder.beginControlFlow("if (retainedFragment.$L != null)", field.getSimpleName().toString())
                    .addStatement("$L.$L = retainedFragment.$L", paramName, field.getSimpleName().toString(), field.getSimpleName().toString())
                    .endControlFlow();
        }

        builder.endControlFlow();
        return builder.build();
    }

    private void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }
}
