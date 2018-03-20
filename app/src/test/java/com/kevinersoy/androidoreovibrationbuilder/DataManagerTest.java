package com.kevinersoy.androidoreovibrationbuilder;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by kevinersoy on 3/2/18.
 */
public class DataManagerTest {

    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp() throws Exception {
        //runs before anything else in this class, once
        sDataManager = DataManager.getInstance();
    }

    @Before
    public void setUp() throws Exception {
        //runs before each test
        sDataManager.getProfiles().clear();
        //sDataManager.initializeExampleProfiles();
    }

    @Test
    public void createNewProfile() throws Exception {
        final String profileName = "Test Profile";
        final String profileIntensity = "100,50,100,50";
        final String profileDelay = "1000,200,500,200";

        int index = sDataManager.createNewProfile();
        ProfileInfo newProfile = sDataManager.getProfiles().get(index);
        newProfile.setName(profileName);
        newProfile.setIntensity(profileIntensity);
        newProfile.setDelay(profileDelay);

        ProfileInfo compareProfile = sDataManager.getProfiles().get(index);
        assertEquals(compareProfile.getName(), profileName);
        assertEquals(compareProfile.getIntensity(), profileIntensity);
        assertEquals(compareProfile.getDelay(), profileDelay);
    }

    @Test
    public void findSimilarProfiles() throws Exception {
        final String profileName = "Test Profile";
        final String profileIntensity = "100,50,100,50";
        final String profileDelay1 = "1000,200,500,200";
        final String profileDelay2 = "50,100,50,100";

        int index1 = sDataManager.createNewProfile();
        ProfileInfo newProfile1 = sDataManager.getProfiles().get(index1);
        newProfile1.setName(profileName);
        newProfile1.setIntensity(profileIntensity);
        newProfile1.setDelay(profileDelay1);

        int index2 = sDataManager.createNewProfile();
        ProfileInfo newProfile2 = sDataManager.getProfiles().get(index2);
        newProfile2.setName(profileName);
        newProfile2.setIntensity(profileIntensity);
        newProfile2.setDelay(profileDelay2);

        int foundIndex1 = sDataManager.findProfile(newProfile1);
        assertEquals(index1, foundIndex1);

        int foundIndex2 = sDataManager.findProfile(newProfile2);
        assertEquals(index2, foundIndex2);
    }

    @Test
    public void createNewProfielOneStepCreation() {
        final String profileName = "My Test Profile";
        final String profileIntensity = "100,50,100,50";
        final String profileDelay = "1000,200,500,200";

        int index = sDataManager.createNewProfile(profileName, profileIntensity, profileDelay);

        ProfileInfo compareProfile = sDataManager.getProfiles().get(index);
        assertEquals(compareProfile.getName(), profileName);
        assertEquals(compareProfile.getIntensity(), profileIntensity);
        assertEquals(compareProfile.getDelay(), profileDelay);
    }

}