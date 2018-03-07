package com.kevinersoy.androidoreovibrationbuilder;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by kevinersoy on 3/7/18.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileCreationTest {
    static DataManager sDataManager;

    @BeforeClass
    public static void classSetUp(){
        sDataManager = DataManager.getInstance();
    }

    @Rule
    public ActivityTestRule<VibrationProfileList> mVibrationProfileListActivity =
            new ActivityTestRule<>(VibrationProfileList.class);




    @Test
    public void createNewProfile(){
        final String name = "Test Profile Name";
        final String delay = "100,,,,50,cat,dog,200,5";
        final String intensity = "~!@#400,,,20,50,20";
        final String filteredDelay = "100,50,200,5";
        final String filteredIntensity = "255,20,50,20";

        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.text_name)).perform(typeText(name));
        onView(withId(R.id.text_delay)).perform(typeText(delay));
        onView(withId(R.id.text_intensity)).perform(typeText(intensity),
                closeSoftKeyboard());
        onView(withId(R.id.button_run)).perform(click());

        //verify text was properly filtered
        onView(withId(R.id.text_delay)).check(matches(withText(containsString(filteredDelay))));
        onView(withId(R.id.text_intensity)).check(matches(withText(containsString(filteredIntensity))));

        pressBack();

        int profileIndex = sDataManager.getProfiles().size() - 1;
        ProfileInfo profile = sDataManager.getProfiles().get(profileIndex);
        assertEquals(name, profile.getName());
        assertEquals(filteredDelay, profile.getDelay());
        assertEquals(filteredIntensity, profile.getIntensity());

    }


}