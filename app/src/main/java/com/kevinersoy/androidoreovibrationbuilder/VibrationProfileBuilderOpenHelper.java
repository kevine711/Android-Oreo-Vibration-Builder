package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kevinersoy on 3/11/18.
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
