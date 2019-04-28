package com.kevinersoy.androidoreovibrationbuilder.ui;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.kevinersoy.androidoreovibrationbuilder.ProfileDataSource;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final ProfileDataSource mDataSource;

    public ViewModelFactory(ProfileDataSource dataSource){
        mDataSource = dataSource;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileListViewModel.class)) {
            return (T) new ProfileListViewModel(mDataSource);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
