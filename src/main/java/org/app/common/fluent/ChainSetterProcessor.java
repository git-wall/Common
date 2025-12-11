package org.app.common.fluent;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("org.app.common.fluent.ChainSetter")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ChainSetterProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ChainSetter.class)) {
            if (element.getKind() == ElementKind.CLASS) {
                try {
                    generateChainMethods((TypeElement) element);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "Error generating chain methods: " + e.getMessage(), element);
                }
            }
        }
        return true;
    }

    private void generateChainMethods(TypeElement classElement) throws IOException {
        ChainSetter annotation = classElement.getAnnotation(ChainSetter.class);
        String className = classElement.getSimpleName().toString();
        String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();

        // Create class extending original class
        TypeSpec.Builder chainClassBuilder = TypeSpec.classBuilder(className + "Builder")
            .addModifiers(Modifier.PUBLIC)
            .superclass(ClassName.get(packageName, className));

        // Find all non-static, non-final fields
        List<VariableElement> fields = getEligibleFields(classElement);

        for (VariableElement field : fields) {
            ChainSetter fieldAnnotation = field.getAnnotation(ChainSetter.class);
            ChainSetter effectiveAnnotation = fieldAnnotation != null ? fieldAnnotation : annotation;

            addChainMethodsForField(chainClassBuilder, field, effectiveAnnotation, className, packageName);
        }

        // Generate the class
        JavaFile javaFile = JavaFile.builder(packageName, chainClassBuilder.build())
            .build();

        javaFile.writeTo(processingEnv.getFiler());
    }

    private List<VariableElement> getEligibleFields(TypeElement classElement) {
        List<VariableElement> fields = new ArrayList<>();

        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosedElement;
                Set<Modifier> modifiers = field.getModifiers();

                if (!modifiers.contains(Modifier.STATIC) &&
                    !modifiers.contains(Modifier.FINAL)) {
                    fields.add(field);
                }
            }
        }

        return fields;
    }

    private void addChainMethodsForField(TypeSpec.Builder classBuilder, VariableElement field,
                                         ChainSetter config, String className, String packageName) {
        String fieldName = field.getSimpleName().toString();
        String capitalizedName = capitalize(fieldName);
        TypeMirror fieldType = field.asType();

        ClassName returnType = ClassName.get(packageName, className + "Builder");

        if (config.chain()) {
            // Override setter to return this
            addChainingMethod(classBuilder, "set" + capitalizedName, fieldName, fieldType, returnType);
        } else {
            // Add additional chaining method
            String methodName = config.fluent() ? fieldName : config.prefix() + capitalizedName;
            addChainingMethod(classBuilder, methodName, fieldName, fieldType, returnType);
        }
    }

    private void addChainingMethod(TypeSpec.Builder classBuilder, String methodName,
                                   String fieldName, TypeMirror fieldType, ClassName returnType) {
        MethodSpec method = MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .returns(returnType)
            .addParameter(TypeName.get(fieldType), fieldName)
            .addStatement("super.set$L($L)", capitalize(fieldName), fieldName)
            .addStatement("return this")
            .build();

        classBuilder.addMethod(method);
    }

    // Build method to return original type
    private void addBuildMethod(TypeSpec.Builder classBuilder, String className, String packageName) {
        ClassName originalType = ClassName.get(packageName, className);

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(originalType)
            .addStatement("return this")
            .build();

        classBuilder.addMethod(buildMethod);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
