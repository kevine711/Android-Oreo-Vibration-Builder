package com.kevinersoy.androidoreovibrationbuilder;


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
        if (mInstance == null) {
            mInstance = new DataManager();
            mInstance.initializeExampleProfiles();
        }
        return mInstance;
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

    public void removeProfile(int index){
        mProfiles.remove(index);
    }

    public void initializeExampleProfiles() {
        mProfiles.add(new ProfileInfo("Example1", "100,50,100,50", "250,500,250,500"));
        mProfiles.add(new ProfileInfo("Example2", "255,10,30,50", "250,500,250,500"));
        mProfiles.add(new ProfileInfo("Example3", "10,50,100,250,100,50,10", "25,25,25,25,25,25,25"));
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
