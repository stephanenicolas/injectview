package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.ViewBinding;
import javassist.CtClass;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindViewStatementInActivity extends FindViewStatement {
  private CtClass targetClazz;

  public FindViewStatementInActivity(CtClass targetClazz, ViewBinding viewBinding) {
    super(viewBinding);
    this.targetClazz = targetClazz;
  }

  @Override public StringBuilder append(StringBuilder builder) throws NotFoundException {
    boolean isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    String root = "";
    if (isActivity || isView) {
      root = "this";
    } else {
      root = "$1";
    }



    super.append(builder);
    builder.append(root).append(".");

    appendFindViewStatement(isActivity, builder)
    .append(";\n");

    checkNullable(builder, binding);

    return builder;
  }
}
