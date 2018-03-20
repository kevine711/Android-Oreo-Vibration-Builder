package com.kevinersoy.androidoreovibrationbuilder;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kevinersoy on 3/20/18.
 */

public final class VibrationBuilderProviderContract {
    private VibrationBuilderProviderContract() {}; //nobody can create an instance of this class

    public static final String AUTHORITY = "com.kevinersoy.androidoreovibrationbuilder.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Profiles implements BaseColumns, ProfilesColumns{
        public static final String PATH = "profiles";
        // content://com.kevinersoy.androidoreovibrationbuilder.provider/courses
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);

        /*if joining with another table, implement that table's Columns interface
        public static final String EXPANDED_PATH = "profiles_expanded";
        public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, EXPANDED_PATH);
        */
    }

    //public static final class {otherTableToExpose} {}

    /*if multiple tables, use nested protected interface to have each table class implement
    //Also implement BaseColumns to expose _ID constant, and CommonColumns defined below
    //for any columns common to both/all tables
    */
    protected interface ProfilesColumns{
        public static final String COLUMN_PROFILE_NAME = "profile_name";
        public static final String COLUMN_PROFILE_INTENSITY = "profile_intensity";
        public static final String COLUMN_PROFILE_DELAY = "profile_delay";
    }
    /*
    protected interface OtherTableColumns{
        public static final String COLUMN_OTHER_COLUMN = "other_column_name";
    }

    protected interface CommonColumns{
        public static final String COMMON_COLUMN_ONE = "common_column_one";
    }



     */
}
