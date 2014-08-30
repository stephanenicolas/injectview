package com.github.stephanenicolas.injectview;

import android.app.Fragment;
import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.morpheus.commons.CtClassFilter;
import com.github.stephanenicolas.morpheus.commons.JavassistUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.build.IClassTransformer;
import javassist.build.JavassistBuildException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
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
      boolean shouldTransform = injectViewfilter.isValid(candidateClass) || hasViewsOrFragments;
      log.debug(
          "Class " + candidateClass.getSimpleName() + " will get transformed: " + shouldTransform);
      return shouldTransform;
    } catch (NotFoundException e) {
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
      if (isActivity(classToTransform)) {
        log.debug("Activity detected " + classToTransform.getSimpleName());
        injectStuffInActivity(classToTransform);
      } else if (isFragment(classToTransform) || isSupportFragment(classToTransform)) {
        log.debug("Fragment detected " + classToTransform.getSimpleName());
        injectStuffInFragment(classToTransform);
      } else if (isView(classToTransform)) {
        log.debug("View detected " + classToTransform.getSimpleName());
        injectStuffInView(classToTransform);
      } else {
        log.debug("Other class detected " + classToTransform.getSimpleName());
        // in other classes (like view holders)
        injectStuffInClass(classToTransform);
      }
      log.debug("Class successfully transformed: " + classToTransform.getSimpleName());
    } catch (Throwable e) {
      log.error("Impossible to transform class." + classToTransform.getName(), e);
      new JavassistBuildException(e);
    }
  }

  private void injectStuffInActivity(final CtClass classToTransform)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException {
    int layoutId = getLayoutId(classToTransform);
    final List<CtField> views =
        getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
    final List<CtField> fragments =
        getAllInjectedFieldsForAnnotation(classToTransform, InjectFragment.class);
    if (views.isEmpty() && fragments.isEmpty()) {
      return;
    }
    log.debug("Injecting stuff in " + classToTransform.getSimpleName());
    CtMethod onCreateMethod = extractExistingMethod(classToTransform, "onCreate");
    if (onCreateMethod != null) {
      log.debug("Has onCreate method already");
      boolean isCallingSetContentView =
          checkIfMethodIsInvoked(onCreateMethod, "setContentView");

      log.debug("onCreate invokes setContentView: " + isCallingSetContentView);

      String insertionMethod = "onCreate";
      if (isCallingSetContentView) {
        layoutId = -1;
        insertionMethod = "setContentView";
      }
      InsertableMethodBuilder builder = new InsertableMethodBuilder(afterBurner);

      builder.insertIntoClass(classToTransform).inMethodIfExists("onCreate")
        .afterACallTo(insertionMethod)
      .withBody(createInjectedBody(classToTransform, views, fragments, layoutId))
      .elseCreateMethodIfNotExists("") //not used, we are sure the method exists
      .doIt();
    } else {
      log.debug("Does not have onCreate method yet");
      String onCreateMethodFull =
          createOnCreateMethod(classToTransform, views, fragments, layoutId);
      classToTransform.addMethod(CtNewMethod.make(onCreateMethodFull, classToTransform));
      log.debug("Inserted " + onCreateMethodFull);
    }
    classToTransform.detach();
    injectStuffInActivity(classToTransform.getSuperclass());
  }

  private void injectStuffInFragment(final CtClass classToTransform)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException {
    final List<CtField> views =
        getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
    final List<CtField> fragments =
        getAllInjectedFieldsForAnnotation(classToTransform, InjectFragment.class);
    if (views.isEmpty() && fragments.isEmpty()) {
      return;
    }
    afterBurner.afterOverrideMethod(classToTransform, "onViewCreated",
        createInjectedBody(classToTransform, views, fragments, -1));

    afterBurner.afterOverrideMethod(classToTransform, "onDestroyView",
        destroyViewStatements(views));

    classToTransform.detach();
    injectStuffInFragment(classToTransform.getSuperclass());
  }

  private void injectStuffInView(final CtClass classToTransform)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException {
    final List<CtField> views =
        getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
    if (views.isEmpty()) {
      return;
    }

    afterBurner.afterOverrideMethod(classToTransform, "onFinishInflate",
        createInjectedBody(classToTransform, views, new ArrayList<CtField>(), -1));
    classToTransform.detach();
    injectStuffInView(classToTransform.getSuperclass());
  }

  private void injectStuffInClass(final CtClass clazz)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException {
    final List<CtField> views = getAllInjectedFieldsForAnnotation(clazz, InjectView.class);
    final List<CtField> fragments = getAllInjectedFieldsForAnnotation(clazz, InjectFragment.class);

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
    injectStuffInFragment(clazz.getSuperclass());
  }

  private boolean checkIfMethodIsInvoked(CtMethod withinMethod,
      String invokedMethod) throws CannotCompileException {
    return new DetectMethodCallEditor(withinMethod, invokedMethod).checkIfisCallingMethod();
  }

  private String createOnCreateMethod(CtClass clazz, List<CtField> views, List<CtField> fragments,
      int layoutId) throws ClassNotFoundException, NotFoundException {
    return "public void onCreate(android.os.Bundle savedInstanceState) { \n"
        + "super.onCreate(savedInstanceState);\n"
        + createInjectedBody(clazz, views, fragments, layoutId)
        + "}";
  }

  private CtMethod extractExistingMethod(final CtClass classToTransform, String methodName) {
    try {
      return classToTransform.getDeclaredMethod(methodName);
    } catch (Exception e) {
      return null;
    }
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

  private String injectFragmentStatements(List<CtField> fragments, String root,
      boolean useChildFragmentManager) throws ClassNotFoundException, NotFoundException {
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

      boolean isUsingSupport =
          !fragmentType.subclassOf(ClassPool.getDefault().get(Fragment.class.getName()));

      String getFragmentManagerString;
      if (useChildFragmentManager) {
        getFragmentManagerString = "getChildFragmentManager()";
      } else if (isUsingSupport) {
        getFragmentManagerString = "getSupportFragmentManager()";
      } else {
        getFragmentManagerString = "getFragmentManager()";
      }
      String getFragmentString =
          isUsingId ? ".findFragmentById(" + id + ")" : ".findFragmentByTag(\"" + tag + "\")";
      buffer.append(root + "." + getFragmentManagerString + getFragmentString + ";\n");
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
      int layoutId) throws ClassNotFoundException, NotFoundException {
    boolean isActivity = isActivity(clazz);
    boolean isFragment = isFragment(clazz);
    boolean isSupportFragment = isSupportFragment(clazz);
    boolean isView = isView(clazz);
    boolean hasViewsOrFragments = !(views.isEmpty() && fragments.isEmpty());

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
      if (isActivity) {
        buffer.append(injectFragmentStatements(fragments, "this", false));
      } else if (isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragments, "this", true));
      } else if (hasViewsOrFragments) {
        buffer.append(injectFragmentStatements(fragments, "$1", true));
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
        buffer.append(injectFragmentStatements(fragments, "$" + (1 + paramIndex), false));
      } else if (isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragments, "$" + (1 + paramIndex), true));
      }
    }
    String string = buffer.toString();
    return string;
  }

  private static class InjectViewCtClassFilter implements CtClassFilter {
    @Override public boolean isValid(CtClass clazz) throws NotFoundException {
      return isActivity(clazz) || isView(clazz) || isFragment(clazz) || isSupportFragment(clazz);
    }
  }

  private final class DetectMethodCallEditor extends ExprEditor {

    private CtMethod withinMethod;
    private String methodName;
    private boolean isCallingMethod;

    private DetectMethodCallEditor(CtMethod withinMethod, String methodName) {
      this.withinMethod = withinMethod;
      this.methodName = methodName;
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
      if (m.getMethodName().equals(methodName)) {
        this.isCallingMethod = true;
      }
    }

    public boolean checkIfisCallingMethod() throws CannotCompileException {
      withinMethod.instrument(this);
      return isCallingMethod;
    }
  }
}
