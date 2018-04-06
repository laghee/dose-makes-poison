/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fetches ingredients that have been added to a local database and populates the view of a personal
 * pantry in alphabetical order. Users can view the ingredient details after clicking on an
 * ingredient, linking to {@link ChemCompareActivity}, where the user can add ingredients or
 * remove them from their personal pantry.
 */
public class PantryActivity extends AppCompatActivity {
    private static final String ERROR_FROM_DATABASE = "Error from database.";
    private static final String PANTRY_IS_EMPTY = "Your pantry is empty! When you search for" +
            " ingredients, they will be stored here.";
    private static final String PANTRY_ACTIVITY = "PantryActivity";
    private static final String RAW_QUERY_BY_NAME = "SELECT * FROM PANTRY ORDER BY NAME";
    private static final String NAME_COL = "NAME";
    private SQLiteDatabase db;
    private Cursor cursor;
    private CursorAdapter chemCursorAdapter;
    private ListView pantryList;
    private TextView emptyPantry;

    //view saved ingredients
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        pantryList = findViewById(android.R.id.list);
        Log.d("PantryActivity", "pantryList = " + pantryList);
        setSupportActionBar(findViewById(R.id.tox_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Log.d("PantryActivity", "ChemPantryTask about to run.");
        new ChemPantryTask().execute();
        Log.d("PantryActivity", "ChemPantryTask just ran.");

        pantryList.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(PantryActivity.this, ChemCompareActivity.class);
            intent.putExtra(ChemCompareActivity.EXTRA_CHEMNO, (int) id);
            intent.putExtra(ChemCompareActivity.EXTRA_CLASSNAME, PANTRY_ACTIVITY);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chemCursorAdapter != null) {
            chemCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        if (chemCursorAdapter != null) {
            chemCursorAdapter.notifyDataSetChanged();
        }
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
            db.close();
        }
    }

    private class ChemPantryTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Cursor doInBackground(Void... chems) {
            SQLiteOpenHelper chemPantryHelper = new PantryDatabaseHelper(PantryActivity.this);
            db = chemPantryHelper.getReadableDatabase();
            Log.d("PantryActivity", "Now db = readableDatabase.");

            try {
                cursor = db.rawQuery(RAW_QUERY_BY_NAME, null);
                Log.d("PantryActivity", "Cursor assigned value.");
            } catch (SQLiteException e) {
                Log.d("PantryActivity", "Exception caught.");
                return null;
            }
            Log.d("PantryActivity", "Cursor being returned.");
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            Log.d("PantryActivity", "onPostExecute running.");
            super.onPostExecute(cursor);

            if (cursor != null && cursor.moveToFirst()) {
                chemCursorAdapter = new SimpleCursorAdapter(PantryActivity.this,
                        android.R.layout.simple_list_item_1,
                        cursor,
                        new String[]{NAME_COL},
                        new int[]{android.R.id.text1},
                        0);
                pantryList.setAdapter(chemCursorAdapter);
            } else if (cursor == null) {
                Log.d("PantryActivity", "Error toast to show.");
                Toast toast = Toast.makeText(PantryActivity.this, ERROR_FROM_DATABASE, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Log.d("PantryActivity", "Cursor is empty.");
                emptyPantry = findViewById(R.id.empty);
                Log.d("PantryActivity/empty", "get empty view: " + emptyPantry);
                emptyPantry.setVisibility(View.VISIBLE);
                Toast toast = Toast.makeText(PantryActivity.this, PANTRY_IS_EMPTY, Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }
}
