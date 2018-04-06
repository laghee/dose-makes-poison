/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * The top-level activity for Dose Makes the Poison. The accompanying view enables users to
 * launch {@link ChemCompareActivity}, {@link PantryActivity}, and {@link LearnMoreActivity}.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Enables search widget for user input.
     *
     * @param view the search view
     */
    public void onSearch(View view){
        startActivity(new Intent(this, ChemCompareActivity.class));
    }


    /**
     * Enables launch of {@link PantryActivity}.
     *
     * @param view my library view
     */
    public void onBrowsePantry(View view){
        startActivity(new Intent(this, PantryActivity.class));
    }

    /**
     * Enables launch of {@link LearnMoreActivity}.
     *
     * @param v the view of random game generator
     */
    public void onLearnMore(View v){
        startActivity(new Intent(this, LearnMoreActivity.class));
    }
}

