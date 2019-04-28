package com.kevinersoy.androidoreovibrationbuilder.db.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profile")
    List<Profile> getAll();

    @Query("SELECT * FROM profile WHERE id LIKE :id LIMIT 1")
    Profile findById(int id);

    @Insert
    void insertAll(List<Profile> profiles);

    @Update
    void update(Profile profile);

    @Delete
    void delete(Profile profile);

}
