package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * These tests are really complex to setup.
 * Take your time for maintenance.
 * @author SNI
 */
@RunWith(InjectViewTestRunner.class)
@Config(manifest= Config.NONE)
public class InjectViewProcessorWithViewTest {
  public static final String VIEW_TAG = "TAG";
  public static final int VIEW_ID = 101;
  public static final int CONTENT_VIEW_ID = 100;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectView_whenUsingSetContentView_withId() {
    TestActivityWithId activity = Robolectric.buildActivity(TestActivityWithId.class)
        .create()
        .get();
    assertNotNull(activity.root.text1);
    assertThat(activity.root.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingSetContentView_withTag() {
    TestActivityWithTag activity = Robolectric.buildActivity(TestActivityWithTag.class)
        .create()
        .get();
    assertNotNull(activity.root.text1);
    assertThat((String) activity.root.text1.getTag(), is(VIEW_TAG));
  }



  public static class TestActivityWithId extends Activity {
    private TestViewWithId root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      root = new TestViewWithId(this);
      setContentView(root);
    }
  }

  public static class TestViewWithId extends LinearLayout {
    @InjectView(VIEW_ID)
    protected TextView text1;

    public TestViewWithId(Context context) {
      super(context);
      final TextView text = new TextView(getContext());
      text.setId(VIEW_ID);
      addView(text);
      onFinishInflate();
    }
  }

  public static class TestActivityWithTag extends Activity {
    private TestViewWithTag root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      root = new TestViewWithTag(this);
      setContentView(root);
    }
  }

  public static class TestViewWithTag extends LinearLayout {
    @InjectView(tag=VIEW_TAG)
    protected TextView text1;

    public TestViewWithTag(Context context) {
      super(context);
      final TextView text = new TextView(getContext());
      text.setTag(VIEW_TAG);
      addView(text);
      onFinishInflate();
    }
  }
}
