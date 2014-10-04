package com.github.stephanenicolas.injectview;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.morpheus.commons.CtClassFilter;
import com.github.stephanenicolas.morpheus.commons.JavassistUtils;
import com.github.stephanenicolas.morpheus.commons.NullableUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import lombok.extern.slf4j.Slf4j;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.findValidParamIndex;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.getAllInjectedFieldsForAnnotation;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isFragment;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isSupportFragment;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;
import static java.lang.String.format;

/**
 * Will inject all fields and fragments from XML.
 *
 * <pre>
 * <ul>
 *   <li>for activities :
 *     <ul>
 *       <li>if they use @ContentView : right after super.onCreate
 *       <li>if they don't use @ContentView : right after setContentView invocation in onCreate
 *       <li>it doesn't matter if you supply your own version of onCreate or setContenView or not.
 *     </ul>
 *   <li>for fragments :
 *     <ul>
 *       <li>right after onViewCreated
 *       <li>views are destroyed right after onViewDestroyed
 *       <li>fragments must return a non null view for onViewCreated to be used by Android..
 *     </ul>
 *   <li>for views :
 *     <ul>
 *       <li>right after onFinishInflate
 *       <li>onFinishInflate is called automatically by Android when inflating a view from XML
 *       <li>onFinishInflate must be called manually in constructors of views with a single context
 * argument. You should invoke it after inflating your layout manually.
 *     </ul>
 *   <li>for other classes (namely MVP presenters and view holder design patterns) :
 *     <ul>
 *       <li>right before any constructor with a single argument of type Activity, Fragment, or
 * View
 *       <li>static inner classes can only be processed if static
 *     </ul>
 * </ul>
 * </pre>
 *
 * @author SNI
 */
@Slf4j
public class InjectViewProcessor implements IClassTransformer {

  private CtClassFilter injectViewfilter = new InjectViewCtClassFilter();
  private AfterBurner afterBurner = new AfterBurner();

  @Override
  public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
    try {
      final List<CtField> views =
          getAllInjectedFieldsForAnnotation(candidateClass, InjectView.class);
      final List<CtField> fragments =
          getAllInjectedFieldsForAnnotation(candidateClass, InjectFragment.class);
      boolean hasViewsOrFragments = !(views.isEmpty() && fragments.isEmpty());
      boolean shouldTransform = hasViewsOrFragments;
      log.debug(
          "Class " + candidateClass.getSimpleName() + " will get transformed: " + shouldTransform);
      return shouldTransform;
    } catch (Exception e) {
      String message = format("Error while filtering class %s", candidateClass.getName());
      log.debug(message, e);
      throw new JavassistBuildException(message, e);
    }
  }

  @Override
  public void applyTransformations(final CtClass classToTransform) throws JavassistBuildException {
    // Actually you must test if it exists, but it's just an example...
    log.debug("Analyzing " + classToTransform.getSimpleName());

    try {
      List<CtField> views = getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
      List<CtField> fragments =
          getAllInjectedFieldsForAnnotation(classToTransform, InjectFragment.class);

      if (isActivity(classToTransform)) {
        log.debug("Activity detected " + classToTransform.getSimpleName());
        injectStuffInActivity(classToTransform, views, fragments);
      } else if (isFragment(classToTransform) || isSupportFragment(classToTransform)) {
        log.debug("Fragment detected " + classToTransform.getSimpleName());
        injectStuffInFragment(classToTransform, views, fragments);
      } else if (isView(classToTransform)) {
        log.debug("View detected " + classToTransform.getSimpleName());
        injectStuffInView(classToTransform, views);
      } else {
        log.debug("Other class detected " + classToTransform.getSimpleName());
        // in other classes (like view holders)
        injectStuffInClass(classToTransform, views, fragments);
      }
      log.debug("Class successfully transformed: " + classToTransform.getSimpleName());
    } catch (Throwable e) {
      log.error("Impossible to transform class." + classToTransform.getName(), e);
      new JavassistBuildException(e);
    }
  }

  private void injectStuffInActivity(final CtClass classToTransform, List<CtField> views,
      List<CtField> fragments)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException, JavassistBuildException {
    log.debug("Injecting stuff in " + classToTransform.getSimpleName());
    CtMethod onCreateMethod = afterBurner.extractExistingMethod(classToTransform, "onCreate");
    if (onCreateMethod != null) {
      log.debug("Has onCreate method already");
      boolean isCallingSetContentView =
          afterBurner.checkIfMethodIsInvoked(onCreateMethod, "setContentView");

      log.debug("onCreate invokes setContentView: " + isCallingSetContentView);

      String insertionMethod = "onCreate";
      int layoutId = -1;
      if (isCallingSetContentView) {
        insertionMethod = "setContentView";
      } else {
        layoutId = getLayoutId(classToTransform);
      }
      InsertableMethodBuilder builder = new InsertableMethodBuilder(afterBurner);

      builder.insertIntoClass(classToTransform)
          .inMethodIfExists("onCreate")
          .afterACallTo(insertionMethod)
          .withBody(createInjectedBody(classToTransform, views, fragments,
              layoutId)).elseCreateMethodIfNotExists("") //not used, we are sure the method exists
          .doIt();
    } else {
      log.debug("Does not have onCreate method yet");
      int layoutId = getLayoutId(classToTransform);
      String onCreateMethodFull =
          createOnCreateMethod(classToTransform, views, fragments, layoutId);
      classToTransform.addMethod(CtNewMethod.make(onCreateMethodFull, classToTransform));
      log.debug("Inserted " + onCreateMethodFull);
    }
    classToTransform.detach();
  }

  private void injectStuffInFragment(final CtClass classToTransform, List<CtField> views,
      List<CtField> fragments)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException, JavassistBuildException {
    afterBurner.afterOverrideMethod(classToTransform, "onViewCreated",
        createInjectedBody(classToTransform, views, fragments, -1));

    afterBurner.afterOverrideMethod(classToTransform, "onDestroyView",
        destroyViewStatements(views));

    classToTransform.detach();
  }

  private void injectStuffInView(final CtClass classToTransform, List<CtField> views)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException, JavassistBuildException {
    if (views.isEmpty()) {
      return;
    }

    afterBurner.afterOverrideMethod(classToTransform, "onFinishInflate",
        createInjectedBody(classToTransform, views, new ArrayList<CtField>(), -1));
    classToTransform.detach();
  }

  private void injectStuffInClass(final CtClass clazz, List<CtField> views, List<CtField> fragments)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException {

    // create or complete onViewCreated
    List<CtConstructor> constructorList =
        JavassistUtils.extractValidConstructors(clazz, injectViewfilter);
    if (constructorList != null && !constructorList.isEmpty()) {
      log.debug("constructor : " + constructorList.toString());
      for (CtConstructor constructor : constructorList) {
        int indexValidParam =
            findValidParamIndex(constructor.getParameterTypes(), injectViewfilter);
        //indexValidParam is > 0 at this stage
        constructor.insertBeforeBody(
            createInjectedBodyWithParam(clazz, constructor.getParameterTypes(), indexValidParam,
                views, fragments, -1));
      }
    } else {
      log.warn(
          "No suitable constructor was found in class {}. Add a constructor with a single argument : Activity, Fragment or View. Don't use non static inner classes.",
          clazz.getName());
    }
    clazz.detach();
  }

  private String createOnCreateMethod(CtClass clazz, List<CtField> views, List<CtField> fragments,
      int layoutId) throws ClassNotFoundException, NotFoundException, JavassistBuildException {
    return "public void onCreate(android.os.Bundle savedInstanceState) { \n"
        + "super.onCreate(savedInstanceState);\n"
        + createInjectedBody(clazz, views, fragments, layoutId)
        + "}";
  }

  private int getLayoutId(final CtClass classToTransform) {
    try {
      Object annotation = classToTransform.getAnnotation(ContentView.class);
      Class clazz = annotation.getClass();
      Method method = clazz.getMethod("value");
      return (Integer) method.invoke(annotation);
    } catch (Exception e) {
      return -1;
    }
  }

  private String injectContentView(int layoutId) {
    return "setContentView(" + layoutId + ");\n";
  }

  private String injectFragmentStatements(List<CtField> fragments, String root) throws ClassNotFoundException, NotFoundException {
    StringBuffer buffer = new StringBuffer();
    for (CtField field : fragments) {
      Object annotation = field.getAnnotation(InjectFragment.class);
      //must be accessed by introspection as I get a Proxy during tests.
      //this proxy comes from Robolectric
      Class annotionClass = annotation.getClass();

      //workaround for robolectric
      //https://github.com/robolectric/robolectric/pull/1240
      int id = 0;
      String tag = "";
      try {
        Method method = annotionClass.getMethod("value");
        id = (Integer) method.invoke(annotation);
        method = annotionClass.getMethod("tag");
        tag = (String) method.invoke(annotation);
      } catch (Exception e) {
        throw new RuntimeException("How can we get here ?");
      }
      boolean isUsingId = id != -1;
      buffer.append(field.getName());
      buffer.append(" = ");
      buffer.append('(');
      CtClass fragmentType = field.getType();
      buffer.append(fragmentType.getName());
      buffer.append(')');

      boolean isUsingSupport = isSupportFragment(fragmentType);

      String getFragmentManagerString;
      if (isUsingSupport) {
        getFragmentManagerString = "getSupportFragmentManager()";
      } else {
        getFragmentManagerString = "getFragmentManager()";
      }
      String getFragmentString =
          isUsingId ? ".findFragmentById(" + id + ")" : ".findFragmentByTag(\"" + tag + "\")";
      buffer.append(root + "." + getFragmentManagerString + getFragmentString + ";\n");
      buffer.append(checkNullable(field));
    }
    return buffer.toString();
  }

  private String injectViewStatements(List<CtField> viewsToInject, CtClass targetClazz)
      throws ClassNotFoundException, NotFoundException {
    boolean isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    StringBuffer buffer = new StringBuffer();
    for (CtField field : viewsToInject) {
      Object annotation = field.getAnnotation(InjectView.class);
      //must be accessed by introspection as I get a Proxy during tests.
      //this proxy comes from Robolectric
      Class annotionClass = annotation.getClass();

      //workaround for robolectric
      //https://github.com/robolectric/robolectric/pull/1240
      int id = 0;
      String tag = "";
      try {
        Method method = annotionClass.getMethod("value");
        id = (Integer) method.invoke(annotation);
        method = annotionClass.getMethod("tag");
        tag = (String) method.invoke(annotation);
      } catch (Exception e) {
        throw new RuntimeException("How can we get here ?");
      }
      boolean isUsingId = id != -1;

      buffer.append(field.getName());
      buffer.append(" = ");
      buffer.append('(');
      buffer.append(field.getType().getName());
      buffer.append(')');

      String root = "";
      String findViewString = "";
      if (isActivity) {
        //in on create
        root = "this";
        findViewString = isUsingId ? "findViewById(" + id + ")"
            : "getWindow().getDecorView().findViewWithTag(\"" + tag + "\")";
      } else if (isView) {
        root = "this";
        findViewString =
            isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      } else {
        root = "$1";
        findViewString =
            isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      }
      buffer.append(root + "." + findViewString + ";\n");
      buffer.append(checkNullable(field));
    }
    log.debug("Inserted :" + buffer.toString());
    return buffer.toString();
  }

  private String injectViewStatementsForParam(List<CtField> viewsToInject, CtClass[] paramClasses,
      int indexParam) throws ClassNotFoundException, NotFoundException {
    CtClass targetClazz = paramClasses[indexParam];
    boolean isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    StringBuffer buffer = new StringBuffer();
    for (CtField field : viewsToInject) {
      Object annotation = field.getAnnotation(InjectView.class);
      //must be accessed by introspection as I get a Proxy during tests.
      //this proxy comes from Robolectric
      Class annotionClass = annotation.getClass();

      //workaround for robolectric
      //https://github.com/robolectric/robolectric/pull/1240
      int id = 0;
      String tag = "";
      try {
        Method method = annotionClass.getMethod("value");
        id = (Integer) method.invoke(annotation);
        method = annotionClass.getMethod("tag");
        tag = (String) method.invoke(annotation);
      } catch (Exception e) {
        throw new RuntimeException("How can we get here ?");
      }
      boolean isUsingId = id != -1;

      buffer.append(field.getName());
      buffer.append(" = ");
      buffer.append('(');
      buffer.append(field.getType().getName());
      buffer.append(')');

      String root = "";
      String findViewString = "";
      if (isActivity) {
        //in on create
        root = "$" + (1 + indexParam);
        findViewString = isUsingId ? "findViewById(" + id + ")"
            : "getWindow().getDecorView().findViewWithTag(\"" + tag + "\")";
      } else if (isView) {
        root = "$" + (1 + indexParam);
        findViewString =
            isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      } else {
        root = "$" + (1 + indexParam) + ".getView()";
        findViewString =
            isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      }
      buffer.append(root + "." + findViewString + ";\n");
      buffer.append(checkNullable(field));
    }
    log.debug("Inserted :" + buffer.toString());
    return buffer.toString();
  }

  private String destroyViewStatements(List<CtField> viewsToInject) {
    StringBuffer buffer = new StringBuffer();
    for (CtField field : viewsToInject) {
      buffer.append(field.getName());
      buffer.append(" = null;\n");
    }
    return buffer.toString();
  }

  private String createInjectedBody(CtClass clazz, List<CtField> views, List<CtField> fragments,
      int layoutId) throws ClassNotFoundException, NotFoundException, JavassistBuildException {
    boolean isActivity = isActivity(clazz);
    boolean isFragment = isFragment(clazz);
    boolean isSupportFragment = isSupportFragment(clazz);
    boolean isView = isView(clazz);

    StringBuffer buffer = new StringBuffer();
    String message = String.format("Class %s has been enhanced.", clazz.getName());
    buffer.append("android.util.Log.d(\"RoboGuice post-processor\",\"" + message + "\");\n");

    if (layoutId != -1) {
      buffer.append(injectContentView(layoutId));
    }
    if (!views.isEmpty()) {
      buffer.append(injectViewStatements(views, clazz));
    }

    if (!fragments.isEmpty()) {
      if( isView) {
        throw new JavassistBuildException("Impossible to use InjectFragments in views. View: " + clazz.getName());
      }
      else if (isActivity || isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragments, "this"));
      }
    }

    return buffer.toString();
  }

  private String createInjectedBodyWithParam(CtClass clazz, CtClass[] paramClasses, int paramIndex,
      List<CtField> views, List<CtField> fragments, int layoutId)
      throws ClassNotFoundException, NotFoundException {
    CtClass paramClass = paramClasses[paramIndex];
    boolean isActivity = isActivity(paramClass);
    boolean isFragment = isFragment(paramClass);
    boolean isSupportFragment = isSupportFragment(paramClass);

    StringBuffer buffer = new StringBuffer();
    String message = String.format("Class %s has been enhanced.", clazz.getName());
    buffer.append("android.util.Log.d(\"RoboGuice post-processor\",\"" + message + "\");\n");

    if (layoutId != -1) {
      buffer.append(injectContentView(layoutId));
    }
    if (!views.isEmpty()) {
      buffer.append(injectViewStatementsForParam(views, paramClasses, paramIndex));
    }

    if (!fragments.isEmpty()) {
      if (isActivity) {
        buffer.append(injectFragmentStatements(fragments, "$" + (1 + paramIndex)));
      } else if (isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragments, "$" + (1 + paramIndex)));
      }
    }
    String string = buffer.toString();
    return string;
  }

  private String checkNullable(CtField field) throws NotFoundException {
    String checkNullable = "";
    String fieldName = field.getName();
    try {
      log.debug("Using pool in Nullable " + System.identityHashCode(field.getType().getClassPool()));
      log.debug("Using pool in Nullable " + field.getType().getClassPool().get("android.support.annotation.Nullable"));
    } catch (NotFoundException e) {
      e.printStackTrace();
    }

    if (NullableUtils.isNotNullable(field)) {
      checkNullable = "if ("
          + fieldName
          + " == null) {\n  throw new RuntimeException(\"Field "
          + fieldName
          + " is null and is not @Nullable.\"); \n}\n";
    }
    return checkNullable;
  }

  private static class InjectViewCtClassFilter implements CtClassFilter {
    @Override public boolean isValid(CtClass clazz) throws NotFoundException {
      return isActivity(clazz) || isView(clazz) || isFragment(clazz) || isSupportFragment(clazz);
    }
  }
}
