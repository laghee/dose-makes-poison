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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



/**
 * Activity for viewing ingredient comparison. Provides an interface for adding or removing ingredients
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
    private static final String TAG = "ChemCompare";
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
    private String chemId;
    private int ld50Val;
    private String compareChem;
    private int spNum;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chem_compare);
        Toolbar toxTool = findViewById(R.id.tox_toolbar);
        setSupportActionBar(toxTool);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        fab = findViewById(R.id.fab);
        if (savedInstanceState != null && savedInstanceState.getBoolean("searchPerformed")) {
            chemName = savedInstanceState.getString("name");
            chemId = savedInstanceState.getString("id");
            ld50Val = savedInstanceState.getInt("LD50");
            TextView name = findViewById(R.id.textview_chemname);
            name.setText(chemName);
            TextView explanation = findViewById(R.id.textview_chemcompare);
            explanation.setText("The Lethal Median Dose for " + chemName + " (" + chemId + ") is: "
                    + ld50Val + " mg/kg.");
            findViewById(R.id.textview_firstsearch).setVisibility(View.GONE);
            int format = getApplicationContext().getResources().getConfiguration().orientation;
            if (format == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                GridLayout toxLayout = findViewById(R.id.gridlayout_toxscale);
                toxLayout.setOrientation(GridLayout.HORIZONTAL);
            }
            findViewById(R.id.framelayout_chemcomparefab).setVisibility(View.VISIBLE);
            fab.show();
        } else {
            handleIntent(getIntent());
            fab.show();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,"You'd like to share this ingredient! Yay!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                View chemView = findViewById(R.id.framelayout_compare);
                chemView.setDrawingCacheEnabled(true);
                Bitmap screen = chemView.getDrawingCache();
                FileOutputStream output;
                try {
                    output = openFileOutput("toxinfo.png", Context.MODE_PRIVATE);
                    screen.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.close();
                } catch (IOException e) {
                    Snackbar.make(chemView, "Ooops! Problem capturing your screen!", Snackbar.LENGTH_LONG);
                }
                File pngFile = new File(getApplicationContext().getFilesDir(), "toxinfo.png");
                Uri uriToImage = FileProvider.getUriForFile(getApplicationContext(), "edu.mills.cs250.toxsense.fileprovider", pngFile);
                Intent shareInfo = new Intent(Intent.ACTION_SEND);
                shareInfo.putExtra(Intent.EXTRA_STREAM, uriToImage);
                shareInfo.setType("image/png");
                shareInfo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareInfo, getResources().getText(R.string.share_info)));
            }
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
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) search.getActionView();
        // Get the SearchView and set the searchable configuration
        sv.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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
            new ChemRefLookupTask().execute(query);
        }
    }



//    /**
//     * Adds and removes a chem to the local pantry.
//     *
//     * @param view the view to add to a pantry
//     */
//    public void onAddToPantryClicked(View view) {
//        int chemNo = (Integer) getIntent().getExtras().get(EXTRA_CHEMNO);
//
//        CheckBox addToPantry = (CheckBox) findViewById(R.id.button_addtopantry);
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
    public void onSaveInstanceState(Bundle savedInstanceInfo) {
        super.onSaveInstanceState(savedInstanceInfo);
        savedInstanceInfo.putString("name", chemName);
        savedInstanceInfo.putString("id", chemId);
        savedInstanceInfo.putInt("LD50", ld50Val);
        Boolean searchPerformed = false;
        if (findViewById(R.id.textview_firstsearch).getVisibility() == View.GONE) {
            searchPerformed = true;
        }
        savedInstanceInfo.putBoolean("searchPerformed", searchPerformed);
    }

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
            String casNum = params[0];
            String[] chemInfo = {null, casNum};
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                Integer pantryId = PantryUtilities.getPantryIdIfExists(db, casNum);
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
                chemName = searchTerm;
                return ChemRefUtilities.getChemId(db, searchTerm);
            } catch (SQLiteException e) {
                Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String regNum) {

            if (regNum != null) {
                Log.d(TAG + " ChRfLkPost", "RegNum = " + regNum);
                chemId = regNum;
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
                Log.d(TAG + " ToxTask", "Connected to: " + toxnetUrl);

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
                String medLethalDose = StringUtils.substringBetween(ld50Text, "(", ")");
                Log.d(TAG, "Grabbed substring btwn parens: '" + StringEscapeUtils.escapeJava(medLethalDose) + "'");
                medLethalDose = StringUtils.removeEnd(medLethalDose, "mg/kg");
                Log.d(TAG, "Removed mg/kg: " + medLethalDose);
                medLethalDose = StringUtils.removeEndIgnoreCase(medLethalDose, "ml/kg");
                Log.d(TAG, "Removed ml/kg: " + medLethalDose);
                try {
                    ld50Val = Integer.parseInt(medLethalDose);
                    Log.d(TAG, "LD50 retrieved: " + ld50Val);
                    TextView name = findViewById(R.id.textview_chemname);
                    name.setText(chemName);
                    TextView explanation = findViewById(R.id.textview_chemcompare);
                    explanation.setText("The Lethal Median Dose for " + chemName + " (" + chemId + ") is: "
                            + ld50Val + " mg/kg.");
                    findViewById(R.id.textview_firstsearch).setVisibility(View.GONE);
                    findViewById(R.id.framelayout_chemcomparefab).setVisibility(View.VISIBLE);
                } catch (NumberFormatException e) {
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
            int rowId = params[0];
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                return PantryUtilities.getChem(db, rowId);
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
//                TextView name = (TextView) findViewById(R.id.textview_chemname);
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
//                CheckBox addToPantry = (CheckBox) findViewById(R.id.button_addtopantry);
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
//            Integer rowId = chems[0].getAsInteger(PANTRYID);
//            SQLiteOpenHelper pantryDatabaseHelper = new PantryDatabaseHelper(ChemCompareActivity.this);
//            try {
//                db = pantryDatabaseHelper.getWritableDatabase();
//                removeChemByPantryId(db, rowId);
//                db.close();
//                return rowId;
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