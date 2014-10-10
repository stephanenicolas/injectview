package com.github.stephanenicolas.injectview;

import com.github.stephanenicolas.afterburner.AfterBurner;
import com.github.stephanenicolas.afterburner.InsertableMethodBuilder;
import com.github.stephanenicolas.afterburner.exception.AfterBurnerImpossibleException;
import com.github.stephanenicolas.injectview.binding.Binder;
import com.github.stephanenicolas.injectview.binding.Binding;
import com.github.stephanenicolas.injectview.binding.FragmentBinding;
import com.github.stephanenicolas.injectview.binding.ViewBinding;
import com.github.stephanenicolas.injectview.statement.DestroyViewStatementInFragment;
import com.github.stephanenicolas.injectview.statement.FindFragmentStatement;
import com.github.stephanenicolas.injectview.statement.FindViewStatementForParam;
import com.github.stephanenicolas.injectview.statement.FindViewStatementInActivity;
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
  private Binder binder = new Binder();

  @Override
  public boolean shouldTransform(CtClass candidateClass) throws JavassistBuildException {
    try {
      binder.extractAllBindings(candidateClass);
      final List<ViewBinding> viewBindings = binder.getViewBindings(candidateClass);
      final List<FragmentBinding> fragmentBindings = binder.getFragmentBindings(candidateClass);
      boolean hasViewsOrFragments = !(viewBindings.isEmpty() && fragmentBindings.isEmpty());
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
      final List<ViewBinding> viewBindings = binder.getViewBindings(classToTransform);
      final List<FragmentBinding> fragmentBindings = binder.getFragmentBindings(classToTransform);

      if (isActivity(classToTransform)) {
        log.debug("Activity detected " + classToTransform.getSimpleName());
        injectStuffInActivity(classToTransform, viewBindings, fragmentBindings);
      } else if (isFragment(classToTransform) || isSupportFragment(classToTransform)) {
        log.debug("Fragment detected " + classToTransform.getSimpleName());
        injectStuffInFragment(classToTransform, viewBindings, fragmentBindings);
      } else if (isView(classToTransform)) {
        log.debug("View detected " + classToTransform.getSimpleName());
        injectStuffInView(classToTransform, viewBindings);
      } else {
        log.debug("Other class detected " + classToTransform.getSimpleName());
        // in other classes (like view holders)
        injectStuffInClass(classToTransform, viewBindings, fragmentBindings);
      }
      log.debug("Class successfully transformed: " + classToTransform.getSimpleName());
    } catch (Throwable e) {
      log.error("Impossible to transform class." + classToTransform.getName(), e);
      new JavassistBuildException(e);
    }
  }

  private void injectStuffInActivity(final CtClass classToTransform, List<ViewBinding> views,
      List<FragmentBinding> fragments)
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
          .withBody(createInjectedBody(classToTransform, views, fragments, layoutId))
          .elseCreateMethodIfNotExists("") //not used, we are sure the method exists
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

  private void injectStuffInFragment(final CtClass classToTransform, List<ViewBinding> views,
      List<FragmentBinding> fragments)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException, JavassistBuildException {
    afterBurner.afterOverrideMethod(classToTransform, "onViewCreated",
        createInjectedBody(classToTransform, views, fragments, -1));

    afterBurner.afterOverrideMethod(classToTransform, "onDestroyView",
        destroyViewStatements(views));

    classToTransform.detach();
  }

  private void injectStuffInView(final CtClass classToTransform, List<ViewBinding> viewBindings)
      throws NotFoundException, ClassNotFoundException, CannotCompileException,
      AfterBurnerImpossibleException, JavassistBuildException {
    if (viewBindings.isEmpty()) {
      return;
    }

    afterBurner.afterOverrideMethod(classToTransform, "onFinishInflate",
        createInjectedBody(classToTransform, viewBindings, new ArrayList<FragmentBinding>(), -1));
    classToTransform.detach();
  }

  private void injectStuffInClass(final CtClass clazz, List<ViewBinding> views, List<FragmentBinding> fragments)
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

  private String createOnCreateMethod(CtClass clazz, List<ViewBinding> views, List<FragmentBinding> fragments,
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

  private String injectFragmentStatements(List<FragmentBinding> fragmentBindings, String root) throws ClassNotFoundException, NotFoundException {
    StringBuilder builder = new StringBuilder();
    for (FragmentBinding fragmentBinding : fragmentBindings) {

      FindFragmentStatement statement = new FindFragmentStatement(root, fragmentBinding);
      statement.append(builder);
    }
    return builder.toString();
  }

  private String injectViewStatements(CtClass targetClazz, List<ViewBinding> viewBindings)
      throws ClassNotFoundException, NotFoundException {
    StringBuilder buffer = new StringBuilder();
    for (ViewBinding viewBinding : viewBindings) {
      new FindViewStatementInActivity(targetClazz, viewBinding).append(buffer);
    }
    log.debug("Inserted :" + buffer.toString());
    return buffer.toString();
  }

  private String injectViewStatementsForParam(List<ViewBinding> viewBindings, CtClass[] paramClasses,
      int indexParam) throws ClassNotFoundException, NotFoundException {
    CtClass targetClazz = paramClasses[indexParam];
    boolean isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    StringBuilder builder = new StringBuilder();
    for (ViewBinding viewBinding : viewBindings) {
      new FindViewStatementForParam(paramClasses, viewBinding, indexParam).append(builder);
    }
    log.debug("Inserted :" + builder.toString());
    return builder.toString();
  }

  private String destroyViewStatements(List<ViewBinding> viewBindings) {
    StringBuilder builder = new StringBuilder();
    for (ViewBinding viewBinding : viewBindings) {
      new DestroyViewStatementInFragment(viewBinding)
          .append(builder)
          .append('\n');
    }
    return builder.toString();
  }

  private String createInjectedBody(CtClass clazz, List<ViewBinding> views, List<FragmentBinding> fragments,
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
      buffer.append(injectViewStatements(clazz, views));
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
      List<ViewBinding> viewBindings, List<FragmentBinding> fragmentBindings, int layoutId)
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
    if (!viewBindings.isEmpty()) {
      buffer.append(injectViewStatementsForParam(viewBindings, paramClasses, paramIndex));
    }

    if (!fragmentBindings.isEmpty()) {
      if (isActivity) {
        buffer.append(injectFragmentStatements(fragmentBindings, "$" + (1 + paramIndex)));
      } else if (isFragment || isSupportFragment) {
        buffer.append(injectFragmentStatements(fragmentBindings, "$" + (1 + paramIndex)));
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
}
