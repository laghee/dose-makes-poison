package edu.mills.cs250.dosemakespoison;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Kat on 3/27/18.
 */

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    /**
     * Name of database table keeping track of all saved chemicals in personal pantry.
     */
    static final String PANTRY_TABLE = "PANTRY";

    /**
     * Name of column in {@link #PANTRY_TABLE} with name of chemical.
     */
    static final String NAME_COL = "NAME";

    /**
     * Name of column in {@link #PANTRY_TABLE} with LD50 value of chemical.
     */
    static final String LD50_COL = "LD50";

    /**
     * Name of column in {@link #PANTRY_TABLE} with chemical comparison name.
     */
    static final String COMPARE_COL = "DESCRIPTION";

    /**
     * Name of column in {@link #PANTRY_TABLE} with chemical spectrum block number.
     */
    static final String SPNUM_COL = "SPNUM";

    /**
     * A local SQLite database for inserting into and deleting chemicals from a local pantry.
     */
    private static final String DB_NAME = "pantrydatabase"; // the name of our database
    private static final int DB_VERSION = 1; // the version of the database


    PantryDatabaseHelper(Context context) {
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
            db.execSQL("CREATE TABLE PANTRY (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAME_COL + " TEXT NOT NULL, "
                    + LD50_COL + " INTEGER NOT NULL, "
                    + COMPARE_COL + " TEXT NOT NULL, "
                    + SPNUM_COL + " INTEGER NOT NULL);");
        }
    }
}
