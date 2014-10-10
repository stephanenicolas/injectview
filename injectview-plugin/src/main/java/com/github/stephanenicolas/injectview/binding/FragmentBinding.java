package com.github.stephanenicolas.injectview.binding;

import lombok.Getter;

/**
 * Created by administrateur on 2014-10-10.
 */
public class FragmentBinding extends FieldBinding {
  @Getter
  private final boolean isSupportFragment;

  public FragmentBinding(String name, String fieldTypeName, int id, String tag, boolean nullable,
      boolean isSupportFragment) {
    super(name, fieldTypeName, id, tag, nullable);
    this.isSupportFragment = isSupportFragment;
  }
}
