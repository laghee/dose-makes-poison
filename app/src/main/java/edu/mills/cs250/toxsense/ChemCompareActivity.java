/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.app.SearchManager;
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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**Activity for viewing ingredient comparison. Provides an interface for adding or removing ingredients
 * from a local pantry by clicking a checkbox. If a user removes an ingredient,
 * {@link PantryActivity} is launched and they are taken back to their pantry.
 */

public class ChemCompareActivity extends AppCompatActivity {

    private static final String TAG = "ChemCompare";

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
        Log.d(TAG, "Toolbar value is: " + toxTool);
        setSupportActionBar(toxTool);
        Log.d(TAG, "getSupportActionBar() returns: " + getSupportActionBar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        handleIntent(getIntent());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
            fab.hide();
//           TODO****: Handle share action
            Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });


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
//                Log.d(TAG, "Error, incoming class not found.");
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        Log.d(TAG, "searchManager = " + searchManager);
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) search.getActionView();
        Log.d(TAG, "ActionView= " + sv);
        // Get the SearchView and set the searchable configuration
        sv.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        Log.d(TAG, "getComponentName()= " + getComponentName());
        Log.d(TAG, "sv.setSearchableInfo= " + searchManager.getSearchableInfo(getComponentName()));
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

    private void handleIntent(Intent intent) {
        // Get the intent, verify the action and get the query
        Log.d(TAG, "Intent to handle = " + intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d(TAG, "Intent to handle = ACTION_SEARCH");
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "Search = " + query);
//            Toast.makeText(getApplicationContext(), "Search = " + query, Toast.LENGTH_LONG).show();
//                doMySearch(query);
            new ChemRefLookupTask().execute(query);
        }
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

    private class CheckPantryForChemTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String chemId = params[0];
            String[] chemInfo = {null, chemId};
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                Integer pantryId = PantryUtilities.getPantryIdIfExists(db, chemId);
                if (pantryId > 0) {
                    chemInfo[0] = pantryId.toString();
                    return chemInfo;
                }
            } catch (SQLiteException e) {
                Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
            }
            return chemInfo;
        }

        @Override
        protected void onPostExecute(String[] chemInfo) {
            if (Integer.parseInt(chemInfo[0]) > 0) {
                new ChemCompareActivity.PantryChemResultsTask().execute(Integer.parseInt(chemInfo[0]));
//            } else {
//                new ChemCompareActivity.SearchChemIDTask().execute(chemInfo[1]);
            }
        }
    }

    private class ChemRefLookupTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String searchTerm = params[0];
            try {
                SQLiteOpenHelper chemRefDatabaseHelper =
                        new ChemRefDatabaseHelper(ChemCompareActivity.this);
                db = chemRefDatabaseHelper.getReadableDatabase();
                return ChemRefUtilities.getChemId(db, searchTerm);
            } catch (SQLiteException e) {
                Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String regNum) {

            if (regNum != null) {
                Log.d(TAG+" ChRfLkPost", "RegNum = " + regNum);
                Toast.makeText(ChemCompareActivity.this, "Chem/CAS registry num = "
                        + regNum, Toast.LENGTH_SHORT).show();
                new ToxnetWebTask().execute(regNum);
            } else {
                Toast toast = Toast.makeText(ChemCompareActivity.this,
                        "ERROR RETRIEVING CHEMID NUMBER", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private class ToxnetWebTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String toxnetUrl = "https://chem.nlm.nih.gov/chemidplus/rn/" + params[0];
            String lethalDoseVal = "";

            try {
                Document doc = Jsoup.connect(toxnetUrl).get();
                Log.d(TAG+" ToxTask", "Connected to: " + toxnetUrl);

                //select the toxicity section
                Elements toxicity = doc.select("div#toxicity");
                Log.d(TAG, "Got Tox section");

                //select the table
                Elements table = toxicity.select("tbody");
                Log.d(TAG, "Got table");

                //select the rows in the table
                Elements rows = table.select("tr");
                Log.d(TAG, "Got rows");


                for (int i = 0; i < rows.size(); i++) {
                    Log.d(TAG, "Looping through row " + i);
                    Element row = rows.get(i);
                    Elements cols = row.select("td");
                    if ((cols.get(1).text().equals("LD50")) && (cols.get(2).text().equals("oral")) &&
                            (cols.get(0).text().equals("rat"))) {
                        lethalDoseVal = cols.get(3).text();
                        Log.d(TAG, "Added a valid value: " + lethalDoseVal + " at row: " + i);
                    }
                }

                if (lethalDoseVal.length() != 0) {
                    Log.d(TAG, "About to return value from JSoup: " + lethalDoseVal);
                    return lethalDoseVal;
                } else {
                    for (int i = 0; i < rows.size(); i++) {
                        Element row = rows.get(i);
                        Elements cols = row.select("td");

                        if ((cols.get(1).text().equals("LD50")) && (cols.get(2).text().equals("oral")) &&
                                (cols.get(0).text().equals("mouse"))) {
                            lethalDoseVal = cols.get(3).text();
                        }

                    }
                    Log.d(TAG, "About to return value from JSoup: " + lethalDoseVal);
                    return lethalDoseVal;
                }
            } catch (IOException e) {
                Log.d(TAG, "Caught IO Exception" + e.getMessage());
                Toast.makeText(ChemCompareActivity.this, "IO EXCEPTION: "
                        + e.getMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        /* TODO set up progress bar or spinny whatsit */
//        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
//        }


        @Override
        protected void onPostExecute(String ld50Text) {

            if (ld50Text != null) {
                Log.d(TAG, "LD50 = " + ld50Text);
                String ld50Val = StringUtils.substringBetween(ld50Text, "(", ")");
                Log.d(TAG, "Grabbed substring btwn parens: " + ld50Val);
                ld50Val = ld50Val.substring(0, ld50Val.indexOf("m"));
                Log.d(TAG, "Removed mg/kg: " + ld50Val);
                try {
                    Log.d(TAG, "LD50 retrieved: " + Integer.parseInt(ld50Val));
                    Toast.makeText(ChemCompareActivity.this, "LD50 NUMBER FOUND: "
                            + Integer.parseInt(ld50Val), Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e){
                    Log.d(TAG, "Caught error: " + e.getMessage());
                }
            } else {
                Log.d(TAG, "NO LD50 NUMBER FOUND!");
                Toast.makeText(ChemCompareActivity.this,
                        "NO LD50 NUMBER FOUND!", Toast.LENGTH_SHORT).show();
            }
        }
    }



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
                Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
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
//                Log.d(TAG, "SQLite Exception caught");
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
//                Log.d(TAG, "SQLite Exception caught while removing chem from db");
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