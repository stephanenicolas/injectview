package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.Binding;
import javassist.NotFoundException;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FindStatement extends Statement {

  protected FindStatement(Binding binding) {
    super(binding);
  }

  @Override public StringBuilder append(StringBuilder builder) throws NotFoundException {
    return builder.append(binding.getFieldName())
        .append(" = (")
        .append(binding.getFieldTypeName())
        .append(") ");
  }
}
