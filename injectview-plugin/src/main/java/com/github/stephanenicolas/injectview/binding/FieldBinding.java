package com.github.stephanenicolas.injectview.binding;

import lombok.Getter;

/**
 * Represents a binding of a view or a fragment.
 * Created by SNI
 */
public abstract class FieldBinding extends Binding {

  @Getter
  protected final String fieldName;
  @Getter
  protected final String fieldTypeName;
  @Getter
  protected final String tag;
  @Getter
  private boolean nullable;
  @Getter
  protected final boolean isUsingId;

  public FieldBinding(String fieldName, String fieldTypeName, int id, String tag,
      boolean nullable) {

    super(id);
    this.tag = tag;
    this.fieldName = fieldName;
    this.fieldTypeName = fieldTypeName;
    this.nullable = nullable;
    this.isUsingId = id != -1;
  }
}
