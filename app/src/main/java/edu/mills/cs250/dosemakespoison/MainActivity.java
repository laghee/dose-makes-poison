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
 * launch {@link SearchResultsActivity}, {@link MyPantryActivity}, and {@link LearnMoreActivity}.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Enables launch of {@link SearchResultsActivity}.
     *
     * @param view the search view
     */
    public void onSearch(View view){
        Intent searchIntent = new Intent(this, SearchResultsActivity.class);
        startActivity(searchIntent);
    }

    /**
     * Enables launch of {@link MyPantryActivity}.
     *
     * @param view my library view
     */
    public void onViewLibrary(View view){
        Intent intent = new Intent(this, MyPantryActivity.class);
        startActivity(intent);
    }

    /**
     * Enables launch of {@link LearnMoreActivity}.
     *
     * @param v the view of random game generator
     */
    public void onRandomGenerator(View v){
        Intent randintent = new Intent(this, LearnMoreActivity.class);
        startActivity(randintent);
    }
}

