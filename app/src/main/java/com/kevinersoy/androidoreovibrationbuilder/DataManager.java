package com.kevinersoy.androidoreovibrationbuilder;


import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;
import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderOpenHelper;
import com.kevinersoy.androidoreovibrationbuilder.db.room.MyDatabase;
import com.kevinersoy.androidoreovibrationbuilder.models.ProfileInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by kevinersoy on 2/27/18.
 * This class will handle all the data.
 * Database communication, storage, loading example profiles
 * Handle GUID generation
 */

public class DataManager {
    private static DataManager mInstance = null;
    private List<ProfileInfo> mProfiles = new ArrayList<>();
    private String mGUID = null;
    private MyDatabase database = null;
    private final String DATABASE_NAME = "profileDB";

    public static DataManager getInstance(){
        //Implement singleton functionality
        if (mInstance == null) {
            mInstance = new DataManager();
        }
        return mInstance;
    }

    public static void loadFromDatabase(VibrationProfileBuilderOpenHelper dbHelper) {
        //Query the database then load the profiles
        AsyncTask<VibrationProfileBuilderOpenHelper, Void, Cursor> task = new AsyncTask<VibrationProfileBuilderOpenHelper, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(VibrationProfileBuilderOpenHelper... vibrationProfileBuilderOpenHelpers) {
                SQLiteDatabase db =  vibrationProfileBuilderOpenHelpers[0].getReadableDatabase();
                String[] profileColumns = {ProfileInfoEntry.COLUMN_PROFILE_NAME,
                        ProfileInfoEntry.COLUMN_PROFILE_INTENSITY,
                        ProfileInfoEntry.COLUMN_PROFILE_DELAY,
                        ProfileInfoEntry._ID};
                return db.query(ProfileInfoEntry.TABLE_NAME, profileColumns, null,
                        null, null, null, null);
                //profileCursor is now positioned before the first record
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                loadProfilesFromDatabase(cursor);
            }
        }.execute(dbHelper);

    }

    private static void loadProfilesFromDatabase(Cursor cursor) {
        //Get column index from the query result for each column requested.
        //If our query changes later, these indices will still point to the correct columns.
        //Next, clear the profiles list, cycle through the query results and populate the profiles
        //list.
        final int profileNamePos = cursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_NAME);
        final int profileIntensityPos = cursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_INTENSITY);
        final int profileDelayPos = cursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_DELAY);
        final int idPos = cursor.getColumnIndex(ProfileInfoEntry._ID);

        DataManager dm = getInstance();
        dm.mProfiles.clear();

        //disk reads below, do in background
        AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object... objects) {
                DataManager dm = (DataManager)objects[0];
                Cursor cursor = (Cursor)objects[1];
                while(cursor.moveToNext()){  //.moveToNext returns true if there was a next row to move to
                    String profileName = cursor.getString(profileNamePos);
                    String profileIntensity = cursor.getString(profileIntensityPos);
                    String profileDelay = cursor.getString(profileDelayPos);
                    int id = cursor.getInt(idPos);
                    ProfileInfo profile = new ProfileInfo(profileName, profileIntensity, profileDelay, id);

                    dm.mProfiles.add(profile);
                }
                cursor.close();
                return null;
            }
        }.execute(dm, cursor);


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

    public MyDatabase getDB(Context appContext){
        if(!(appContext instanceof Application)){
            throw new IllegalArgumentException("getDB requires Application context");
        }
        if(database == null){
            database = Room.databaseBuilder(appContext, MyDatabase.class, DATABASE_NAME).build();
        }
        return database;
    }

    public boolean dbReady(){
        return database != null;
    }

    //Get globally unique identifier
    public String getGUID(Context context){
        if(mGUID != null){
            return mGUID;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String GUID = sharedPreferences.getString("GUID", "default");
        if(GUID.equals("default")){
            GUID = generateGUID();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("GUID", GUID);
            editor.apply();
        }
        mGUID = GUID;
        return GUID;
    }

    //Generate globally unique identifier
    private String generateGUID(){
        return UUID.randomUUID().toString();
    }
}
