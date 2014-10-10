package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.Binding;
import javassist.NotFoundException;

/**
 * Created by administrateur on 2014-10-10.
 */
public abstract class Statement<T extends Binding> {

  protected T binding;

  public abstract StringBuilder append(StringBuilder stringBuilder) throws NotFoundException;

  protected Statement(T binding) {
    this.binding = binding;
  }

  @Override
  public String toString() {
    try {
      StringBuilder builder = new StringBuilder();
      return append(builder).toString();
    } catch (NotFoundException e) {
      throw new RuntimeException("Impossible to create a string representation of this statement.",
          e);
    }
  }
}
