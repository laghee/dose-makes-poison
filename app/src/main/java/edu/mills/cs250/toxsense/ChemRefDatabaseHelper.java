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

public class ChemRefDatabaseHelper extends SQLiteOpenHelper {

    /**
     * Name of reference database table keeping track of chemical names, synonyms, ChemIDPlus
     * database id, and CAS Registry numbers.
     */
    static final String CHEMREF_TABLE = "CHEMREF";

    /**
     * Name of column in {@link #CHEMREF_TABLE} with name of chemical.
     */
    static final String NAME_COL = "NAME";

    /**
     * Name of column in {@link #CHEMREF_TABLE} with LD50 value of chemical.
     */
    static final String CHEMID_COL = "CHEMID";

    /**
     * Name of column in {@link #CHEMREF_TABLE} with chemical comparison name.
     */
    static final String SYN_COL = "SYNONYMS";

    /**
     * Name of column in {@link #CHEMREF_TABLE} with chemical comparison name.
     */
    static final String CAS_COL = "CASNUM";

    /**
     * A local SQLite database for inserting into and deleting chemicals from a local pantry.
     */
    private static final String DB_NAME = "chemrefdatabase"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database


    ChemRefDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE CHEMREF (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAME_COL + " TEXT NOT NULL, "
                    + CHEMID_COL + " INTEGER NOT NULL, "
                    + SYN_COL + " TEXT NOT NULL, "
                    + CAS_COL + " INTEGER NOT NULL);");
        }
    }
}
