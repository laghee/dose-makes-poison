/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.dosemakespoison;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * The top-level activity for Dose Makes the Poison. The accompanying view enables users to
 * launch {@link ChemResultsActivity}, {@link PantryActivity}, and {@link LearnMoreActivity}.
 */
public class MainActivity extends Activity {

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
        Intent searchIntent = new Intent(this, ChemResultsActivity.class);
        startActivity(searchIntent);
    }


    /**
     * Enables launch of {@link PantryActivity}.
     *
     * @param view my library view
     */
    public void onBrowsePantry(View view){
        Intent pantryIntent = new Intent(this, PantryActivity.class);
        startActivity(pantryIntent);
    }

    /**
     * Enables launch of {@link LearnMoreActivity}.
     *
     * @param v the view of random game generator
     */
    public void onLearnMore(View v){
        Intent learnIntent = new Intent(this, LearnMoreActivity.class);
        startActivity(learnIntent);
    }
}

