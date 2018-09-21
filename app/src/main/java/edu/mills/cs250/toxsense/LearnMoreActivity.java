/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;


public class LearnMoreActivity extends AppCompatActivity {

    private static final String TAG = "LearnMore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);
        TextView usdhhs = findViewById(R.id.textview_usdhhs);
        usdhhs.setMovementMethod(LinkMovementMethod.getInstance());
        TextView ccohs = findViewById(R.id.textview_ccohs);
        ccohs.setMovementMethod(LinkMovementMethod.getInstance());
        TextView cami = findViewById(R.id.textview_camiryan);
        cami.setMovementMethod(LinkMovementMethod.getInstance());
        TextView sciam = findViewById(R.id.textview_sciam);
        sciam.setMovementMethod(LinkMovementMethod.getInstance());
        Toolbar toxToolbar = findViewById(R.id.tox_toolbar);
        setSupportActionBar(toxToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "Toolbar value is: " + toxToolbar);
        Log.d(TAG, "getSupportActionBar returns: " + getSupportActionBar());
    }
}
