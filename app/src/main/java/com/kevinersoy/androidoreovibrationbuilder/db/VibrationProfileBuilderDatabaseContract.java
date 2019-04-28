package com.kevinersoy.androidoreovibrationbuilder.db;

import android.provider.BaseColumns;

/**
 * Created by kevinersoy on 3/11/18.
 * The purpose of this class is to define constants for our table structure and SQL strings used
 * when creating the table/s.
 * If necessary (if multiple tables and we're using JOIN in a query) we can include a method
 * here to provide the table name qualified version of column names.
 * ex. return TABLE_NAME + "." + column
 */

public final class VibrationProfileBuilderDatabaseContract {
    //final because there's no reason for another class to inherit from this class
    private VibrationProfileBuilderDatabaseContract() {} //non creatable

    public static final class ProfileInfoEntry implements BaseColumns{
        public static final String TABLE_NAME = "profile_info";
        public static final String COLUMN_PROFILE_NAME = "profile_name";
        public static final String COLUMN_PROFILE_INTENSITY = "profile_intensity";
        public static final String COLUMN_PROFILE_DELAY = "profile_delay";

        // CREATE TABLE profile_info (profile_name, profile_intensity, profile_delay)
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_PROFILE_NAME + " TEXT, " +
                        COLUMN_PROFILE_INTENSITY + " TEXT, " +
                        COLUMN_PROFILE_DELAY + " TEXT)";

    }


}
