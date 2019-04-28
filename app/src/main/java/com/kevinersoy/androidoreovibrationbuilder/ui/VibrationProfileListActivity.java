package com.kevinersoy.androidoreovibrationbuilder.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kevinersoy.androidoreovibrationbuilder.BuildConfig;
import com.kevinersoy.androidoreovibrationbuilder.DataManager;
import com.kevinersoy.androidoreovibrationbuilder.ProfileSyncService;
import com.kevinersoy.androidoreovibrationbuilder.R;
import com.kevinersoy.androidoreovibrationbuilder.provider.VibrationBuilderProviderContract.Profiles;
import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderOpenHelper;

public class VibrationProfileListActivity extends AppCompatActivity
            implements LoaderManager.LoaderCallbacks<Cursor>{
    private ProfileRecyclerAdapter mProfileRecyclerAdapter;
    private VibrationProfileBuilderOpenHelper mDbOpenHelper;
    public static final int LOADER_PROFILES = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        //Create database open helper instance
        //If database didn't exist, create and add example profiles
        mDbOpenHelper = new VibrationProfileBuilderOpenHelper(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VibrationProfileListActivity.this, VibrationProfileActivity.class));
            }
        });

        initializeContent();
    }

    private void enableStrictMode() {
        if(BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("VibrationProfileListActivity", "Options item selected");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            upload();
        } else if (id == R.id.action_restore){
            restore();
        }
        return super.onOptionsItemSelected(item);
    }

    private void restore() {
        //Use intent service to restore all profiles from the server
        Toast.makeText(this, getString(R.string.restoringprofilesfromserver), Toast.LENGTH_SHORT).show();
        ProfileSyncService.startActionFetch(this, true);
    }

    private void upload() {
        //Use intent service to upload all profiles to server
        Toast.makeText(this, getString(R.string.uploadingtoserver), Toast.LENGTH_SHORT).show();
        ProfileSyncService.startActionUpload(this, ProfileSyncService.ALL_PROFILES);
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(LOADER_PROFILES, null, this);

    }

    private void initializeContent() {
        //Tell DataManager to load the profiles from the database into the List<ProfileInfo> field
        DataManager.loadFromDatabase(mDbOpenHelper);

        //Set up RecyclerView with a layout manager
        final RecyclerView recyclerProfiles = (RecyclerView) findViewById(R.id.list_profiles);
        final LinearLayoutManager profilesLayoutManager = new LinearLayoutManager(this);
        recyclerProfiles.setLayoutManager(profilesLayoutManager);

        //Set field for our recycler adapter passing a null cursor and set this adapter for
        //our RecyclerView
        //Cursor will get set when we loadProfiles() in onResume()
        mProfileRecyclerAdapter = new ProfileRecyclerAdapter(this, null);
        recyclerProfiles.setAdapter(mProfileRecyclerAdapter);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_PROFILES) {
            final String[] profileColumns = {
                    Profiles._ID,
                    Profiles.COLUMN_PROFILE_NAME,
                    Profiles.COLUMN_PROFILE_INTENSITY,
                    Profiles.COLUMN_PROFILE_DELAY
            };
            loader = new CursorLoader(this, Profiles.CONTENT_URI, profileColumns,
                    null, null, null);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER_PROFILES){
            mProfileRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(loader.getId() == LOADER_PROFILES){
            mProfileRecyclerAdapter.changeCursor(null);
        }
    }
}

