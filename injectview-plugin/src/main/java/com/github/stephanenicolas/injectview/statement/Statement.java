package com.github.stephanenicolas.injectview.statement;

import com.github.stephanenicolas.injectview.binding.Binding;
import javassist.NotFoundException;

/**
 * A Java statement that will be used to generate byte code that will be weaved.
 * A statement is issued for a {@link Binding}.
 * Created by SNI.
 */
public abstract class Statement<T extends Binding> {

  /** The binding for which the statement will be issued.*/
  protected T binding;

  /**
   * Creates a new statement for a given binding.
   * @param binding
   */
  protected Statement(T binding) {
    this.binding = binding;
  }

  /**
   * Appends the present statement to {@code builder}.
   * @param builder the StringBuilder to append the statement to.
   * @return the {@code builder}
   * @throws NotFoundException
   */
  public abstract StringBuilder append(StringBuilder builder) throws NotFoundException;


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
