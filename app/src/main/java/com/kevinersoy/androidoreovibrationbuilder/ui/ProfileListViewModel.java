package com.kevinersoy.androidoreovibrationbuilder.ui;

import android.arch.lifecycle.ViewModel;

import com.kevinersoy.androidoreovibrationbuilder.ProfileDataSource;
import com.kevinersoy.androidoreovibrationbuilder.db.room.Profile;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class ProfileListViewModel extends ViewModel {

    private final ProfileDataSource mDataSource;

    private List<Profile> mProfiles;

    public ProfileListViewModel(ProfileDataSource dataSource){
        mDataSource = dataSource;
    }

    public Flowable<List<Profile>> getProfiles() {
        return mDataSource.getProfiles()
                .map(profiles -> {
                    mProfiles = profiles;
                    return profiles;
                });
    }

    public List<Long> updateProfiles(final List<Profile> profiles){
        mProfiles = profiles;
        return mDataSource.insertOrUpdateAll(profiles);
    }

}
