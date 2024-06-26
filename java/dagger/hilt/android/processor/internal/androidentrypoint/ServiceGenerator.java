/*
 * Copyright (C) 2019 The Dagger Authors.
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

package dagger.hilt.android.processor.internal.androidentrypoint;

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.xprocessing.XElements.getSimpleName;
import static java.util.stream.Collectors.joining;

import androidx.room.compiler.processing.JavaPoetExtKt;
import androidx.room.compiler.processing.XConstructorElement;
import androidx.room.compiler.processing.XExecutableParameterElement;
import androidx.room.compiler.processing.XFiler;
import androidx.room.compiler.processing.XProcessingEnv;
import androidx.room.compiler.processing.XTypeParameterElement;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import dagger.hilt.android.processor.internal.AndroidClassNames;
import dagger.hilt.processor.internal.Processors;
import java.io.IOException;
import javax.lang.model.element.Modifier;

/** Generates an Hilt Service class for the @AndroidEntryPoint annotated class. */
public final class ServiceGenerator {
  private final XProcessingEnv env;
  private final AndroidEntryPointMetadata metadata;
  private final ClassName generatedClassName;

  public ServiceGenerator(XProcessingEnv env, AndroidEntryPointMetadata metadata) {
    this.env = env;
    this.metadata = metadata;
    generatedClassName = metadata.generatedClassName();
  }

  // @Generated("ServiceGenerator")
  // abstract class Hilt_$CLASS extends $BASE {
  //   ...
  // }
  public void generate() throws IOException {
    TypeSpec.Builder builder =
        TypeSpec.classBuilder(generatedClassName.simpleName())
            .superclass(metadata.baseClassName())
            .addModifiers(metadata.generatedClassModifiers())
            .addMethods(baseClassConstructors())
            .addMethod(onCreateMethod());

    JavaPoetExtKt.addOriginatingElement(builder, metadata.element());
    Generators.addGeneratedBaseClassJavadoc(builder, AndroidClassNames.ANDROID_ENTRY_POINT);
    Processors.addGeneratedAnnotation(builder, env, getClass());
    Generators.copyLintAnnotations(metadata.element(), builder);
    Generators.copySuppressAnnotations(metadata.element(), builder);

    metadata.baseElement().getTypeParameters().stream()
        .map(XTypeParameterElement::getTypeVariableName)
        .forEachOrdered(builder::addTypeVariable);

    Generators.addInjectionMethods(metadata, builder);

    Generators.addComponentOverride(metadata, builder);

    env.getFiler()
        .write(
            JavaFile.builder(generatedClassName.packageName(), builder.build()).build(),
            XFiler.Mode.Isolating);
  }

  private ImmutableList<MethodSpec> baseClassConstructors() {
    return metadata.baseElement().getConstructors().stream()
        .map(ServiceGenerator::toMethodSpec)
        .collect(toImmutableList());
  }

  private static MethodSpec toMethodSpec(XConstructorElement constructor) {
    ImmutableList<ParameterSpec> params =
        constructor.getParameters().stream()
            .map(ServiceGenerator::toParameterSpec)
            .collect(toImmutableList());

    return MethodSpec.constructorBuilder()
        .addParameters(params)
        .addStatement("super($L)", params.stream().map(p -> p.name).collect(joining(",")))
        .build();
  }

  private static ParameterSpec toParameterSpec(XExecutableParameterElement parameter) {
    return ParameterSpec.builder(parameter.getType().getTypeName(), getSimpleName(parameter))
        .build();
  }

  // @CallSuper
  // @Override
  // protected void onCreate() {
  //   inject();
  //   super.onCreate();
  // }
  private MethodSpec onCreateMethod() throws IOException {
    return MethodSpec.methodBuilder("onCreate")
        .addAnnotation(AndroidClassNames.CALL_SUPER)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addStatement("inject()")
        .addStatement("super.onCreate()")
        .build();
  }
}
