package com.kevinersoy.androidoreovibrationbuilder;

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
import android.view.View;

import com.kevinersoy.androidoreovibrationbuilder.VibrationBuilderProviderContract.Profiles;

public class VibrationProfileList extends AppCompatActivity
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
                startActivity(new Intent(VibrationProfileList.this, VibrationProfile.class));
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
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().initLoader(LOADER_PROFILES, null, this);

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

