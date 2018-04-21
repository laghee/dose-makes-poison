/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static edu.mills.cs250.toxsense.ChemRefDatabaseHelper.CHEMID_COL;
import static edu.mills.cs250.toxsense.ChemRefDatabaseHelper.CHEMREF_TABLE;
import static edu.mills.cs250.toxsense.PantryDatabaseHelper.NAME_COL;

public class ChemRefUtilities {

        private ChemRefUtilities() {}

        /**
         * Looks up a chemical in the reference database and retrieves its registry number.
         *
         * @param db the local database
         * @param name the chemical name
         * @return the chemID
         */
        public static String getChemId(SQLiteDatabase db, String name) {

            String chemId = "";
            Cursor cursor = db.query(CHEMREF_TABLE,
                    new String[]{CHEMID_COL},
            NAME_COL + " = ?",
                    new String[]{name},
            null, null, null);
            if (cursor.moveToFirst()) {
                chemId = cursor.getString(0);
            }
            cursor.close();
            return chemId;
        }
    }

