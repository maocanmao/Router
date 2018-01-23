package com.maocan.router_compiler;

import static com.maocan.router.annotation.RouterConstant.OPTION_MODULE_NAME;
import static com.maocan.router.annotation.RouterConstant.ROUTER_MAPPING;
import static com.maocan.router.annotation.RouterConstant.ROUTE_MAP_FULL_NAME;
import static com.maocan.router.annotation.RouterConstant.SUPPORT_ANNOTATION_TYPE;

import com.google.auto.service.AutoService;
import com.maocan.router.annotation.Router;
import com.maocan.router.annotation.RouterConstant;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

@SupportedAnnotationTypes(SUPPORT_ANNOTATION_TYPE)
@SupportedOptions(OPTION_MODULE_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {

  private String mModuleName;
  private Messager mMessager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    mModuleName = processingEnv.getOptions().get(OPTION_MODULE_NAME);
    mMessager = processingEnv.getMessager();
    mMessager.printMessage(Kind.NOTE, "Router init!");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<? extends TypeElement> elements = (Set<? extends TypeElement>) roundEnv
        .getElementsAnnotatedWith(Router.class);
    if (null == elements || elements.isEmpty()) {
      return true;
    }
    Set<TypeElement> typeElements = new HashSet<>();
    for (Element e : elements) {
      if (isElementsValid(e)) {
        typeElements.add((TypeElement) e);
      }
    }
    mMessager.printMessage(Kind.NOTE, "get elementTable: " + typeElements.toString());
    generateRouteCode(typeElements, mModuleName);
    return true;
  }

  private boolean isElementsValid(Element element) {
    if (isSubType(element, RouterConstant.ANDROID_ACTIVITY)) {
      return true;
    }
    return false;
  }

  private boolean isSubType(Element element, String typeString) {
    return processingEnv.getTypeUtils()
        .isSubtype(element.asType(), processingEnv.getElementUtils().getTypeElement(
            typeString).asType());
  }

  private void generateRouteCode(Set<TypeElement> typeElementSet, String moduleName) {
    // Map<String, Class<?>> map
    ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(HashMap.class),
        ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class),
            WildcardTypeName.subtypeOf(Object.class)));

    //override method 'mapping'
    ParameterSpec parameterSpec = ParameterSpec.builder(mapTypeName, "routerMap").build();
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(RouterConstant.MAPPING)
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(parameterSpec);

    for (TypeElement e : typeElementSet) {
      Router router = e.getAnnotation(Router.class);
      String path = router.path();
      methodBuilder.addStatement("routerMap.put($S,$T.class)", path, ClassName.get(e));
    }

    TypeElement interfaceType = processingEnv.getElementUtils().getTypeElement(ROUTE_MAP_FULL_NAME);

    mMessager.printMessage(Kind.NOTE, "interfaceType:" + interfaceType);

    TypeSpec type = TypeSpec.classBuilder(moduleName + ROUTER_MAPPING)
        .addSuperinterface(ClassName.get(interfaceType))
        .addModifiers(Modifier.PUBLIC)
        .addMethod(methodBuilder.build())
        .build();

    try {
      JavaFile.builder(RouterConstant.PACKAGE_NAME, type).build().writeTo(processingEnv.getFiler());
    } catch (IOException e) {
      e.printStackTrace();
    }
    mMessager.printMessage(Kind.NOTE, "Router finished");
  }
}
