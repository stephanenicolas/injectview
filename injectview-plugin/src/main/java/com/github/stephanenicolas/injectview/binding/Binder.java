package com.github.stephanenicolas.injectview.binding;

import com.github.stephanenicolas.injectview.ContentView;
import com.github.stephanenicolas.injectview.InjectFragment;
import com.github.stephanenicolas.injectview.InjectView;
import com.github.stephanenicolas.injectview.binding.Binding;
import com.github.stephanenicolas.injectview.binding.FragmentBinding;
import com.github.stephanenicolas.injectview.binding.ViewBinding;
import com.github.stephanenicolas.morpheus.commons.NullableUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.getAllInjectedFieldsForAnnotation;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isSupportFragment;

/**
 * Created by administrateur on 2014-10-10.
 */
public class Binder {

  private Map<CtClass, List<ViewBinding>> mapClassToViewBindings = new HashMap<>();
  private Map<CtClass, List<FragmentBinding>> mapClassToFragmentBindings = new HashMap<>();
  private Map<CtClass, ContentViewBinding> mapClassToContentViewBinding = new HashMap<>();

  public void extractAllBindings(CtClass clazz) throws NotFoundException, ClassNotFoundException {
    List<ViewBinding> viewBindings = extractViewBindings(clazz);
    List<FragmentBinding> fragmentBindings = extractFragmentBindings(clazz);
    ContentViewBinding contentViewbinding = extractContentViewBinding(clazz);
    mapClassToViewBindings.put(clazz, viewBindings);
    mapClassToFragmentBindings.put(clazz, fragmentBindings);
    mapClassToContentViewBinding.put(clazz, contentViewbinding);
  }

  public List<ViewBinding> getViewBindings(CtClass clazz) {
    return mapClassToViewBindings.get(clazz);
  }

  public List<FragmentBinding> getFragmentBindings(CtClass clazz) {
    return mapClassToFragmentBindings.get(clazz);
  }

  public ContentViewBinding getContentViewBinding(CtClass clazz) {
    return mapClassToContentViewBinding.get(clazz);
  }

  private ContentViewBinding extractContentViewBinding(CtClass clazz)
      throws ClassNotFoundException, NotFoundException {

      try {
        Object annotation = clazz.getAnnotation(ContentView.class);
        Class annotationClass = annotation.getClass();
        Method method = annotationClass.getMethod("value");
        return new ContentViewBinding((Integer) method.invoke(annotation));
      } catch (Exception e) {
        return null;
      }
  }

  private List<ViewBinding> extractViewBindings(CtClass clazz)
      throws ClassNotFoundException, NotFoundException {

    final List<CtField> views = getAllInjectedFieldsForAnnotation(clazz, InjectView.class);

    List<ViewBinding> viewBindings = new ArrayList<>();

    for (CtField field : views) {
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
      boolean nullable = NullableUtils.isNullable(field);

      ViewBinding viewBinding =
          new ViewBinding(field.getName(), field.getType().getName(), id, tag, nullable);

      viewBindings.add(viewBinding);
    }
    return viewBindings;
  }

  private List<FragmentBinding> extractFragmentBindings(CtClass clazz)
      throws ClassNotFoundException, NotFoundException {

    final List<CtField> views = getAllInjectedFieldsForAnnotation(clazz, InjectFragment.class);

    List<FragmentBinding> fragmentBindings = new ArrayList<>();

    for (CtField field : views) {
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

      boolean nullable = NullableUtils.isNullable(field);

      boolean isSupportFragment = isSupportFragment(field.getType());

      FragmentBinding fragmentBinding =
          new FragmentBinding(field.getName(), field.getType().getName(), id, tag, nullable,
              isSupportFragment);

      fragmentBindings.add(fragmentBinding);
    }
    return fragmentBindings;
  }
}
