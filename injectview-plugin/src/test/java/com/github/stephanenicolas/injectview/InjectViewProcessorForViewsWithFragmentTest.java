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
public class InjectViewProcessorForViewsWithFragmentTest {
  public static final String FRAGMENT_TAG = "TAG";
  public static final String FRAGMENT_TAG2 = "TAG2";
  public static final String FRAGMENT_TAG3 = "TAG3";
  public static final int VIEW_ID = 101;
  public static final String VIEW_TAG = "VIEW_TAG";

  private InjectViewProcessor processor = new InjectViewProcessor();

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withoutOnViewCreated() {
    TestUsingIdActivity
        activity = Robolectric.buildActivity(TestUsingIdActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithoutOnViewCreated.text1);
    assertThat(activity.testFragmentWithoutOnViewCreated.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withOnViewCreated() {
    TestUsingIdActivity
        activity = Robolectric.buildActivity(TestUsingIdActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithOnViewCreated.text1);
    assertThat(activity.testFragmentWithOnViewCreated.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withId_withInheritance() {
    TestUsingIdActivity
        activity = Robolectric.buildActivity(TestUsingIdActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithInheritance.text1);
    assertThat(activity.testFragmentWithInheritance.text1.getId(), is(VIEW_ID));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withTag_withoutOnViewCreated() {
    TestUsingTagActivity
        activity = Robolectric.buildActivity(TestUsingTagActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithoutOnViewCreated.text1);
    assertThat((String)activity.testFragmentWithoutOnViewCreated.text1.getTag(), is(VIEW_TAG));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withTag_withOnViewCreated() {
    TestUsingTagActivity
        activity = Robolectric.buildActivity(TestUsingTagActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithOnViewCreated.text1);
    assertThat((String)activity.testFragmentWithOnViewCreated.text1.getTag(), is(VIEW_TAG));
  }

  @Test
  public void shouldInjectView_whenUsingAtContentView_withTag_withInheritance() {
    TestUsingTagActivity
        activity = Robolectric.buildActivity(TestUsingTagActivity.class)
        .create()
        .get();
    assertNotNull(activity.testFragmentWithInheritance.text1);
    assertThat((String)activity.testFragmentWithInheritance.text1.getTag(), is(VIEW_TAG));
  }



  public static class TestUsingIdActivity extends Activity {
    TestFragmentUsingIdWithoutOnViewCreated testFragmentWithoutOnViewCreated = new TestFragmentUsingIdWithoutOnViewCreated();
    TestFragmentUsingIdWithOnViewCreated testFragmentWithOnViewCreated = new TestFragmentUsingIdWithOnViewCreated();
    TestFragmentUsingIdWithInheritance testFragmentWithInheritance = new TestFragmentUsingIdWithInheritance();

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

  public static class TestFragmentUsingIdWithoutOnViewCreated extends Fragment {
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

  public static class TestFragmentUsingIdWithOnViewCreated extends Fragment {
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
    }
  }

  public static class TestFragmentUsingIdWithInheritance extends TestFragmentUsingIdWithoutOnViewCreated {
  }

  public static class TestUsingTagActivity extends Activity {
    TestFragmentUsingTagWithoutOnViewCreated testFragmentWithoutOnViewCreated = new TestFragmentUsingTagWithoutOnViewCreated();
    TestFragmentUsingTagWithOnViewCreated testFragmentWithOnViewCreated = new TestFragmentUsingTagWithOnViewCreated();
    TestFragmentUsingTagWithInheritance testFragmentWithInheritance = new TestFragmentUsingTagWithInheritance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout linearLayout = new LinearLayout(this);
      setContentView(linearLayout);

      getFragmentManager().beginTransaction().add(testFragmentWithoutOnViewCreated, FRAGMENT_TAG).commit();
      getFragmentManager().beginTransaction().add(testFragmentWithOnViewCreated, FRAGMENT_TAG2).commit();
      getFragmentManager().beginTransaction().add(testFragmentWithInheritance, FRAGMENT_TAG3).commit();
    }
  }

  public static class TestFragmentUsingTagWithoutOnViewCreated extends Fragment {
    @InjectView(tag=VIEW_TAG)
    public TextView text1;

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

  public static class TestFragmentUsingTagWithOnViewCreated extends Fragment {
    @InjectView(tag=VIEW_TAG)
    public TextView text1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
      LinearLayout layout = new LinearLayout(getActivity());
      final TextView text1 = new TextView(getActivity());
      text1.setTag(VIEW_TAG);
      layout.addView(text1);
      return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
    }
  }

  public static class TestFragmentUsingTagWithInheritance extends TestFragmentUsingTagWithoutOnViewCreated {
  }
}
