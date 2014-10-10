package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FragmentBinding;
import com.github.stephanenicolas.injectview.binding.ViewBinding;
import javassist.CtClass;
import javassist.NotFoundException;

import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isActivity;
import static com.github.stephanenicolas.morpheus.commons.JavassistUtils.isView;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindFragmentStatement extends FindStatement {
  private String root;

  public FindFragmentStatement(String root, FragmentBinding fragmentBinding) {
    super(fragmentBinding);
    this.root = root;
  }

  @Override public StringBuilder append(StringBuilder builder) throws NotFoundException {

    String getFragmentManagerString;
    if (((FragmentBinding)binding).isSupportFragment()) {
      getFragmentManagerString = "getSupportFragmentManager()";
    } else {
      getFragmentManagerString = "getFragmentManager()";
    }

    final String findFragmentString;
    if (binding.isUsingId()) {
      findFragmentString = "findFragmentById(" + binding.getId() + ")";
    } else {
      findFragmentString = "findFragmentByTag(\"" + binding.getTag() + "\")";
    }

    super.append(builder);
    builder.append(root)
        .append('.')
        .append(getFragmentManagerString)
        .append('.')
        .append(findFragmentString)
        .append(";\n");

    checkNullable(builder, binding);

    return builder;
  }
}
