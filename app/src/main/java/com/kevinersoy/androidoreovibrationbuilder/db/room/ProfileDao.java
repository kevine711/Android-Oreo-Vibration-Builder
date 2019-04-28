package com.kevinersoy.androidoreovibrationbuilder.db.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface ProfileDao {

    @Query("SELECT * FROM profile")
    Flowable<List<Profile>> getProfiles();

    @Query("SELECT * FROM profile WHERE id LIKE :id LIMIT 1")
    Flowable<Profile> findById(int id);

    @Query("SELECT COUNT(name) FROM profile")
    Flowable<Integer> getSize();

    @Query("SELECT COUNT(id) FROM profile WHERE name LIKE :name AND intensity LIKE :intensity AND delay LIKE :delay")
    int findAndCount(String name, String intensity, String delay);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Profile> profiles);

    // Insert profile.  If Profile exists, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insert(Profile profile);

    @Delete
    void delete(Profile profile);

}
