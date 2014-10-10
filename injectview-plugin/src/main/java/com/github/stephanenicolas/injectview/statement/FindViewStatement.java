package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.Binding;
import javassist.NotFoundException;

/**
 * Created by administrateur on 2014-10-10.
 */
public abstract class FindViewStatement extends FindStatement {

  protected FindViewStatement(Binding binding) {
    super(binding);
  }

  protected StringBuilder appendFindViewStatement(boolean isActivity, StringBuilder builder) {
    if (isActivity) {
      if (binding.isUsingId()) {
        builder.append("findViewById(")
            .append(binding.getId())
            .append(")");
      } else {
        builder.append("getWindow().getDecorView().findViewWithTag(\"")
            .append(binding.getTag())
            .append("\")")
            .toString();
      }
    } else {
      if (binding.isUsingId()) {
        builder.append("findViewById(")
            .append(binding.getId())
            .append(")");
      } else {
        builder.append("findViewWithTag(\"")
            .append(binding.getTag())
            .append("\")");
      }
    }
    return builder;
  }
}
