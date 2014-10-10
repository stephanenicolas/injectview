package com.github.stephanenicolas.injectview.binding;

/**
 * Represents a binding of a view or a fragment.
 * Created by SNI
 */
public abstract class Binding {

  protected final String name;
  protected final String fieldTypeName;
  protected final int id;
  protected final String tag;
  private boolean nullable;
  protected final boolean isUsingId;

  public Binding(String fieldName, String fieldTypeName, int id, String tag, boolean nullable) {
    this.id = id;
    this.tag = tag;
    this.name = fieldName;
    this.fieldTypeName = fieldTypeName;
    this.nullable = nullable;
    this.isUsingId = id != -1;
  }

  public String getFieldName() {
    return name;
  }

  public String getFieldTypeName() {
    return fieldTypeName;
  }

  public int getId() {
    return id;
  }

  public String getTag() {
    return tag;
  }

  public boolean isUsingId() {
    return isUsingId;
  }

  public boolean isNullable() {
    return nullable;
  }
}
