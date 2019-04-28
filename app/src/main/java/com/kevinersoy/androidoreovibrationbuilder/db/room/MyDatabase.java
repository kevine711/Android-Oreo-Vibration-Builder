package com.kevinersoy.androidoreovibrationbuilder.db.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Profile.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {
    public abstract ProfileDao profileDao();
}
