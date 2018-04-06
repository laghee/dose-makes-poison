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

import static edu.mills.cs250.toxsense.PantryDatabaseHelper.COMPARE_COL;
import static edu.mills.cs250.toxsense.PantryDatabaseHelper.LD50_COL;
import static edu.mills.cs250.toxsense.PantryDatabaseHelper.NAME_COL;
import static edu.mills.cs250.toxsense.PantryDatabaseHelper.PANTRY_TABLE;
import static edu.mills.cs250.toxsense.PantryDatabaseHelper.SPNUM_COL;

public class ChemRefUtilities {

        private ChemRefUtilities() {}

        /**
         * Retrieves chemical from local database based on the row id.
         *
         * @param db the SQLite database
         * @param chemId the row id
         * @return the chemical
         */
        public static Chem getChem(SQLiteDatabase db, int chemId) {
            Cursor cursor = db.query(PANTRY_TABLE,
                    new String[]{NAME_COL, LD50_COL, COMPARE_COL, SPNUM_COL},
                    "_id = ?",
                    new String[]{Integer.toString(chemId)},
                    null, null, null);

            Chem chem = null;
            if (cursor.moveToFirst()) {
                chem = new Chem(cursor.getString(0), cursor.getInt(1), cursor.getInt(1), cursor.getString(2),
                        cursor.getInt(3));
            }
            return chem;
        }

        /**
         * Inserts chemical into the local database.
         *
         * @param db the SQLite database
         * @param chem the chemical
         * @return the row id of chemical
         */
        public static long insertChem(SQLiteDatabase db, Chem chem) {
            ContentValues chemValues = new ContentValues();
            chemValues.put(NAME_COL, chem.getName());
            chemValues.put(LD50_COL, chem.getLd50Val());
            chemValues.put(COMPARE_COL, chem.getCompareChem());
            chemValues.put(SPNUM_COL, chem.getSpectrumNum());


            Log.d("DatabaseUtils", "Successfully wrote" + chem.getName() + "to db");
            return db.insert("PANTRY", null, chemValues);
        }

        /**
         * Removes a chemical from the local database by the id of the row.
         *
         * @param db the local database
         * @param pantryId the id in the pantry
         */
        public static void removeChemByPantryId(SQLiteDatabase db, Integer pantryId) {
            db.delete(PANTRY_TABLE, "_id = ?", new String[]{pantryId.toString()});
        }

        /**
         * Checks for a chemical in the local database and, if it exists, retrieves its row id number.
         *
         * @param db the local database
         * @param name the chemical name
         * @return the pantry id
         */
        public static int getPantryIdIfExists(SQLiteDatabase db, String name) {

            int pantryId = -1;
            Cursor cursor = db.rawQuery("SELECT _id FROM " + PANTRY_TABLE + " WHERE " + NAME_COL +
                    " = " + name + " LIMIT 1", null);
            if (cursor.moveToFirst()) {
                pantryId = cursor.getInt(0);
            }
            return pantryId;
        }
    }

