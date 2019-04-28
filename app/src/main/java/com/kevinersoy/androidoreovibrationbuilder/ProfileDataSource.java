package com.kevinersoy.androidoreovibrationbuilder;

import com.kevinersoy.androidoreovibrationbuilder.db.room.Profile;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public interface ProfileDataSource {

    /**
     * Gets the profiles from the data source.
     *
     * @return the profiles from the data source.
     */
    Flowable<List<Profile>> getProfiles();

    /**
     * Gets the profile from the data source.
     *
     * @return the profile from the data source with given id.
     */
    Flowable<Profile> getProfile(int id);

    /**
     * Inserts the profile into the data source, or, if this is an existing profile, updates it.
     *
     * @param profile the profile to be inserted or updated.
     */
    Flowable<Long> insertOrUpdateProfile(Profile profile);

    /**
     * Inserts the profiles into the data source, or, if this is an existing profile, updates it.
     *
     * @param profiles the profiles to be inserted or updated.
     */
    Completable insertOrUpdateAll(List<Profile> profiles);

    /**
     * Deletes the profile from the data source.
     */
    Completable delete(Profile profile);

    /**
     * @return record count.
     */
    Flowable<Integer> getSize();
}
