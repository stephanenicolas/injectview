package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.widget.LinearLayout;
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
public class InjectViewProcessorForFragmentsWithActivityTest {
  public static final String VIEW_TAG = "TAG";
  public static final String VIEW_TAG2 = "TAG2";
  public static final int VIEW_ID = 101;
  public static final int VIEW_ID2 = 102;
  public static final int CONTENT_VIEW_ID = 100;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectFragment_whenUsingSetContentView() {
    TestActivityWithoutContentView activity = Robolectric.buildActivity(TestActivityWithoutContentView.class)
        .create()
        .get();
    assertNotNull(activity.fragment);
    assertThat(activity.fragment.getId(), is(VIEW_ID));
    assertNotNull(activity.fragment2);
    assertThat(activity.fragment2.getTag(), is(VIEW_TAG2));
  }

  @Test
  public void shouldInjectFragment_whenUsingContentView() {
    TestActivityWithContentView activity = Robolectric.buildActivity(TestActivityWithContentView.class)
        .create()
        .get();
    assertNotNull(activity.fragment);
    assertThat(activity.fragment.getId(), is(VIEW_ID));
    assertNotNull(activity.fragment2);
    assertThat(activity.fragment2.getTag(), is(VIEW_TAG2));
  }


  public static class TestActivityWithoutContentView extends Activity {
    @InjectFragment(VIEW_ID)
    protected Fragment fragment;
    @InjectFragment(tag=VIEW_TAG2)
    protected Fragment fragment2;

    protected LinearLayout root;

    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      root = new LinearLayout(this);
      root.setId(CONTENT_VIEW_ID);
      LinearLayout fragmentHost = new LinearLayout(this);
      fragmentHost.setId(VIEW_ID);
      root.addView(fragmentHost);
      LinearLayout fragmentHost2 = new LinearLayout(this);
      fragmentHost2.setId(VIEW_ID2);
      root.addView(fragmentHost2);
      setContentView(root);
    }

    @Override public FragmentManager getFragmentManager() {
      FragmentManager fragmentManager = super.getFragmentManager();
      if (fragmentManager.findFragmentById(VIEW_ID) == null ) {
        fragmentManager.beginTransaction().add(VIEW_ID, new DummyFragment(), VIEW_TAG).commit();
      }
      if (fragmentManager.findFragmentByTag(VIEW_TAG2) == null ) {
        fragmentManager.beginTransaction().add(VIEW_ID2, new DummyFragment(), VIEW_TAG2).commit();
      }
      fragmentManager.executePendingTransactions();
      return fragmentManager;
    }
  }

  @ContentView(CONTENT_VIEW_ID)
  public static class TestActivityWithContentView extends Activity {
    @InjectFragment(VIEW_ID)
    protected Fragment fragment;
    @InjectFragment(tag=VIEW_TAG2)
    protected Fragment fragment2;

    protected LinearLayout root;

    @Override public FragmentManager getFragmentManager() {
      FragmentManager fragmentManager = super.getFragmentManager();
      if (fragmentManager.findFragmentById(VIEW_ID) == null ) {
        fragmentManager.beginTransaction().add(VIEW_ID, new DummyFragment(), VIEW_TAG).commit();
      }
      if (fragmentManager.findFragmentByTag(VIEW_TAG2) == null ) {
        fragmentManager.beginTransaction().add(VIEW_ID2, new DummyFragment(), VIEW_TAG2).commit();
      }
      fragmentManager.executePendingTransactions();
      return fragmentManager;
    }

    @Override
    public void setContentView(int layoutResID) {
      root = new LinearLayout(this);
      root.setId(CONTENT_VIEW_ID);
      LinearLayout fragmentHost = new LinearLayout(this);
      fragmentHost.setId(VIEW_ID);
      root.addView(fragmentHost);
      LinearLayout fragmentHost2 = new LinearLayout(this);
      fragmentHost2.setId(VIEW_ID2);
      root.addView(fragmentHost2);
      setContentView(root);
    }
  }

  public static class DummyFragment extends Fragment {
    public DummyFragment() {
    }
  }

}
