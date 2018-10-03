package edu.mills.cs250.toxsense;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

public class ChemSuggestionProvider extends ContentProvider {

    private static final String TAG = "ChemSuggestionProvider";
    private SQLiteDatabase db;

    public ChemSuggestionProvider() {
        super();
    }

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate called");
        try {
            SQLiteOpenHelper chemRefDatabaseHelper =
                    new ToxDatabaseHelper(getContext());
            db = chemRefDatabaseHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            Log.d(TAG, "Caught SQLite Exception" + e.getMessage());
        }
        Log.d(TAG, "onCreate finished");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String sel,
        @Nullable String[] selArgs, @Nullable String sortOrder) {
        Log.d(TAG, "Query() called with: " + uri.getLastPathSegment().toLowerCase());

        String selectionArg = uri.getLastPathSegment().toLowerCase();;
        String[] columns = new String[] {"_id", ToxsenseDbUtilities.REF_NAME_COL,
                ToxsenseDbUtilities.REGNUM_COL};

        Cursor cursor = db.query(ToxsenseDbUtilities.CHEMREF_TABLE, columns,
                    ToxsenseDbUtilities.REF_NAME_COL + " LIKE ?",
                    new String[]{"%" + selectionArg + "%"}, null, null, null);
        Log.d(TAG, "Cursor= " + cursor);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }


}
