package com.kevinersoy.androidoreovibrationbuilder;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kevinersoy.androidoreovibrationbuilder.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinersoy on 2/27/18.
 * This class will handle all the data.
 * Database communication, storage, loading example profiles
 */

public class DataManager {
    private static DataManager mInstance = null;
    private List<ProfileInfo> mProfiles = new ArrayList<>();



    public static DataManager getInstance(){
        //Implement singleton functionality
        if (mInstance == null) {
            mInstance = new DataManager();
        }
        return mInstance;
    }

    public static void loadFromDatabase(VibrationProfileBuilderOpenHelper dbHelper) {
        //Query the database then load the profiles
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] profileColumns = {ProfileInfoEntry.COLUMN_PROFILE_NAME,
                ProfileInfoEntry.COLUMN_PROFILE_INTENSITY,
                ProfileInfoEntry.COLUMN_PROFILE_DELAY,
                ProfileInfoEntry._ID};
        Cursor profileCursor = db.query(ProfileInfoEntry.TABLE_NAME, profileColumns, null, null, null, null, null);
        //profileCursor is now positioned before the first record
        loadProfilesFromDatabase(profileCursor);
    }

    private static void loadProfilesFromDatabase(Cursor cursor) {
        //Get column index from the query result for each column requested.
        //If our query changes later, these indices will still point to the correct columns.
        //Next, clear the profiles list, cycle through the query results and populate the profiles
        //list.
        int profileNamePos = cursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_NAME);
        int profileIntensityPos = cursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_INTENSITY);
        int profileDelayPos = cursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_DELAY);
        int idPos = cursor.getColumnIndex(ProfileInfoEntry._ID);

        DataManager dm = getInstance();
        dm.mProfiles.clear();
        while(cursor.moveToNext()){  //.moveToNext returns true if there was a next row to move to
            String profileName = cursor.getString(profileNamePos);
            String profileIntensity = cursor.getString(profileIntensityPos);
            String profileDelay = cursor.getString(profileDelayPos);
            int id = cursor.getInt(idPos);
            ProfileInfo profile = new ProfileInfo(profileName, profileIntensity, profileDelay, id);

            dm.mProfiles.add(profile);
        }
        cursor.close();
    }

    public List<ProfileInfo> getProfiles() {
        return mProfiles;
    }

    public int createNewProfile(){
        ProfileInfo profile = new ProfileInfo(null, null, null);
        mProfiles.add(profile);
        return mProfiles.size() - 1;
    }

    public int findProfile(ProfileInfo profile){
        for(int i = 0; i < mProfiles.size(); i++){
            if(profile.equals(mProfiles.get(i)))
                return i;
        }
        return -1;
    }


    public int createNewProfile(String profileName, String profileIntensity, String profileDelay) {
        int index = createNewProfile();
        ProfileInfo profile = getProfiles().get(index);
        profile.setName(profileName);
        profile.setIntensity(profileIntensity);
        profile.setDelay(profileDelay);

        return index;
    }
}
