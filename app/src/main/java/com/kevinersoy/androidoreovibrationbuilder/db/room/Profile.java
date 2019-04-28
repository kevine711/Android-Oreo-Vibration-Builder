package com.kevinersoy.androidoreovibrationbuilder.db.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.kevinersoy.androidoreovibrationbuilder.DataManager;

@Entity
public class Profile {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "intensity")
    private String intensity;

    @ColumnInfo(name = "delay")
    private String delay;

    @ColumnInfo(name = "guid")
    private String guid;

    @Ignore
    public Profile(String name, String intestity, String delay, String guid){
        this.name = name;
        this.intensity = intestity;
        this.delay = delay;
        this.guid = guid;
    }

    public Profile(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
