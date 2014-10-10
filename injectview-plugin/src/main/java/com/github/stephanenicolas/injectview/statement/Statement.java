package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.Binding;
import javassist.NotFoundException;

/**
 * Created by administrateur on 2014-10-10.
 */
public abstract class Statement {

  protected Binding binding;

  public abstract StringBuilder append(StringBuilder stringBuilder) throws NotFoundException;

  protected Statement(Binding binding) {
    this.binding = binding;
  }

  @Override
  public String toString() {
    try {
      StringBuilder builder = new StringBuilder();
      return append(builder).toString();
    } catch (NotFoundException e) {
      throw new RuntimeException("Impossible to create a string representation of this statement.", e);
    }
  }

  protected StringBuilder checkNullable(StringBuilder builder, Binding binding) throws NotFoundException {
    String fieldName = binding.getFieldName();
    if (!binding.isNullable()) {
      builder.append("if (")
          .append(fieldName)
          .append(" == null) {\n  throw new RuntimeException(\"Field ")
          .append(fieldName)
          .append(" is null and is not @Nullable.\"); \n}\n");
    }
    return builder;
  }


}
