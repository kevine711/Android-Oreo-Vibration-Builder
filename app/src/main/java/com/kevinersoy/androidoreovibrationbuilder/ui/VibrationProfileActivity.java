package com.kevinersoy.androidoreovibrationbuilder.ui;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.kevinersoy.androidoreovibrationbuilder.DataManager;
import com.kevinersoy.androidoreovibrationbuilder.models.ProfileInfo;
import com.kevinersoy.androidoreovibrationbuilder.R;
import com.kevinersoy.androidoreovibrationbuilder.provider.VibrationBuilderProviderContract.Profiles;
import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderOpenHelper;

import java.util.ArrayList;
import java.util.List;

import static com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;

/**
 * Created by kevinersoy on 2/27/18.
 * This is the Activity representing the vibration profile.  Used to edit/run profiles.
 * 2/28 - Changing intent to pass position rather than actual profile since DataManager is singleton
 */

public class VibrationProfileActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String PROFILE_ID = "com.kevinersoy.androidoreovibrationbuilder.PROFILE_ID";
    public static final String ORIGINAL_PROFILE_NAME = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_PROFILE_NAME";
    public static final String ORIGINAL_PROFILE_INTENSITY = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_PROFILE_INTENSITY";
    public static final String ORIGINAL_PROFILE_DELAY = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_PROFILE_DELAY";
    private static final String ORIGINAL_URI = "com.kevinersoy.androidoreovibrationbuilder.ORIGINAL_URI";
    public static final int ID_NOT_SET = -1;
    public static final int LOADER_PROFILES = 0;
    private ProfileInfo mProfile = new ProfileInfo("", "", "");
    private Boolean mIsNewProfile;
    private EditText mTextName;
    private EditText mTextIntensity;
    private EditText mTextDelay;
    private int mProfilePosition;
    private boolean mIsCancelling;
    private boolean mIsDeleting;
    private String mOriginalName;
    private String mOriginalIntensity;
    private String mOriginalDelay;
    private VibrationProfileBuilderOpenHelper mDbOpenHelper;
    private int mProfileId;
    private Cursor mProfileCursor;
    private int mProfileNamePos;
    private int mProfileIntensityPos;
    private int mProfileDelayPos;
    private Uri mProfileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new VibrationProfileBuilderOpenHelper(this);

        //check if we're populating existing data, or if this is a new profile
        //mProfileUri will get set here if new
        checkIntent();

        //save original values
        if (savedInstanceState == null) {
            saveOriginalValues();
        } else {
            restoreOriginalValues(savedInstanceState);
        }

        mTextName = (EditText)findViewById(R.id.text_name);
        mTextIntensity = (EditText)findViewById(R.id.text_intensity);
        mTextDelay = (EditText)findViewById(R.id.text_delay);

        //mProfileUri will get set here if profile already exists
        if(!mIsNewProfile)
            getLoaderManager().initLoader(LOADER_PROFILES, null, this);


        //validate inputs on text changed
        mTextIntensity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    validateInputs();
                }
            }
        });
        mTextDelay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
                if (mTextDelay.getText().toString().isEmpty() || mTextIntensity.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), R.string.missing_inputs,
                            Toast.LENGTH_LONG).show();
                } else {
                    waveformVibration();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void validateInputs(){
        String text = mTextDelay.getText().toString();
        mTextDelay.setText(cleanDelayInput(text));

        text = mTextIntensity.getText().toString();
        mTextIntensity.setText(cleanIntensityInput(text));
    }

    private String cleanDelayInput(String input){
        //use regex to replace non numeric digits (excl ",")
        //also replace multiple delimiters
        input = input.replaceAll("[^\\d,]", ""); //remove all non numeric/","
        input = input.replaceAll("[,]{2,}",","); //remove duplicate ","
        return input;
    }

    private String cleanIntensityInput(String input){
        input = input.replaceAll("[^\\d,]", ""); //remove all non numeric/","
        //correct for max intensity 255
        List<Integer> intensity = new ArrayList<Integer>();
        for (String s : input.split(",")){
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

        return buildCsvString(intensity);
    }

    private String buildCsvString(List list){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<list.size(); i++){
            sb.append(list.get(i));
            if (i != list.size()-1)
                sb.append(",");
        }
        /*  Java 8 not allowed
        mTextIntensity.setText(intensity.stream()
                .map(i -> i.toString())
                .collect(Collectors.joining(",")));
        */
        return sb.toString();
    }

    private void waveformVibration() {
        //get vibrator and build the VibrationEffect, then run it
        //validate inputs
        validateInputs();

        //get reference to Vibrator
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //parse text inputs
        List<Long> delay = new ArrayList<Long>();
        for (String s : mTextDelay.getText().toString().split(",")){
            delay.add(Long.parseLong(s));
        }
        List<Integer> intensity = new ArrayList<Integer>();
        for (String s : mTextIntensity.getText().toString().split(",")){
            intensity.add(Integer.parseInt(s));
        }

        //correct for offset in input counts (add trailing zeros)
        correctInputCountOffset(delay, intensity);

        //update Edit Text with trailing zeros
        mTextIntensity.setText(buildCsvString(intensity));
        mTextDelay.setText(buildCsvString(delay));

        //convert to primitive arrays
        long[] aDelay = new long[delay.size()];
        for(int i = 0; i < delay.size(); i++)
            aDelay[i] = delay.get(i);
        int[] aIntensity = new int[intensity.size()];
        for(int i = 0; i < intensity.size(); i++)
            aIntensity[i] = intensity.get(i);

        if (v != null) {
            v.vibrate(VibrationEffect.createWaveform(aDelay, aIntensity, -1));
        } else {
            Toast.makeText(this, R.string.vibrator_not_found,
                    Toast.LENGTH_LONG).show();
        }

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
        mProfileUri = Uri.parse(savedInstanceState.getString(ORIGINAL_URI));
    }

    private void saveOriginalValues() {
        mOriginalName = mProfile.getName();
        mOriginalIntensity = mProfile.getIntensity();
        mOriginalDelay = mProfile.getDelay();
    }

    private void displayProfile() {
        String profileName = mProfileCursor.getString(mProfileNamePos);
        String profileIntensity = mProfileCursor.getString(mProfileIntensityPos);
        String profileDelay = mProfileCursor.getString(mProfileDelayPos);
        mTextName.setText(profileName);
        mTextIntensity.setText(profileIntensity);
        mTextDelay.setText(profileDelay);
    }

    private void checkIntent() {
        //If new profile, create one in DataManager.  If not, extract data from intent
        Intent intent = getIntent();
        mProfileId = intent.getIntExtra(PROFILE_ID, ID_NOT_SET);
        mIsNewProfile = (mProfileId == ID_NOT_SET);
        if (mIsNewProfile){
            createNewProfile();
        }
    }

    private void createNewProfile() {
        final ContentValues values = new ContentValues();
        values.put(Profiles.COLUMN_PROFILE_NAME, "");
        values.put(Profiles.COLUMN_PROFILE_INTENSITY, "");
        values.put(Profiles.COLUMN_PROFILE_DELAY, "");

        //use content resolver to insert row instead of accessing database directly.  Uri passed
        //back is a Uri pointing to the new row.
        AsyncTask<ContentValues, Void, Uri> task = new AsyncTask<ContentValues, Void, Uri>() {
            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                return getContentResolver().insert(Profiles.CONTENT_URI, contentValues[0]);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mProfileUri = uri;
            }
        }.execute(values);

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
        outState.putString(ORIGINAL_URI, mProfileUri.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewProfile) {  //if cancelling on a new profile, delete the profile
                deleteProfileFromDatabase();
            } else {
                restoreOldValues();  //cancelling on existing profile, restore previous values
            }
        } else {  //not cancelling, save the profile (unless deleting)
            if(mIsDeleting){
                deleteProfileFromDatabase();
            } else {
                saveProfile();
            }
        }
    }

    private void deleteProfileFromDatabase() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(mProfileUri,null,null);
                return null;
            }
        }.execute();
    }

    private void restoreOldValues() {
        mProfile.setName(mOriginalName);
        mProfile.setIntensity(mOriginalIntensity);
        mProfile.setDelay(mOriginalDelay);
    }

    private void saveProfile() {
        String name = mTextName.getText().toString();
        String intensity = mTextIntensity.getText().toString();
        String delay = mTextDelay.getText().toString();
        saveProfileToDatabase(name, intensity, delay);
    }

    private void saveProfileToDatabase(String name, String intensity, String delay){
        final ContentValues values = new ContentValues();
        values.put(ProfileInfoEntry.COLUMN_PROFILE_NAME, name);
        values.put(ProfileInfoEntry.COLUMN_PROFILE_INTENSITY, intensity);
        values.put(ProfileInfoEntry.COLUMN_PROFILE_DELAY, delay);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().update(mProfileUri, values, null, null);
                return null;
            }
        }.execute();


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
        } else if (id == R.id.action_next) {
            moveNext();
        } else if (id == R.id.action_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.areyousure))
                    .setTitle(getResources().getString(R.string.delete))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mIsDeleting = true;
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //disable "next" from options menu when on the last profile
        //system schedules a call to this method after we invalidateOptionsMenu in moveNext
        MenuItem item = menu.findItem(R.id.action_next);
        int lastProfileIndex = DataManager.getInstance().getProfiles().size() - 1;
        item.setEnabled(lastProfileIndex > mProfilePosition);
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveNext() {
        //move to the next profile in the list
        ++mProfilePosition;
        mProfile = DataManager.getInstance().getProfiles().get(mProfilePosition);

        saveOriginalValues();
        displayProfile();
        //invalidateOptionsMenu so the system schedules a call to onPrepareOptionsMenu where we
        //disable "next" if we're on the last profile.
        invalidateOptionsMenu();
    }

    private void sendEmail() {
        String subject = "Vibration Profile: " + mTextName.getText().toString();
        String text = "Intensity: \n" + mTextIntensity.getText().toString() + "\n" +
                "Delay: \n" + mTextDelay.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822"); //Mime type for Email
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if (id == LOADER_PROFILES)
            loader = createLoaderProfiles();
        return loader;
    }

    private CursorLoader createLoaderProfiles() {
        String[] profileColumns = {
                Profiles.COLUMN_PROFILE_NAME,
                Profiles.COLUMN_PROFILE_INTENSITY,
                Profiles.COLUMN_PROFILE_DELAY,
        };
        mProfileUri = ContentUris.withAppendedId(Profiles.CONTENT_URI, mProfileId);
        return new CursorLoader(this, mProfileUri, profileColumns,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_PROFILES)
            loadFinishedProfiles(data);
    }

    private void loadFinishedProfiles(Cursor data) {
        mProfileCursor = data;
        mProfileNamePos = mProfileCursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_NAME);
        mProfileIntensityPos = mProfileCursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_INTENSITY);
        mProfileDelayPos = mProfileCursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_DELAY);
        mProfileCursor.moveToFirst(); //go to the first row of the query result
        displayProfile();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == LOADER_PROFILES) {
            if(mProfileCursor != null)
                mProfileCursor.close();
        }

    }
}
