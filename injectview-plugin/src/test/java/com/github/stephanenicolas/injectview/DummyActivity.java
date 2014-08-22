package com.github.stephanenicolas.injectview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
* Created by administrateur on 2014-08-21.
*/
public class DummyActivity extends Activity {
  @InjectView(101)
  protected TextView text1;

  protected LinearLayout root;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    root = new LinearLayout(this);
    setContentView(root);
  }

  @Override public View findViewById(int id) {
    System.out.println("Inside findViewById");
    final TextView text1 = new TextView(this);
    root.addView(text1);
    text1.setId(id);
    return text1;
  }
}
