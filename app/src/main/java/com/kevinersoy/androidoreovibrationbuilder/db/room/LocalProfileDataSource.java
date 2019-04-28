package com.kevinersoy.androidoreovibrationbuilder.db.room;

import com.kevinersoy.androidoreovibrationbuilder.ProfileDataSource;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class LocalProfileDataSource implements ProfileDataSource {

    private final ProfileDao mProfileDao;

    public LocalProfileDataSource(ProfileDao profileDao){
        mProfileDao = profileDao;
    }

    @Override
    public Flowable<List<Profile>> getProfiles() {
        return mProfileDao.getProfiles();
    }

    @Override
    public Flowable<Profile> getProfile(int id) {
        return mProfileDao.findById(id);
    }

    @Override
    public Flowable<Long> insertOrUpdateProfile(Profile profile) {
        return mProfileDao.insert(profile);
    }

    @Override
    public Completable insertOrUpdateAll(List<Profile> profiles) {
        return mProfileDao.insertAll(profiles);
    }

    @Override
    public Completable delete(Profile profile) {
        return mProfileDao.delete(profile);
    }

    @Override
    public Flowable<Integer> getSize() {
        return mProfileDao.getSize();
    }
}
