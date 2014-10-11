package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.FieldBinding;
import javassist.NotFoundException;

/**
 * A statement to find/retrieve a view or a fragment.
 * Created by SNI.
 */
public abstract class FindStatement extends Statement<FieldBinding> {

  protected FindStatement(FieldBinding binding) {
    super(binding);
  }

  /**
   * Appends a null check for a given {@link FieldBinding} if the field is not
   * annotated with a Nullable annotation. All annotations whose name match 'Nullable'
   * are considered to be Nullable annotation.
   * @param binding the binding that will be checked for Nullable annotation.
   * @param builder the StringBuilder in which the statement is appended.
   * @return {@code builder}
   */
  protected StringBuilder checkNullable(FieldBinding binding, StringBuilder builder) {
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

  /**
   * Appends a partial statement of the form {@code this.a = (type)} to a StringBuilder.
   * @param builder the StringBuilder in which the partial statement is appended.
   * @return {@code builder}
   */
  protected StringBuilder appendAssignment(StringBuilder builder) {
    return builder
        .append("this.")
        .append(binding.getFieldName())
        .append(" = (")
        .append(binding.getFieldTypeName())
        .append(") ");
  }
}
