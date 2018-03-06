package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevinersoy on 2/27/18.
 * This is the Activity representing the vibration profile.  Used to edit/run profiles.
 * 2/28 - Changing intent to pass position rather than actual profile since DataManager is singleton
 */

public class VibrationProfile extends AppCompatActivity {
    public static final String PROFILE_POSITION = "com.kevinersoy.androidoreovibrationbuilder.PROFILE_POSITION";
    public static final String ORIGINAL_PROFILE_NAME = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_PROFILE_NAME";
    public static final String ORIGINAL_PROFILE_INTENSITY = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_PROFILE_INTENSITY";
    public static final String ORIGINAL_PROFILE_DELAY = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_PROFILE_DELAY";
    public static final int POSITION_NOT_SET = -1;
    private ProfileInfo mProfile;
    private Boolean mIsNewProfile;
    private EditText textName;
    private EditText textIntensity;
    private EditText textDelay;
    private int mProfilePosition;
    private boolean mIsCancelling;
    private String mOriginalName;
    private String mOriginalIntensity;
    private String mOriginalDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check if we're populating existing data, or if this is a new profile
        checkIntent();

        //save original values
        if (savedInstanceState == null) {
            saveOriginalValues();
        } else {
            restoreOriginalValues(savedInstanceState);
        }

        textName = (EditText)findViewById(R.id.text_name);
        textIntensity = (EditText)findViewById(R.id.text_intensity);
        textDelay = (EditText)findViewById(R.id.text_delay);

        if(!mIsNewProfile)
            displayProfile(textName, textIntensity, textDelay);


        //validate inputs on text changed
        textIntensity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    validateInputs();
                }
            }
        });
        textDelay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    validateInputs();
                }
            }
        });



        //set up onClick listener for Run button
        Button mButton = (Button)findViewById(R.id.button_run);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Generate vibration based on profile
                waveformVibration();
            }
        });
    }

    private void validateInputs(){
        //use regex to replace non numeric digits (excl ",")
        //also replace multiple delimiters
        String text = textDelay.getText().toString();
        text = text.replaceAll("[^\\d,]", ""); //remove all non numeric/","
        text = text.replaceAll("[,]{2,}",","); //remove duplicate ","
        textDelay.setText(text);

        text = textIntensity.getText().toString();
        text = text.replaceAll("[^\\d,]", "");
        textIntensity.setText(text);

        //correct for max intensity 255
        List<Integer> intensity = new ArrayList<Integer>();
        for (String s : text.split(",")){
            int value;
            try {
                value = Integer.parseInt(s);
            } catch (NumberFormatException nfe){
                if (s.isEmpty()) {
                    value = -1;
                } else {
                    value = 255;
                }
            }
            if (value > 255){
                intensity.add(255);
            } else {
                if (value > 0) //value will be -1 if no text between delimiter
                    intensity.add(value);
            }
        }
        //convert back to csv String and set EditText value
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<intensity.size(); i++){
            sb.append(intensity.get(i));
            if (i != intensity.size()-1)
                    sb.append(",");
        }
        textIntensity.setText(sb.toString());
        /*  Java 8 not allowed
        textIntensity.setText(intensity.stream()
                .map(i -> i.toString())
                .collect(Collectors.joining(",")));
        */
    }



    private void waveformVibration() {
        //get vibrator and build the VibrationEffect, then run it
        //validate inputs
        validateInputs();

        //get reference to Vibrator
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //parse text inputs
        List<Long> delay = new ArrayList<Long>();
        for (String s : textDelay.getText().toString().split(",")){
            delay.add(Long.parseLong(s));
        }
        List<Integer> intensity = new ArrayList<Integer>();
        for (String s : textIntensity.getText().toString().split(",")){
            intensity.add(Integer.parseInt(s));
        }

        //correct for offset in input counts
        correctInputCountOffset(delay, intensity);

        //convert to primitive arrays
        long[] aDelay = new long[delay.size()];
        for(int i = 0; i < delay.size(); i++)
            aDelay[i] = delay.get(i);
        int[] aIntensity = new int[intensity.size()];
        for(int i = 0; i < delay.size(); i++)
            aDelay[i] = delay.get(i);

        v.vibrate(VibrationEffect.createWaveform(aDelay, aIntensity, -1));

    }



    private void correctInputCountOffset(List<Long> delay, List<Integer> intensity) {
        int dSize = delay.size();
        int iSize = intensity.size();
        if (dSize > iSize){
            for (int i = 0; i<dSize-iSize; i++)
                intensity.add(0);
        } else {
            for (int i = 0; i<iSize-dSize; i++)
                delay.add((long)0);
        }
    }

    private void restoreOriginalValues(Bundle savedInstanceState) {
        mOriginalName = savedInstanceState.getString(ORIGINAL_PROFILE_NAME);
        mOriginalIntensity = savedInstanceState.getString(ORIGINAL_PROFILE_INTENSITY);
        mOriginalDelay = savedInstanceState.getString(ORIGINAL_PROFILE_DELAY);
    }

    private void saveOriginalValues() {
        mOriginalName = mProfile.getName();
        mOriginalIntensity = mProfile.getIntensity();
        mOriginalDelay = mProfile.getDelay();
    }

    private void displayProfile(EditText textName, EditText textIntensity, EditText textDelay) {
        textName.setText(mProfile.getName());
        textIntensity.setText(mProfile.getIntensity());
        textDelay.setText(mProfile.getDelay());
    }

    private void checkIntent() {
        //If new profile, create one in DataManager.  If not, extract data from intent
        Intent intent = getIntent();
        int position = intent.getIntExtra(PROFILE_POSITION, POSITION_NOT_SET);
        mIsNewProfile = (position == POSITION_NOT_SET);
        if (mIsNewProfile){
            createNewProfile();
        } else {
            mProfile = DataManager.getInstance().getProfiles().get(position);
        }
    }

    private void createNewProfile() {
        DataManager dm = DataManager.getInstance();
        mProfilePosition = dm.createNewProfile();
        mProfile = dm.getProfiles().get(mProfilePosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vibration_profile, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_PROFILE_NAME, mOriginalName);
        outState.putString(ORIGINAL_PROFILE_INTENSITY, mOriginalIntensity);
        outState.putString(ORIGINAL_PROFILE_DELAY, mOriginalDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewProfile) {  //if cancelling on a new profile, delete the profile
                DataManager.getInstance().removeProfile(mProfilePosition);
            } else {
                restoreOldValues();  //cancelling on existing profile, restore previous values
            }
        } else {  //not cancelling, save the profile
            saveProfile();
        }
    }

    private void restoreOldValues() {
        mProfile.setName(mOriginalName);
        mProfile.setIntensity(mOriginalIntensity);
        mProfile.setDelay(mOriginalDelay);
    }

    private void saveProfile() {
        mProfile.setName(textName.getText().toString());
        mProfile.setIntensity(textIntensity.getText().toString());
        mProfile.setDelay(textDelay.getText().toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) { //cancel, don't save
            mIsCancelling = true;
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {
        String subject = "Vibration Profile: " + textName.getText().toString();
        String text = "Intensity: \n" + textIntensity.getText().toString() + "\n" +
                "Delay: \n" + textDelay.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822"); //Mime type for Email
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }
}
