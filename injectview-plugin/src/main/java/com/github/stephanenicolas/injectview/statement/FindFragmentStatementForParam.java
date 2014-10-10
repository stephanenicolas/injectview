package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FragmentBinding;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindFragmentStatementForParam extends FindFragmentStatement {

  public FindFragmentStatementForParam(FragmentBinding fragmentBinding, int indexParam) {
    super("$" + (1 + indexParam), fragmentBinding);
  }
}
