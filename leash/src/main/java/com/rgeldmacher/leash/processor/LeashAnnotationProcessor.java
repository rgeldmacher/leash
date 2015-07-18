/*
 * Copyright 2015 Robert Geldmacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.rgeldmacher.leash.processor;

import com.rgeldmacher.leash.Leash;
import com.rgeldmacher.leash.Retain;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Annotation processor for the Retain annotation.
 *
 * @author robertgeldmacher
 */
@SupportedAnnotationTypes("com.rgeldmacher.leash.Retain")
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
                writeJavaFile(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                error(entry.getKey(), "Could not create leash support class", e);
            }
        }

        return true;
    }

    private Map<TypeElement, Set<Element>> getFieldsByType(Set<? extends Element> elements) {
        Map<TypeElement, Set<Element>> fieldsByType = new HashMap<>(100);
        for (Element element : elements) {
            if (!typeIsActivityOrFragment(element.getEnclosingElement())) {
                error(element, "The @Retain annotation can only be applied to fields of an Activity or Fragment");
            } else if (element.getModifiers().contains(Modifier.FINAL) ||
                    element.getModifiers().contains(Modifier.STATIC) ||
                    element.getModifiers().contains(Modifier.PROTECTED) ||
                    element.getModifiers().contains(Modifier.PRIVATE)) {
                error(element, "Field must not be private, protected, static or final");
                continue;
            } else if (typeIsPrimitive(element.asType())) {
                error(element, "Primitive types cannot be retained currently. Use @SaveState instead");
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

    private void writeJavaFile(TypeElement classWithAnnotations, Set<Element> annotatedFields) throws IOException {
        TypeSpec retainedFragmentSpec = createRetainedFragmentSpec(classWithAnnotations, annotatedFields);
        ClassName retainedFragmentType = ClassName.bestGuess(retainedFragmentSpec.name);

        MethodSpec getRetainedFragmentMethodSpec = createGetRetainedFragmentMethodSpec(classWithAnnotations, retainedFragmentType);

        MethodSpec restoreMethodSpec = createRestoreMethodSpec(classWithAnnotations, annotatedFields, retainedFragmentType, getRetainedFragmentMethodSpec);
        MethodSpec retainMethodSpec = createRetainMethodSpec(classWithAnnotations, annotatedFields, retainedFragmentType, getRetainedFragmentMethodSpec);
        MethodSpec clearMethodSpec = createClearMethodSpec(classWithAnnotations, annotatedFields, retainedFragmentType, getRetainedFragmentMethodSpec);

        TypeVariableName typeVariableName = TypeVariableName.get("T", ClassName.get(classWithAnnotations));

        TypeSpec leash = TypeSpec.classBuilder(classWithAnnotations.getSimpleName() + Leash.SUFFIX)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariableName)
                .addMethod(restoreMethodSpec)
                .addMethod(retainMethodSpec)
                .addMethod(clearMethodSpec)
                .addMethod(getRetainedFragmentMethodSpec)
                .addType(retainedFragmentSpec)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Leash.LeashBinding.class), typeVariableName))
                .build();

        JavaFile.builder(ClassName.get(classWithAnnotations).packageName(), leash)
                .build().writeTo(filer);
    }

    private TypeSpec createRetainedFragmentSpec(TypeElement classWithAnnotations, Set<Element> annotatedFields) {
        MethodSpec ctor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode("setRetainInstance(true);")
                .build();

        ArrayList<FieldSpec> fieldSpecs = new ArrayList<>(annotatedFields.size());
        for (Element field : annotatedFields) {
            FieldSpec fieldSpec = FieldSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString())
                    .build();
            fieldSpecs.add(fieldSpec);
        }

        return TypeSpec.classBuilder(classWithAnnotations.getSimpleName() + "RetainedDataFragment")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .superclass(getFragmentClass(classWithAnnotations))
                .addMethod(ctor)
                .addFields(fieldSpecs)
                .build();
    }

    private MethodSpec createGetRetainedFragmentMethodSpec(TypeElement classWithAnnotations, ClassName retainedFragmentType) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getRetainedFragment")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(getActivityClass(classWithAnnotations), "activity")
                .returns(retainedFragmentType)
                .beginControlFlow("if (activity != null)");

        addGetFragmentManagerSnippet(builder, classWithAnnotations);

        builder.beginControlFlow("if (fm != null)")
                .addStatement("Fragment retainedFragment = fm.findFragmentByTag($S)", retainedFragmentType.simpleName())
                .beginControlFlow("if (retainedFragment == null)")
                .addStatement("retainedFragment = new $T()", retainedFragmentType)
                .addStatement("fm.beginTransaction().add(retainedFragment, $S).commit()", retainedFragmentType.simpleName())
                .endControlFlow()
                .beginControlFlow("if (retainedFragment instanceof $T)", retainedFragmentType)
                .addStatement("return ($T) retainedFragment", retainedFragmentType)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return null");

        return builder.build();
    }

    private MethodSpec createRetainMethodSpec(TypeElement classWithAnnotations, Set<Element> annotatedFields, ClassName retainedFragmentType, MethodSpec getRetainedFragmentMethodSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("retain")
                .addModifiers(Modifier.PUBLIC);

        String methodParam = addGetRetainedFragmentSnippet(builder, classWithAnnotations, retainedFragmentType, getRetainedFragmentMethodSpec);

        builder.beginControlFlow("if (retainedFragment != null)");
        for (Element field : annotatedFields) {
            builder.addStatement("retainedFragment.$L = $L.$L", field.getSimpleName().toString(), methodParam, field.getSimpleName().toString());
        }

        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec createRestoreMethodSpec(TypeElement classWithAnnotations, Set<Element> annotatedFields, ClassName retainedFragmentType, MethodSpec getRetainedFragmentMethodSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("restore")
                .addModifiers(Modifier.PUBLIC);

        String methodParam = addGetRetainedFragmentSnippet(builder, classWithAnnotations, retainedFragmentType, getRetainedFragmentMethodSpec);

        builder.beginControlFlow("if (retainedFragment != null)");
        for (Element field : annotatedFields) {
            builder.beginControlFlow("if (retainedFragment.$L != null)", field.getSimpleName().toString())
                    .addStatement("$L.$L = retainedFragment.$L", methodParam, field.getSimpleName().toString(), field.getSimpleName().toString())
                    .endControlFlow();
        }

        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec createClearMethodSpec(TypeElement classWithAnnotations, Set<Element> annotatedFields, ClassName retainedFragmentType, MethodSpec getRetainedFragmentMethodSpec) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("clear")
                .addModifiers(Modifier.PUBLIC);

        addGetRetainedFragmentSnippet(builder, classWithAnnotations, retainedFragmentType, getRetainedFragmentMethodSpec);

        builder.beginControlFlow("if (retainedFragment != null)");
        for (Element field : annotatedFields) {
            builder.addStatement("retainedFragment.$L = null", field.getSimpleName().toString());
        }

        builder.endControlFlow();

        return builder.build();
    }

    private String addGetRetainedFragmentSnippet(MethodSpec.Builder builder, TypeElement classWithAnnotations, ClassName retainedFragmentType, MethodSpec getRetainedFragmentMethodSpec) {
        String parameterName;
        if (typeIsFragment(classWithAnnotations)) {
            parameterName = "fragment";
            builder.addParameter(TypeVariableName.get("T"), parameterName);
            builder.addStatement("$T activity = null", getActivityClass(classWithAnnotations))
                    .beginControlFlow("if ($L != null)", parameterName)
                    .addStatement("activity = $L.getActivity()", parameterName)
                    .endControlFlow()
                    .addStatement("$T retainedFragment = $N(activity)", retainedFragmentType, getRetainedFragmentMethodSpec);
        } else {
            parameterName = "activity";
            builder.addParameter(TypeVariableName.get("T", ClassName.get(classWithAnnotations)), parameterName);
            builder.addStatement("$T retainedFragment = $N($L)", retainedFragmentType, getRetainedFragmentMethodSpec, parameterName);
        }

        return parameterName;
    }

    private void addGetFragmentManagerSnippet(MethodSpec.Builder builder, TypeElement classWithAnnotations) {
        if (useSupportLibrary(classWithAnnotations)) {
            builder.addStatement("$T fm = activity.getSupportFragmentManager()", ClassName.get("android.support.v4.app", "FragmentManager"));
        } else {
            builder.addStatement("$T fm = activity.getFragmentManager()", ClassName.get("android.app", "FragmentManager"));
        }
    }

    private void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }

    private ClassName getActivityClass(TypeElement type) {
        if (useSupportLibrary(type)) {
            return ClassName.get("android.support.v4.app", "FragmentActivity");
        } else {
            return ClassName.get("android.app", "Activity");
        }
    }

    private ClassName getFragmentClass(TypeElement type) {
        if (useSupportLibrary(type)) {
            return ClassName.get("android.support.v4.app", "Fragment");
        } else {
            return ClassName.get("android.app", "Fragment");
        }
    }

    private boolean useSupportLibrary(TypeElement type) {
        return types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.FragmentActivity").asType())
                || types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.Fragment").asType());
    }

    private boolean typeIsFragment(TypeElement type) {
        return types.isAssignable(type.asType(), elements.getTypeElement("android.app.Fragment").asType())
                || types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.Fragment").asType());
    }

    private boolean typeIsActivityOrFragment(Element type) {
        return type != null &&
                (types.isAssignable(type.asType(), elements.getTypeElement("android.app.Activity").asType())
                        || types.isAssignable(type.asType(), elements.getTypeElement("android.app.Fragment").asType())
                        || types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.FragmentActivity").asType())
                        || types.isAssignable(type.asType(), elements.getTypeElement("android.support.v4.app.Fragment").asType()));
    }

    private boolean typeIsPrimitive(TypeMirror type) {
        TypeKind kind = type.getKind();
        return kind == TypeKind.BOOLEAN || kind == TypeKind.BYTE || kind == TypeKind.CHAR ||
                kind == TypeKind.DOUBLE || kind == TypeKind.FLOAT || kind == TypeKind.INT ||
                kind == TypeKind.LONG || kind == TypeKind.SHORT;
    }
}
