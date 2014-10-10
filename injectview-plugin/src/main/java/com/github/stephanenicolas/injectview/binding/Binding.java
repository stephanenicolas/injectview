package com.github.stephanenicolas.injectview.binding;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a binding of a view or a fragment.
 * Created by SNI
 */
@RequiredArgsConstructor
public abstract class Binding {
  @Getter
  protected final int id;
}
