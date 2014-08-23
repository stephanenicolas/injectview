package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import com.github.stephanenicolas.afterburner.AfterBurner;
import java.lang.annotation.Annotation;
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
 *       <li>right before any constructor with a single argument of type Activity, Fragment, or View
 *       <li>static inner classes can only be processed if static
 *     </ul>
 * </ul>
 * </pre>
 * @author SNI
 */
@Slf4j
public class  InjectViewProcessor implements IClassTransformer {

  @Override
  public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
    boolean isActivity = isActivity(candidateClass);
    boolean isFragment = isFragment(candidateClass);
    boolean isSupportFragment = isSupportFragment(candidateClass);
    boolean isView = isView(candidateClass);

    boolean hasAfterBurner = checkIfAfterBurnerAlreadyActive(candidateClass);
    final List<CtField> views = getAllInjectedFieldsForAnnotation(candidateClass, InjectView.class);
    final List<CtField> fragments = getAllInjectedFieldsForAnnotation(candidateClass, InjectFragment.class);
    boolean hasViewsOrFragments = !(views.isEmpty() && fragments.isEmpty());
    boolean shouldTransform = !hasAfterBurner && (isActivity
        || isFragment
        || isSupportFragment
        || isView
        || hasViewsOrFragments);
    log.debug("Class " + candidateClass.getSimpleName() + " will get transformed: " + shouldTransform);
    return shouldTransform;
  }

  @Override
  public void applyTransformations(final CtClass classToTransform) throws JavassistBuildException {
    // Actually you must test if it exists, but it's just an example...
    log.debug("Analyzing " + classToTransform.getSimpleName());
    boolean isActivity = isActivity(classToTransform);
    boolean isFragment = isFragment(classToTransform);
    boolean isSupportFragment = isSupportFragment(classToTransform);
    boolean isView = isView(classToTransform);

    try {
      if (isActivity) {
        log.debug("Activity detected " + classToTransform.getSimpleName());
        injectStuffInActivity(classToTransform);
      } else if (isFragment || isSupportFragment) {
        log.debug("Fragment detected " + classToTransform.getSimpleName());
        injectStuffInFragment(classToTransform);
      } else if (isView) {
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

  private void injectStuffInActivity(final CtClass classToTransform) throws NotFoundException, ClassNotFoundException,
      CannotCompileException {
    int layoutId = getLayoutId(classToTransform);
    final List<CtField> views = getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
    final List<CtField> fragments = getAllInjectedFieldsForAnnotation(classToTransform, InjectFragment.class);
    if (views.isEmpty() && fragments.isEmpty()) {
      return;
    }
    log.debug("Injecting stuff in " + classToTransform.getSimpleName());
    CtMethod onCreateMethod = extractExistingMethod(classToTransform, "onCreate");
    if (onCreateMethod != null) {
      log.debug("Has onCreate method already");
      boolean isCallingSetContentView = checkIfMethodIsInvoked(classToTransform, onCreateMethod, "setContentView");

      log.debug("onCreate invokes setContentView: " + isCallingSetContentView);

      String insertionMethod = "onCreate";
      if (isCallingSetContentView) {
        layoutId = -1;
        insertionMethod = "setContentView";
      }
      InjectorEditor injectorEditor = new InjectorEditor(classToTransform, fragments, views, layoutId, insertionMethod);
      onCreateMethod.instrument(injectorEditor);
      if (!injectorEditor.isSuccessful) {
        throw new CannotCompileException("Transformation failed.");
      }
    } else {
      log.debug("Does not have onCreate method yet");
      String onCreateMethodFull = createOnCreateMethod(classToTransform, views, fragments, layoutId);
      classToTransform.addMethod(
          CtNewMethod.make(onCreateMethodFull,
              classToTransform));
      log.debug("Inserted " + onCreateMethodFull);
    }
    classToTransform.detach();
    injectStuffInActivity(classToTransform.getSuperclass());
  }

  private void injectStuffInFragment(final CtClass classToTransform) throws NotFoundException, ClassNotFoundException, CannotCompileException {
    final List<CtField> views = getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
    final List<CtField> fragments = getAllInjectedFieldsForAnnotation(classToTransform, InjectFragment.class);
    if (views.isEmpty() && fragments.isEmpty()) {
      return;
    }
    // create or complete onViewCreated
    CtMethod onViewCreatedMethod = extractExistingMethod(classToTransform, "onViewCreated");
    log.debug("onViewCreatedMethod : " + onViewCreatedMethod);
    if (onViewCreatedMethod != null) {
      InjectorEditor injectorEditor = new InjectorEditor(classToTransform, fragments, views, -1, "onViewCreated");
      onViewCreatedMethod.instrument(injectorEditor);
      if (!injectorEditor.isSuccessful) {
        throw new CannotCompileException("Transformation failed.");
      }
    } else {
      classToTransform.addMethod(CtNewMethod.make(createOnViewCreatedMethod(classToTransform, views, fragments), classToTransform));
    }
    // create or complete onDestroyView
    CtMethod onDestroyViewMethod = extractExistingMethod(classToTransform, "onDestroyView");
    log.debug("onDestroyView : " + onDestroyViewMethod);
    if (onDestroyViewMethod != null) {
      InjectorEditor injectorEditor = new InjectorEditor(classToTransform, fragments, views, -1, "onDestroyView");
      onDestroyViewMethod.instrument(injectorEditor);
      if (!injectorEditor.isSuccessful) {
        throw new CannotCompileException("Transformation failed.");
      }
    } else {
      classToTransform.addMethod(CtNewMethod.make(createOnDestroyViewMethod(classToTransform, views), classToTransform));
    }
    classToTransform.detach();
    injectStuffInFragment(classToTransform.getSuperclass());
  }

  private void injectStuffInView(final CtClass classToTransform) throws NotFoundException, ClassNotFoundException, CannotCompileException {
    final List<CtField> views = getAllInjectedFieldsForAnnotation(classToTransform, InjectView.class);
    if (views.isEmpty()) {
      return;
    }
    CtMethod onFinishInflate = extractExistingMethod(classToTransform, "onFinishInflate");
    log.debug("onFinishInflateMethod : " + onFinishInflate);
    if (onFinishInflate != null) {
      InjectorEditor injectorEditor = new InjectorEditor(classToTransform, new ArrayList<CtField>(), views, -1, "onFinishInflate");
      onFinishInflate.instrument(injectorEditor);
      if (!injectorEditor.isSuccessful) {
        throw new CannotCompileException("Transformation failed.");
      }
    } else {
      classToTransform.addMethod(CtNewMethod.make(createOnFinishInflateMethod(classToTransform, views), classToTransform));
    }
    classToTransform.detach();
    injectStuffInView(classToTransform.getSuperclass());
  }

  private void injectStuffInClass(final CtClass clazz) throws NotFoundException, ClassNotFoundException, CannotCompileException {
    final List<CtField> views = getAllInjectedFieldsForAnnotation(clazz, InjectView.class);
    final List<CtField> fragments = getAllInjectedFieldsForAnnotation(clazz, InjectFragment.class);
    if (views.isEmpty() && fragments.isEmpty()) {
      return;
    }
    // create or complete onViewCreated
    List<CtConstructor> constructorList = extractExistingConstructors(clazz);
    if ( constructorList != null && !constructorList.isEmpty() ) {
      log.debug("constructor : " + constructorList.toString());
      for (CtConstructor constructor : constructorList) {
        constructor.insertBeforeBody(createInjectedBodyWithParam(clazz, constructor.getParameterTypes()[0],views, fragments, -1));
      }
    } else {
      log.warn("No suitable constructor was found in class {}. Add a constructor with a single argument : Activity, Fragment or View. Don't use non static inner classes.", clazz.getName());
    }
    clazz.detach();
    injectStuffInFragment(clazz.getSuperclass());
  }

  private boolean checkIfMethodIsInvoked(final CtClass clazz, CtMethod withinMethod, String invokedMEthod) throws CannotCompileException {
    DetectMethodCallEditor dectectSetContentViewEditor = new DetectMethodCallEditor(clazz, invokedMEthod);
    withinMethod.instrument(dectectSetContentViewEditor);
    boolean isCallingSetContentView = dectectSetContentViewEditor.isCallingMethod();
    return isCallingSetContentView;
  }

  private String createOnCreateMethod(CtClass clazz, List<CtField> views, List<CtField> fragments, int layoutId) throws ClassNotFoundException, NotFoundException {
    return "public void onCreate(android.os.Bundle savedInstanceState) { \n" + "super.onCreate(savedInstanceState);\n" + createInjectedBody(clazz, views, fragments, layoutId) + "}";
  }

  private String createOnViewCreatedMethod(CtClass clazz, List<CtField> views, List<CtField> fragments) throws ClassNotFoundException, NotFoundException {
    return "public void onViewCreated(android.view.View view, android.os.Bundle savedInstanceState) { \n" + "super.onViewCreated(view, savedInstanceState);\n"
        + createInjectedBody(clazz, views, fragments, -1) + "}";
  }

  private String createOnFinishInflateMethod(CtClass clazz, List<CtField> views) throws ClassNotFoundException, NotFoundException {
    return "public void onFinishInflate() { \n" + "super.onFinishInflate();\n" + createInjectedBody(clazz, views) + "}";
  }

  private String createOnDestroyViewMethod(CtClass clazz, List<CtField> views) {
    return "public void onDestroyView() { \n" + "super.onDestroyView();\n" + destroyViewStatements(views) + "}";
  }

  private CtMethod extractExistingMethod(final CtClass classToTransform, String methodName) {
    try {
      return classToTransform.getDeclaredMethod(methodName);
    } catch (Exception e) {
      return null;
    }
  }

  private List<CtConstructor> extractExistingConstructors(final CtClass classToTransform) {
    try {
      List<CtConstructor> constructors = new ArrayList<CtConstructor>();
      CtConstructor[] declaredConstructors = classToTransform.getDeclaredConstructors();
      for (CtConstructor constructor : declaredConstructors) {
        CtClass[] paramClasses = constructor.getParameterTypes();
        if( paramClasses.length == 1 ) {
          if( paramClasses[0].subclassOf(ClassPool.getDefault().get(View.class.getName()))) {
            constructors.add(constructor);
          }
          if( paramClasses[0].subclassOf(ClassPool.getDefault().get(Activity.class.getName()))) {
            constructors.add(constructor);
          }
          if( paramClasses[0].subclassOf(ClassPool.getDefault().get(Fragment.class.getName()))) {
            constructors.add(constructor);
          }

          try {
            Class<?> supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
            if( paramClasses[0].subclassOf(ClassPool.getDefault().get(supportFragmentClass.getName()))) {
              constructors.add(constructor);
            }
          } catch (Exception e) {
            //nothing to do, support is not present
          }
        }
      }
      return constructors;
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

  private boolean checkIfAfterBurnerAlreadyActive(final CtClass classToTransform) {
    try {
      classToTransform.getDeclaredField("afterBurnerActive");
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void markAfterBurnerActiveInClass(final CtClass classToTransform) throws CannotCompileException {
    classToTransform.addField(new CtField(CtClass.booleanType, "afterBurnerActive", classToTransform));
  }

  private String injectContentView(int layoutId) {
    return "setContentView(" + layoutId + ");\n";
  }

  private String injectFragmentStatements(List<CtField> fragments, String root, boolean useChildFragmentManager) throws ClassNotFoundException, NotFoundException {
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

      boolean isUsingSupport = !fragmentType.subclassOf(ClassPool.getDefault().get(Fragment.class.getName()));

      String getFragmentManagerString;
      if (useChildFragmentManager) {
        getFragmentManagerString = "getChildFragmentManager()";
      } else if (isUsingSupport)
        getFragmentManagerString = "getSupportFragmentManager()";
      else
        getFragmentManagerString = "getFragmentManager()";
      String getFragmentString = isUsingId ? ".findFragmentById(" + id + ")" : ".findFragmentByTag(\"" + tag + "\")";
      buffer.append(root + "." + getFragmentManagerString + getFragmentString + ";\n");
    }
    return buffer.toString();
  }

  private String injectViewStatements(List<CtField> viewsToInject, CtClass targetClazz) throws ClassNotFoundException, NotFoundException {
    boolean isActivity = isActivity(targetClazz);
    boolean isFragment = isFragment(targetClazz);
    boolean isSupportFragment = isSupportFragment(targetClazz);
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
        findViewString = isUsingId ? "findViewById(" + id + ")" : "getWindow().getDecorView().findViewWithTag(\"" + tag + "\")";
      } else if (isView){
        root = "this";
        findViewString = isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      } else {
        root = "$1";
        findViewString = isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      }
      buffer.append(root + "." + findViewString + ";\n");
    }
    log.debug("Inserted :" + buffer.toString());
    return buffer.toString();
  }

  private String injectViewStatementsForParam(List<CtField> viewsToInject, CtClass targetClazz) throws ClassNotFoundException, NotFoundException {
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
        root = "$1";
        findViewString = isUsingId ? "findViewById(" + id + ")" : "getWindow().getDecorView().findViewWithTag(\"" + tag + "\")";
      } else if (isView){
        root = "$1";
        findViewString = isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
      } else {
        root = "$1.getView()";
        findViewString = isUsingId ? "findViewById(" + id + ")" : "findViewWithTag(\"" + tag + "\")";
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

  private List<CtField> getAllInjectedFieldsForAnnotation(CtClass clazz, Class<? extends Annotation> annotationClazz) {
    List<CtField> result = new ArrayList<CtField>();
    CtField[] allFields = clazz.getDeclaredFields();
    log.debug("Scanning fields in " + clazz.getName());
    for (CtField field : allFields) {
      log.debug("Discovered field " + field.getName());
      if (field.hasAnnotation(annotationClazz)) {
        log.debug("Field " + field.getName() + " has annotation " + annotationClazz.getSimpleName());
        result.add(field);
      }
    }
    return result;
  }

  private String createInjectedBody(CtClass clazz, List<CtField> views) throws ClassNotFoundException, NotFoundException {
    return createInjectedBody(clazz, views, new ArrayList<CtField>(), -1);
  }

  private String createInjectedBody(CtClass clazz, List<CtField> views, List<CtField> fragments, int layoutId) throws ClassNotFoundException, NotFoundException {
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
      if (isActivity || isView) {
        buffer.append(injectViewStatements(views, clazz));
      } else if (isFragment || isSupportFragment ) {
        buffer.append(injectViewStatements(views, clazz));
      }
    }

    if (!fragments.isEmpty()) {
      if (isActivity) {
        buffer.append(injectFragmentStatements(fragments, "this", false));
      } else if (isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragments, "this", true));
      } else if( hasViewsOrFragments ) {
        buffer.append(injectFragmentStatements(fragments, "$1", true));
      }
    }

    String string = buffer.toString();
    return string;
  }

  private String createInjectedBodyWithParam(CtClass clazz, CtClass paramClass, List<CtField> views, List<CtField> fragments, int layoutId) throws ClassNotFoundException, NotFoundException {
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
      buffer.append(injectViewStatementsForParam(views, paramClass));
    }

    if (!fragments.isEmpty()) {
      if (isActivity) {
        buffer.append(injectFragmentStatements(fragments, "$1", false));
      } else if (isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragments, "$1", true));
      }
    }
    String string = buffer.toString();
    return string;
  }

  private boolean isActivity(CtClass clazz) {
    try {
      return isSubClass(clazz, Activity.class);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isFragment(CtClass clazz) {
    try {
      return isSubClass(clazz, Fragment.class);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isSupportFragment(CtClass clazz) {
    try {
      Class<?> supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
      return isSubClass(clazz, supportFragmentClass);
    } catch (Exception e) {
      //can happen
      return false;
    }
  }

  private boolean isView(CtClass clazz) {
    try {
      return isSubClass(clazz, View.class);
    } catch (NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isSubClass(CtClass clazz, Class<?> superClass ) throws NotFoundException {
    return clazz.subclassOf(clazz.getClassPool().get(superClass.getName()));
  }

  private final class InjectorEditor extends ExprEditor {
    private final CtClass classToTransform;
    private final List<CtField> fragments;
    private final List<CtField> views;
    private final int layoutId;
    private String insertionMethod;
    private boolean isSuccessful = true;

    private InjectorEditor(CtClass classToTransform, List<CtField> fragments, List<CtField> views, int layoutId, String insertionMethod) {
      this.classToTransform = classToTransform;
      this.fragments = fragments;
      this.views = views;
      this.layoutId = layoutId;
      this.insertionMethod = insertionMethod;
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
      try {
        log.debug("method call " + m.getMethodName());
        if (m.getMethodName().equals(insertionMethod)) {
          log.debug("insertion method " + m.getMethodName());

          String string = "$_ = $proceed($$);\n" +
              createInjectedBody(m.getEnclosingClass(), views, fragments, layoutId);
          log.debug("Injected : " + string);

          m.replace(string);
          // mark class to avoid duplicate
          markAfterBurnerActiveInClass(classToTransform);
          log.info("Class {} has been enhanced.", classToTransform.getName());
        }
      } catch (Throwable e) {
        isSuccessful = false;
        log.error("A problem occured during transformation",e);
        throw new CannotCompileException("A problem occured during transformation", e);
      }
    }

  }

  private final class DetectMethodCallEditor extends ExprEditor {

    private String methodName;
    private boolean isCallingMethod;

    private DetectMethodCallEditor(CtClass classToTransform, String methodName) {
      this.methodName = methodName;
    }

    @Override
    public void edit(MethodCall m) throws CannotCompileException {
      if (m.getMethodName().equals(methodName)) {
        this.isCallingMethod = true;
      }
    }

    public boolean isCallingMethod() {
      return isCallingMethod;
    }

  }
}
