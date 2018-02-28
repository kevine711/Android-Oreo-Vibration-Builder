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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check if we're populating existing data, or if this is a new profile
        checkIntent();

        textName = (EditText)findViewById(R.id.text_name);
        textIntensity = (EditText)findViewById(R.id.text_intensity);
        textDelay = (EditText)findViewById(R.id.text_delay);

        if(!mIsNewProfile)
            displayProfile(textName, textIntensity, textDelay);
    }

    private void displayProfile(EditText textName, EditText textIntensity, EditText textDelay) {
        textName.setText(mProfile.getName());
        textIntensity.setText(mProfile.getIntensity());
        textDelay.setText(mProfile.getDelay());
    }

    private void checkIntent() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(PROFILE_POSITION, POSITION_NOT_SET);
        mIsNewProfile = (position == POSITION_NOT_SET);
        if (!mIsNewProfile)
            mProfile = DataManager.getInstance().getProfiles().get(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vibration_profile, menu);
        return true;
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
