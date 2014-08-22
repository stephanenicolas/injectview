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
public class InjectViewProcessorForViewsWithViewTest {
  public static final String VIEW_TAG = "TAG";
  public static final String VIEW_TAG2 = "TAG2";
  public static final int VIEW_ID = 101;
  public static final int VIEW_ID2 = 102;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectView_withId() {
    TestActivityWithId activity = Robolectric.buildActivity(TestActivityWithId.class)
        .create()
        .get();
    assertNotNull(activity.viewWithoutOnFinishInflate.text1);
    assertThat(activity.viewWithoutOnFinishInflate.text1.getId(), is(VIEW_ID));
    assertNotNull(activity.viewWithOnFinishInflate.text1);
    assertThat(activity.viewWithOnFinishInflate.text1.getId(), is(VIEW_ID2));
  }

  @Test
  public void shouldInjectView_withTag() {
    TestActivityWithTag activity = Robolectric.buildActivity(TestActivityWithTag.class)
        .create()
        .get();
    assertNotNull(activity.viewWithoutOnFinishInflate.text1);
    assertThat((String) activity.viewWithoutOnFinishInflate.text1.getTag(), is(VIEW_TAG));
    assertNotNull(activity.viewWithOnFinishInflate.text1);
    assertThat((String) activity.viewWithOnFinishInflate.text1.getTag(), is(VIEW_TAG2));
  }

  public static class TestActivityWithId extends Activity {
    private TestViewWithIdWithoutOnFinishInflate viewWithoutOnFinishInflate;
    private TestViewWithIdWithOnFinishInflate viewWithOnFinishInflate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      viewWithoutOnFinishInflate = new TestViewWithIdWithoutOnFinishInflate(this);
      viewWithOnFinishInflate = new TestViewWithIdWithOnFinishInflate(this);
      LinearLayout layout = new LinearLayout(this);
      layout.addView(viewWithoutOnFinishInflate);
      layout.addView(viewWithOnFinishInflate);
      setContentView(layout);
    }
  }

  public static class TestViewWithIdWithoutOnFinishInflate extends LinearLayout {
    @InjectView(VIEW_ID)
    protected TextView text1;

    public TestViewWithIdWithoutOnFinishInflate(Context context) {
      super(context);
      final TextView text = new TextView(getContext());
      text.setId(VIEW_ID);
      addView(text);
      onFinishInflate();
    }
  }

  public static class TestViewWithIdWithOnFinishInflate extends LinearLayout {
    @InjectView(VIEW_ID2)
    protected TextView text1;

    public TestViewWithIdWithOnFinishInflate(Context context) {
      super(context);
      final TextView text = new TextView(getContext());
      text.setId(VIEW_ID2);
      addView(text);
      onFinishInflate();
    }

    @Override
    protected void onFinishInflate() {
      super.onFinishInflate();
      text1.setText("");
    }
  }

  public static class TestActivityWithTag extends Activity {
    private TestViewWithTagWithoutOnFinishInflate viewWithoutOnFinishInflate;
    private TestViewWithTagWithOnFinishInflate viewWithOnFinishInflate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      viewWithoutOnFinishInflate = new TestViewWithTagWithoutOnFinishInflate(this);
      viewWithOnFinishInflate = new TestViewWithTagWithOnFinishInflate(this);
      LinearLayout layout = new LinearLayout(this);
      layout.addView(viewWithoutOnFinishInflate);
      layout.addView(viewWithOnFinishInflate);
      setContentView(layout);
    }
  }

  public static class TestViewWithTagWithoutOnFinishInflate extends LinearLayout {
    @InjectView(tag=VIEW_TAG)
    protected TextView text1;

    public TestViewWithTagWithoutOnFinishInflate(Context context) {
      super(context);
      final TextView text = new TextView(getContext());
      text.setTag(VIEW_TAG);
      addView(text);
      onFinishInflate();
    }
  }

  public static class TestViewWithTagWithOnFinishInflate extends LinearLayout {
    @InjectView(tag=VIEW_TAG2)
    protected TextView text1;

    public TestViewWithTagWithOnFinishInflate(Context context) {
      super(context);
      final TextView text = new TextView(getContext());
      text.setTag(VIEW_TAG2);
      addView(text);
      onFinishInflate();
    }

    @Override
    protected void onFinishInflate() {
      super.onFinishInflate();
      text1.setText("");
    }
  }
}
