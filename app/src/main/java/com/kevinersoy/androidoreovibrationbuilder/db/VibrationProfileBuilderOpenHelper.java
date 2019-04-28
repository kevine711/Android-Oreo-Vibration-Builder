package com.kevinersoy.androidoreovibrationbuilder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kevinersoy on 3/11/18.
 * We're extending SQLiteOpenHelper which allows us to getReadableDatabase()  (or Writable)
 * When those calls are initiated, the database will be created if it doesn't exist yet.
 * For this creation, we're Overriding onCreate, and executing the SQL to create the table
 * and insert the examples
 * We're not using onUpgrade at the moment, but if the table changes in the future, we'll increment
 * the DATABASE_VERSION constant and make the necessary changes in the onUpgrade method depending
 * on the oldVersion and newVersion
 */

public class VibrationProfileBuilderOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "VibrationProfileBuilder.db";
    public static final int DATABASE_VERSION = 1;

    public VibrationProfileBuilderOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(VibrationProfileBuilderDatabaseContract.ProfileInfoEntry.SQL_CREATE_TABLE);

        DatabaseDataWorker worker = new DatabaseDataWorker(db);
        worker.insertSampleProfiles();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
