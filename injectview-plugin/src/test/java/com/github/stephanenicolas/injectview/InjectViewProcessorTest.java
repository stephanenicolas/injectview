package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * A class transformer to inject views.
 *
 * @author SNI
 */
@RunWith(RobolectricTestRunner.class)
public class InjectViewProcessorTest {

  protected DummyActivity activity;

  @Before
  public void setup() {
    activity = Robolectric.buildActivity(DummyActivity.class)
        .create()
        .get();
  }

  @Test
  public void shouldInjectView() {
    assertNotNull(activity.text1);
    assertThat(activity.text1, is(activity.findViewById(101)));
  }

  public static class DummyActivity extends Activity {
    @InjectView(101)
    protected TextView text1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      final LinearLayout root = new LinearLayout(this);

      final TextView text1 = new TextView(this);
      root.addView(text1);
      text1.setId(101);
      setContentView(root);
    }
  }
}
