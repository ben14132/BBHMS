package com.example.mygooglemaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 12;

    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_PP_CODE = "ppCode";
    public static final String COLUMN_PP_NAME = "ppName";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CAT = "cat";
    public static final String COLUMN_POSITION = "position";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_PP_CODE + " TEXT PRIMARY KEY, " +
                    COLUMN_PP_NAME + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_NAME + " TEXT, "+
                    COLUMN_CAT + " TEXT," +
                    COLUMN_POSITION + " INTEGER " +
                    ")";


    public DatabaseHelper(Context context) {
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

    public long insertLocation(String ppCode, String ppName, double latitude, double longitude, String cat) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get the maximum position value from the database
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_POSITION + ") FROM " + TABLE_NAME, null);
        int maxPosition = 0;
        if (cursor != null && cursor.moveToFirst()) {
            maxPosition = cursor.getInt(0); // get the maximum position value
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_PP_CODE, ppCode);
        values.put(COLUMN_PP_NAME, ppName);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_NAME, ppName);
        values.put(COLUMN_CAT, cat);
        values.put(COLUMN_POSITION, maxPosition + 1); // set the new position value

        cursor = db.query(TABLE_NAME, null, COLUMN_PP_CODE + "=?", new String[]{ppCode}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            // The search already exists, don't insert it again
            cursor.close();
            db.close();
            return -1;
        }

        long newRowId = db.insert(TABLE_NAME, null, values);
        db.close();

        return newRowId;
    }


    public void updateLocationName(String ppCode, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);
        db.update(TABLE_NAME, values, COLUMN_PP_CODE + " = ?", new String[]{ppCode});
        db.close();
    }


    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
    public void deleteLocation(String ppCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_PP_CODE + "=?", new String[]{ppCode});
        db.close();
    }

    /* public void updateCarParkPosition(String position, int newPosition) {
         SQLiteDatabase db = this.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put(COLUMN_POSITION, newPosition);
         db.update(TABLE_NAME, values, COLUMN_POSITION + " = ?", new String[]{position});
         db.close();
     }*/
    public Cursor getAllLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, null, null, null, null,"position ASC");
    }

    public void reorderItem(String ppCode, int newPosition) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get current position of the item
        int oldPosition = getPositionForPPCode(db, ppCode);

        if (oldPosition == -1 || oldPosition == newPosition) {
            db.close();
            return;
        }

        if (oldPosition < newPosition) {
            // Move the items between old and new positions up (decrease their position)
            db.execSQL("UPDATE " + TABLE_NAME +
                            " SET " + COLUMN_POSITION + " = " + COLUMN_POSITION + " - 1 " +
                            " WHERE " + COLUMN_POSITION + " > ? AND " + COLUMN_POSITION + " <= ?",
                    new Object[]{oldPosition, newPosition});
        } else {
            // Move the items between old and new positions down (increase their position)
            db.execSQL("UPDATE " + TABLE_NAME +
                            " SET " + COLUMN_POSITION + " = " + COLUMN_POSITION + " + 1 " +
                            " WHERE " + COLUMN_POSITION + " >= ? AND " + COLUMN_POSITION + " < ?",
                    new Object[]{newPosition, oldPosition});
        }

        // Set the position of the item to the newPosition
        updatePositionForPPCode(db, ppCode, newPosition);

        db.close();
    }

    private void incrementPosition(SQLiteDatabase db, int position) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_POSITION, position + 1);
        db.update(TABLE_NAME, values, COLUMN_POSITION + "=?", new String[]{String.valueOf(position)});
    }

    private void decrementPosition(SQLiteDatabase db, int position) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_POSITION, position - 1);
        db.update(TABLE_NAME, values, COLUMN_POSITION + "=?", new String[]{String.valueOf(position)});
    }


    private int getPositionForPPCode(SQLiteDatabase db, String ppCode) {
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_POSITION}, COLUMN_PP_CODE + "=?", new String[]{ppCode}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int position = cursor.getInt(cursor.getColumnIndex(COLUMN_POSITION));
                cursor.close();
                return position;
            }
            cursor.close();
        }
        return -1; // Indicate invalid position
    }

    private void updatePositionForPPCode(SQLiteDatabase db, String ppCode, int newPosition) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_POSITION, newPosition);
        db.update(TABLE_NAME, values, COLUMN_PP_CODE + "=?", new String[]{ppCode});
    }
}

    /*
    public void insertLocationAsync(final String ppCode, final String ppName, final double latitude, final double longitude) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                return insertLocation(ppCode, ppName, latitude, longitude);
            }

            @Override
            protected void onPostExecute(Long newRowId) {
                super.onPostExecute(newRowId);
                // Handle the result if needed
            }
        }.execute();
    }

    // AsyncTask for updating location name
    public void updateLocationNameAsync(final String ppCode, final String newPPName) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                updateLocationName(ppCode, newPPName);
                return null;
            }
        }.execute();
    }

    // AsyncTask for deleting all locations
    public void deleteAllLocationsAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteAllLocations();
                return null;
            }
        }.execute();
    }

    // AsyncTask for deleting a specific location by ppCode
    public void deleteLocationAsync(final String ppCode) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                deleteLocation(ppCode);
                return null;
            }
        }.execute();
    }

    // AsyncTask for retrieving all locations
    public void getAllLocationsAsync(final OnLocationsRetrievedListener listener) {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                return getAllLocations();
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                super.onPostExecute(cursor);
                listener.onLocationsRetrieved(cursor);
            }
        }.execute();
    }
    public interface OnLocationsRetrievedListener {
        void onLocationsRetrieved(Cursor cursor);
    }*/


