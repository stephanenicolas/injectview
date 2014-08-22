package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * These tests are really complex to setup.
 * Take your time for maintenance.
 * @author SNI
 */
@RunWith(InjectViewTestRunner.class)
public class InjectViewProcessorWithFragmentTest {
  public static final String FRAGMENT_TAG = "TAG";
  public static final String FRAGMENT_TAG2 = "TAG2";
  public static final String FRAGMENT_TAG3 = "TAG3";
  public static final int VIEW_ID = 101;

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withoutOnViewCreated() {
    TestActivity
        activity = Robolectric.buildActivity(TestActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithoutOnViewCreated.text1);
    assertThat(activity.testFragmentWithoutOnViewCreated.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withOnViewCreated() {
    TestActivity
        activity = Robolectric.buildActivity(TestActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithOnViewCreated.text1);
    assertThat(activity.testFragmentWithOnViewCreated.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withInheritance() {
    TestActivity
        activity = Robolectric.buildActivity(TestActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithInheritance.text1);
    assertThat(activity.testFragmentWithInheritance.text1.getId(), is(VIEW_ID));
  }



  public static class TestActivity extends Activity {
    TestFragmentWithoutOnViewCreated testFragmentWithoutOnViewCreated = new TestFragmentWithoutOnViewCreated();
    TestFragmentWithOnViewCreated testFragmentWithOnViewCreated = new TestFragmentWithOnViewCreated();
    TestFragmentWithInheritance testFragmentWithInheritance = new TestFragmentWithInheritance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getFragmentManager().beginTransaction().add(testFragmentWithoutOnViewCreated, FRAGMENT_TAG).commit();
      getFragmentManager().beginTransaction().add(testFragmentWithOnViewCreated, FRAGMENT_TAG2).commit();
      getFragmentManager().beginTransaction().add(testFragmentWithInheritance, FRAGMENT_TAG3).commit();
    }

    @Override
    public View findViewById(int id) {
      if (id==VIEW_ID) {
        final TextView text1 = new TextView(this);
        text1.setId(id);
        return text1;
      } else {
        return super.findViewById(id);
      }
    }
  }

  public static class TestFragmentWithoutOnViewCreated extends Fragment {
    @InjectView(VIEW_ID)
    public TextView text1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
      LinearLayout layout = new LinearLayout(getActivity());
      final TextView text1 = new TextView(getActivity());
      text1.setId(VIEW_ID);
      layout.addView(text1);
      return layout;
    }
  }

  public static class TestFragmentWithOnViewCreated extends Fragment {
    @InjectView(VIEW_ID)
    public TextView text1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
      LinearLayout layout = new LinearLayout(getActivity());
      final TextView text1 = new TextView(getActivity());
      text1.setId(VIEW_ID);
      layout.addView(text1);
      return layout;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
    }
  }

  public static class TestFragmentWithInheritance extends TestFragmentWithoutOnViewCreated {
  }
}
