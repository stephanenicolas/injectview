package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/**
 * These tests are really complex to setup.
 * Take your time for maintenance.
 * @author SNI
 */
@RunWith(InjectViewTestRunner.class)
@Config(manifest= Config.NONE)
public class InjectViewProcessorForViewsWithActivityTest {
  public static final String VIEW_TAG = "TAG";
  public static final int VIEW_ID = 101;
  public static final int CONTENT_VIEW_ID = 100;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectView_whenUsingSetContentView_withId() {
    TestActivityWithId activity = Robolectric.buildActivity(TestActivityWithId.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat(activity.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingSetContentView_withTag() {
    TestActivityWithTag activity = Robolectric.buildActivity(TestActivityWithTag.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat((String) activity.text1.getTag(), is(VIEW_TAG));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId() {
    TestActivityWithContentViewAndId activity = Robolectric.buildActivity(TestActivityWithContentViewAndId.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat(activity.text1.getId(), is(VIEW_ID));
    //http://stackoverflow.com/a/8817003/693752
    View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    assertNotNull(root);
    assertThat((Integer) root.getTag(), is(CONTENT_VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withTag() {
    TestActivityWithContentViewAndTag activity = Robolectric.buildActivity(TestActivityWithContentViewAndTag.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat((String) activity.text1.getTag(), is(VIEW_TAG));
    //http://stackoverflow.com/a/8817003/693752
    View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    assertNotNull(root);
    assertThat((Integer) root.getTag(), is(CONTENT_VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withoutOnCreate() {
    TestActivityWithContentViewAndIdWithoutOnCreate activity = Robolectric.buildActivity(TestActivityWithContentViewAndIdWithoutOnCreate.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat(activity.text1.getId(), is(VIEW_ID));
    //http://stackoverflow.com/a/8817003/693752
    View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    assertNotNull(root);
    assertThat((Integer) root.getTag(), is(CONTENT_VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withTag_withoutOnCreate() {
    TestActivityWithContentViewAndTagWithoutOnCreate activity = Robolectric.buildActivity(TestActivityWithContentViewAndTagWithoutOnCreate.class)
        .create()
        .get();
    assertNotNull(activity.text1);
    assertThat((String) activity.text1.getTag(), is(VIEW_TAG));
    //http://stackoverflow.com/a/8817003/693752
    View root = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    assertNotNull(root);
    assertThat((Integer) root.getTag(), is(CONTENT_VIEW_ID));
  }



  public static class TestActivityWithId extends Activity {
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
  }

  @ContentView(CONTENT_VIEW_ID)
  public static class TestActivityWithContentViewAndId extends Activity {
    @InjectView(VIEW_ID)
    protected TextView text1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    }

    @Override public View findViewById(int id) {
      if (id==VIEW_ID) {
        final TextView text1 = new TextView(this);
        text1.setId(id);
        return text1;
      } else {
        return super.findViewById(id);
      }
    }

    @Override public void setContentView(int layoutResID) {
      LinearLayout layout = new LinearLayout(this);
      layout.setTag(CONTENT_VIEW_ID);
      setContentView(layout);
    }
  }

  @ContentView(CONTENT_VIEW_ID)
  public static class TestActivityWithContentViewAndTag extends Activity {
    @InjectView(tag=VIEW_TAG)
    protected TextView text1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
    }

    @Override public void setContentView(int layoutResID) {
      LinearLayout linearLayout = new LinearLayout(this);
      linearLayout.setTag(CONTENT_VIEW_ID);
      final TextView text = new TextView(this);
      text.setTag(VIEW_TAG);
      linearLayout.addView(text);
      setContentView(linearLayout);
    }
  }


  @ContentView(CONTENT_VIEW_ID)
  public static class TestActivityWithContentViewAndIdWithoutOnCreate extends Activity {
    @InjectView(VIEW_ID)
    protected TextView text1;

    @Override public View findViewById(int id) {
      if (id==VIEW_ID) {
        final TextView text1 = new TextView(this);
        text1.setId(id);
        return text1;
      } else {
        return super.findViewById(id);
      }
    }

    @Override public void setContentView(int layoutResID) {
      LinearLayout layout = new LinearLayout(this);
      layout.setTag(CONTENT_VIEW_ID);
      setContentView(layout);
    }
  }

  @ContentView(CONTENT_VIEW_ID)
  public static class TestActivityWithContentViewAndTagWithoutOnCreate extends Activity {
    @InjectView(tag=VIEW_TAG)
    protected TextView text1;

    @Override public void setContentView(int layoutResID) {
      LinearLayout linearLayout = new LinearLayout(this);
      linearLayout.setTag(CONTENT_VIEW_ID);
      final TextView text = new TextView(this);
      text.setTag(VIEW_TAG);
      linearLayout.addView(text);
      setContentView(linearLayout);
    }
  }
}
