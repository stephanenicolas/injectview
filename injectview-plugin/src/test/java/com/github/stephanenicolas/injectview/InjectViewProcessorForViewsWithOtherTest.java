package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
public class InjectViewProcessorForViewsWithOtherTest {
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
  }


  public static class TestActivityWithId extends Activity {
    private TestViewWithIdWithoutOnFinishInflate viewWithoutOnFinishInflate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout layout = new LinearLayout(this);
      final TextView text1 = new TextView(this);
      text1.setId(VIEW_ID);
      layout.addView(text1);
      viewWithoutOnFinishInflate = new TestViewWithIdWithoutOnFinishInflate(layout);
      setContentView(layout);
    }

  }

  public static class TestViewWithIdWithoutOnFinishInflate {
    @InjectView(VIEW_ID)
    protected TextView text1;

    public TestViewWithIdWithoutOnFinishInflate(View view) {
      text1.setText("");
    }
  }

}
