package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FragmentBinding;
import javassist.NotFoundException;

/**
 * A statement to find/retrieve a fragment.
 * The statements support both native and support fragments.
 * <br/> e.g. : this.getSupportFragmentManager().findFragmentByTag("foo");
 * @author SNI.
 */
public class FindFragmentStatement extends FindStatement {
  protected String root;

  /**
   * Creates a statement to find a fragment when the root is a parameter of a method.
   * @param fragmentBinding the fragment data.
   * @param indexParam the index of the parameter that owns a fragment.
   */
  public FindFragmentStatement(FragmentBinding fragmentBinding, int indexParam) {
    super(fragmentBinding);
    this.root = "$" + (1 + indexParam);
  }

  /**
   * Creates a statement to find a fragment inside a fragment or an activity.
   * @param fragmentBinding the fragment data.
   */
  public FindFragmentStatement(FragmentBinding fragmentBinding) {
    super(fragmentBinding);
    this.root = "this";
  }

  @Override
  public StringBuilder append(StringBuilder builder) throws NotFoundException {

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
