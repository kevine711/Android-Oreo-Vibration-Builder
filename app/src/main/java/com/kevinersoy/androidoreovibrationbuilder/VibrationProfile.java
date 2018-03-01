package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by kevinersoy on 2/27/18.
 * This is the Activity representing the vibration profile.  Used to edit/run profiles.
 * 2/28 - Changing intent to pass position rather than actual profile since DataManager is singleton
 */

public class VibrationProfile extends AppCompatActivity {
    public static final String PROFILE_POSITION = "com.kevinersoy.androidoreovibrationbuilder.PROFILE_POSITION";
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
        saveOriginalValues();

        textName = (EditText)findViewById(R.id.text_name);
        textIntensity = (EditText)findViewById(R.id.text_intensity);
        textDelay = (EditText)findViewById(R.id.text_delay);

        if(!mIsNewProfile)
            displayProfile(textName, textIntensity, textDelay);
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
