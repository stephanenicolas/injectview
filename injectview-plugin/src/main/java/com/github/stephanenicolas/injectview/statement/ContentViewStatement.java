package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.ContentView;
import com.github.stephanenicolas.injectview.binding.ContentViewBinding;

/**
 * A statement to install the main layout of an activity.
 * Will issue a java statement matching its {@link ContentView} annotation.
 * @author SNI
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
