package edu.mills.cs250.dosemakespoison;

import android.os.Bundle;
import android.app.Activity;

public class MyPantryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pantry);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
