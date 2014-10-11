package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FragmentBinding;
import javassist.NotFoundException;

/**
 * A statement to find/retrieve a fragment.
 * Created by SNI.
 */
public class FindFragmentStatement extends FindStatement {
  protected String root;

  public FindFragmentStatement(FragmentBinding fragmentBinding, int indexParam) {
    super(fragmentBinding);
    this.root = "$" + (1 + indexParam);
  }

  public FindFragmentStatement(FragmentBinding fragmentBinding) {
    super(fragmentBinding);
    this.root = "this";
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

    checkNullable(binding, builder);

    return builder;
  }
}
