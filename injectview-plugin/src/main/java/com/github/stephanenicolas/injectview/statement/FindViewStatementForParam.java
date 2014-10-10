package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.ViewBinding;
import javassist.CtClass;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindViewStatementForParam extends FindViewStatement {
  private CtClass[] paramClasses;
  private int indexParam;

  public FindViewStatementForParam(CtClass[] paramClasses, ViewBinding viewBinding, int indexParam) {
    super(viewBinding);
    this.paramClasses = paramClasses;
    this.indexParam = indexParam;
  }

  @Override public StringBuilder append(StringBuilder builder) throws NotFoundException {
    CtClass targetClazz = paramClasses[indexParam];
    boolean isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    String root = "";
    if (isActivity || isView) {
      root = "$" + (1 + indexParam);
    } else {
      root = "$" + (1 + indexParam) + ".getView()";
    }



    super.append(builder)
    .append(root).append(".");

    appendFindViewStatement(isActivity, builder)
    .append(";\n");

    checkNullable(builder, binding);

    return builder;
  }
}
