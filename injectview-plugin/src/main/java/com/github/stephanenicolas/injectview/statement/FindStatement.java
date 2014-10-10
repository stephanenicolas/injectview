package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FieldBinding;
import javassist.NotFoundException;

/**
 * Created by administrateur on 2014-10-10.
 */
public abstract class FindStatement extends Statement<FieldBinding> {

  protected FindStatement(FieldBinding binding) {
    super(binding);
  }

  protected StringBuilder checkNullable(StringBuilder builder, FieldBinding binding)
      throws NotFoundException {
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

  public StringBuilder appendAssignment(StringBuilder builder) throws NotFoundException {
    return builder.append(binding.getFieldName())
        .append(" = (")
        .append(binding.getFieldTypeName())
        .append(") ");
  }
}
