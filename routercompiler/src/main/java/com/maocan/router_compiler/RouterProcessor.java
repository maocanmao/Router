package com.maocan.router_compiler;

import com.google.auto.service.AutoService;
import com.maocan.router.annotation.RouterConstant;
import com.maocan.router.annotation.Router;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("com.maocan.router.annotation.Route")
@AutoService(Processor.class)
public class RouterProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Set<? extends TypeElement> elements = (Set<? extends TypeElement>) roundEnv
        .getElementsAnnotatedWith(Router.class);
    if (null == elements || elements.isEmpty()) {
      return true;
    }
    Set<TypeElement> typeElements = new HashSet<>();
    for (Element e : elements) {
        if(isElementsVailed(e)){
          typeElements.add((TypeElement) e);
        }
    }
    return true;
  }

  private boolean isElementsVailed(Element element) {
    if (isSubType(element, RouterConstant.ANDROID_ACTIVITY)) {
      return true;
    }
    return false;
  }

  private boolean isSubType(Element element,String typeString) {
    return processingEnv.getTypeUtils()
        .isSubtype(element.asType(), processingEnv.getElementUtils().getTypeElement(
            typeString).asType());
  }
}
