/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**Activity for viewing ingredient comparison. Provides an interface for adding or removing ingredients
 * from a local pantry by clicking a checkbox. If a user removes an ingredient,
 * {@link PantryActivity} is launched and they are taken back to their pantry.
 */

public class ChemCompareActivity extends AppCompatActivity {

    /**
     * Label for chem's id number. Called by {@link PantryActivity},
     * {@link MainActivity}.
     */
    public static final String EXTRA_CHEMNO = "chemNo";
    /**
     * Label for activity name. Called by {@link PantryActivity} and
     * {@link MainActivity}.
     */
    public static final String EXTRA_CLASSNAME = "class";
    private static final String WEB_ACTIVITY = "WebActivity";
    private static final String PANTRY_ACTIVITY = "PantryActivity";
    private static final String ERROR_RETRIEVE_CHEM = "Error retrieving chem.";
    private static final String DB_UNAVAIL = "Database unavailable.";
    private static final String PANTRYID = "PANTRYID";
    private static final String ERROR_SAVING_CHEM = "Error saving chem.";
    private static final String CHEM_SAVED = "Chem saved!";
    private static final String CHEM_REMOVED = "Chem removed.";

    private boolean web = true;
    private SQLiteDatabase db;
    private String chemName;
    private int ld50Val;
    private String compareChem;
    private int spNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chem_compare);
        Toolbar toxTool = (Toolbar) findViewById(R.id.tox_toolbar);
        Log.d("ChemCompare", "Toolbar value is: " + toxTool);
        setSupportActionBar(toxTool);
        Log.d("ChemCompareActivity", "getSupportActionBar() returns: " + getSupportActionBar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            fab.hide();
//           TODO****: Handle share action
            Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

//        // Get the intent, verify the action and get the query
//        Intent intent = getIntent();
//        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
//            Log.d("ChemCompare", "Intent = ACTION_SEARCH");
//            String query = intent.getStringExtra(SearchManager.QUERY);
////                doMySearch(query);
//        }

////        Get the chem from the intent
//        int chemNo = (Integer) getIntent().getExtras().get(EXTRA_CHEMNO);
//        String className = (String) getIntent().getExtras().get(EXTRA_CLASSNAME);
//
//        switch (className) {
//            case PANTRY_ACTIVITY:
//                web = false;
//                new ChemCompareActivity.PantryChemResultsTask().execute(chemNo);
//                break;
//            case WEB_ACTIVITY:
//                new ChemCompareActivity.CheckPantryForChemTask().execute(chemNo);
//                break;
//            default:
//                Log.d("ChemCompare", "Error, incoming class not found.");
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d("ChemCompare", "Intent = ACTION_SEARCH");
            String query = intent.getStringExtra(SearchManager.QUERY);
//                doMySearch(query);
        }
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        Log.d("ChemResults-onCreateOpt", "searchManager = " + searchManager);
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) search.getActionView();
        Log.d("Chem-onCreateOpt", "ActionView= " + sv);
        // Get the SearchView and set the searchable configuration
        sv.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        Log.d("Chem-onCreateOpts", "getComponentName()= " + getComponentName());
        Log.d("Chem-onCreateOpts", "sv.setSearchableInfo= " + searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == (R.id.action_search)) {
            Toast.makeText(getApplicationContext(), "Search = " + onSearchRequested(), Toast.LENGTH_LONG).show();
            return onSearchRequested();
        } else {
            return false;
        }
    }

    @Override
    public boolean onSearchRequested() {
//        Bundle appData = new Bundle();
//        appData.putBoolean(ChemCompareActivity., true);
//        startSearch(null, false, false);
//        return true;
        return super.onSearchRequested();
    }

    /**
     * Enables relaunch of {@link MainActivity}.
     *
     * @param view the search view
     */
    public void goBack(View view) {
        Intent backIntent = new Intent(this, MainActivity.class);
        startActivity(backIntent);
    }


//    /**
//     * Adds and removes a chem to the local pantry.
//     *
//     * @param view the view to add to a pantry
//     */
//    public void onAddToPantryClicked(View view) {
//        int chemNo = (Integer) getIntent().getExtras().get(EXTRA_CHEMNO);
//
//        CheckBox addToPantry = (CheckBox) findViewById(R.id.addToPantry);
//
//        if (addToPantry.isChecked()) {
//            Chem chem = new Chem(chemName, ld50Val, compareChem, spNum);
//            new ChemCompareActivity.AddChemToPantryTask().execute(chem);
//        } else {
//            ContentValues chemNum = new ContentValues();
//            chemNum.put(PANTRYID, chemNo);
//            new ChemCompareActivity.RemoveChemFromPantryTask().execute(chemNum);
//        }
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class CheckPantryForChemTask extends AsyncTask<Integer, Void, Integer[]> {
        @Override
        protected Integer[] doInBackground(Integer... params) {
            int chemId = params[0];
            Integer[] chemInfo = {0, chemId};
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                Integer pantryId = PantryUtilities.getPantryIdIfExists(db, chemId);
                if (pantryId > 0) {
                    chemInfo[0] = pantryId;
                    return chemInfo;
                }
            } catch (SQLiteException e) {
                Log.d("PantryChemResults: ", "Caught SQLite Exception" + e.getMessage());
            }
            return chemInfo;
        }

        @Override
        protected void onPostExecute(Integer[] chemInfo) {
            if (chemInfo[0] > 0) {
                new ChemCompareActivity.PantryChemResultsTask().execute(chemInfo[0]);
//            } else {
//                new ChemCompareActivity.SearchChemIDTask().execute(chemInfo[1]);
            }
        }
    }

//    private class FetchBGGTask extends AsyncTask<Integer, Void, FetchItem> {
//        @Override
//        protected FetchItem doInBackground(Integer... params) {
//            int chemId = params[0];
//            try {
//                FetchItem fetchedItem = BGG.fetch(Arrays.asList(chemId), ThingType.BOARDGAME,
//                        ThingType.BOARDGAME_EXPANSION).iterator().next();
//                return fetchedItem;
//            } catch (FetchException e) {
//                Log.d("FetchBGGTask", "Fetch Exception: " + e.getMessage());
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(FetchItem fetchedItem) {
//
//            if (fetchedItem != null) {
//                //Populate the chem image
//                ImageView photo = (ImageView) findViewById(R.id.photo);
//                chemImageUrl = HTTPS + fetchedItem.getImageUrl();
//                Picasso.with(ChemCompareActivity.this).load(chemImageUrl).into(photo);
//
//                //Populate the chem name
//                TextView name = (TextView) findViewById(R.id.chem_name);
//                chemName = fetchedItem.getName();
//                name.setText(chemName);
//
//                //Populate the chem description
//                TextView description = (TextView) findViewById(R.id.description);
//                chemDescription = XmlEscape.unescapeXml(fetchedItem.getDescription());
//                chemDescription = HtmlEscape.unescapeHtml(chemDescription);
//                description.setText(chemDescription);
//
//                //Populate the chem theme
//                TextView theme = (TextView) findViewById(R.id.theme);
//                List<String> themeList = fetchedItem.getCategories();
//                chemThemes = join(", ", themeList);
//                theme.setText(chemThemes);
//
//            } else {
//                Toast toast = Toast.makeText(ChemCompareActivity.this,
//                        ERROR_RETRIEVE_GAME, Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        }
//    }

    private class PantryChemResultsTask extends AsyncTask<Integer, Void, Chem> {
        @Override
        protected Chem doInBackground(Integer... params) {
            int chemId = params[0];

            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                return PantryUtilities.getChem(db, chemId);
            } catch (SQLiteException e) {
                Log.d("PantryChemResults: ", "Caught SQLite Exception" + e.getMessage());
                return null;
            }
        }
//
//        @Override
//        protected void onPostExecute(Chem chem) {
//            if (chem != null) {
//
//                //Populate the chem name
//                TextView name = (TextView) findViewById(R.id.chem_name);
//                name.setText(chem.getName());
//
//                //Populate the chem blurb
//                TextView ld50 = (TextView) findViewById(R.id.ld50);
//                ld50.setText("The LD50 value for " + chem.getName() + " is: " + chem.getLd50Val() +
//                        ". This is about as toxic as: " + chem.getCompareChem() + ".");
//                //Populate the chem description
//                ImageView spectrum = (ImageView) findViewById(R.id.spectrum);
//                spectrum.setImageResource(chem.getSpectrumNum());
//
//                //Populate the pantry checkbox
//                CheckBox addToPantry = (CheckBox) findViewById(R.id.addToPantry);
//                addToPantry.setChecked(true);
//            } else {
//                Toast toast = Toast.makeText(ChemCompareActivity.this, DB_UNAVAIL, Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        }
//    }

        //Inner class to add the chem to the pantry
//    private class AddChemToPantryTask extends AsyncTask<Chem, Void, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(Chem... chem) {
//            Chem newChem = chem[0];
//            try {
//                SQLiteOpenHelper pantryDatabaseHelper =
//                        new PantryDatabaseHelper(ChemCompareActivity.this);
//                db = pantryDatabaseHelper.getWritableDatabase();
//                insertChem(db, newChem);
//                db.close();
//                return true;
//            } catch (SQLiteException e) {
//                Log.d("ChemCompareActivity", "SQLite Exception caught");
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Boolean success) {
//            if (!success) {
//                Toast.makeText(ChemCompareActivity.this, ERROR_SAVING_CHEM, Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(ChemCompareActivity.this, CHEM_SAVED, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    //Inner class to remove the chem from the Pantry
//    private class RemoveChemFromPantryTask extends AsyncTask<ContentValues, Void, Integer> {
//
//        @Override
//        protected Integer doInBackground(ContentValues... chems) {
//            Integer chemId = chems[0].getAsInteger(PANTRYID);
//            SQLiteOpenHelper pantryDatabaseHelper = new PantryDatabaseHelper(ChemCompareActivity.this);
//            try {
//                db = pantryDatabaseHelper.getWritableDatabase();
//                removeChemByPantryId(db, chemId);
//                db.close();
//                return chemId;
//            } catch (SQLiteException e) {
//                Log.d("ChemCompareActivity", "SQLite Exception caught while removing chem from db");
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Integer pantryId) {
//            if (pantryId == null) {
//                Toast toast = Toast.makeText(ChemCompareActivity.this,
//                        DB_UNAVAIL, Toast.LENGTH_SHORT);
//                toast.show();
//            } else {
//                Toast toast = Toast.makeText(ChemCompareActivity.this,
//                        CHEM_REMOVED, Toast.LENGTH_SHORT);
//                toast.show();
//                Intent intent = new Intent(ChemCompareActivity.this, PantryActivity.class);
//                intent.putExtra(ChemCompareActivity.EXTRA_CHEMNO, pantryId);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//            }
//        }
//    }
    }
}