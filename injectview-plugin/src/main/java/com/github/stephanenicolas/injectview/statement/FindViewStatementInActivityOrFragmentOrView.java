package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.ViewBinding;
import javassist.CtClass;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindViewStatementInActivityOrFragmentOrView extends FindViewStatement {
  private CtClass targetClazz;

  public FindViewStatementInActivityOrFragmentOrView(CtClass targetClazz, ViewBinding viewBinding) {
    super(viewBinding);
    this.targetClazz = targetClazz;
  }

  @Override
  public StringBuilder append(StringBuilder builder) throws NotFoundException {
    boolean isActivity = isActivity(targetClazz);
    boolean isView = isView(targetClazz);

    String root = "";
    if (isActivity || isView) {
      root = "this";
    } else {
      root = "$1";
    }

    appendAssignment(builder);
    appendFindViewStatement(root, isActivity, builder);
    checkNullable(builder, binding);

    return builder;
  }
}
