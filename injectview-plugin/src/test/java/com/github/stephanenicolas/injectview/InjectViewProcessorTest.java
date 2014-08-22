package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
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

  @Before
  public void setup() throws JavassistBuildException, NotFoundException, CannotCompileException {
  }

  @Test
  public void shouldInjectView_whenUsingSetContentView_withId() {
    TestActivityWithId activity = Robolectric.buildActivity(TestActivityWithId.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat(activity.text1.getId(), is(TestActivityWithId.VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingSetContentView_withTag() {
    TestActivityWithTag activity = Robolectric.buildActivity(TestActivityWithTag.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat((String) activity.text1.getTag(), is(TestActivityWithTag.VIEW_TAG));
  }

  public static class TestActivityWithId extends Activity {
    public static final int VIEW_ID = 101;
    @InjectView(VIEW_ID)
    protected TextView text1;

    protected LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      root = new LinearLayout(this);
      setContentView(root);
    }

    @Override public View findViewById(int id) {
      final TextView text1 = new TextView(this);
      root.addView(text1);
      text1.setId(id);
      return text1;
    }
  }

  public static class TestActivityWithTag extends Activity {
    public static final String VIEW_TAG = "TAG";
    @InjectView(tag=VIEW_TAG)
    protected TextView text1;

    protected LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      root = new LinearLayout(this);
      final TextView text = new TextView(this);
      root.addView(text);
      text.setTag(VIEW_TAG);
      setContentView(root);
    }

    @Override public Window getWindow() {
      return super.getWindow();
    }
  }
}
