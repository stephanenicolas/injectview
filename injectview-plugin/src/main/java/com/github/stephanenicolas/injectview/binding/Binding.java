package com.github.stephanenicolas.injectview.binding;

/**
 * Represents a binding of a view or a fragment.
 * Created by SNI
 */
public abstract class Binding {

  protected final int id;

  public Binding(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
