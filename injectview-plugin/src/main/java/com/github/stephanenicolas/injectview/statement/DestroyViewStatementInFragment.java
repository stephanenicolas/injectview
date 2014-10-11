package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.ContentView;
import com.github.stephanenicolas.injectview.binding.FieldBinding;
import com.github.stephanenicolas.injectview.binding.ViewBinding;

/**
 * A statement to assign to null all views of a fragment.
 * This statement is needed to prevent context leaks owned by views of a dead fragment.
 * @author SNI
 */
public class DestroyViewStatementInFragment extends Statement<FieldBinding> {

  public DestroyViewStatementInFragment(ViewBinding viewBinding) {
    super(viewBinding);
  }

  public StringBuilder append(StringBuilder builder) {
    builder.append(binding.getFieldName());
    builder.append(" = null;");
    return builder;
  }
}
