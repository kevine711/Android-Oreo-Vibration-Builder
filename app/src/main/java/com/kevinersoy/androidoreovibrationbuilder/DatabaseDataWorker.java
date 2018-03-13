package com.kevinersoy.androidoreovibrationbuilder;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by kevinersoy on 3/11/18.
 * The purpose of this class is to insert sample profiles when the database is first created.
 * Our VibrationProfileBuilderOpenHelper class is the only class calling this worker class.
 */

public class DatabaseDataWorker {
    private SQLiteDatabase mDb;

    public DatabaseDataWorker(SQLiteDatabase db) {
        mDb = db;
    }

    public void insertSampleProfiles() {
        insertProfile("Example 1", "255,220,200,170,140,120,90,60,30", "50,50,50,50,50,50,50,50,50");
        insertProfile("Example2", "255,30,255,130,40,20", "30,75,30,30,30,80");
        insertProfile("Example3", "255,30,255,30,255,30,255,30", "30,75,30,75,30,75,30,75");
    }

    private void insertProfile(String name, String intensity, String delay){
        ContentValues values = new ContentValues();
        values.put(VibrationProfileBuilderDatabaseContract.ProfileInfoEntry.COLUMN_PROFILE_NAME, name);
        values.put(VibrationProfileBuilderDatabaseContract.ProfileInfoEntry.COLUMN_PROFILE_INTENSITY, intensity);
        values.put(VibrationProfileBuilderDatabaseContract.ProfileInfoEntry.COLUMN_PROFILE_DELAY, delay);

        long newRowId = mDb.insert(VibrationProfileBuilderDatabaseContract.ProfileInfoEntry.TABLE_NAME, null, values);
    }
}
