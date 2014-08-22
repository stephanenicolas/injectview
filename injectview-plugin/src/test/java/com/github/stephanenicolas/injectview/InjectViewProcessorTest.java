package com.github.stephanenicolas.injectview;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.build.JavassistBuildException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.robolectric.Robolectric;

/**
 * A class transformer to inject views.
 *
 * @author SNI
 */
@RunWith(InjectViewTestRunner.class)
public class InjectViewProcessorTest {

  private InjectViewProcessor processor = new InjectViewProcessor();
  protected DummyActivity activity;

  @Before
  public void setup() throws JavassistBuildException, NotFoundException, CannotCompileException {
    activity = Robolectric.buildActivity(DummyActivity.class)
        .create()
        .get();
  }

  @Test
  public void shouldInjectView() {
    assertNotNull(((DummyActivity) activity).text1);
    assertThat(((DummyActivity) activity).text1.getId(), is(activity.findViewById(101).getId()));
  }
}
