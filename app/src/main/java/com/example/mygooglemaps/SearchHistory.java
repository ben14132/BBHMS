package com.example.mygooglemaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.widget.TextView;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SearchHistory extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "history.db";
    private static final int DATABASE_VERSION = 12;

    public static final String TABLE_NAME = "history";
    public static final String COLUMN_INDEX = "id";
    public static final String COLUMN_SEARCH = "search";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_INDEX + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SEARCH + " TEXT" +
                    ")";

    public SearchHistory(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    public void deleteLocation(String search) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_SEARCH + "=?", new String[]{search});
        db.close();
    }

    public long insertLocation(String search) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the search already exists in the database
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_SEARCH + "=?", new String[]{search}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            // The search already exists, don't insert it again
            cursor.close();
            db.close();
            return -1; // You can return a specific value to indicate that the insertion was not successful
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_SEARCH, search);
        long newRowId = db.insert(TABLE_NAME, null, values);

        cursor.close();
        db.close();
        return newRowId;
    }

    /*TextView textViewDisplay = findViewById(R.id.textViewDisplay);
    void displayData() {
        Cursor cursor = this.getAllLocations();
        StringBuilder data = new StringBuilder();

        if (cursor.moveToFirst()) {
            do {
                String search = cursor.getString(cursor.getColumnIndex(SearchHistory.COLUMN_SEARCH));

                data.append("Search: ").append(search);
            } while (cursor.moveToNext());
            TextView textViewDisplay = ((Activity) context).findViewById(R.id.textViewDisplay);
            (R.id.textViewDisplay).setText(' '+data.toString());
        } else {
            (R.id.textViewDisplay).setText("No data available!");
        }
        cursor.close();
    }*/

    public Cursor getAllLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null, "id DESC");
    }
    /*
    public void insertSearchHistoryAsync(final String search) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                insertLocation(search);
                return null;
            }
        }.execute();
    }

    public void deleteAllSearchHistoryAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteAllLocations();
                return null;
            }
        }.execute();
    }

    public void deleteSearchHistoryAsync(final String id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteLocation(id);
                return null;
            }
        }.execute();
    }

    public void getAllSearchHistoryAsync(final OnSearchHistoryRetrievedListener listener) {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                return getAllLocations();
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                super.onPostExecute(cursor);
                listener.onSearchHistoryRetrieved(cursor);
            }
        }.execute();
    }

    public interface OnSearchHistoryRetrievedListener {
        void onSearchHistoryRetrieved(Cursor cursor);
    }*/
}

