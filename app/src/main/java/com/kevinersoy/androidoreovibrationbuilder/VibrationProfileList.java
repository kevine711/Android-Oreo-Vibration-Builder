package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kevinersoy.androidoreovibrationbuilder.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;

public class VibrationProfileList extends AppCompatActivity {
    private ProfileRecyclerAdapter mProfileRecyclerAdapter;
    private VibrationProfileBuilderOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles();
    }

    private void loadProfiles() {
        //Load profiles and set our adapter's cursor in background via AsyncTask
        AsyncTask<Object, Integer, Cursor> task = new AsyncTask<Object, Integer, Cursor>() {
            @Override
            protected Cursor doInBackground(Object[] objects) {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
                String[] profileColumns = {
                        ProfileInfoEntry.COLUMN_PROFILE_NAME,
                        ProfileInfoEntry.COLUMN_PROFILE_INTENSITY,
                        ProfileInfoEntry.COLUMN_PROFILE_DELAY,
                        ProfileInfoEntry._ID};
                return db.query(ProfileInfoEntry.TABLE_NAME, profileColumns,
                        null, null, null, null, null);
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                mProfileRecyclerAdapter.changeCursor(cursor);
            }
        }.execute();

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


}

