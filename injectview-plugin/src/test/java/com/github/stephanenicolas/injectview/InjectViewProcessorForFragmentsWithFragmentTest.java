package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 *
 * @author SNI
 */
@RunWith(InjectViewTestRunner.class) @Config(manifest = Config.NONE)
public class InjectViewProcessorForFragmentsWithFragmentTest {
  public static final String VIEW_TAG = "TAG";
  public static final String VIEW_TAG2 = "TAG2";
  public static final int VIEW_ID = 101;
  public static final int VIEW_ID2 = 102;
  public static final int VIEW_ID3 = 103;
  public static final int CONTENT_VIEW_ID = 100;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectFragment() {
    TestActivityWithContentView activity =
        Robolectric.buildActivity(TestActivityWithContentView.class).create().get();
    assertNotNull(activity.fragment);
    assertThat(activity.fragment.getId(), is(VIEW_ID));
    assertNotNull(activity.fragment.fragment);
    assertThat(activity.fragment.fragment.getId(), is(VIEW_ID2));
    assertNotNull(activity.fragment.fragment2);
    assertThat(activity.fragment.fragment2.getTag(), is(VIEW_TAG2));
  }

  public static class TestActivityWithContentView extends Activity {
    public static class TestFragment extends Fragment {
      @InjectFragment(VIEW_ID2)
      protected Fragment fragment;
      @InjectFragment(tag = VIEW_TAG2)
      protected Fragment fragment2;

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
          Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        final TextView text1 = new TextView(getActivity());
        text1.setTag(VIEW_TAG);
        layout.addView(text1);
        return layout;
      }
    }

    @InjectFragment(VIEW_ID)
    protected TestFragment fragment;
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
      LinearLayout fragmentHost3 = new LinearLayout(this);
      fragmentHost3.setId(VIEW_ID3);
      root.addView(fragmentHost3);
      setContentView(root);
    }

    @Override
    public FragmentManager getFragmentManager() {
      FragmentManager fragmentManager = super.getFragmentManager();
      if (fragmentManager.findFragmentById(VIEW_ID) == null) {
        fragmentManager.beginTransaction().add(VIEW_ID, new TestFragment(), VIEW_TAG).commit();
      }
      if (fragmentManager.findFragmentById(VIEW_ID2) == null) {
        fragmentManager.beginTransaction().add(VIEW_ID2, new DummyFragment(), VIEW_TAG).commit();
      }
      if (fragmentManager.findFragmentByTag(VIEW_TAG2) == null) {
        fragmentManager.beginTransaction().add(VIEW_ID3, new DummyFragment(), VIEW_TAG2).commit();
      }
      fragmentManager.executePendingTransactions();
      return fragmentManager;
    }
  }

  public static class DummyFragment extends Fragment {
    public DummyFragment() {
    }
  }
}
