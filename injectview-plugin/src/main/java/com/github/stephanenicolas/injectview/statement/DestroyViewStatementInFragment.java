package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.ViewBinding;

/**
 * Created by administrateur on 2014-10-10.
 */
public class DestroyViewStatementInFragment extends Statement {

  public DestroyViewStatementInFragment(ViewBinding viewBinding) {
    super(viewBinding);
  }

  public StringBuilder append(StringBuilder builder) {
    builder.append(binding.getFieldName());
    builder.append(" = null;");
    return builder;
  }

}
