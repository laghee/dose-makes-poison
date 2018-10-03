package edu.mills.cs250.toxsense;

import android.content.Context;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;


public class ToxDatabaseHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "toxdata";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "ToxDbHelper";


    public ToxDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper... initialized: "
                + context.getDatabasePath(DATABASE_NAME).getAbsolutePath());
    }

}
