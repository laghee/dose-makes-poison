/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChemRefDatabaseHelper extends SQLiteOpenHelper {

    /**
     * A local SQLite database for inserting into and deleting chemicals from a local pantry.
     */
    private static final String TAG = "ChemRefDBhelper";
    private final Context context;

    /**
     * Name of database table keeping track of all saved chemicals in personal pantry.
     */
    static final String CHEMREF_TABLE = "CHEMREF";

    /**
     * Name of column in {@link #CHEMREF_TABLE} with name of chemical.
     */
    static final String NAME_COL = "NAME";

    /**
     * Name of column in {@link #CHEMREF_TABLE} with CAS Registry number.
     */
    static final String CHEMID_COL = "CHEMID";


    private static final String DB_NAME = "chem_ref_db"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database

    private boolean createDb = false;


    ChemRefDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate DB");
        createDb = true;
        }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade DB");
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.d(TAG, "onOpen DB");
        if (createDb) {
            createDb = false;
            copyDatabaseFromAssets(db);
        }
    }

    private void copyDatabaseFromAssets(SQLiteDatabase db) {
        Log.d(TAG, "copy DB");
        InputStream dbInput = null;
        OutputStream dbOutput = null;
        try {
            dbInput = context.getAssets().open("databases/toxsense_db.sql");
            dbOutput = new FileOutputStream(db.getPath());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dbInput.read(buffer)) > 0) {
                dbOutput.write(buffer, 0, length);
            }
            dbOutput.flush();

            SQLiteDatabase copiedDb = context.openOrCreateDatabase(DB_NAME, 0, null);
            copiedDb.execSQL("PRAGMA user_version = " + DB_VERSION);
            copiedDb.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(TAG + " Error copying database");
        } finally {
            try {
                if (dbOutput != null) {
                    dbOutput.close();
                }
                if (dbInput != null) {
                    dbInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error(TAG + " Error closing streams");
            }
        }
    }


}
