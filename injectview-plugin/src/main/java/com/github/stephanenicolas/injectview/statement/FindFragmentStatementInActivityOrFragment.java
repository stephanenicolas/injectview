package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FragmentBinding;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindFragmentStatementInActivityOrFragment extends FindFragmentStatement {
  private String root;

  public FindFragmentStatementInActivityOrFragment(FragmentBinding fragmentBinding) {
    super("this", fragmentBinding);
  }
}
