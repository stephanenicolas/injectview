package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.ContentViewBinding;

/**
 * Created by administrateur on 2014-10-10.
 */
public class ContentViewStatement extends Statement<ContentViewBinding> {

  public ContentViewStatement(ContentViewBinding binding) {
    super(binding);
  }

  @Override
  public StringBuilder append(StringBuilder builder) {
    return builder.append("setContentView(").append(binding.getId()).append(");");
  }
}
