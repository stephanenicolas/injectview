package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FieldBinding;

/**
 * Created by administrateur on 2014-10-10.
 */
public abstract class FindViewStatement extends FindStatement {

  protected FindViewStatement(FieldBinding binding) {
    super(binding);
  }

  protected StringBuilder appendFindViewStatement(String root, boolean isActivity,
      StringBuilder builder) {

    builder.append(root).append('.');

    if (isActivity) {
      if (binding.isUsingId()) {
        builder.append("findViewById(").append(binding.getId()).append(")");
      } else {
        builder.append("getWindow().getDecorView().findViewWithTag(\"")
            .append(binding.getTag())
            .append("\")");
      }
    } else {
      if (binding.isUsingId()) {
        builder.append("findViewById(").append(binding.getId()).append(")");
      } else {
        builder.append("findViewWithTag(\"").append(binding.getTag()).append("\")");
      }
    }

    builder.append(";\n");
    return builder;
  }
}
