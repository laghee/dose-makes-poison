package edu.mills.cs250.dosemakespoison;

import android.os.Bundle;
import android.app.Activity;

public class LearnMoreActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
