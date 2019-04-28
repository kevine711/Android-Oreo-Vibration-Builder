package com.kevinersoy.androidoreovibrationbuilder;


import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;
import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderOpenHelper;
import com.kevinersoy.androidoreovibrationbuilder.db.room.ExampleProfiles;
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

    public MyDatabase getDB(final Context appContext){
        if(!(appContext instanceof Application)){
            throw new IllegalArgumentException("getDB requires Application context");
        }
        if(database == null){
            database = Room.databaseBuilder(appContext, MyDatabase.class, DATABASE_NAME)
                    /*.addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            getInstance().getDB(appContext).profileDao().insertAll(ExampleProfiles.getAll());
                        }
                    })
                    */.build();
        }
        return database;
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
