package edu.mills.cs250.dosemakespoison;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Fetches ingredients that have been added to a local database and populates the view of a personal
 * pantry in alphabetical order. Users can view the ingredient details after clicking on an
 * ingredient, linking to {@link ChemResultsActivity}, where the user can add ingredients or
 * remove them from their personal pantry.
 */
public class PantryActivity extends ListActivity {
    private static final String ERROR_FROM_DATABASE = "Error from database.";
    private static final String PANTRY_ACTIVITY = "PantryActivity";
    private static final String RAW_QUERY_BY_NAME = "SELECT * FROM PANTRY ORDER BY NAME";
    private static final String NAME_COL = "NAME";
    private SQLiteDatabase db;
    private Cursor cursor;
    private CursorAdapter chemCursorAdapter;

    //view saved ingredients
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ChemPantryTask().execute();
//        getActionBar().setDisplayHomeAsUpEnabled(true);
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

    @Override
    public void onListItemClick(ListView listView,
                                View itemView,
                                int position,
                                long id) {
        Intent intent = new Intent(PantryActivity.this, ChemResultsActivity.class);
        intent.putExtra(ChemResultsActivity.EXTRA_CHEMNO, (int) id);
        intent.putExtra(ChemResultsActivity.EXTRA_CLASSNAME, PANTRY_ACTIVITY);
        startActivity(intent);
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

            try {
                cursor = db.rawQuery(RAW_QUERY_BY_NAME, null);
            } catch (SQLiteException e) {
                return null;
            }
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            ListView listChems = getListView();
            if (cursor != null) {
                chemCursorAdapter = new SimpleCursorAdapter(PantryActivity.this,
                        android.R.layout.simple_list_item_1,
                        cursor,
                        new String[]{NAME_COL},
                        new int[]{android.R.id.text1},
                        0);
                listChems.setAdapter(chemCursorAdapter);
            } else {
                Toast toast = Toast.makeText(PantryActivity.this, ERROR_FROM_DATABASE, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
