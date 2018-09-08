/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class ToxsenseDbUtilities {

    /**
     * Name of database table for looking up chemical registry numbers.
     */
    static final String CHEMREF_TABLE = "chemref";

    /**
     * Name of column in chemref table with name of chemical.
     */
    static final String REF_NAME_COL = "name";

    /**
     * Name of column in chemref table with CAS Registry number.
     */
    static final String REGNUM_COL = "regnum";

    /**
     * Name of database table keeping track of all saved chemicals in personal pantry.
     */
    static final String PANTRY_TABLE = "pantry";

    /**
     * Name of column in pantry table with name of chemical.
     */
    static final String CHEM_NAME_COL = "name";

    /**
     * Name of column in pantry table with CAS Registry number.
     */
    static final String CHEMID_COL = "chemid";

    /**
     * Name of column in pantry table with LD50 value of chemical.
     */
    static final String LD50_COL = "ld50";

    /**
     * Name of column in pantry table with chemical comparison name.
     */
    static final String COMPARISON_COL = "comparison";

    /**
     * Name of column in pantry table with chemical spectrum box view ID number.
     */
    static final String VIEWID_COL = "viewid";

    private ToxsenseDbUtilities() {
    }

    /**
     * Looks up a chemical in the chemical reference table and retrieves its registry number.
     *
     * @param db   the local database
     * @param name the chemical name
     * @return the regID
     */
    public static String getChemRegId(SQLiteDatabase db, String name) {

        String regId = "";
        Cursor cursor = db.query(CHEMREF_TABLE,
                new String[]{REGNUM_COL},
                REF_NAME_COL + " = ?",
                new String[]{name},
                null, null, null);
        if (cursor.moveToFirst()) {
            regId = cursor.getString(0);
        }
        cursor.close();
        return regId;
    }

    /**
     * Retrieves a stored chemical from the pantry table based on the row id.
     *
     * @param db       the SQLite database
     * @param pantryId the row id
     * @return the chemical
     */
    public static Chem getChem(SQLiteDatabase db, int pantryId) {
        Cursor cursor = db.query(PANTRY_TABLE,
                new String[]{CHEM_NAME_COL,
                        CHEMID_COL,
                        LD50_COL,
                        COMPARISON_COL,
                        VIEWID_COL},
                "_id = ?",
                new String[]{Integer.toString(pantryId)},
                null, null, null);

        Chem chem = null;
        if (cursor.moveToFirst()) {
            chem = new Chem(cursor.getString(0), cursor.getString(1), cursor.getInt(2),
                    cursor.getString(3), cursor.getInt(4));
        }

        cursor.close();

        return chem;
    }

    /**
     * Inserts a chemical into the pantry table.
     *
     * @param db   the SQLite database
     * @param chem the chemical
     * @return the row id of chemical
     */
    public static long insertChem(SQLiteDatabase db, Chem chem) {
        ContentValues chemValues = new ContentValues();
        chemValues.put(CHEM_NAME_COL, chem.getName());
        chemValues.put(CHEMID_COL, chem.getChemId());
        chemValues.put(LD50_COL, chem.getLd50Val());
        chemValues.put(COMPARISON_COL, chem.getComparisonChem());
        chemValues.put(VIEWID_COL, chem.getComparisonViewId());


        Log.d("DatabaseUtils", "Successfully wrote" + chem.getName() + "to db");
        return db.insert("PANTRY", null, chemValues);
    }

    /**
     * Removes a chemical from the pantry table by the id of the row.
     *
     * @param db       the local database
     * @param pantryId the id in the pantry
     */
    public static void removeChemByPantryId(SQLiteDatabase db, Integer pantryId) {
        db.delete(PANTRY_TABLE, "_id = ?", new String[]{pantryId.toString()});
    }

    /**
     * Checks for a chemical in the pantry table and, if it exists, retrieves its row id number.
     *
     * @param db     the local database
     * @param chemid the ChemID database number
     * @return the pantry id
     */
    public static int getPantryIdIfExists(SQLiteDatabase db, String chemid) {

        int pantryId = -1;
        Cursor cursor = db.query(PANTRY_TABLE,
                new String[]{"_id"},
                CHEMID_COL + " = ?",
                new String[]{chemid},
                null, null, null);
        if (cursor.moveToFirst()) {
            pantryId = cursor.getInt(0);
        }
        cursor.close();

        return pantryId;
    }
}

