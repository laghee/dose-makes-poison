package edu.mills.cs250.dosemakespoison;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static edu.mills.cs250.dosemakespoison.PantryDBUtilities.*;

/**
 * Activity for viewing ingredient details. Provides an interface for adding or removing ingredients
 * from a local pantry by clicking a checkbox. If a user removes an ingredient,
 * {@link PantryActivity} is launched and they are taken back to their pantry.
 * <p>
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

public class ChemResultsActivity extends Activity {

    private boolean web = true;

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

    private static final String SEARCH_ACTIVITY = "SearchResultsActivity";
    private static final String PANTRY_ACTIVITY = "PantryActivity";
    private static final String ERROR_RETRIEVE_CHEM = "Error retrieving chem.";
    private static final String DB_UNAVAIL = "Database unavailable.";
    private static final String PANTRYID = "PANTRYID";
    private static final String ERROR_SAVING_CHEM = "Error saving chem.";
    private static final String CHEM_SAVED = "Chem saved!";
    private static final String CHEM_REMOVED = "Chem removed.";
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private SQLiteDatabase db;
    private String chemName;
    private String ld50Num;
    private String chemImageUrl;
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_results);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //Get the chem from the intent
        int chemNo = (Integer) getIntent().getExtras().get(EXTRA_CHEMNO);
        String className = (String) getIntent().getExtras().get(EXTRA_CLASSNAME);

        switch (className) {
            case PANTRY_ACTIVITY:
                web = false;
                new ChemResultsActivity.PantryChemResultsTask().execute(chemNo);
                break;
            case SEARCH_ACTIVITY:
                new ChemResultsActivity.CheckPantryForChemTask().execute(chemNo);
                break;
            default:
                Log.d("ChemResults", "Error, class not found.");
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Adds and removes a chem to the local pantry.
     *
     * @param view the view to add to a pantry
     */
    public void onAddToPantryClicked(View view) {
        int chemNo = (Integer) getIntent().getExtras().get(EXTRA_CHEMNO);

        CheckBox addToPantry = (CheckBox) findViewById(R.id.addToPantry);

        if (addToPantry.isChecked()) {
            Chem chem = new Chem(chemImageUrl, chemName, ld50Num, chemNo);
            new ChemResultsActivity.AddChemToPantryTask().execute(chem);
        } else {
            ContentValues chemNum = new ContentValues();
            chemNum.put(PANTRYID, chemNo);
            new ChemResultsActivity.RemoveChemFromPantryTask().execute(chemNum);
        }
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

    private class CheckPantryForChemTask extends AsyncTask<Integer, Void, Integer[]> {
        @Override
        protected Integer[] doInBackground(Integer... params) {
            int chemId = params[0];
            Integer[] chemInfo = {0, chemId};
            try {
                SQLiteOpenHelper pantryDatabaseHelper =
                        new SQLitePantryDatabaseHelper(ChemResultsActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                Integer pantryId = getPantryIdIfExists(db, chemId);
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
                new ChemResultsActivity.PantryChemResultsTask().execute(chemInfo[0]);
            } else {
                new ChemResultsActivity.SearchWebTask().execute(chemInfo[1]);
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
//                Picasso.with(ChemResultsActivity.this).load(chemImageUrl).into(photo);
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
//                Toast toast = Toast.makeText(ChemResultsActivity.this,
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
                        new SQLitePantryDatabaseHelper(ChemResultsActivity.this);
                db = pantryDatabaseHelper.getReadableDatabase();
                Chem chem = getChem(db, chemId);
                return chem;
            } catch (SQLiteException e) {
                Log.d("PantryChemResults: ", "Caught SQLite Exception" + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Chem chem) {
            if (chem != null) {
                //Populate the chem spectrum
                ImageView photo = (ImageView) findViewById(R.id.photo);
                Picasso.with(ChemResultsActivity.this).load(chem.getImage()).into(photo);

                //Populate the chem name
                TextView name = (TextView) findViewById(R.id.chem_name);
                name.setText(chem.getName());

                //Populate the chem description
                TextView description = (TextView) findViewById(R.id.description);
                description.setText(chem.getDescription());


                //Populate the pantry checkbox
                CheckBox addToPantry = (CheckBox) findViewById(R.id.addToPantry);
                addToPantry.setChecked(true);
            } else {
                Toast toast = Toast.makeText(ChemResultsActivity.this, DB_UNAVAIL, Toast.LENGTH_SHORT);
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
                        new SQLitePantryDatabaseHelper(ChemResultsActivity.this);
                db = pantryDatabaseHelper.getWritableDatabase();
                insertChem(db, newChem);
                db.close();
                return true;
            } catch (SQLiteException e) {
                Log.d("ChemResultsActivity", "SQLite Exception caught");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(ChemResultsActivity.this, ERROR_SAVING_CHEM, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChemResultsActivity.this, CHEM_SAVED, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Inner class to remove the chem from the Pantry
    private class RemoveChemFromPantryTask extends AsyncTask<ContentValues, Void, Integer> {

        @Override
        protected Integer doInBackground(ContentValues... chems) {
            Integer chemId = chems[0].getAsInteger(PANTRYID);
            SQLiteOpenHelper pantryDatabaseHelper = new SQLitePantryDatabaseHelper(ChemResultsActivity.this);
            try {
                db = pantryDatabaseHelper.getWritableDatabase();
                if (web) {
                    removeChemByWebId(db, chemId);
                } else {
                    removeChemByPantryId(db, chemId);
                }
                db.close();
                return chemId;
            } catch (SQLiteException e) {
                Log.d("ChemResultsActivity", "SQLite Exception caught while removing chem from db");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer pantryId) {
            if (pantryId == null) {
                Toast toast = Toast.makeText(ChemResultsActivity.this,
                        DB_UNAVAIL, Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(ChemResultsActivity.this,
                        CHEM_REMOVED, Toast.LENGTH_SHORT);
                toast.show();
                Intent intent = new Intent(ChemResultsActivity.this, PantryActivity.class);
                intent.putExtra(ChemResultsActivity.EXTRA_CHEMNO, pantryId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

}
