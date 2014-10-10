package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FragmentBinding;
import javassist.NotFoundException;

/**
 * Created by administrateur on 2014-10-10.
 */
public abstract class FindFragmentStatement extends FindStatement {
  protected String root;

  public FindFragmentStatement(String root, FragmentBinding fragmentBinding) {
    super(fragmentBinding);
    this.root = root;
  }

  @Override public StringBuilder append(StringBuilder builder) throws NotFoundException {

    String getFragmentManagerString;
    if (((FragmentBinding) binding).isSupportFragment()) {
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

    appendAssignment(builder).append(root)
        .append('.')
        .append(getFragmentManagerString)
        .append('.')
        .append(findFragmentString)
        .append(";\n");

    checkNullable(builder, binding);

    return builder;
  }
}
