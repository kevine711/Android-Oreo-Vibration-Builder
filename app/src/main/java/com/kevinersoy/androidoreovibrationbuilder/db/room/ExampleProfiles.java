package com.kevinersoy.androidoreovibrationbuilder.db.room;

import java.util.ArrayList;
import java.util.List;

public class ExampleProfiles {

    public static List<Profile> getAll(){
        List<Profile> profiles = new ArrayList<Profile>();
        Profile profile = new Profile("Example1", "255,220,200,170,140,120,90,60,30", "50,50,50,50,50,50,50,50,50", "example");
        profiles.add(profile);
        profile = new Profile("Example2", "255,30,255,130,40,20", "30,75,30,30,30,80", "example");
        profiles.add(profile);
        profile = new Profile("Example3", "255,30,255,30,255,30,255,30", "30,75,30,75,30,75,30,75", "example");
        profiles.add(profile);

        return profiles;
    }

}
