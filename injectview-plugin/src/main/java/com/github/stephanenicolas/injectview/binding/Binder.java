package com.github.stephanenicolas.injectview.binding;

import com.github.stephanenicolas.injectview.ContentView;
import com.github.stephanenicolas.injectview.InjectFragment;
import com.github.stephanenicolas.injectview.InjectView;
import com.github.stephanenicolas.morpheus.commons.NullableUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.getAllInjectedFieldsForAnnotation;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isSupportFragment;

/**
 * Retrieves bindings from a given class.
 * @author SNI
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
      Class<?> annotationClass = annotation.getClass();
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
      String fieldName = field.getName();
      String fieldTypeName = field.getType().getName();
      final int id = extractIdFromAnnotation(field, InjectView.class);
      final String tag = extractTagFromAnnotation(field, InjectView.class);
      boolean nullable = NullableUtils.isNullable(field);

      viewBindings.add(new ViewBinding(fieldName, fieldTypeName, id, tag, nullable));
    }
    return viewBindings;
  }

  private List<FragmentBinding> extractFragmentBindings(CtClass clazz)
      throws ClassNotFoundException, NotFoundException {

    final List<CtField> views = getAllInjectedFieldsForAnnotation(clazz, InjectFragment.class);

    List<FragmentBinding> fragmentBindings = new ArrayList<>();

    for (CtField field : views) {
      String fieldName = field.getName();
      String fieldTypeName = field.getType().getName();
      final int id = extractIdFromAnnotation(field, InjectFragment.class);
      final String tag = extractTagFromAnnotation(field, InjectFragment.class);
      boolean nullable = NullableUtils.isNullable(field);
      boolean isSupportFragment = isSupportFragment(field.getType());

      fragmentBindings.add(new FragmentBinding(fieldName, fieldTypeName, id, tag, nullable,
          isSupportFragment));
    }
    return fragmentBindings;
  }

  private String extractTagFromAnnotation(CtField field, Class<?> clz) {
    String tag;
    //workaround for robolectric
    //https://github.com/robolectric/robolectric/pull/1240
    try {
      Object annotation = field.getAnnotation(clz);
      //must be accessed by introspection as I get a Proxy during tests.
      //this proxy comes from Robolectric
      Class<?> annotationClass = annotation.getClass();
      Method method = annotationClass.getMethod("tag");
      tag = (String) method.invoke(annotation);
    } catch (Exception e) {
      throw new RuntimeException("How can we get here ?");
    }
    return tag;
  }

  private int extractIdFromAnnotation(CtField field, Class<?> clz) {
    int id;
    //workaround for robolectric
    //https://github.com/robolectric/robolectric/pull/1240
    try {
      Object annotation = field.getAnnotation(clz);
      //must be accessed by introspection as I get a Proxy during tests.
      //this proxy comes from Robolectric
      Class<?> annotationClass = annotation.getClass();
      Method method = annotationClass.getMethod("value");
      id = (Integer) method.invoke(annotation);
    } catch (Exception e) {
      throw new RuntimeException("How can we get here ?");
    }
    return id;
  }
}
