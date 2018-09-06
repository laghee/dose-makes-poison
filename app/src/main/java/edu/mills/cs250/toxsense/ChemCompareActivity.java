/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.app.SearchManager;
import android.content.ContentValues;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

    protected static final String EXTRA_CLASSNAME = "class";
    protected static final String EXTRA_PANTRY_ID = "pantryId";
    protected static final String PANTRY_ACTIVITY = "PantryActivity";
    private static final String TAG = "ChemCompare";
    private static final String WEB_ACTIVITY = "WebActivity";
    private static final String ERROR_RETRIEVE_CHEM = "Error retrieving chem.";
    private static final String DB_UNAVAIL = "Database unavailable.";
    private static final String ERROR_SAVING_CHEM = "Error saving chem.";
    private static final String CHEM_SAVED = "Chem saved!";
    private static final String CHEM_REMOVED = "Chem removed.";
    private static final String WATER = "water";
    private static final String SUGAR = "sugar";
    private static final String ALCOHOL = "alcohol";
    private static final String SALT = "salt";
    private static final String CAFFEINE = "caffeine";
    private static final String ARSENIC = "arsenic";
    private static final String CYANIDE = "cyanide";
    private static final String BETWEEN = "between";
    private static final String ROW_ID = "pantryRowId";
    private static final String SEARCH_DONE = "searchPerformed";
    private static final String CHEM_NAME = "chemName";
    private static final String CHEM_ID = "chemId";
    private static final String LD50 = "ld50Num";
    private static final String COMPARISON_VIEW_ID = "viewId";
    private static final String COMPARISON_CHEM = "comparisonName";
    private static final String OPTIONAL_EXTRA_CHEM = "secondName";
    private static final String IS_MORE_TOXIC = "moreTox";
    private static final String TOX_DIFFERENCE = "ld50Diff";
    // The LD50 values for water, sugar, alcohol, salt, caffeine, arsenic, and cyanide, respectively
    private static final int [] TOX_SPECTRUM = {90000, 30000, 7060, 3000, 200, 15, 5};

    private boolean web = true;
    private SQLiteDatabase chemRefDb;
    private SQLiteDatabase pantryDb;
    private String chemName;
    private String chemId;
    private int ld50Val;
    private String comparisonChem;
    private String secondComparison;
    private int comparisonViewId;
    private int pantryId;
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
        if (savedInstanceState != null && savedInstanceState.getBoolean(SEARCH_DONE)) {
            displaySearchResults(savedInstanceState);
        } else {
            Log.d(TAG, "Intent logged: " + getIntent());
            handleIntent(getIntent());
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
        Log.d(TAG, "Intent to handle = " + intent + " EXTRAS: " + intent.getExtras());
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d(TAG, "Intent to handle = ACTION_SEARCH");
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "Search = " + query);
            new ChemRefLookupTask().execute(query);
        } else if ((intent.getExtras() != null) && (intent.getExtras().get(EXTRA_CLASSNAME).equals(PANTRY_ACTIVITY))) {
            Log.d(TAG, "Non-search intent passed to handleIntent(): " + intent.getExtras());
            pantryId = (int) intent.getExtras().get(EXTRA_PANTRY_ID);
            new PantryChemResultsTask().execute(pantryId);
        } else {
            Log.d(TAG, "Non-search, non-pantry intent passed to handleIntent().");
        }
    }

    private boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE;
    }

    private void handleLandscapeOrientation(TextView explanation) {
        RelativeLayout body = findViewById(R.id.relativelayout_comparebody);
        FrameLayout.LayoutParams bodyLp = (FrameLayout.LayoutParams) body.getLayoutParams();
        bodyLp.setMargins(0, 130, 0 ,0);
        body.setLayoutParams(bodyLp);
        GridLayout spectrum = findViewById(R.id.gridlayout_toxscale);
        spectrum.setOrientation(GridLayout.HORIZONTAL);
        RelativeLayout.LayoutParams explanationLp = (RelativeLayout.LayoutParams) explanation.getLayoutParams();
        explanationLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        explanationLp.removeRule(RelativeLayout.END_OF);
        explanation.setLayoutParams(explanationLp);
        RelativeLayout.LayoutParams spectrumLp = (RelativeLayout.LayoutParams) spectrum.getLayoutParams();
        spectrumLp.addRule(RelativeLayout.BELOW, R.id.textview_chemcompare);
        spectrumLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        spectrum.setLayoutParams(spectrumLp);
    }

    private void displaySearchResults(Bundle results) {
        TextView name = findViewById(R.id.textview_chemname);
        chemName = results.getString(CHEM_NAME);
        name.setText(chemName);
        TextView explanation = findViewById(R.id.textview_chemcompare);
        String explanationText;
        ld50Val = results.getInt(LD50);
        comparisonChem = results.getString(COMPARISON_CHEM);
        secondComparison = results.getString(OPTIONAL_EXTRA_CHEM);
        if (!secondComparison.equals("")) {
            explanationText = String.format(getString(R.string.between_values), chemName, ld50Val, comparisonChem, secondComparison);
        } else {
            explanationText = String.format(getString(R.string.comparison_text), chemName, ld50Val, comparisonChem);
        }
        explanationText = explanationText.concat(getString(R.string.results_disclaimer));
        explanation.setText(explanationText);
        if (isLandscapeMode(getApplicationContext())) {
            handleLandscapeOrientation(explanation);
        }
        comparisonViewId = results.getInt(COMPARISON_VIEW_ID);
        TextView compareView = findViewById(comparisonViewId);
        compareView.setForeground(getResources().getDrawable(R.drawable.black_dot, null));
        compareView.setForegroundGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        findViewById(R.id.textview_firstsearch).setVisibility(View.GONE);
        findViewById(R.id.framelayout_chemcomparefab).setVisibility(View.VISIBLE);
        fab.show();
    }

    private Bundle getToxComparisonResults(int ld50) {
        Bundle toxComparisonResults = new Bundle(5);
        int upperChemIndex = -1;
        int lowerChemIndex = -1;
        int lowDiff = -1;
        int highDiff = -1;
        boolean moreToxThan = false;
        int toxDiff = 0;
        int closestChemIndex = -1;
        String closestChemName;
        int closestChemViewId;
        String optSecondChem = "";

        for (int i = 0; upperChemIndex < 0; i++) {
            if (ld50 < TOX_SPECTRUM[i]) {
                lowerChemIndex = i;
                lowDiff = TOX_SPECTRUM[i] - ld50;
            } else {
                upperChemIndex = i;
                highDiff = ld50 - TOX_SPECTRUM[i];
            }
        }
        if (lowDiff < highDiff) {
            moreToxThan = true;
            toxDiff = lowDiff;
            closestChemIndex = lowerChemIndex;
        } else if (lowDiff > highDiff) {
            toxDiff = highDiff;
            closestChemIndex = upperChemIndex;
        }

        Bundle chemValues = getComparisonChemValues(closestChemIndex);
        closestChemViewId = chemValues.getInt(COMPARISON_VIEW_ID);
        closestChemName = chemValues.getString(COMPARISON_CHEM);

        if (closestChemName.equals(BETWEEN)) {
            chemValues = getComparisonChemValues(lowerChemIndex);
            closestChemName = chemValues.getString(COMPARISON_CHEM);
            chemValues = getComparisonChemValues(upperChemIndex);
            optSecondChem = chemValues.getString(COMPARISON_CHEM);
        }
        toxComparisonResults.putBoolean(IS_MORE_TOXIC, moreToxThan);
        toxComparisonResults.putInt(TOX_DIFFERENCE, toxDiff);
        toxComparisonResults.putString(COMPARISON_CHEM, closestChemName);
        toxComparisonResults.putString(OPTIONAL_EXTRA_CHEM, optSecondChem);
        toxComparisonResults.putInt(COMPARISON_VIEW_ID, closestChemViewId);
        return toxComparisonResults;
    }

    private Bundle getComparisonChemValues(int closestChemIndex) {
        Bundle toxComparison = new Bundle(2);
        String closestChemName;
        int closestChemViewId;

        switch (closestChemIndex) {
            case 0: closestChemName = WATER;
                closestChemViewId = R.id.textview_water;
                break;
            case 1: closestChemName = SUGAR;
                closestChemViewId = R.id.textview_sugar;
                break;
            case 2: closestChemName = ALCOHOL;
                closestChemViewId = R.id.textview_alcohol;
                break;
            case 3: closestChemName = SALT;
                closestChemViewId = R.id.textview_salt;
                break;
            case 4: closestChemName = CAFFEINE;
                closestChemViewId = R.id.textview_caffeine;
                break;
            case 5: closestChemName = ARSENIC;
                closestChemViewId = R.id.textview_arsenic;
                break;
            case 6: closestChemName = CYANIDE;
                closestChemViewId = R.id.textview_cyanide;
                break;
            default: closestChemName = BETWEEN;
                closestChemViewId = -1;
                break;
        }

        toxComparison.putString(COMPARISON_CHEM, closestChemName);
        toxComparison.putInt(COMPARISON_VIEW_ID, closestChemViewId);
        return toxComparison;
    }

    /**
     * Adds or removes a chem to or from the local pantry.
     *
     * @param view the view to add to or remove from a pantry
     */
    public void onAddToPantryClicked(View view) {

        ToggleButton addToPantry = findViewById(R.id.button_addtopantry);

        if (addToPantry.isChecked()) {
            Chem chem = new Chem(chemName, chemId, ld50Val, comparisonChem, comparisonViewId);
            new AddChemToPantryTask().execute(chem);
        } else {
            ContentValues chemNum = new ContentValues();
            chemNum.put(ROW_ID, pantryId);
            new RemoveChemFromPantryTask().execute(chemNum);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceInfo) {
        super.onSaveInstanceState(savedInstanceInfo);
        savedInstanceInfo.putString(CHEM_NAME, chemName);
        savedInstanceInfo.putString(CHEM_ID, chemId);
        savedInstanceInfo.putInt(LD50, ld50Val);
        savedInstanceInfo.putString(COMPARISON_CHEM, comparisonChem);
        savedInstanceInfo.putString(OPTIONAL_EXTRA_CHEM, secondComparison);
        savedInstanceInfo.putInt(COMPARISON_VIEW_ID, comparisonViewId);
        Boolean searchPerformed = false;
        if (findViewById(R.id.textview_firstsearch).getVisibility() == View.GONE) {
            searchPerformed = true;
        }
        savedInstanceInfo.putBoolean(SEARCH_DONE, searchPerformed);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chemRefDb != null) {
            chemRefDb.close();
        }
        if (pantryDb != null) {
            pantryDb.close();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private class ChemRefLookupTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String searchTerm = params[0];
            try {
                SQLiteOpenHelper chemRefDatabaseHelper =
                        new ChemRefDatabaseHelper(ChemCompareActivity.this);
                chemRefDb = chemRefDatabaseHelper.getReadableDatabase();
                chemName = searchTerm;
                return ChemRefUtilities.getChemId(chemRefDb, searchTerm);
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

            if ((ld50Text !=  null) && (!ld50Text.equals(""))) {
                String medLethalDose = StringUtils.substringBetween(ld50Text, "(", ")");
                medLethalDose = StringUtils.removeEnd(medLethalDose, "mg/kg");
                medLethalDose = StringUtils.removeEndIgnoreCase(medLethalDose, "ml/kg");
                try {
                    ld50Val = Integer.parseInt(medLethalDose);
                    Log.d(TAG, "LD50 retrieved: " + ld50Val);
                    Bundle toxComparisonResults = getToxComparisonResults(ld50Val);
                    toxComparisonResults.putString(CHEM_NAME, chemName);
                    toxComparisonResults.putInt(LD50, ld50Val);
                    displaySearchResults(toxComparisonResults);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "Caught error: " + e.getMessage());
                }
            } else {
                Log.d(TAG, "NO LD50 NUMBER FOUND!");
                Toast.makeText(ChemCompareActivity.this,
                        "NO TOXICITY VALUE FOUND ON TOXNET!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //    private class CheckPantryForChemTask extends AsyncTask<String, Void, String[]> {
//        @Override
//        protected String[] doInBackground(String... params) {
//            String casNum = params[0];
//            String[] chemInfo = {null, casNum};
//            try {
//                SQLiteOpenHelper pantryDatabaseHelper =
//                        new PantryDatabaseHelper(ChemCompareActivity.this);
//                db = pantryDatabaseHelper.getReadableDatabase();
//                Integer pantryId = PantryUtilities.getPantryIdIfExists(db, casNum);
//                if (pantryId > 0) {
//                    chemInfo[0] = pantryId.toString();
//                    return chemInfo;
//                }
//            } catch (SQLiteException e) {
//                Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
//            }
//            return chemInfo;
//        }
//
//        @Override
//        protected void onPostExecute(String[] chemInfo) {
//            if (Integer.parseInt(chemInfo[0]) > 0) {
//                new PantryChemResultsTask().execute(Integer.parseInt(chemInfo[0]));
//            } else {
//                new SearchChemIDTask().execute(chemInfo[1]);
//            }
//        }
//    }

    private class PantryChemResultsTask extends AsyncTask<Integer, Void, Chem> {
        @Override
        protected Chem doInBackground(Integer... params) {
            int rowId = params[0];
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                pantryDb = pantryDatabaseHelper.getReadableDatabase();
                return PantryUtilities.getChem(pantryDb, rowId);
            } catch (SQLiteException e) {
                Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Chem chem) {
            if (chem != null) {
                Bundle chemProps = new Bundle(5);
                chemProps.putString(CHEM_NAME, chem.getName());
                chemProps.putString(CHEM_ID, chem.getChemId());
                chemProps.putInt(LD50, chem.getLd50Val());
                chemProps.putString(COMPARISON_CHEM, chem.getComparisonChem());
                chemProps.putString(OPTIONAL_EXTRA_CHEM, "");
                chemProps.putInt(COMPARISON_VIEW_ID, chem.getComparisonViewId());
                displaySearchResults(chemProps);
                ToggleButton toggleButton = findViewById(R.id.button_addtopantry);
                toggleButton.setChecked(true);
            } else {
                Toast toast = Toast.makeText(ChemCompareActivity.this, DB_UNAVAIL, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

        //Inner class to add the chem to the pantry
    private class AddChemToPantryTask extends AsyncTask<Chem, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Chem... chem) {
            Chem newChem = chem[0];
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new PantryDatabaseHelper(ChemCompareActivity.this);
                pantryDb = pantryDatabaseHelper.getWritableDatabase();
                PantryUtilities.insertChem(pantryDb, newChem);
                pantryDb.close();
                return true;
            } catch (SQLiteException e) {
                Log.d(TAG, "SQLite Exception caught");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(ChemCompareActivity.this, ERROR_SAVING_CHEM, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChemCompareActivity.this, CHEM_SAVED, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Inner class to remove the chem from the Pantry
    private class RemoveChemFromPantryTask extends AsyncTask<ContentValues, Void, Integer> {

        @Override
        protected Integer doInBackground(ContentValues... chems) {
//            String chemId = chems[0].getAsString(CHEM_ID);
            int rowId = chems[0].getAsInteger(ROW_ID);
            SQLiteOpenHelper pantryDatabaseHelper = new PantryDatabaseHelper(ChemCompareActivity.this);
            try {
                pantryDb = pantryDatabaseHelper.getWritableDatabase();
//                int pantryId = PantryUtilities.getPantryIdIfExists(pantryDb, chemId);
                PantryUtilities.removeChemByPantryId(pantryDb, rowId);
                pantryDb.close();
                return rowId;
            } catch (SQLiteException e) {
                Log.d(TAG, "SQLite Exception caught while removing chem from db");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer rowId) {
            if (rowId == null) {
                Toast toast = Toast.makeText(ChemCompareActivity.this,
                        DB_UNAVAIL, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(ChemCompareActivity.this,
                        CHEM_REMOVED, Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(ChemCompareActivity.this, PantryActivity.class);
                intent.putExtra(ChemCompareActivity.EXTRA_PANTRY_ID, pantryId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

}
