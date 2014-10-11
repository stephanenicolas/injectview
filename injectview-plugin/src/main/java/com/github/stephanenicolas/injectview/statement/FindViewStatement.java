package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.ViewBinding;
import javassist.CtClass;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;

/**
 * A statement to find/retrieve a view.
 * <br/> e.g. : this.findViewById(R.id.foo);
 *
 * @author SNI
 */
public class FindViewStatement extends FindStatement {

  private String root;
  private boolean isActivity;

  /**
   * Creates a statement to find a view when the root is a parameter of a method.
   *
   * @param viewBinding the view data.
   * @param paramClasses all parameters of this method.
   * @param indexParam the index of the parameter that owns the view.
   */
  public FindViewStatement(ViewBinding viewBinding, CtClass[] paramClasses, int indexParam)
      throws NotFoundException {

    super(viewBinding);
    CtClass targetClazz = paramClasses[indexParam];
    isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    if (isActivity || isView) {
      root = "$" + (1 + indexParam);
    } else {
      root = "$" + (1 + indexParam) + ".getView()";
    }
  }

  /**
   * Creates a statement to find a fragment inside a fragment or an activity.
   * @param viewBinding the fragment data.
   * @param targetClazz the class that owns the view.
   */
  public FindViewStatement(ViewBinding viewBinding, CtClass targetClazz) throws NotFoundException {
    super(viewBinding);
    isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    if (isActivity || isView) {
      root = "this";
    } else {
      root = "$1";
    }
  }

  @Override
  public StringBuilder append(StringBuilder builder) throws NotFoundException {
    appendAssignment(builder);
    appendFindViewStatement(isActivity, builder);
    checkNullable(binding, builder);
    return builder;
  }

  /**
   * Appends a partial statement to find/retrieve a view given its binding.
   *
   * @param isActivity whether or not root is an activity.
   * @param builder the StringBuilder to which the statement will appended.
   * @return {@code builder}
   */
  private StringBuilder appendFindViewStatement(boolean isActivity, StringBuilder builder) {

    builder.append(root).append('.');

    if (binding.isUsingId()) {
      builder.append("findViewById(").append(binding.getId()).append(")");
    } else {
      if (isActivity) {
        builder.append("getWindow().getDecorView().findViewWithTag(\"")
            .append(binding.getTag())
            .append("\")");
      } else {
        builder.append("findViewWithTag(\"").append(binding.getTag()).append("\")");
      }
    }

    builder.append(";\n");
    return builder;
  }
}
