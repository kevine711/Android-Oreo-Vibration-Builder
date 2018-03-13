package com.kevinersoy.androidoreovibrationbuilder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kevinersoy on 2/27/18.
 * This class will hold all the necessary info to populate the VibrationProfile Activity.
 * This POJO will implement parcelable in order to be passed as an intent extra from
 * VibrationProfileList.
 * 2/28 - Changing intent to pass position rather than actual profile since DataManager is singleton
 */

public final class ProfileInfo implements Parcelable {
    private String mName;
    private String mIntensity;
    private String mDelay;
    private int mId;


    //Default constructor, public
    public ProfileInfo (String name, String intensity, String delay){
        mName = name;
        mIntensity = intensity;
        mDelay = delay;
    }

    //Default constructor, public
    public ProfileInfo (String name, String intensity, String delay, int id){
        mName = name;
        mIntensity = intensity;
        mDelay = delay;
        mId = id;
    }

    //Constructor used when recreating ProfileInfo POJO from parcel
    private ProfileInfo(Parcel source){
        //Read back in same order as when writing to parcel
        mName = source.readString();
        mIntensity = source.readString();
        mDelay = source.readString();
    }


    /*
     * Getters and Setters
     */

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getIntensity() {
        return mIntensity;
    }

    public void setIntensity(String intensity) {
        this.mIntensity = intensity;
    }

    public String getDelay() {
        return mDelay;
    }

    public void setDelay(String delay) {
        this.mDelay = delay;
    }

    //Override toString to return the name of the profile
    @Override
    public String toString() {
        return mName;
    }

    /*
     *Below methods for Parcelable interface
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mIntensity);
        dest.writeString(mDelay);
    }

    //Use abstract class and make use of a different constructor for ProfileInfo
    //which takes a parcel
    public final static Parcelable.Creator<ProfileInfo> CREATOR =
            new Parcelable.Creator<ProfileInfo>() {

                @Override
                public ProfileInfo createFromParcel(Parcel source) {
                    return new ProfileInfo(source);
                }

                @Override
                public ProfileInfo[] newArray(int size) {
                    return new ProfileInfo[size];
                }
            };

}
