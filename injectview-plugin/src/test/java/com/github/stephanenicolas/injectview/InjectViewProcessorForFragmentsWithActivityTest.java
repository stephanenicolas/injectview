package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
  public static final int VIEW_ID = 101;
  public static final int CONTENT_VIEW_ID = 100;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectView_whenUsingSetContentView_withId() {
    TestActivityWithContentView activity = Robolectric.buildActivity(TestActivityWithContentView.class)
        .create()
        .get();
    assertNotNull(activity.fragment);
    assertThat(activity.fragment.getId(), is(VIEW_ID));
  }


  @ContentView(CONTENT_VIEW_ID)
  public static class TestActivityWithContentView extends Activity {
    @InjectFragment(VIEW_ID)
    protected Fragment fragment;

    protected LinearLayout root;

    @Override public FragmentManager getFragmentManager() {
      FragmentManager fragmentManager = super.getFragmentManager();
      if (fragmentManager.findFragmentById(VIEW_ID) == null ) {
        fragmentManager.beginTransaction().add(VIEW_ID, new DummyFragment(), VIEW_TAG).commit();
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
      setContentView(root);
    }
  }

  public static class DummyFragment extends Fragment {
    public DummyFragment() {
    }
  }

}
